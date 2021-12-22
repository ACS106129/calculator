package calc;

import calc.controller.AppController;
import calc.manager.ResourceManager;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

public class App extends Application {
    public void start(final Stage primaryStage) {
        final Pair<Parent, Object> fx = ResourceManager.getInstance().getFX(AppController.ATTACH_NAME).orElseThrow();
        final AppController ac = (AppController) fx.getValue();
        final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        ac.setPrimaryStage(primaryStage);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("110522089 - Calculator");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(fx.getKey(), Color.TRANSPARENT));
        primaryStage.show();
        final double width = primaryStage.getWidth(), height = primaryStage.getHeight();
        primaryStage.setMinWidth(width);
        primaryStage.setMinHeight(height);
        primaryStage.setX((screenBounds.getWidth() - width) / 2);
        primaryStage.setY((screenBounds.getHeight() - height) / 2);
        ac.blurBackground();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}