/*
 * Copyright 2012-2016 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * Please cite the following publication(s):
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.workflow.fx;

import eu.mihosoft.scaledfx.ScaleBehavior;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

import java.util.Iterator;

/**
 * Scales content to always fit in the bounds of this pane. Useful for workflows
 * with lots of windows.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ScalableContentPane extends Region {
    private Scale contentScaleTransform;
    private final Property<Pane> contentPaneProperty = new SimpleObjectProperty();
    private double contentScaleWidth = 1.0D;
    private double contentScaleHeight = 1.0D;
    private boolean aspectScale = true;
    private boolean autoRescale = true;
    private final DoubleProperty minScaleXProperty = new SimpleDoubleProperty(4.9E-324D);
    private final DoubleProperty maxScaleXProperty = new SimpleDoubleProperty(1.7976931348623157E308D);
    private final DoubleProperty minScaleYProperty = new SimpleDoubleProperty(4.9E-324D);
    private final DoubleProperty maxScaleYProperty = new SimpleDoubleProperty(1.7976931348623157E308D);
    private final BooleanProperty fitToWidthProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty fitToHeightProperty = new SimpleBooleanProperty(true);
    private final ObjectProperty<ScaleBehavior> scaleBehavior;
    private boolean manualReset;

    public ScalableContentPane() {
        this.scaleBehavior = new SimpleObjectProperty(ScaleBehavior.ALWAYS);
        this.setContent(new Pane());
        this.needsLayoutProperty().addListener((ov, oldV, newV) -> {
            if(newV.booleanValue() && (this.getWidth() <= this.getPrefWidth() || this.getHeight() <= this.getPrefHeight()) || this.getPrefWidth() == -1.0D || this.getPrefHeight() == -1.0D) {
                this.computeScale();
            }

        });
        this.fitToWidthProperty().addListener((ov, oldValue, newValue) -> {
            this.requestLayout();
        });
        this.fitToHeightProperty().addListener((ov, oldValue, newValue) -> {
            this.requestLayout();
        });
        this.scaleBehaviorProperty().addListener((ov, oldV, newV) -> {
            this.requestLayout();
        });
    }

    public Pane getContent() {
        return (Pane)this.contentPaneProperty.getValue();
    }

    public final void setContent(Pane contentPane) {
        this.contentPaneProperty.setValue(contentPane);
        contentPane.setManaged(false);
        this.initContentPaneListener();
        this.contentScaleTransform = new Scale(1.0D, 1.0D);
        this.getContentScaleTransform().setPivotX(0.0D);
        this.getContentScaleTransform().setPivotY(0.0D);
        this.getContentScaleTransform().setPivotZ(0.0D);
        this.getContent().getTransforms().add(this.getContentScaleTransform());
        this.getChildren().add(contentPane);
        ChangeListener changeListener = (ov, oldValue, newValue) -> {
            this.requestScale();
        };
        this.getContentScaleTransform().setOnTransformChanged((event) -> {
            this.requestLayout();
        });
        this.minScaleXProperty().addListener(changeListener);
        this.minScaleYProperty().addListener(changeListener);
        this.maxScaleXProperty().addListener(changeListener);
        this.maxScaleYProperty().addListener(changeListener);
    }

    public Property<Pane> contentProperty() {
        return this.contentPaneProperty;
    }

    public final Scale getContentScaleTransform() {
        return this.contentScaleTransform;
    }

    protected void layoutChildren() {
        super.layoutChildren();
    }

    private void computeScale() {
        double realWidth = this.getContent().prefWidth(this.getLayoutBounds().getHeight());
        double realHeight = this.getContent().prefHeight(this.getLayoutBounds().getWidth());
        double leftAndRight = this.getInsets().getLeft() + this.getInsets().getRight();
        double topAndBottom = this.getInsets().getTop() + this.getInsets().getBottom();
        double contentWidth = this.getLayoutBounds().getWidth() - leftAndRight;
        double contentHeight = this.getLayoutBounds().getHeight() - topAndBottom;
        this.contentScaleWidth = contentWidth / realWidth;
        this.contentScaleHeight = contentHeight / realHeight;
        this.contentScaleWidth = Math.max(this.contentScaleWidth, this.getMinScaleX());
        this.contentScaleWidth = Math.min(this.contentScaleWidth, this.getMaxScaleX());
        this.contentScaleHeight = Math.max(this.contentScaleHeight, this.getMinScaleY());
        this.contentScaleHeight = Math.min(this.contentScaleHeight, this.getMaxScaleY());
        boolean partOfSceneGraph = true;
        double realContentWidth;
        if(this.isAspectScale()) {
            realContentWidth = Math.min(this.contentScaleWidth, this.contentScaleHeight);
            if(this.getScaleBehavior() != ScaleBehavior.ALWAYS && !this.manualReset) {
                if(this.getScaleBehavior() == ScaleBehavior.IF_NECESSARY && realContentWidth < this.getContentScaleTransform().getX() && this.getLayoutBounds().getWidth() > 0.0D && partOfSceneGraph) {
                    this.getContentScaleTransform().setX(realContentWidth);
                    this.getContentScaleTransform().setY(realContentWidth);
                }
            } else {
                this.getContentScaleTransform().setX(realContentWidth);
                this.getContentScaleTransform().setY(realContentWidth);
            }
        } else if(this.getScaleBehavior() != ScaleBehavior.ALWAYS && !this.manualReset) {
            if(this.getScaleBehavior() == ScaleBehavior.IF_NECESSARY) {
                if(this.contentScaleWidth < this.getContentScaleTransform().getX() && this.getLayoutBounds().getWidth() > 0.0D && partOfSceneGraph) {
                    this.getContentScaleTransform().setX(this.contentScaleWidth);
                }

                if(this.contentScaleHeight < this.getContentScaleTransform().getY() && this.getLayoutBounds().getHeight() > 0.0D && partOfSceneGraph) {
                    this.getContentScaleTransform().setY(this.contentScaleHeight);
                }
            }
        } else {
            this.getContentScaleTransform().setX(this.contentScaleWidth);
            this.getContentScaleTransform().setY(this.contentScaleHeight);
        }

        double resizeScaleW = this.getContentScaleTransform().getX();
        double resizeScaleH = this.getContentScaleTransform().getY();
        this.getContent().relocate(this.getInsets().getLeft(), this.getInsets().getTop());
        if(this.isFitToWidth()) {
            realContentWidth = contentWidth / resizeScaleW;
        } else {
            realContentWidth = contentWidth / this.contentScaleWidth;
        }

        double realContentHeight;
        if(this.isFitToHeight()) {
            realContentHeight = contentHeight / resizeScaleH;
        } else {
            realContentHeight = contentHeight / this.contentScaleHeight;
        }

        this.getContent().resize(realContentWidth, realContentHeight);
    }

    public void requestScale() {
        this.computeScale();
    }

    public void resetScale() {
        if(!this.manualReset) {
            this.manualReset = true;

            try {
                this.computeScale();
            } finally {
                this.manualReset = false;
            }

        }
    }

    protected double computeMinWidth(double d) {
        double result = this.getInsets().getLeft() + this.getInsets().getRight();
        result += this.getContent().prefWidth(d) * this.getMinScaleX();
        return result;
    }

    protected double computeMinHeight(double d) {
        double result = this.getInsets().getTop() + this.getInsets().getBottom();
        result += this.getContent().prefHeight(d) * this.getMinScaleY();
        return result;
    }

    protected double computePrefWidth(double d) {
        double result = this.getInsets().getLeft() + this.getInsets().getRight();
        result += this.getContent().prefWidth(d) * this.contentScaleWidth;
        return result;
    }

    protected double computePrefHeight(double d) {
        double result = this.getInsets().getTop() + this.getInsets().getBottom();
        result += this.getContent().prefHeight(d) * this.contentScaleHeight;
        return result;
    }

    private void initContentPaneListener() {
        ChangeListener boundsListener = (ov, oldValue, newValue) -> {
            if(this.isAutoRescale()) {
                this.setNeedsLayout(false);
                this.getContent().requestLayout();
                this.requestLayout();
            }

        };
        ChangeListener numberListener = (ov, oldValue, newValue) -> {
            if(this.isAutoRescale()) {
                this.setNeedsLayout(false);
                this.getContent().requestLayout();
                this.requestLayout();
            }

        };
        this.getContent().getChildren().addListener((ListChangeListener<? super Node>) (c) -> {
            label30:
            while(true) {
                if(c.next()) {
                    Iterator var3;
                    Node n;
                    if(c.wasRemoved()) {
                        var3 = c.getRemoved().iterator();

                        while(true) {
                            if(!var3.hasNext()) {
                                continue label30;
                            }

                            n = (Node)var3.next();
                            n.boundsInLocalProperty().removeListener(boundsListener);
                            n.layoutXProperty().removeListener(numberListener);
                            n.layoutYProperty().removeListener(numberListener);
                        }
                    }

                    if(!c.wasAdded()) {
                        continue;
                    }

                    var3 = c.getAddedSubList().iterator();

                    while(true) {
                        if(!var3.hasNext()) {
                            continue label30;
                        }

                        n = (Node)var3.next();
                        n.boundsInLocalProperty().addListener(boundsListener);
                        n.layoutXProperty().addListener(numberListener);
                        n.layoutYProperty().addListener(numberListener);
                    }
                }

                return;
            }
        });
    }

    public boolean isAspectScale() {
        return this.aspectScale;
    }

    public void setAspectScale(boolean aspectScale) {
        this.aspectScale = aspectScale;
    }

    public boolean isAutoRescale() {
        return this.autoRescale;
    }

    public void setAutoRescale(boolean autoRescale) {
        this.autoRescale = autoRescale;
    }

    public DoubleProperty minScaleXProperty() {
        return this.minScaleXProperty;
    }

    public DoubleProperty minScaleYProperty() {
        return this.minScaleYProperty;
    }

    public DoubleProperty maxScaleXProperty() {
        return this.maxScaleXProperty;
    }

    public DoubleProperty maxScaleYProperty() {
        return this.maxScaleYProperty;
    }

    public double getMinScaleX() {
        return this.minScaleXProperty().get();
    }

    public double getMaxScaleX() {
        return this.maxScaleXProperty().get();
    }

    public double getMinScaleY() {
        return this.minScaleYProperty().get();
    }

    public double getMaxScaleY() {
        return this.maxScaleYProperty().get();
    }

    public void setMinScaleX(double s) {
        this.minScaleXProperty().set(s);
    }

    public void setMaxScaleX(double s) {
        this.maxScaleXProperty().set(s);
    }

    public void setMinScaleY(double s) {
        this.minScaleYProperty().set(s);
    }

    public void setMaxScaleY(double s) {
        this.maxScaleYProperty().set(s);
    }

    public void setFitToWidth(boolean value) {
        this.fitToWidthProperty().set(value);
    }

    public boolean isFitToWidth() {
        return this.fitToWidthProperty().get();
    }

    public final BooleanProperty fitToWidthProperty() {
        return this.fitToWidthProperty;
    }

    public void setFitToHeight(boolean value) {
        this.fitToHeightProperty().set(value);
    }

    public final BooleanProperty fitToHeightProperty() {
        return this.fitToHeightProperty;
    }

    public boolean isFitToHeight() {
        return this.fitToHeightProperty().get();
    }

    public final ObjectProperty<ScaleBehavior> scaleBehaviorProperty() {
        return this.scaleBehavior;
    }

    public void setScaleBehavior(ScaleBehavior behavior) {
        this.scaleBehaviorProperty().set(behavior);
    }

    public ScaleBehavior getScaleBehavior() {
        return (ScaleBehavior)this.scaleBehaviorProperty().get();
    }
}
