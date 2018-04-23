package eu.mihosoft.vrl.workflow.fx;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import jfxtras.labs.util.event.EventHandlerGroup;
import jfxtras.labs.util.event.MouseControlUtil;
import jfxtras.scene.control.window.SelectableNode;
import jfxtras.scene.control.window.Window;
import jfxtras.scene.control.window.WindowUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created  by jseidelmann on 10.08.2017.
 * Modified by dwigand    on 23.04.2018.
 */
public class Breakpoint {

    private Parent parent;
    private String id;
    private SelectableCircle breakPoint = new SelectableCircle();
    private InteractiveCurve next;
    private InteractiveCurve prev;
    private boolean isVis = true;
    private boolean drag = false;

    private double snappingSigma = 10;

    private EventHandlerGroup<MouseEvent> dragHandlerGroup;
    private EventHandlerGroup<MouseEvent> pressHandlerGroup;
    private EventHandlerGroup<MouseEvent> pressReleaseHandlerGroup;

    public Breakpoint(Parent parent){
        this.parent = parent;
        dragHandlerGroup = new EventHandlerGroup<>();
        pressHandlerGroup = new EventHandlerGroup<>();
        pressReleaseHandlerGroup = new EventHandlerGroup<>();
        initBreakPoint();
    }

    private void initBreakPoint(){
        // TODO DLW come up with a proper way to guide the drag w.r.t. the layout.
//        dragHandlerGroup.addHandler();
//        pressHandlerGroup.addHandler();

        breakPoint.setOnMouseDragged(dragHandlerGroup);
        breakPoint.setOnMousePressed(pressHandlerGroup);
        breakPoint.setOnMouseReleased(pressReleaseHandlerGroup);

        breakPoint.layoutXProperty().unbind();
        breakPoint.layoutYProperty().unbind();

        GuidedDraggingControllerImpl draggingController = new GuidedDraggingControllerImpl();
        draggingController.apply(breakPoint, dragHandlerGroup, pressHandlerGroup, pressReleaseHandlerGroup, true);

        id = UUID.randomUUID().toString();

        breakPoint.setOnDragDetected(mouseEvent -> {
            drag = true;
        });

        breakPoint.setOnMouseClicked(mouseEvent -> {
            if(!drag) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    handelDelete();
                }
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    prev.triggerVisual();
                }
            }
            drag = false;
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

    public SelectableCircle getBreakPoint() {
        return breakPoint;
    }

    public String getID(){
        return id;
    }

    public boolean isVis(){
        return isVis;
    }

    public void setVis(boolean b){
        isVis = b;
    }

    // TODO DLW create a new DraggingControllerImpl clone
    class GuidedDraggingControllerImpl {

        private double nodeX;
        private double nodeY;
        private double mouseX;
        private double mouseY;
        private EventHandler<MouseEvent> mouseDraggedEventHandler;
        private EventHandler<MouseEvent> mousePressedEventHandler;
        private EventHandler<MouseEvent> mouseReleasedEventHandler;
        private boolean centerNode = false;

        private Line snapLineX = null;
        private Line snapLineY = null;

        public GuidedDraggingControllerImpl() {
            snapLineX = new Line();
            snapLineY = new Line();
        }

        public void apply(Node n, EventHandlerGroup<MouseEvent> draggedEvtHandler,
                          EventHandlerGroup<MouseEvent> pressedEvtHandler,
                          EventHandlerGroup<MouseEvent> pressReleasedEvtHandler,
                          boolean centerNode) {
            init(n);
            draggedEvtHandler.addHandler(mouseDraggedEventHandler);
            pressedEvtHandler.addHandler(mousePressedEventHandler);
            pressReleasedEvtHandler.addHandler(mouseReleasedEventHandler);
            this.centerNode = centerNode;
        }

        private void init(final Node n) {
            mouseDraggedEventHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    performDrag(n, event);
                    event.consume();
                }
            };

            mousePressedEventHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    performDragBegin(n, event);
                    event.consume();
                }
            };

            mouseReleasedEventHandler = new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (snapLineX != null && parent.getChildrenUnmodifiable().contains(snapLineX)) {
                        NodeUtil.removeFromParent(snapLineX);
                    }
                    if (snapLineY != null && parent.getChildrenUnmodifiable().contains(snapLineY)) {
                        NodeUtil.removeFromParent(snapLineY);
                    }
                    event.consume();
                }
            };
        }

        public void performDrag(
                Node n, MouseEvent event) {
            final double parentScaleX = n.getParent().
                    localToSceneTransformProperty().getValue().getMxx();
            final double parentScaleY = n.getParent().
                    localToSceneTransformProperty().getValue().getMyy();

            // Get the exact moved X and Y
            double offsetX = event.getSceneX() - mouseX;
            double offsetY = event.getSceneY() - mouseY;

            nodeX += offsetX;
            nodeY += offsetY;


            double scaledX;
            double scaledY;

            // TODO Do NOT consider selected break points!
            //
            // 1) find node(s) from relatedNodes closest to n w.r.t. x and y.
            // However, x and y can be independent in terms of nodes.
            // Only consider dx and dy that are within a specific sigma.
            //
            // 2) find out when centerNode is used...
            //
            // 3) when x and y are found, snap n accordingly.
            //

            // TODO now get shapes in the scene
            Node closestXNode = null;
            Node closestYNode = null;

            if (centerNode) {
                Point2D p2d = n.getParent().sceneToLocal(mouseX, mouseY);
                scaledX = p2d.getX();
                scaledY = p2d.getY();
                double offsetForAllX = scaledX - n.getLayoutX();
                double offsetForAllY = scaledY - n.getLayoutY();

                for (Node refNode : parent.getChildrenUnmodifiable()) {
                    if (refNode instanceof FlowNodeWindow && refNode instanceof Path) {
                        continue;
                    }

                    // avoid myself
                    if (refNode == n) {
                        continue;
                    }

                    double tmpX = Math.abs(scaledX - refNode.getLayoutX());
                    if (tmpX < snappingSigma) {
                        if (closestXNode == null) {
                            closestXNode = refNode;
                        } else if (tmpX < closestXNode.getLayoutX()) {
                            closestXNode = refNode;
                        }
                    }
                    double tmpY = Math.abs(scaledY - refNode.getLayoutY());
                    if (tmpY < snappingSigma) {
                        if (closestYNode == null) {
                            closestYNode = refNode;
                        } else if (tmpY < closestYNode.getLayoutY()) {
                            closestYNode = refNode;
                        }
                    }

                }

                // TODO either let sigma break out by this or my distance to relatedNodes?
                if (n instanceof SelectableNode &&((SelectableNode)n).isSelected()) {
                    dragSelectedWindows(((SelectableNode)n),offsetForAllX, offsetForAllY);
                }
            } else {
                scaledX = nodeX * 1 / (parentScaleX);
                scaledY = nodeY * 1 / (parentScaleY);
                double offsetForAllX = scaledX - n.getLayoutX();
                double offsetForAllY = scaledY - n.getLayoutY();
                if (n instanceof SelectableNode &&((SelectableNode)n).isSelected()) {
                    dragSelectedWindows(((SelectableNode)n),offsetForAllX, offsetForAllY);
                }
            }

            if (closestXNode != null) {
//                System.out.println("closestXNode " + closestXNode.getLayoutX() + " ("+ closestXNode.getLayoutY() + ")");
                snapLineX.setStartX(closestXNode.getLayoutX());
                snapLineX.setEndX(closestXNode.getLayoutX());

                snapLineX.setStartY(n.getBoundsInParent().getMinY() - 20);
                snapLineX.setEndY(n.getBoundsInParent().getMaxY() + 20);

                n.setLayoutX(closestXNode.getLayoutX());

                if (!parent.getChildrenUnmodifiable().contains(snapLineX)) {
                    NodeUtil.addToParent(parent, snapLineX);
                }
                snapLineX.toFront();
            } else {
//                System.out.println("X no snap!");
                if (parent.getChildrenUnmodifiable().contains(snapLineX)) {
                    NodeUtil.removeFromParent(snapLineX);
                }

                n.setLayoutX(scaledX);
            }

            if (closestYNode != null) {
//                System.out.println("closestYNode (" + closestYNode.getLayoutX() + ") "+ closestYNode.getLayoutY() + "");
                snapLineY.setStartY(closestYNode.getLayoutY());
                snapLineY.setEndY(closestYNode.getLayoutY());

                snapLineY.setStartX(n.getBoundsInParent().getMinX() - 20);
                snapLineY.setEndX(n.getBoundsInParent().getMaxX() + 20);

                n.setLayoutY(closestYNode.getLayoutY());

                if (!parent.getChildrenUnmodifiable().contains(snapLineY)) {
                    NodeUtil.addToParent(parent, snapLineY);
                }
                snapLineY.toFront();
            } else {
//                System.out.println("Y no snap!");
                if (parent.getChildrenUnmodifiable().contains(snapLineY)) {
                    NodeUtil.removeFromParent(snapLineY);
                }
                n.setLayoutY(scaledY);
            }

//            if (closestXNode !=null || closestYNode != null) {
//                Platform.runLater(() -> {
//                    try {
//                        Point2D bTmp = n.getParent().localToScene(n.getLayoutX(), n.getLayoutY());
//                        java.awt.Robot robot = new java.awt.Robot();
//                        robot.mouseMove((int) bTmp.getX(), (int) bTmp.getY());
//                    } catch (java.awt.AWTException e) {
//                        e.printStackTrace();
//                    }
//                });
//            }

            // again set current Mouse x AND y position
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        }

//        // Not sure if we need this?!
//        private ArrayList<Breakpoint> getAllInteractiveCurveParts() {
//            ArrayList<Breakpoint> ret = new ArrayList<Breakpoint>();
//
//            InteractiveCurve temp = getPrev();
//            while (temp != null && temp.getbPrev() != null) {
//                ret.add(temp.getbPrev());
//                temp = temp.getbPrev().getPrev();
//            }
//
//            temp = getNext();
//            while (temp != null && temp.getbNext() != null) {
//                ret.add(temp.getbNext());
//                temp = temp.getbNext().getNext();
//            }
//            return ret;
//        }

        private void dragSelectedWindows(SelectableNode control,double offsetForAllX, double offsetForAllY) {
            for (SelectableNode sN : WindowUtil.
                    getDefaultClipboard().getSelectedItems()) {

                if(sN instanceof SelectableCircle) {
                    SelectableCircle c = (SelectableCircle) sN;
                    c.setLayoutX(c.getLayoutX()+offsetForAllX);
                    c.setLayoutY(c.getLayoutY()+offsetForAllY);
                }
                if (sN == control
                        || !(sN instanceof Window)) {
                    continue;
                }

                Window selectedWindow = (Window) sN;

                if (((Node)control).getParent().
                        equals(selectedWindow.getParent())) {

                    selectedWindow.setLayoutX(
                            selectedWindow.getLayoutX()
                                    + offsetForAllX);
                    selectedWindow.setLayoutY(
                            selectedWindow.getLayoutY()
                                    + offsetForAllY);
                }
            } // end for sN
        }

        public void performDragBegin(
                Node n, MouseEvent event) {

            final double parentScaleX = n.getParent().
                    localToSceneTransformProperty().getValue().getMxx();
            final double parentScaleY = n.getParent().
                    localToSceneTransformProperty().getValue().getMyy();

            // record the current mouse X and Y position on Node
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            if (centerNode) {
                Point2D p2d = n.getParent().sceneToLocal(mouseX, mouseY);
                nodeX = p2d.getX();
                nodeY = p2d.getY();
            } else {
                nodeX = n.getLayoutX() * parentScaleX;
                nodeY = n.getLayoutY() * parentScaleY;
            }

            n.toFront();
        }

    }

}
