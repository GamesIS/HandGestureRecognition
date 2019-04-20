package application;

import java.io.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;
;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static application.ObjRecognitionController.cropImageView;

public class Main extends Application
{
	/**
	 * The main class for a JavaFX application. It creates and handles the main
	 * window with its resources (style, graphics, etc.).
	 *
	 * This application looks for a hand in the video stream based on manual HSV input. It then
	 * detects the number of fingers being held up.
	 *
	 */

	public static ObjectMapper objectMapper = new ObjectMapper();
	public static String RESOURCES_PATH = System.getProperty("user.dir") + "\\resources\\";
	public static String PROPERTIES_FILE = RESOURCES_PATH + "properties.json";
	private ObjRecognitionController controller;
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			try {
				startOpenCV();
				//throw new Exception("ex");
			}catch (Exception e){
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
			primaryStage.setTitle("Object Recognition");
			primaryStage.setScene(scene);
			// show the GUI
			primaryStage.show();


			primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
			
			///////////////
			StackPane cropLayout = new StackPane();
			cropLayout.getChildren().add(cropImageView);

			Scene cropScene = new Scene(cropLayout, 400, 400);
			Stage cropWindow = new Stage();
			cropWindow.setTitle("Crop Image");
			cropWindow.setScene(cropScene);

			cropWindow.setX(primaryStage.getX() + 200);
			cropWindow.setY(primaryStage.getY() + 100);

			cropWindow.show();
			///////////////

			// set the proper behavior on closing the application
			controller = loader.getController();
			loadJson();
			primaryStage.setOnCloseRequest((we -> controller.setClosed()));
		}
		catch (Exception e)
		{
			showError(e);
		}
	}

	private void startOpenCV(){
		String opencvpath = System.getProperty("user.dir") + "\\resources\\native\\";
		try{
			System.load(opencvpath +"\\x64\\"+ Core.NATIVE_LIBRARY_NAME + ".dll");
		}
		catch (Throwable e){
			try {
				System.load(opencvpath +"\\x86\\"+ Core.NATIVE_LIBRARY_NAME + ".dll");
			}
			catch (Throwable ex){
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

	private void saveJson(){
		try {
			File file = new File(PROPERTIES_FILE);
			Properties properties = controller.getProperties();
			objectMapper.writeValue(file, properties);
		} catch (IOException e) {
			System.out.println("Error save JSON");
			e.printStackTrace();
		}
	}
	public void loadJson() {
		try {
			File file = new File(PROPERTIES_FILE);
			Properties properties = objectMapper.readValue(file, new TypeReference<Properties>(){});
			controller.setProperties(properties);
		} catch (IOException e) {
			System.out.println("Error loading JSON");
			Platform.exit();
		}
	}

	public static void saveImage(Mat image){
		Mat resizeimage = new Mat();
		Size sz = new Size(50,50);
		Imgproc.resize( image, resizeimage, sz );
		Imgcodecs.imwrite(RESOURCES_PATH + "\\images\\1.jpg", resizeimage);
	}


	public static void main(String[] args)
	{
		launch(args);
	}
	public static void log(String text)
	{
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
}