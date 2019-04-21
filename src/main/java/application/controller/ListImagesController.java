package application.controller;

import application.Main;
import application.NeuralNetwork;
import application.db.Hand;
import application.db.Image;
import application.utils.StudyingThread;
import application.utils.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static application.NeuralNetwork.COUNT_OUTPUT_NEURON;
import static application.controller.ObjRecognitionController.currentCropImage;


public class ListImagesController {
    private static ListImagesController controller;
    private static final int WIDTH_RECTANGLE = 8;
    public static final int COUNT_PIXEL = 50;
    private static int[][] currentDrawImageAray = new int[COUNT_PIXEL][COUNT_PIXEL];

    public ComboBox handsBox;

    @FXML
    private TableView<Image> imageTable;
    @FXML
    private TableColumn<Image, String> pathColumn;
    @FXML
    private TableColumn<Image, String> nameColumn;

    @FXML
    private ListView<Image> nameList;

    @FXML
    private ListView<String> resultList;

    @FXML
    private ListView<Hand> customList;

    @FXML
    private ImageView imageView;

    @FXML
    private Canvas canvas;

    @FXML
    private Canvas drawCanvas;

    @FXML
    private GridPane drawGrid;

    @FXML
    public Label percent;

    @FXML
    public Label nameLabel;

    @FXML
    public Button studying;
    @FXML
    public Button clear;
    @FXML
    public Button recognizeButton;
    @FXML
    public Button save;

    @FXML
    public TextField nameChar;

    @FXML
    public javafx.scene.image.Image image;

    @FXML
    public LineChart<Number, Number> convergence;

    @FXML
    NumberAxis xAxisIteration;
    @FXML
    NumberAxis yAxisError;

    private static ObservableList<String> obResList;

    private static StudyingThread studyingThread;

    @FXML
    public void initialize() {
        convergence.setTitle("Ошибка от эпохи");

        studying.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            studyingThread = new StudyingThread(main.getNeuralNetwork(), main.getHands());
            Thread myThready = new Thread(studyingThread);    //Создание потока "myThready"

            myThready.start();
        });


        clear.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            for (int i = 0; i < rec.length; i++) {
                for (int j = 0; j < rec[i].length; j++) {
                    rec[i][j].setFill(Color.WHITE);
                }
            }
        });
        recognizeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            recognize(getDrawImageArray());
        });
        save.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {

            //
            Utils.saveImage(currentCropImage, (String) handsBox.getValue());
            //TODO можно еще сделать, чтобы сразу в общий список добавлялось в рантайме
            /*
            Hand ch = new Hand(getDrawImageArray(), chName);
            Hand.saveChar(ch);
            main.getHands().add(ch);
            */
        });

        setHandComboBox();


        series1 = new XYChart.Series();
        series1.setName("Зависимость ошибки от эпохи");

        convergence.getData().addAll(series1);
    }

    public void setHandComboBox(){
        ObservableList<String> handsBoxObsList= FXCollections.observableArrayList();
        handsBoxObsList.addAll("fist", "one", "two", "three", "four", "hand", "ok", "class", "bad", "none");
        handsBox.setItems(handsBoxObsList);
        handsBox.setValue("none");
    }

    public void recognize(int[] imageArray){
        Main.getNeuralNetwork().calculate(imageArray);
        Platform.runLater(this::writePercent);
    }

    public void recognize(int[][] imageArray){
        Main.getNeuralNetwork().calculate(imageArray);
        writePercent();
    }
    public XYChart.Series series1;

    private Main main;

    private Rectangle[][] rec;

    public ListImagesController() {
        controller = this;
    }

    public static ListImagesController getController() {
        return controller;
    }

    public void setMain(Main main) {
        this.main = main;

        imageTable.setItems(main.getImageData());

        imageTable.setVisible(false);
        nameList.setItems(main.getImageData());


        nameList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        imageView.setSmooth(false);


        xAxisIteration.setLabel("Epoch");
        yAxisError.setLabel("Error");

        canvas.setVisible(false);

        makeGrid();


        ObservableList<Hand> hands = FXCollections.observableArrayList();

        hands.addAll(Hand.loadImages());


        customList.setItems(hands);

        resultList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        nameList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Image image = imageTable.getItems().get(newValue.intValue());
            imageView.setImage(image.getImage());
            GraphicsContext gc = canvas.getGraphicsContext2D();

            int[][] imageArray = image.getImageArray();
            for (int i = 0; i < imageArray.length; i++) {
                for (int j = 0; j < imageArray[i].length; j++) {
                    if (imageArray[i][j] == 0) gc.setFill(Color.WHITE);
                    else gc.setFill(Color.BLACK);
                    gc.fillRect(20 * i, 20 * j, 20, 20);

                }
            }

            for (int i = 0; i < imageArray.length; i++) {
                for (int j = 0; j < imageArray.length; j++) {
                    if (imageArray[i][j] == 0) rec[i][j].setFill(Color.WHITE);
                    else rec[i][j].setFill(Color.RED);
                }
            }
            main.getNeuralNetwork().calculate(imageArray);

            writePercent();

        });

        customList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            Hand ch = customList.getItems().get(newValue.intValue());
            int[][] imageArray = ch.getImageArray();
            drawMatrix(imageArray);
        });
    }

    //Отрисовка матрицы
    private void drawMatrix(int[][] imageArray) {
        currentDrawImageAray = imageArray;
        for (int row = 0; row < imageArray.length; row++) {
            for (int col = 0; col < imageArray.length; col++) {
                rec[row][col].setFill(Color.grayRgb(imageArray[row][col]));
            }
        }
    }

    private int[][] getDrawImageArray() {
        /*int[][] imageArray = new int[COUNT_PIXEL][COUNT_PIXEL];
        for(int i = 0; i < rec.length; i++ ){
            for(int j = 0; j< rec[i].length; j++){
                ((Color) rec[i][j].getFill()).get;
            }
        }*/
        return currentDrawImageAray;
    }

    private void writePercent() {
        DecimalFormat decimalFormatter = new DecimalFormat("###.###");
        decimalFormatter.setMinimumIntegerDigits(2);
        decimalFormatter.setMinimumFractionDigits(3);

        obResList = FXCollections.observableArrayList();


        List<Sorted> sortList = new ArrayList<>();
        for (int i = 0; i < COUNT_OUTPUT_NEURON; i++) {
            double error = main.getNeuralNetwork().getError(i);
            sortList.add(new Sorted("" + NeuralNetwork.getOutputChar(i) + " = " + decimalFormatter.format(error) + "%", error));

            //obResList.add("" + NeuralNetwork.getOutputChar(i) + " = "+ decimalFormatter.format(main.getNeuralNetwork().getError(i)) +"%");
        }
        System.out.println();
        Collections.sort(sortList);

        for (int i = sortList.size() - 1; i >= 0; i--) {
            obResList.add(sortList.get(i).strVal);
        }
        resultList.setItems(obResList);
        System.out.println();
    }

    class Sorted implements Comparable {
        String strVal;
        double dblVal;

        public Sorted() {
        }

        public Sorted(String strVal, double dblVal) {
            this.strVal = strVal;
            this.dblVal = dblVal;
        }

        @Override
        public int compareTo(Object o) {
            Sorted oSort = (Sorted) o;
            if (oSort.dblVal > this.dblVal) return -1;
            if (oSort.dblVal < this.dblVal) return 1;
            return 0;
        }
    }

    public GridPane makeGrid() {

        drawGrid.setGridLinesVisible(true);
        rec = new Rectangle[COUNT_PIXEL][COUNT_PIXEL];

        for (int i = 0; i < rec.length; i++) {
            for (int j = 0; j < rec[i].length; j++) {
                rec[i][j] = new Rectangle();
                rec[i][j].setX(i * WIDTH_RECTANGLE);
                rec[i][j].setY(j * WIDTH_RECTANGLE);
                rec[i][j].setWidth(WIDTH_RECTANGLE);
                rec[i][j].setHeight(WIDTH_RECTANGLE);
                rec[i][j].setFill(null);
                rec[i][j].setStroke(Color.BLACK);
                //p.getChildren().add(rec[i][j]);
                drawGrid.add(rec[i][j], i, j);
            }
        }

        drawGrid.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::draw);

        drawGrid.setOnMouseClicked(this::draw);

        return drawGrid;
    }

    private void draw(MouseEvent event) {
        double posX = event.getX();
        double posY = event.getY();

        System.out.println("posX = " + posX + " posY = " + posY);

        posX -= (posX / 15);
        posY -= (posY / 15);

        int colX = (int) ((posX / WIDTH_RECTANGLE));
        int colY = (int) ((posY / WIDTH_RECTANGLE));
        System.out.println("colX = " + colX + " colY = " + colY);

        if (colX < COUNT_PIXEL && colY < COUNT_PIXEL) {
            if (event.getButton() == MouseButton.SECONDARY) {
                rec[colX][colY].setFill(Color.WHITE);
            } else {
                rec[colX][colY].setFill(Color.RED);
            }
        }
    }
}
