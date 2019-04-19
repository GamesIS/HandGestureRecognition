package application;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
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
	public static String RESOURCES_PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\";
	public static String PROPERTIES_FILE = RESOURCES_PATH + "properties.json";
	private ObjRecognitionController controller;
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
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
			e.printStackTrace();
		}
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
		String libpath = System.getProperty("java.library.path");
		//libpath = libpath + ";C:/Users/Ilya/Desktop/ObjectDetection/lib/native";
		System.out.println(System.setProperty("java.library.path", libpath));

		String opencvpath = System.getProperty("user.dir") + "\\lib\\native\\";
		System.out.println(opencvpath);
		String libPath = System.getProperty("java.library.path");
		System.out.println(libPath);
		System.out.println(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
		System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");

		//Runtime.getRuntime().loadLibrary("C:/Users/Ilya/Desktop/ObjectDetection/lib/opencv_java401.dll");
		//loadNativeLibrary("C:/Users/Ilya/Desktop/ObjectDetection/lib/native", "opencv_java401.dll");
		//System.loadLibrary("opencv_java401");
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		launch(args);
	}

	private static synchronized boolean loadNativeLibrary(String path, String name) {
		File libPath = new File(path, name);
		if (libPath.exists()) {
			try {
				System.load("C:/Users/Ilya/Desktop/ObjectDetection/lib/native/opencv_java401.dll");
				return true;
			} catch (UnsatisfiedLinkError e) {
				System.err.println(e);
				return false;
			}

		} else
			return false;
	}
}