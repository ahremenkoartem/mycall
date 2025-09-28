package resh.connect.mycall.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import resh.connect.mycall.client.model.Participant;

import java.util.*;
import java.util.concurrent.*;

public class ClientApplication extends Application {

    private TextField ipField, portField, keyField, nicknameField;
    private ChoiceBox<String> roomChoiceBox;
    private ToggleButton micToggle, roomConnectButton;
    private ListView<Participant> participantsList;
    private Label statusLabel;
    private Button serverConnectButton;
    private Label connectedRoomLabel;

    private ObservableList<String> availableRooms = FXCollections.observableArrayList();
    private ObservableList<Participant> participants = FXCollections.observableArrayList();

    private boolean serverConnected = false;
    private boolean roomConnected = false;
    private String connectedRoom = null; // Комната, к которой реально подключены

    private ScheduledExecutorService scheduler;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MyCall Client");

        // Установка иконки приложения
        Image appIcon = new Image(getClass().getResourceAsStream("/resh.ico"));
        primaryStage.getIcons().add(appIcon);
        // Дополнительно устанавливаем иконку для панели задач Windows через AWT Taskbar API
        if (java.awt.Taskbar.isTaskbarSupported()) {
            java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
            try {
                java.awt.Image awtImage = java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resh.ico"));
                taskbar.setIconImage(awtImage);
            } catch (Exception e) {
                System.err.println("Ошибка установки иконки в панель задач: " + e.getMessage());
            }
        }

        ipField = new TextField();
        ipField.setPromptText("IP сервера");
        ipField.setPrefWidth(200);

        portField = new TextField();
        portField.setPromptText("Порт");
        portField.setPrefWidth(80);

        HBox ipPortBox = new HBox(10, ipField, portField);

        keyField = new TextField();
        keyField.setPromptText("Ключ подключения");

        nicknameField = new TextField();
        nicknameField.setPromptText("Введите никнейм");

        roomChoiceBox = new ChoiceBox<>();
        roomChoiceBox.setItems(availableRooms);

        micToggle = new ToggleButton("Выключить микрофон");
        micToggle.setSelected(true);
        micToggle.setVisible(false);
        micToggle.setDisable(true);

        micToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                micToggle.setText("Выключить микрофон");
                enableMicrophone();
            } else {
                micToggle.setText("Включить микрофон");
                disableMicrophone();
            }
        });

        serverConnectButton = new Button("Подключиться к серверу");

        roomConnectButton = new ToggleButton("Подключиться к комнате");
        roomConnectButton.setDisable(true);

        connectedRoomLabel = new Label();

        HBox roomControlBox = new HBox(10, roomConnectButton, connectedRoomLabel);

        participantsList = new ListView<>();
        participantsList.setItems(participants);

        participantsList.setCellFactory(lv -> new ListCell<Participant>() {
            @Override
            protected void updateItem(Participant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String micStatus = item.isMicOn() ? "🎤" : "🔇";
                    setText(item.getNickname() + " " + micStatus);
                }
            }
        });

        statusLabel = new Label("Статус: Не подключен");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                new Label("IP адрес и порт:"),
                ipPortBox,
                new Label("Ключ:"), keyField,
                new Label("Никнейм:"), nicknameField,
                serverConnectButton,
                new Label("Выберите комнату:"), roomChoiceBox,
                roomControlBox,
                new Label("Участники:"), participantsList,
                micToggle,
                statusLabel);

        serverConnectButton.setOnAction(e -> {
            if (!serverConnected) {
                connectToServer();
            } else {
                disconnectFromServer();
            }
        });

        roomConnectButton.setOnAction(e -> {
            if (!roomConnected) {
                connectToRoom();
            } else {
                disconnectFromRoom();
            }
        });

        roomChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
            // Обновление списка участников для выбранной комнаты (независимо от подключения)
            if (serverConnected && newRoom != null) {
                updateParticipantsForRoom(newRoom);
            }
        });

        Scene scene = new Scene(root, 450, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        String ip = ipField.getText().trim();
        String port = portField.getText().trim();
        String key = keyField.getText().trim();
        String nickname = nicknameField.getText().trim();

        if (ip.isEmpty() || port.isEmpty() || key.isEmpty() || nickname.isEmpty()) {
            statusLabel.setText("Заполните IP, порт, ключ и никнейм.");
            return;
        }

        int portNumber;
        try {
            portNumber = Integer.parseInt(port);
            if (portNumber < 1 || portNumber > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("Некорректный номер порта.");
            return;
        }

        statusLabel.setText("Подключение к серверу...");
        serverConnected = true;
        serverConnectButton.setText("Отключиться от сервера");

        loadRoomsFromServer();
        roomConnectButton.setDisable(false);
        statusLabel.setText("Подключен к серверу " + ip + ":" + port + " как " + nickname);

        ipField.setDisable(true);
        portField.setDisable(true);
        keyField.setDisable(true);
        nicknameField.setDisable(true);

        startParticipantsAutoUpdate();
    }

    private void disconnectFromServer() {
        stopParticipantsAutoUpdate();

        if (roomConnected) {
            disconnectFromRoom();
        }

        serverConnected = false;
        serverConnectButton.setText("Подключиться к серверу");
        roomConnectButton.setDisable(true);
        roomConnectButton.setSelected(false);
        roomConnectButton.setText("Подключиться к комнате");
        connectedRoomLabel.setText("");

        micToggle.setVisible(false);
        micToggle.setDisable(true);
        micToggle.setSelected(false);
        participants.clear();
        availableRooms.clear();
        statusLabel.setText("Отключен от сервера");

        ipField.setDisable(false);
        portField.setDisable(false);
        keyField.setDisable(false);
        nicknameField.setDisable(false);
    }

    private void connectToRoom() {
        String room = roomChoiceBox.getValue();
        if (room == null) {
            statusLabel.setText("Выберите комнату для подключения.");
            return;
        }
        roomConnected = true;
        connectedRoom = room;
        roomConnectButton.setText("Отключиться от комнаты");
        updateConnectedRoomLabel();

        statusLabel.setText("Подключен к комнате '" + room + "'");

        micToggle.setVisible(true);
        micToggle.setDisable(false);
        micToggle.setSelected(true);
        micToggle.setText("Выключить микрофон");
        enableMicrophone();
    }

    private void disconnectFromRoom() {
        roomConnected = false;
        connectedRoom = null;
        roomConnectButton.setText("Подключиться к комнате");
        updateConnectedRoomLabel();

        participants.clear();
        statusLabel.setText("Отключен от комнаты");

        micToggle.setVisible(false);
        micToggle.setDisable(true);
        micToggle.setSelected(false);
        disableMicrophone();
    }

    private void updateConnectedRoomLabel() {
        if (connectedRoom != null && roomConnected) {
            connectedRoomLabel.setText(" (Подключен к: " + connectedRoom + ")");
        } else {
            connectedRoomLabel.setText("");
        }
    }

    private void loadRoomsFromServer() {
        availableRooms.clear();
        availableRooms.addAll("Главная", "Комната 1", "Комната 2", "Свободный чат");
        if (!availableRooms.isEmpty()) {
            roomChoiceBox.setValue(availableRooms.get(0));
            updateParticipantsForRoom(availableRooms.get(0));
        }
    }

    private void startParticipantsAutoUpdate() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (!serverConnected) return;
            String viewedRoom = roomChoiceBox.getValue();
            if (viewedRoom == null) return;

            List<Participant> updatedParticipants = simulateServerParticipants(viewedRoom);

            Platform.runLater(() -> {
                if (viewedRoom.equals(roomChoiceBox.getValue())) {
                    participants.setAll(updatedParticipants);
                    statusLabel.setText("Обновлён список участников комнаты '" + viewedRoom + "'");
                }
            });
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateParticipantsForRoom(String room) {
        Platform.runLater(() -> {
            List<Participant> updatedParticipants = simulateServerParticipants(room);
            participants.setAll(updatedParticipants);
            statusLabel.setText("Обновлён список участников комнаты '" + room + "'");
        });
    }

    private void stopParticipantsAutoUpdate() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private List<Participant> simulateServerParticipants(String room) {
        Random rand = new Random();
        List<Participant> baseParticipants;

        switch (room) {
            case "Главная" -> baseParticipants = new ArrayList<>(List.of(
                    new Participant("Алиса", true),
                    new Participant("Боб", false),
                    new Participant("Кэрол", true)
            ));
            case "Комната 1" -> baseParticipants = new ArrayList<>(List.of(
                    new Participant("Давид", false),
                    new Participant("Ева", true)
            ));
            case "Комната 2" -> baseParticipants = new ArrayList<>(List.of(
                    new Participant("Фрэнк", true)
            ));
            case "Свободный чат" -> baseParticipants = new ArrayList<>(List.of(
                    new Participant("Гари", false),
                    new Participant("Хелен", false),
                    new Participant("Иван", true)
            ));
            default -> baseParticipants = new ArrayList<>(List.of(
                    new Participant("Пользователь", true)
            ));
        }

        // Добавляем себя только если просматриваем комнату подключения
        if (roomConnected && connectedRoom != null && room.equals(connectedRoom)) {
            String myNick = nicknameField.getText().trim();
            boolean myMicStatus = micToggle.isSelected();
            boolean containsMe = baseParticipants.stream().anyMatch(p -> p.getNickname().equals(myNick));
            if (!myNick.isEmpty() && !containsMe) {
                baseParticipants.add(new Participant(myNick, myMicStatus));
            }
        }

        // Случайно добавляем нового участника
        if (rand.nextBoolean()) {
            baseParticipants.add(new Participant("Новый участник " + rand.nextInt(100), rand.nextBoolean()));
        }

        return baseParticipants;
    }

    private void enableMicrophone() {
        System.out.println("Микрофон включен");
    }

    private void disableMicrophone() {
        System.out.println("Микрофон выключен");
    }

    @Override
    public void stop() {
        stopParticipantsAutoUpdate();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
