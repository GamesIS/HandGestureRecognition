package application;

import application.controller.ListImagesController;
import application.controller.ObjRecognitionController;
import application.db.Hand;
import application.db.Image;
import application.db.Properties;
import application.utils.Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;

import static application.controller.ObjRecognitionController.cropImageView;

;


public class Main extends Application {
    /**
     * The main class for a JavaFX application. It creates and handles the main
     * window with its resources (style, graphics, etc.).
     * <p>
     * This application looks for a hand in the video stream based on manual HSV input. It then
     * detects the number of fingers being held up.
     */

    public static ObjectMapper objectMapper = new ObjectMapper();
    public static String RESOURCES_PATH = System.getProperty("user.dir") + "\\resources\\";
    public static String PROPERTIES_FILE = RESOURCES_PATH + "properties.json";
    public ObjRecognitionController objRecognitionController;
    public static ListImagesController listImagesController;

    private Stage cnnStage;
    private BorderPane cnnLayout;
    private ObservableList<Image> imageData = FXCollections.observableArrayList();
    private static NeuralNetwork neuralNetwork;
    private ObservableList<Hand> hands = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        try {
            try {
                startOpenCV();
                //throw new Exception("ex");
            } catch (Exception e) {
                showError(e);
            }
            //Thread.setDefaultUncaughtExceptionHandler(Main::showError);
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("ObjRecognition.fxml"));
            // store the root element so that the controllers can use it
            BorderPane root = (BorderPane) loader.load();
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 1000, 600);
            //scene.getStylesheets().add(Main.class.getClassLoader().getResource("application.css").toExternalForm());
            // create the stage with the given title and the previously created
            primaryStage.setOnCloseRequest(evt -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you really want to close this applicetion?", ButtonType.YES, ButtonType.NO);
                ButtonType result = alert.showAndWait().orElse(ButtonType.NO);

                if (ButtonType.NO.equals(result)) {
                    // no choice or no clicked -> don't close
                    evt.consume();
                }
            });
            // scene
            primaryStage.setTitle("Система распознавания жестов");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();


            primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);

            ///////////////
            StackPane cropLayout = new StackPane();
            cropLayout.getChildren().add(cropImageView);

            Scene cropScene = new Scene(cropLayout, 400, 400);
            Stage cropWindow = new Stage();
            cropWindow.setTitle("Регион интересов (ROI)");
            cropWindow.setScene(cropScene);

            cropWindow.setX(primaryStage.getX() + 200);
            cropWindow.setY(primaryStage.getY() + 100);

            cropWindow.show();
            ///////////////

            // set the proper behavior on closing the application
            objRecognitionController = loader.getController();
            loadJson();
            primaryStage.setOnCloseRequest((we -> objRecognitionController.setClosed()));

            /////
            startWindowCNN();
            /////

        } catch (Exception e) {
            showError(e);
        }
    }

    private void startWindowCNN() {
        neuralNetwork = new NeuralNetwork();
        neuralNetwork.load();
        //hands.addAll(Hand.loadImages());

        cnnStage = new Stage();
        cnnStage.setTitle("NeuralNetwork");

        initRootLayout();

        showListImages();

        //primaryStage.getScene().getStylesheets().add("sample/style.css");
    }

    private void startOpenCV() {
        String opencvpath = System.getProperty("user.dir") + "\\resources\\native\\";
        try {
            System.load(opencvpath + "\\x64\\" + Core.NATIVE_LIBRARY_NAME + ".dll");
        } catch (Throwable e) {
            try {
                System.load(opencvpath + "\\x86\\" + Core.NATIVE_LIBRARY_NAME + ".dll");
            } catch (Throwable ex) {
                showError(ex);
            }
            //log(getStackTrace(e));
        }
    }

    private static void showError(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error alert");
        alert.setHeaderText(e.getMessage());

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");

        String stackTrace = getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);

        // Set content for Dialog Pane
        alert.getDialogPane().setContent(dialogPaneContent);

        alert.showAndWait();
    }

    private static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

    private void closeWindowEvent(WindowEvent event) {
        System.out.println("Exit Application");
        saveJson();
        Platform.exit();
    }

    private void saveJson() {
        try {
            File file = new File(PROPERTIES_FILE);
            Properties properties = objRecognitionController.getProperties();
            objectMapper.writeValue(file, properties);
        } catch (IOException e) {
            System.out.println("Error save JSON");
            e.printStackTrace();
        }
    }

    public void loadJson() {
        try {
            File file = new File(PROPERTIES_FILE);
            Properties properties = objectMapper.readValue(file, new TypeReference<Properties>() {
            });
            objRecognitionController.setProperties(properties);
        } catch (IOException e) {
            System.out.println("Error loading JSON");
            Platform.exit();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void log(String text) {
        File tmpFile = null;
        try {
            tmpFile = new File(RESOURCES_PATH + "\\test.txt");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            //FXMLLoader loader = new FXMLLoader();
            //loader.setLocation(Main.class.getResource("sample.fxml"));
            cnnLayout = (BorderPane) FXMLLoader.load(Main.class.getClassLoader().getResource("sample.fxml"));

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(cnnLayout);
            cnnStage.setScene(scene);
            cnnStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showListImages() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getClassLoader().getResource("listimages.fxml"));
            AnchorPane listImage = (AnchorPane) loader.load();

            cnnLayout.setCenter(listImage);

            listImagesController = loader.getController();
            listImagesController.setMain(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public ObservableList<Hand> getHands() {
        return hands;
    }

    public ObservableList<Image> getImageData() {
        return imageData;
    }
}