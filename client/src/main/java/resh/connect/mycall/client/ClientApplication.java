package resh.connect.mycall.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Инициализация UI клиента
        primaryStage.setTitle("MyCall Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
