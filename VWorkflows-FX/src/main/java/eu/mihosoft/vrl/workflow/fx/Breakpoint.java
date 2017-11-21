package eu.mihosoft.vrl.workflow.fx;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfxtras.labs.util.event.MouseControlUtil;

import java.util.UUID;

/**
 * Created by jseidelmann on 10.08.17.
 */
public class Breakpoint {

    private Parent parent;
    private String id;
    private Circle breakPoint = new Circle();
    private InteractiveCurve next;
    private InteractiveCurve prev;

    public Breakpoint(Parent parent){
        this.parent = parent;
        initBreakPoint();

    }

    private void initBreakPoint(){
        breakPoint.setRadius(15);
        breakPoint.setStroke(Color.YELLOW);
        breakPoint.setStrokeWidth(3);
        MouseControlUtil.makeDraggable(breakPoint);
        id = UUID.randomUUID().toString();
        breakPoint.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                handelDelete();

            }

        } );

    }

    private void handelDelete() {
        prev.setEndXBinding(next.getEndXBinding());
        prev.setEndYBinding(next.getEndYBinding());
        prev.setbNext(next.getbNext());
        if(next.getbNext()!=null)next.getbNext().setPrevCurve(prev);
        NodeUtil.removeFromParent(next.getPath());
        NodeUtil.removeFromParent(breakPoint);
        prev = null;
        next = null;

    }

    public void remove(){
        handelDelete();
    }


    public void setNextCurve(Breakpoint nextB, DoubleBinding endX,DoubleBinding endY) {

        this.next = new InteractiveCurve(parent);
        this.next.setbNext(nextB);
        this.next.setbPref(this);
        if(next.getbNext()==null) {
            this.next.setEndXBinding(endX);
            this.next.setEndYBinding(endY);
        }
        else {
            next.setEndXBinding(new DoubleBinding() {
                {
                    super.bind(next.getbNext().breakPoint.layoutXProperty(),
                            next.getbNext().breakPoint.translateXProperty());
                }

                @Override
                protected double computeValue() {
                    return next.getbNext().breakPoint.getLayoutX() + next.getbNext().breakPoint.getTranslateX();
                }
            });

            next.setEndYBinding(new DoubleBinding() {
                {
                    super.bind(next.getbNext().breakPoint.layoutYProperty(),
                            next.getbNext().breakPoint.translateYProperty());
                }

                @Override
                protected double computeValue() {
                    return next.getbNext().breakPoint.getLayoutY() + next.getbNext().breakPoint.getTranslateY();
                }
            });
        }



        this.next.setStartXBinding(new DoubleBinding() {
            {
                super.bind(breakPoint.layoutXProperty(),
                        breakPoint.translateXProperty(),
                        breakPoint.radiusProperty());
            }

            @Override
            protected double computeValue() {
                return breakPoint.getLayoutX()
                        + breakPoint.getTranslateX();
                        //+ breakPoint.getRadius();
            }
        });

        this.next.setStartYBinding(new DoubleBinding() {
            {
                super.bind(breakPoint.layoutYProperty(),
                        breakPoint.translateYProperty()//,
                        //breakPoint.radiusProperty()
                );
            }

            @Override
            protected double computeValue() {
                return breakPoint.getLayoutY()
                        + breakPoint.getTranslateY();
                        //+ breakPoint.getRadius();
            }
        });


    }
    public void addToParent(){
        next.addToParent();
        NodeUtil.addToParent(parent,breakPoint);

        breakPoint.toFront();
        if(next.getbNext()!=null)next.getbNext().toFront();
    }

    public void setPrevCurve(InteractiveCurve prev) {
        this.prev = prev;
    }

    public InteractiveCurve getPrev() {
        return prev;
    }

    public InteractiveCurve getNext() {
        return next;
    }

    public void setY(double y) {
        breakPoint.setLayoutY(y);
    }

    public void setX(double x) {
        breakPoint.setLayoutX(x);
    }

    public void toFront() {
        breakPoint.toFront();
    }

    public Circle getBreakPoint() {
        return breakPoint;
    }

    public String getID(){
        return id;
    }
}
