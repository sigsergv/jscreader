/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.CandidateApplications;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.scene.control.SplitPane;
import javafx.application.Platform;


public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        // start Manager
        DeviceManager.getInstance();
        CandidateApplications.getInstance();

        // required to properly terminate all threads
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });

        var deviceSelector = new DeviceSelector();
        deviceSelector.setPrefWidth(Double.MAX_VALUE);

        var cardTree = new CardItemsTree();
        var outputArea = new CardInfoTextView(cardTree);

        var sp = new SplitPane();
        // we need to set divider position after everything is drawn
        var changeListener = new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp.setDividerPosition(0, 0.3);
            }
        };
        sp.widthProperty().addListener(changeListener);
        sp.heightProperty().addListener(changeListener);

        sp.getItems().addAll(cardTree, outputArea);
        // sp.setDividerPositions(0.6f, 0.9f, 0.1f);

        VBox.setVgrow(sp, javafx.scene.layout.Priority.ALWAYS);

        var vbox = new VBox(4);  // spacing between items inside vbox layout
        vbox.setPadding(new javafx.geometry.Insets(4));  // set padding around vbox
        vbox.getChildren().addAll(deviceSelector, sp);

        var scene = new Scene(vbox, 900, 500);
        stage.setScene(scene);
        stage.setTitle("JSmartCardReader");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Stage createProgressWindow(String labelText) {
        var progressWindow = new Stage(javafx.stage.StageStyle.UNDECORATED);
        // var progressWindow = new Stage(javafx.stage.StageStyle.DECORATED);
        progressWindow.initOwner(Main.getPrimaryStage());
        progressWindow.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        var label = new Label(labelText);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        var scene = new Scene(label, 400, 100);
        progressWindow.setScene(scene);
        progressWindow.show();
        return progressWindow;        
    }

    public static void closeProgressWindow(Stage progressWindow) {
        if (progressWindow == null) {
            return;
        }
        progressWindow.close();
    }
}
