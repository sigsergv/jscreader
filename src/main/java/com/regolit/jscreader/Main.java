/* INSERT LICENSE HERE */

package com.regolit.jscreader;

import com.regolit.jscreader.util.CandidateApplications;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.scene.layout.HBox;
import javafx.application.Platform;


public class Main extends Application {
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

        var hbox = new HBox(4);
        hbox.getChildren().addAll(cardTree, outputArea);
        HBox.setHgrow(outputArea, javafx.scene.layout.Priority.ALWAYS);
        // textarea.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox.setVgrow(hbox, javafx.scene.layout.Priority.ALWAYS);

        var vbox = new VBox(4);  // spacing between items inside vbox layout
        vbox.setPadding(new javafx.geometry.Insets(4));  // set padding around vbox
        vbox.getChildren().addAll(deviceSelector, hbox);

        var scene = new Scene(vbox, 900, 500);
        stage.setScene(scene);
        stage.setTitle("JSmartCardReader");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
