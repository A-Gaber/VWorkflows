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

import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.*;
import javafx.util.*;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public abstract class AbstractFXConnectionSkin implements FXConnectionSkin {

    // -- properties
    protected final ObjectProperty<Connector> senderProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Connector> receiverProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Connection> modelProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Parent> parentProperty = new SimpleObjectProperty<>();
    // -- properties

    // -- mutable fields
    protected Path connectionPath;
    protected InteractiveCurve interactiveCurve;
    protected ArrayList<Circle> circleArrayList;
    protected Circle receiverConnectorUI;
    protected VFlow controller;
    protected ConnectorShape senderShape;
    protected ConnectorShape receiverShape;
    protected ConnectionListener connectionListener;

    private boolean initialized;
    // -- mutable fields

    // -- immutable fields
    protected final String type;
    protected final FXSkinFactory skinFactory;
    // -- immutable fields

    public AbstractFXConnectionSkin(FXSkinFactory skinFactory, Parent parent, VFlow controller, String type) {
        setParent(parent);
        this.skinFactory = skinFactory;
        this.controller = controller;
        this.type = type;
    }

    @Override
    public final FXConnectionSkin init() {
        synchronized (this) {
            if (!initialized) {
                initSenderAndReceiver();
                initConnnectionPath();
                initStyle();
                initConnectionListener();
                makeDraggable();
                postInitialize();
                initialized = true;
            }
        }
        return this;
    }

    protected void postInitialize() {

    }

    protected abstract void initSenderAndReceiver();

    protected abstract void initStyle();

    protected void initConnnectionPath() {
        final Node senderNode = senderShape.getNode();

        DoubleBinding startXBinding = new DoubleBinding() {
            {
                super.bind(senderNode.layoutXProperty(),
                        senderNode.translateXProperty(),
                        senderShape.radiusProperty());
            }

            @Override
            protected double computeValue() {
                return senderNode.getLayoutX()
                        + senderNode.getTranslateX()
                        + senderShape.getRadius();
            }
        };

        DoubleBinding startYBinding = new DoubleBinding() {
            {
                super.bind(senderNode.layoutYProperty(),
                        senderNode.translateYProperty(),
                        senderShape.radiusProperty());
            }

            @Override
            protected double computeValue() {
                return senderNode.getLayoutY()
                        + senderNode.getTranslateY()
                        + senderShape.getRadius();
            }
        };

        DoubleBinding endXBinding = new DoubleBinding() {
            {
                super.bind(receiverConnectorUI.layoutXProperty(),
                        receiverConnectorUI.translateXProperty());
            }

            @Override
            protected double computeValue() {
                return receiverConnectorUI.getLayoutX() + receiverConnectorUI.getTranslateX();
            }
        };

        DoubleBinding endYBinding = new DoubleBinding() {
            {
                super.bind(receiverConnectorUI.layoutYProperty(),
                        receiverConnectorUI.translateYProperty());
            }

            @Override
            protected double computeValue() {
                return receiverConnectorUI.getLayoutY() + receiverConnectorUI.getTranslateY();
            }
        };

        DoubleBinding controlX1Binding = new DoubleBinding() {
            {
                super.bind(startXBinding, endXBinding);
            }

            @Override
            protected double computeValue() {
                return ( startXBinding.get() + endXBinding.get() ) / 2;
            }
        };

        DoubleBinding controlY1Binding = new DoubleBinding() {
            {
                super.bind(startYBinding);
            }

            @Override
            protected double computeValue() {
                return startYBinding.get();
            }
        };

        DoubleBinding controlX2Binding = new DoubleBinding() {
            {
                super.bind(startXBinding, endXBinding);
            }

            @Override
            protected double computeValue() {
                return ( startXBinding.get() + endXBinding.get() ) / 2;
            }
        };

        DoubleBinding controlY2Binding = new DoubleBinding() {
            {
                super.bind(endYBinding);
            }

            @Override
            protected double computeValue() {
                return endYBinding.get();
            }
        };

        //MoveTo moveTo = new MoveTo();
        //LineTo curveTo = new LineTo();

        interactiveCurve = new InteractiveCurve(getParent());
        interactiveCurve.setFirstLine(interactiveCurve);

        interactiveCurve.setStartYBinding(startYBinding);
        interactiveCurve.setStartXBinding(startXBinding);
        interactiveCurve.setEndXBinding(endXBinding);
        interactiveCurve.setEndYBinding(endYBinding);
        connectionPath = interactiveCurve.getPath();


        //moveTo.xProperty().bind(startXBinding);
        //moveTo.yProperty().bind(startYBinding);

        //curveTo.controlX1Property().bind(controlX1Binding);
        //curveTo.controlY1Property().bind(controlY1Binding);
        //curveTo.controlX2Property().bind(controlX2Binding);
        //curveTo.controlY2Property().bind(controlY2Binding);

        //curveTo.xProperty().bind(endXBinding);
        //curveTo.yProperty().bind(endYBinding);
    }

    protected void initConnectionListener() {
        connectionListener
                = new DefaultConnectionListener(
                skinFactory, controller, receiverConnectorUI);
    }

    protected abstract void makeDraggable();

    protected Path getConnectionPath() {
        return connectionPath;
    }

    @Override
    public Connector getSender() {
        return senderProperty.get();
    }

    @Override
    public void setSender(Connector n) {
        senderProperty.set(n);
    }

    @Override
    public ObjectProperty<Connector> senderProperty() {
        return senderProperty;
    }

    @Override
    public Connector getReceiver() {
        return receiverProperty.get();
    }

    @Override
    public void setReceiver(Connector n) {
        receiverProperty.set(n);
    }

    @Override
    public ObjectProperty<Connector> receiverProperty() {
        return receiverProperty;
    }

    @Override
    public Path getNode() {
        return connectionPath;
    }

    @Override
    public Parent getContentNode() {
        return getParent();
    }

    @Override
    public void setModel(Connection model) {
        modelProperty.set(model);
    }

    @Override
    public Connection getModel() {
        return modelProperty.get();
    }

    @Override
    public ObjectProperty<Connection> modelProperty() {
        return modelProperty;
    }

    protected final void setParent(Parent parent) {
        parentProperty.set(parent);
    }

    protected Parent getParent() {
        return parentProperty.get();
    }

    protected ObjectProperty<Parent> parentProperty() {
        return parentProperty;
    }

    @Override
    public void add() {
        NodeUtil.addToParent(getParent(), connectionPath);
        NodeUtil.addToParent(getParent(), receiverConnectorUI);

        receiverConnectorUI.toFront();
        connectionPath.toBack();
    }

    @Override
    public void remove() {
        NodeUtil.removeFromParent(connectionPath);
        NodeUtil.removeFromParent(receiverConnectorUI);
        InteractiveCurve temp = interactiveCurve;
        InteractiveCurve temp2 = interactiveCurve;
        while (temp.getbNext() != null) {
            temp2 = temp.getbNext().getNext();
            temp.getbNext().remove();
            temp = temp2;

            System.out.println(temp.getId());
        }
    }

    @Override
    public VFlow getController() {
        return controller;
    }

    @Override
    public void setController(VFlow controller) {
        this.controller = controller;
    }

    @Override
    public FXSkinFactory getSkinFactory() {
        return skinFactory;
    }

    @Override
    public void receiverToFront() {
        receiverConnectorUI.toFront();
    }

    @Override
    public Shape getReceiverUI() {
        return receiverConnectorUI;
    }

    @Override
    public ConnectorShape getSenderShape() {
        return senderShape;
    }

    @Override
    public ConnectorShape getReceiverShape() {
        return receiverShape;
    }

    @Override
    public void addBreakpoint(double x, double y){
        interactiveCurve.addBreakPoint(x,y);
        //printBreakPoint(interactiveCurve);


    }

    private void printBreakPoint(InteractiveCurve interactiveCurve){
        if(interactiveCurve.getbNext() != null) {
            printBreakPoint(interactiveCurve.getbNext().getNext());
            System.out.println(interactiveCurve.getbNext().getBreakPoint().getCenterX()+ "   "+interactiveCurve.getbNext().getBreakPoint().getCenterY());
            System.out.println(interactiveCurve.getbNext().getID());
        }
    }

    @Override
    public ArrayList<javafx.util.Pair> getPoints(){
        Stack<javafx.util.Pair> pointStack = new Stack<>();
        ArrayList<javafx.util.Pair> list = new ArrayList<>();
        InteractiveCurve temp = interactiveCurve;
        while (temp.getbNext()!=null){
            pointStack.add(new javafx.util.Pair(temp.getbNext().getBreakPoint().getLayoutX(),temp.getbNext().getBreakPoint().getLayoutY()));

            temp = temp.getbNext().getNext();
        }
        while (!pointStack.isEmpty()){
            list.add(pointStack.pop());
        }

        return list;
    }

    public ArrayList<Circle> getBreakpoints(){
        ArrayList<Circle> breakPoints = new ArrayList<>();
        InteractiveCurve temp = interactiveCurve;
        while (temp.getbNext()!=null){
            breakPoints.add(temp.getbNext().getBreakPoint());
            temp = temp.getbNext().getNext();
        }
        return breakPoints;
    }

    @Override
    public void addPoints(ArrayList<javafx.util.Pair> pointsList){
        pointsList.forEach(pair -> {
            addBreakpoint((Double) pair.getKey(),(Double) pair.getValue());
        });


    }

    public void highlight(boolean s){
        interactiveCurve.highlight(s);
    }
    public void triggerVisual(boolean s){
        interactiveCurve.triggerVisual(s);
    }


}
