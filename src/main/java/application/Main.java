package application;

import java.io.File;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

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

			// set the proper behavior on closing the application
			ObjRecognitionController controller = loader.getController();
			primaryStage.setOnCloseRequest((we -> controller.setClosed()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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