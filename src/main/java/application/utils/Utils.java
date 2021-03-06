package application.utils;


import application.db.Hand;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import static application.Main.RESOURCES_PATH;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.0 (2016-09-17)
 * @since 1.0
 */
public final class Utils {
    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        } catch (Exception e) {
            System.err.println("Cannot convert the Mat object: " + e);
            return null;
        }
    }

    public static Mat resizeMat(Mat image) {
        Mat resizeimage = new Mat();
        Size sz = new Size(50, 50);
        Imgproc.resize(image, resizeimage, sz);

        return resizeimage;
    }

    public static int[] mat2Array(Mat mRgba) { //TODO вроде норм работает
        MatOfInt rgb = new MatOfInt(CvType.CV_32S);
        mRgba.convertTo(rgb, CvType.CV_32S);
        int[] rgba = new int[(int) (rgb.total() * rgb.channels())];
        rgb.get(0, 0, rgba);
        return rgba;
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property a {@link ObjectProperty}
     * @param value    the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }


    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    public static int[] convertBinaryArrayToSingle(int[][] imageArray) {
        int k = 0;
        int[] tmpArray = new int[imageArray.length * imageArray[0].length];//Возможно заменить на imageArray.length * imageArray[0].length
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                tmpArray[k++] = imageArray[i][j];
            }
        }
        return tmpArray;
    }

    public static BufferedImage convertToGrayScale(BufferedImage image) {
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    public static Mat matRGB2MatGray(Mat img){
        int rows = img.rows(); //Calculates number of rows
        int cols = img.cols(); //Calculates number of columns
        int ch = img.channels(); //Calculates number of channels (Grayscale: 1, RGB: 3, etc.)

        for (int i=0; i<rows; i++)
        {
            for (int j=0; j<cols; j++)
            {
                double[] data = img.get(i, j); //Stores element in an array
                for (int k = 0; k < ch; k++) //Runs for the available number of channels
                {
                    data[k] = data[k] * 2; //Pixel modification done here
                }
                img.put(i, j, data); //Puts element back into matrix
            }
        }
        return img;
    }

    public static int[] getArrayRGB(Mat original){
        // Create an empty image in matching format
        BufferedImage gray = new BufferedImage(original.width(), original.height(), BufferedImage.TYPE_BYTE_GRAY);

        // Get the BufferedImage backing array and copy the pixels directly into it
        byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
        original.get(0, 0, data);

        int[] imageData = new int[original.width() * original.height()];
        imageData = gray.getData().getPixels(0, 0, original.width(), original.height(), imageData);
        return imageData;
    }

    public static int[] matToGrayIntArray(Mat original) {
        // init
        BufferedImage newImg = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        newImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        final byte[] targetPixels = ((DataBufferByte) newImg.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        int[] imageData = new int[width * width];
        imageData = convertToGrayScale(newImg).getData().getPixels(0, 0, width, height, imageData);
        return imageData;
    }

    public static void saveImage(Mat resizedImage, String gesture, boolean binary) {
        String folderGesture = RESOURCES_PATH + (binary?"\\binary\\":"\\images_data\\") + gesture;

        File dir = new File(folderGesture);
        int max_index = 0;
        if(!dir.exists()){
            dir.mkdir();
        }
        if(dir.isDirectory())
        {
            // получаем все вложенные объекты в каталоге
            for(File item : dir.listFiles()){
                String name = item.getName().split("\\.")[0].split("_")[1];
                int index = Integer.valueOf(name);
                max_index = max_index<index?index:max_index;
            }
        }
        max_index++;

        Imgcodecs.imwrite(folderGesture + "\\"+gesture+"_"+ + max_index + ".jpg", resizedImage);
    }

}