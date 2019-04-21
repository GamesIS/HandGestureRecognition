package application.db;


import application.Main;
import application.NeuralNetwork;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Hand")
@XmlAccessorType(XmlAccessType.FIELD)
public class Hand {
    @XmlElement(name = "imageArray")
    private int[][] imageArray;
    @XmlElement(name = "hand")
    private String hand;
    @XmlElement(name = "num")
    private int num;

    private StringProperty name;
    public String path;


    private static final String FILE_PATH = Main.RESOURCES_PATH + "/images_data";
    private static final String FILE_EXPANSION = ".xml";

    public Hand() {
        this.name = new SimpleStringProperty();
        path = "";
    }

    public Hand(int[][] imageArray, String handType) {
        this.imageArray = imageArray;
        this.hand = handType;
        this.num = NeuralNetwork.getOutputNumber(handType);
        this.name = new SimpleStringProperty();
    }



    public static List<Hand> loadImages() {
        List<Hand> hands = new ArrayList<>();
        Hand hand;
        File mainDir = new File(FILE_PATH);
        File[] dirs = mainDir.listFiles();
        if (dirs != null) {
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    // получаем все вложенные объекты в каталоге
                    for (File img : dir.listFiles()) {
                        BufferedImage bufferedImage = null;
                        try {
                            bufferedImage = ImageIO.read(new File(img.getAbsolutePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int[][] imageArray = imageToArray(bufferedImage);

                        //images.add(new Image(img.getPath(), img.getName(), tmp, img.toURI().toURL().toString()));
                        //neuralNetwork.calculate(tmp);
                        String fileName = img.getName().split("\\.")[0];
                        //images.add(new Hand(tmp, img.getName().split("\\.")[0].charAt(0)));

                        hand = new Hand(imageArray, dir.getName());
                        hand.setName(dir.getName() + " - " + img.getName());
                        hands.add(hand);
                    }
                }
            }
        }
        return hands;
    }

    public static int[][] imageToArray(BufferedImage image) {// Перевод изображения в Градации серого
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int[] rgb = hexToRGB(image.getRGB(row, col));
                result[row][col] = (rgb[0] + rgb[1] + rgb[2]) / 3;
            }
        }
        return result;
    }

    public static int[] hexToRGB(int argbHex) {
        int[] rgb = new int[3];

        rgb[0] = (argbHex & 0xFF0000) >> 16; //get red
        rgb[1] = (argbHex & 0xFF00) >> 8; //get green
        rgb[2] = (argbHex & 0xFF); //get blue

        return rgb;//return array
    }

    /*public static int[][] imageToArray(BufferedImage image) {// Перевод в бинарное изображение
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (image.getRGB(row, col) < -1) {
                    result[row][col] = 1;
                } else {
                    result[row][col] = 0;
                }
            }
        }
        return result;
    }*/


    public int[][] getImageArray() {
        return imageArray;
    }

    public String getHand() {
        return hand;
    }

    public int getNum() {
        return num;
    }

    public void setImageArray(int[][] imageArray) {
        this.imageArray = imageArray;
    }

    public void setHand(String hand) {
        this.hand = hand;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String toString() {
        return name.get();
    }
}
