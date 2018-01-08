package eu.mihosoft.vrl.workflow.fx;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.UUID;

/**
 * Created by jseidelmann on 09.08.17.
 */
public class InteractiveCurve{


    private InteractiveCurve firstLine;
    private String id;
    private Parent parent;
    private Path path;
    private DoubleBinding startXBinding;
    private DoubleBinding startYBinding;
    private DoubleBinding endXBinding;
    private DoubleBinding endYBinding;

    private MoveTo moveTo = new MoveTo();
    private LineTo curveTo = new LineTo();


    private Breakpoint bPref = null;
    private Breakpoint bNext = null;

    public InteractiveCurve(Parent parent){
        this.parent = parent;
        id = UUID.randomUUID().toString();

        path = new Path(moveTo,curveTo);
        path.getStyleClass().setAll(
                "vnode-connection");

        path.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                handleBreakpointAdd(mouseEvent.getX(),mouseEvent.getY());
                handleVisualHigh(firstLine.bPref);
                handleVisualHigh(firstLine.bNext);
                return;
            } else {
                if((bNext != null && bNext.isVis()) || bPref !=null && bPref.isVis()){
                    handleVisualLow(firstLine.bPref);
                    handleVisualLow(firstLine.bNext);
                } else {
                    handleVisualHigh(firstLine.bPref);
                    handleVisualHigh(firstLine.bNext);
                }





            }


        });
    }



    private void handleVisualHigh(Breakpoint br){
        if(br == null) return;
        br.getBreakPoint().setOpacity(0.8);
        br.setVis(true);
        if(br.getNext() != this) br.getNext().handleVisualHigh(br.getNext().bNext);
        if(br.getPrev() != this) br.getPrev().handleVisualHigh(br.getPrev().bNext);

    }

    private void handleVisualLow(Breakpoint br){
        if(br == null) return;
        br.getBreakPoint().setOpacity(0.1);
        br.setVis(false);
        if(br.getNext() != this) br.getNext().handleVisualLow(br.getNext().bNext);
        if(br.getPrev() != this) br.getPrev().handleVisualLow(br.getPrev().bNext);

    }

    private void handleBreakpointAdd(double x , double y){
        Breakpoint temp = bNext;

        bNext = new Breakpoint(parent);
        bNext.setX(x);
        bNext.setY(y);


        bNext.setNextCurve(temp,endXBinding,endYBinding);
        bNext.getNext().setFirstLine(firstLine);
        bNext.setPrevCurve(this);
        if(temp!=null)temp.setPrevCurve(bNext.getNext());

        endXBinding = new DoubleBinding() {
            {
                super.bind(bNext.getBreakPoint().layoutXProperty(),
                        bNext.getBreakPoint().translateXProperty());
            }

            @Override
            protected double computeValue() {
                return bNext.getBreakPoint().getLayoutX() + bNext.getBreakPoint().getTranslateX();
            }
        };

        endYBinding = new DoubleBinding() {
            {
                super.bind(bNext.getBreakPoint().layoutYProperty(),
                        bNext.getBreakPoint().translateYProperty());
            }

            @Override
            protected double computeValue() {
                return bNext.getBreakPoint().getLayoutY() + bNext.getBreakPoint().getTranslateY();
            }
        };

        setEndXBinding(endXBinding);
        setEndYBinding(endYBinding);

        bNext.addToParent();
        bNext.toFront();
        //if(bPref != null) bPref.toFront();
    }

    public void addBreakPoint(double x, double y){
        handleBreakpointAdd(x,y);
    }

    public void setEndXBinding(DoubleBinding endXBinding) {
        this.endXBinding = endXBinding;
        curveTo.xProperty().bind(endXBinding);
    }

    public void setEndYBinding(DoubleBinding endYBinding) {
        this.endYBinding = endYBinding;
        curveTo.yProperty().bind(endYBinding);
    }

    public void setStartYBinding(DoubleBinding startYBinding) {
        this.startYBinding = startYBinding;
        moveTo.yProperty().bind(startYBinding);

    }

    public void setStartXBinding(DoubleBinding startXBinding) {
        this.startXBinding = startXBinding;
        moveTo.xProperty().bind(startXBinding);
    }

    public void addToParent(){
        NodeUtil.addToParent(parent, path);
    }

    public void setbPref(Breakpoint bPref) {
        this.bPref = bPref;
    }

    public void setbNext(Breakpoint bNext) {
        this.bNext = bNext;
    }

    public Path getPath() {
        return path;
    }

    public Breakpoint getbNext() {
        return bNext;
    }

    public String getId() {
        return id;
    }

    public DoubleBinding getEndXBinding(){
        return endXBinding;
    }
    public DoubleBinding getEndYBinding(){
        return endYBinding;
    }

    public void setFirstLine(InteractiveCurve i){
        firstLine = i;
    }
}
