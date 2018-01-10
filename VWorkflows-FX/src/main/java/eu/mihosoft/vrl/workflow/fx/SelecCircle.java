package eu.mihosoft.vrl.workflow.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfxtras.scene.control.window.SelectableNode;
import jfxtras.scene.control.window.WindowUtil;

public class SelecCircle extends Circle implements SelectableNode {

    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty selectableProperty = new SimpleBooleanProperty(true);
    private boolean noSkin = true;


    @Override
    public boolean requestSelection(boolean select) {
        if (!isSelectable()) {
            return false;
        }

        selectedProperty.set(select);

        return true;
    }

    @Override
    public boolean isSelected() {
        return selectedProperty.getValue();
    }

    @Override
    public ReadOnlyBooleanProperty selectedProperty() {

        return selectedProperty;
    }

    public boolean isSelectable() {
        return selectableProperty.get();
    }

    public boolean noSkin(){
        return noSkin;
    }

    public void skin(){
        noSkin = false;
        this.setFill(Color.BEIGE);
    }

    public void deSkin(){
        noSkin =true;
        this.setFill(Color.BLACK);
    }
}
