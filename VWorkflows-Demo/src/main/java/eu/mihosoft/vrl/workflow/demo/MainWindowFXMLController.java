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
package eu.mihosoft.vrl.workflow.demo;

import edu.uci.ics.jung.graph.DirectedGraph;
import eu.mihosoft.vrl.workflow.ClickEvent;
import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.FlowFactory;
import eu.mihosoft.vrl.workflow.LayoutGeneratorNaive;
import eu.mihosoft.vrl.workflow.LayoutGeneratorSmart;
import eu.mihosoft.vrl.workflow.MouseButton;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.VisualizationRequest;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import eu.mihosoft.vrl.workflow.fx.OptimizableContentPane;
import eu.mihosoft.vrl.workflow.fx.ScalableContentPane;
import eu.mihosoft.vrl.workflow.fx.VCanvas;
import eu.mihosoft.vrl.workflow.io.WorkflowIO;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import jfxtras.scene.control.window.Window;

/**
 * FXML Controller class
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class MainWindowFXMLController implements Initializable {

    private int counter = 0;
    private Window clipboard;
    private VFlow specialViewFlow1;
    private VFlow specialViewFlow2;
    private FXMLLoader fxmlLoader;
    private OptionsWindowFXMLController options;
    private Stage optionsstage;
    private LayoutGeneratorSmart smartLayout;
    private LayoutGeneratorNaive naiveLayout;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       canvas = new VCanvas();

        Pane root = canvas.getContent();

        contentPane.getChildren().add(canvas);

        rootPane = root;

        onGenerateAction(null);
        
        // layout interface:
        smartLayout = new LayoutGeneratorSmart();
        naiveLayout = new LayoutGeneratorNaive();
        
        fxmlLoader = new FXMLLoader(getClass().getResource("OptionsWindowFXML.fxml"));
        optionsstage = new Stage();
        optionsstage.setTitle("Smart Layout Options");
        
        try {
            Parent p = (Parent) fxmlLoader.load();
            optionsstage.setScene(new Scene(p));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        options = fxmlLoader.getController();
        options.setGenerator(smartLayout);
        options.setStage(optionsstage);
    }
    
    private Pane rootPane;
    private VCanvas canvas;
    VFlow workflow;

    @FXML
    public void onLoadAction(ActionEvent e) {
        System.out.print(" >> loading workflow from xml");

        try {
            workflow = WorkflowIO.loadFromXML(Paths.get("flow01.xml"));

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(" [done]");

        updateUI();
    }

    @FXML
    public void onSaveAction(ActionEvent e) {

        if (workflow == null) {
            return;
        }

        System.out.print(" >> saving workflow as xml");
        try {
            WorkflowIO.saveToXML(Paths.get("flow01.xml"), workflow.getModel());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(" [done]");
    }
    
    // <editor-fold defaultstate="collapsed" desc="Menu items">
    @FXML
    private CheckMenuItem checkDebugLayout;
    
    @FXML
    private CheckMenuItem checkNaiveRecursive;
    
    @FXML
    private CheckMenuItem checkNaiveAutoscaleNodes;
    
    @FXML
    private CheckMenuItem checkNaiveLaunchRemoveCycles;
    // </editor-fold>
    
    // <editor-fold desc="Development" defaultstate="collapsed">
    @FXML
    public void onNaiveAction(ActionEvent e) {
        this.naiveLayout.setDebug(this.checkDebugLayout.isSelected());
        this.naiveLayout.setRecursive(this.checkNaiveRecursive.isSelected());
        this.naiveLayout.setAutoscaleNodes(this.checkNaiveAutoscaleNodes.isSelected());
        this.naiveLayout.setLaunchRemoveCycles(this.checkNaiveLaunchRemoveCycles.isSelected());
        this.naiveLayout.setWorkflow(this.workflow);
        this.naiveLayout.generateLayout();
    }
    
    @FXML
    public void onSmartRunAction(ActionEvent e) {
        this.smartLayout.setDebug(this.checkDebugLayout.isSelected());
        switch(this.smartLayout.getGraphmode()) {
            case 0:
                this.smartLayout.setWorkflow(this.workflow);
                break;
            case 1:
                LayoutGeneratorSmart altlay = new LayoutGeneratorSmart();
                altlay.setWorkflow(this.workflow);
                altlay.generateLayout();
                DirectedGraph<VNode, Connection> jgraph = altlay.getModelGraph();
                this.smartLayout.setModelGraph(jgraph);
                this.smartLayout.setRecursive(false);
                break;
            case 2:
                // run with nodelist
                break;
        }
        this.smartLayout.generateLayout();
    }
    
    @FXML
    public void onSmartOptionsAction(ActionEvent e) {
        options.setWorkflow(workflow);
        options.set();
        optionsstage.show();
    }
    // </editor-fold>
    
    // <editor-fold desc="Test cases" defaultstate="collapsed">
    private int testcase = 0;
    
    @FXML
    public void onOriginalAction(ActionEvent e) {
        testcase = 1;
        onGenerateAction(e);
    }
    
    @FXML
    public void onAdditionalEdgeAction(ActionEvent e) {
        testcase = 2;
        onGenerateAction(e);
    }
    
    @FXML
    public void onAdditionalGraphAction(ActionEvent e) {
        testcase = 3;
        onGenerateAction(e);
    }
    
    @FXML
    public void onTriLaneAction(ActionEvent e) {
        testcase = 4;
        onGenerateAction(e);
    }
    
    @FXML
    public void onDifferentSizesAction(ActionEvent e) {
        testcase = 5;
        onGenerateAction(e);
    }
    
    @FXML
    public void onCycleAction(ActionEvent e) {
        testcase = 6;
        onGenerateAction(e);
    }
    
    @FXML
    public void onTestTree1(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestTree2(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestTree3(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestNontree1(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestNontree2(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestNontree3(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestCyclic1(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestCyclic2(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestCyclic3(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestSizes1(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestSizes2(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestSizes3(ActionEvent e) {
        // generation
    }
    
    @FXML
    public void onTestRunAll(ActionEvent e) {
        // run
    }
    
    // </editor-fold>
    
    @FXML
    public void onStableAction(ActionEvent e) {
        LayoutGeneratorSmart layouter = new LayoutGeneratorSmart(false);
        layouter.setWorkflow(workflow);
        layouter.generateLayout();
    }

    @FXML
    public void onGenerateAction(ActionEvent e) {

        counter = 0;

        System.out.print(" >> generate workflow");

        workflow = FlowFactory.newFlow();

        updateUI();

        workflowTest(workflow, 5, 10);
//        workflowTest(workflow, 2, 2);

        System.out.println(" [done]");

        System.out.println(" --> #nodes: " + counter);

    }
    @FXML
    public Pane contentPane;

    /**
     * @return the rootPane
     */
    public Pane getRootPane() {
        return rootPane;
    }

    public void workflowTest(VFlow workflow, int depth, int width) {

        if (depth < 1) {
            return;
        }

        if ("4687".equals(workflow.getModel().getId())) {
            System.out.println("FLOW: " + workflow.getModel().getId());
            specialViewFlow1 = workflow;
        }

        if ("4688".equals(workflow.getModel().getId())) {
            System.out.println("FLOW: " + workflow.getModel().getId());
            specialViewFlow2 = workflow;
        }

        String[] connectionTypes = {"control", "data", "event"};

        for (int i = 0; i < width; i++) {

            counter++;

            VNode n;

            if (i % 2 == 0) {
                VFlow subFlow = workflow.newSubFlow();
                n = subFlow.getModel();
                workflowTest(subFlow, depth - 1, width);
            } else {
                n = workflow.newNode();
            }

            n.setTitle("Node " + n.getId());
            
//            n.getVisualizationRequest().set(VisualizationRequest.KEY_MAX_CONNECTOR_SIZE, 10.0);

            String type = connectionTypes[i % connectionTypes.length];

            n.setMainInput(n.addInput(type)).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            n.setMainInput(n.addInput("event")).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            
//            n.getVisualizationRequest().set(VisualizationRequest.KEY_DISABLE_EDITING, true);

            for (int j = 0; j < 3; j++) {
                n.addInput(type).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            }

//            n.addInput(type);
//            n.addInput(type);
            n.addOutput(type).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            n.setMainOutput(n.addOutput("event")).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            n.addOutput(type).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);

            for (int j = 0; j < 3; j++) {
                n.addOutput(type).getVisualizationRequest().set(VisualizationRequest.KEY_CONNECTOR_AUTO_LAYOUT, true);
            }

            for (final Connector connector : n.getConnectors()) {
                connector.addClickEventListener(new EventHandler<ClickEvent>() {

                    @Override
                    public void handle(ClickEvent t) {

                        if (t.getButton() != MouseButton.SECONDARY) {
                            return;
                        }

                        System.out.println("Connector: " + connector.getId() + ", btn: " + t.getButton());
                        if (t.getEvent() instanceof MouseEvent) {
                            MouseEvent evt = (MouseEvent) t.getEvent();

                            ContextMenu menu = new ContextMenu(new MenuItem("Connector: " + connector.getId() + ", btn: " + t.getButton()));

                            menu.show(rootPane, evt.getScreenX(), evt.getScreenY());
                        }
                    }
                });
            }

            n.setWidth(300);
            n.setHeight(200);

            n.setX((i % 5) * (n.getWidth() + 30));
            n.setY((i / 5) * (n.getHeight() + 30));
        }
        
        // Create example connections
        ObservableList<VNode> nodes;
        switch (this.testcase) {
            case 1: // original
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(1), nodes.get(8).getInputs().get(3));
                workflow.connect(nodes.get(5).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(5).getOutputs().get(3), nodes.get(7).getInputs().get(1));
                workflow.connect(nodes.get(7).getOutputs().get(1), nodes.get(8).getInputs().get(0));
                workflow.connect(nodes.get(8).getOutputs().get(2), nodes.get(9).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(0), nodes.get(6).getInputs().get(0));
                workflow.connect(nodes.get(6).getOutputs().get(0), nodes.get(0).getInputs().get(0));
                workflow.connect(nodes.get(0).getOutputs().get(0), nodes.get(9).getInputs().get(0));
                break;
            case 2: // additional edge
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(1), nodes.get(8).getInputs().get(3));
                workflow.connect(nodes.get(5).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(5).getOutputs().get(3), nodes.get(7).getInputs().get(1));
                workflow.connect(nodes.get(7).getOutputs().get(1), nodes.get(8).getInputs().get(0));
                workflow.connect(nodes.get(8).getOutputs().get(2), nodes.get(9).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(0), nodes.get(6).getInputs().get(0));
                workflow.connect(nodes.get(6).getOutputs().get(0), nodes.get(0).getInputs().get(0));
                workflow.connect(nodes.get(0).getOutputs().get(0), nodes.get(9).getInputs().get(0));
                workflow.connect(nodes.get(2).getOutputs().get(2), nodes.get(8).getInputs().get(4));
                break;
            case 3: // additional graph
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(1), nodes.get(8).getInputs().get(3));
                workflow.connect(nodes.get(5).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(8).getOutputs().get(2), nodes.get(9).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(0), nodes.get(6).getInputs().get(0));
                workflow.connect(nodes.get(6).getOutputs().get(0), nodes.get(0).getInputs().get(0));
                workflow.connect(nodes.get(0).getOutputs().get(0), nodes.get(9).getInputs().get(0));
                workflow.connect(nodes.get(2).getOutputs().get(1), nodes.get(4).getInputs().get(1));
                workflow.connect(nodes.get(4).getOutputs().get(1), nodes.get(7).getInputs().get(1));
                break;
            case 4: // tri lane
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(0).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(0).getOutputs().get(1), nodes.get(5).getInputs().get(1));
                workflow.connect(nodes.get(0).getOutputs().get(1), nodes.get(6).getInputs().get(1));
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(2).getInputs().get(1));
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(7).getInputs().get(1));
                workflow.connect(nodes.get(5).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(6).getOutputs().get(1), nodes.get(7).getInputs().get(1));
                workflow.connect(nodes.get(2).getOutputs().get(1), nodes.get(8).getInputs().get(1));
                workflow.connect(nodes.get(7).getOutputs().get(1), nodes.get(8).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(1), nodes.get(8).getInputs().get(1));
                break;
            case 5: // different sizes
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(1), nodes.get(8).getInputs().get(3));
                workflow.connect(nodes.get(5).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(5).getOutputs().get(3), nodes.get(7).getInputs().get(1));
                workflow.connect(nodes.get(7).getOutputs().get(1), nodes.get(8).getInputs().get(0));
                workflow.connect(nodes.get(8).getOutputs().get(2), nodes.get(9).getInputs().get(1));
                workflow.connect(nodes.get(3).getOutputs().get(0), nodes.get(6).getInputs().get(0));
                workflow.connect(nodes.get(6).getOutputs().get(0), nodes.get(0).getInputs().get(0));
                workflow.connect(nodes.get(0).getOutputs().get(0), nodes.get(9).getInputs().get(0));
                nodes.get(5).setHeight(nodes.get(5).getHeight() * 1.5);
                nodes.get(5).setWidth(nodes.get(5).getWidth() * 1.5);
                nodes.get(7).setHeight(nodes.get(7).getHeight() * 2);
                nodes.get(8).setWidth(nodes.get(8).getWidth() * 2);
                break;
            case 6: // cycle
                nodes = workflow.getNodes();
                workflow.connect(nodes.get(0).getOutputs().get(1), nodes.get(1).getInputs().get(1));
                workflow.connect(nodes.get(1).getOutputs().get(1), nodes.get(2).getInputs().get(1));
                workflow.connect(nodes.get(2).getOutputs().get(1), nodes.get(3).getInputs().get(1));
                workflow.connect(nodes.get(2).getOutputs().get(1), nodes.get(4).getInputs().get(1));
                workflow.connect(nodes.get(4).getOutputs().get(1), nodes.get(0).getInputs().get(1));
                break;
            default:
                break;
        }
    }

    private void updateUI() {

        rootPane.getChildren().clear();

//        ScalableContentPane minimapPane1 = createMinimap("Minimap 1");
//        ScalableContentPane minimapPane2 = createMinimap("Minimap 2");
//
//        if (workflow == null) {
//            return;
//        }
        workflow.getModel().setVisible(true);

        FXSkinFactory skinFactory = new FXSkinFactory(rootPane);

        workflow.setSkinFactories(skinFactory);
        
       

//        skinFactory.setConnectionFillColor("control", new Color(1.0, 1.0, 0.0, 0.75));
//        skinFactory.setConnectionStrokeColor("control", new Color(120 / 255.0, 140 / 255.0, 1, 0.42));
//
//        skinFactory.setConnectionFillColor("data", new Color(0.1, 0.1, 0.1, 0.5));
//        skinFactory.setConnectionStrokeColor("data", new Color(120 / 255.0, 140 / 255.0, 1, 0.42));
//
//        skinFactory.setConnectionFillColor("event", new Color(255.0 / 255.0, 100.0 / 255.0, 1, 0.5));
//        skinFactory.setConnectionStrokeColor("event", new Color(120 / 255.0, 140 / 255.0, 1, 0.42));
//        workflow.addSkinFactories(new FXSkinFactory(minimapPane1.getContentPane()),
//                new FXSkinFactory(minimapPane2.getContentPane()));
//        ScalableContentPane minimapPane3 = createMinimap("Minimap 3");
//        ScalableContentPane minimapPane4 = createMinimap("Minimap 4");
//
//        if (specialViewFlow1 != null) {
//            specialViewFlow1.addSkinFactories(new FXSkinFactory(minimapPane3.getContentPane()));
//        }
//
//        if (specialViewFlow2 != null) {
//            specialViewFlow2.addSkinFactories(new FXSkinFactory(minimapPane4.getContentPane()));
//        }
//
//        workflow.newSubFlow();
    }

    private ScalableContentPane createMinimap(String title) {
        //        clipboard = new Window("Clipboard/Broken!");
        //        clipboard.setPrefSize(80, 80);
        //        clipboard.setResizableWindow(false);
        //
        //        clipboard.setVisible(false);
        //        rootPane.getChildren().add(clipboard);
        OptimizableContentPane minimapContent = new OptimizableContentPane();
        ScalableContentPane minimapPane = new ScalableContentPane();
        minimapContent.getChildren().add(minimapPane);
        Window minimap = new Window(title);
        minimap.setStyle("-fx-background-color: rgba(120,140,255,0.2);-fx-border-color: rgba(120,140,255,0.42);-fx-border-width: 2;");
        minimap.setPrefSize(300, 200);
        minimap.setContentPane(minimapContent);
        rootPane.getChildren().add(minimap);
        return minimapPane;
    }
//    void registerShell(VRLShell shell) {
//        shell.addConstant("flow", workflow);
//    }
}
