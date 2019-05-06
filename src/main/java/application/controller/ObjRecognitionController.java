package application.controller;

import application.Main;
import application.db.Properties;
import application.utils.Utils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.ea.javacnn.JavaCNN;
import org.ea.javacnn.data.DataBlock;
import org.ea.javacnn.losslayers.LossLayer;
import org.ea.javacnn.readers.ImageReader;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static application.controller.ListImagesController.obResList;


/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the image segmentation process.
 */
public class ObjRecognitionController {
    @FXML
    public Button camShiftButton;

    public static final ImageView cropImageView = new ImageView();
    public static Mat currentCropImage;
    public static Mat currentCropBinaryImage;
    /*public Slider Y_MIN;
    public Slider Y_MAX;
    public Slider Cr_MIN;
    public Slider Cr_MAX;
    public Slider Cb_MIN;
    public Slider Cb_MAX;*/
    public Slider kernel;
    //public Slider sigma;
    // FXML camera button
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;
    // the FXML area for showing the biraryMask
    @FXML
    private ImageView maskImage;

    @FXML
    public Slider blur;
    // FXML slider for setting HSV ranges
    @FXML
    private Slider hueStart;
    @FXML
    private Slider hueStop;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;
    // FXML label to show the current values set with the sliders
    @FXML
    private Label hsvCurrentValues;
    @FXML
    private Text resultText;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture = new VideoCapture();
    {
        capture.set(3, 640);
        capture.set(4, 480);
    }
    // a flag to change the button behavior
    private boolean cameraActive;

    // property for object binding
    private ObjectProperty<String> hsvValuesProp;

    private static boolean isTracking = false;

    private Point trackPoint1 = new Point(200, 125);
    private Point trackPoint2 = new Point(450, 375);
    private Rect trackRectangle = new Rect(trackPoint1, trackPoint2);

    private Mat biraryMask = new Mat();
    private Mat blurredImage = new Mat();
    private Mat hsvImage = new Mat();

    ///////////////////////TODO
    public static ImageReader mr = new ImageReader("images_data/train");
    public static JavaCNN net = JavaCNN.loadModel("CNN.bin");
    ///////////////////////TODO

    /**
     * The action triggered by pushing the button on the GUI
     */
    @FXML
    private void startCamera() {
        System.out.println("Test1");
        // bind a text property with the string containing the current range of
        // HSV values for object detection
        hsvValuesProp = new SimpleObjectProperty<>();
        this.hsvCurrentValues.textProperty().bind(hsvValuesProp);

        // set a fixed width for all the image to show and preserve image ratio
        this.imageViewProperties(this.originalFrame, 400);
        this.imageViewProperties(this.maskImage, 400);
        this.imageViewProperties(cropImageView, 400);

        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    // effectively grab and process a single frame
                    Mat frame = grabFrame();
                    // convert and show the frame
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(originalFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.cameraButton.setText("Выключить камеру");
            } else {
                // log the error
                System.err.println("Failed to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.cameraButton.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
        System.out.println("Test2");
    }

    @FXML
    private void startTracking() {
        if (isTracking) {
            tmp = 0;
            camShiftButton.setText("Запустить отслеживание");
            isTracking = false;
            trackRectangle = new Rect(trackPoint1, trackPoint2);
        } else {
            camShiftButton.setText("Выключить отслеживание");
            isTracking = true;
        }
    }

    public Mat histMask(Mat frame){
       /* Mat dst = new Mat();
        Mat hsv = new Mat();
        Mat disc = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        Imgproc.calcBackProject(Collections.singletonList(hsv), new MatOfInt(0, 1), hand_hist, dst, new MatOfFloat(0f, 180f, 0f, 256f), 1);

        disc = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(11, 11));
        Imgproc.filter2D(dst, disc,-1,  dst);*/

        Mat forHist = frame;
        Mat hsv = new Mat();
        Imgproc.cvtColor(forHist, hsv, Imgproc.COLOR_BGR2HSV);
        Mat hue = new Mat(hsv.size(), hsv.depth());
        Core.mixChannels(Collections.singletonList(hsv), Collections.singletonList(hue), new MatOfInt(0, 0));


        Mat backproj = new Mat();
       Imgproc.calcBackProject(Collections.singletonList(hue), new MatOfInt(0), hist, backproj, new MatOfFloat(hueRange), 1);
        //Image backprojImg = HighGui.toBufferedImage(backproj);

        /*
        cutFromBinary(){

        }*/

        //Imgproc.threshold(hand_hist, dst, 50,255,Imgproc.THRESH_BINARY);\

        //cutFromBinary(frame, dst);

        //Mat thresh = Core.merge(Cothresh, thresh, thresh))

        return backproj;
    }


    public static int tmp = 0;
    public static Mat hand_hist;
    public static Mat hist;
    public static List<Mat> hueList;
    public static float[] hueRange;

    public void calcHandHist(Mat originalFrame){
       /* Mat forHist = new Mat(originalFrame, trackRectangle);
        Imgproc.cvtColor(forHist, forHist, Imgproc.COLOR_BGR2HSV);
        hand_hist = new Mat();
        MatOfFloat histRange = new MatOfFloat(0f, 180f, 0f, 256f);
        Imgproc.calcHist(Collections.singletonList(forHist), new MatOfInt(0, 1), new Mat(), hand_hist, new MatOfInt( 180, 256), histRange);
        Core.normalize(hand_hist, hand_hist, 0, 255, Core.NORM_MINMAX);*/

        Mat forHist = new Mat(originalFrame, trackRectangle);
        Mat hsv = new Mat();
        Imgproc.cvtColor(forHist, hsv, Imgproc.COLOR_BGR2HSV);
        Mat hue = new Mat(hsv.size(), hsv.depth());
        Core.mixChannels(Collections.singletonList(hsv), Collections.singletonList(hue), new MatOfInt(0, 0));

        int bins = 25;
        int histSize = Math.max(bins, 2);
        hueRange = new float [2];
        hueRange[0] = 0;
        hueRange[1] = 180;

        hist = new Mat();
        hueList = Collections.singletonList(hue);
        Imgproc.calcHist(hueList, new MatOfInt(0), new Mat(), hist, new MatOfInt(histSize), new MatOfFloat(hueRange), false);
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);


        //Imgproc.threshold(hand_hist, originalFrame, 50,255,0);
        //Utils.saveImage(originalFrame, "test", false);
    }


    public void onCameraFrame(Mat scene, Rect maxRect, Mat originalFrame) {
        //System.out.println(trackRectangle.size().toString());//TODO разрешение ищображения
        if(tmp++ == 0){
            //calcHandHist(originalFrame);
        }
        //cv2.normalize(hand_hist, hand_hist, 0, 255, cv2.NORM_MINMAX)

        RotatedRect box = Video.CamShift(scene, trackRectangle, new TermCriteria(TermCriteria.EPS, 10, 1));
        trackRectangle = box.boundingRect();
        Point pt1 = new Point(trackRectangle.x, trackRectangle.y);
        Point pt2 = new Point(trackRectangle.x + trackRectangle.width, trackRectangle.y + trackRectangle.height);

        Rect rc = new Rect(pt1, pt2);
        Scalar RED = new Scalar(255, 0, 0);

		/*if(maxRect.area() < trackRectangle.area()){
            maxRect = trackRectangle;
		}*/
        //Imgproc.rectangle(originalFrame, pt1, pt2, RED);
        //Imgproc.rectangle(scene, trackRectangle, RED);


        cropImage(originalFrame, trackRectangle, maxRect, biraryMask);

        //return scene;
    }

    public void cropImage(Mat scene, Rect rc, Rect maxRect, Mat biraryMask) {
        //try
        Mat origCropImage = null;
        Mat binaryCropImage = null;
        try {
            origCropImage = new Mat(scene, rc);
            binaryCropImage = new Mat(biraryMask, rc);
        } catch (CvException ex) {
            //System.out.println("Выход за пределы изображения");// TODO поправить
        }
        if (origCropImage == null) {
            origCropImage = new Mat(scene, maxRect);
        }
        if (binaryCropImage == null) {
            binaryCropImage = new Mat(biraryMask, maxRect);
        }
        //saveImage(origCropImage);
        origCropImage = Utils.resizeMat(origCropImage);
        binaryCropImage = Utils.resizeMat(binaryCropImage);
        currentCropImage = origCropImage;
        currentCropBinaryImage = binaryCropImage;
        this.updateImageView(cropImageView, Utils.mat2Image(currentCropBinaryImage));
        //this.updateImageView(cropImageView, Utils.mat2Image(currentCropImage));

        //Main.listImagesController.recognize(mat2Array(origCropImage));

        recognize(Utils.matToGrayIntArray(currentCropBinaryImage));
        if(obResList != null){
            if(obResList.get(0).equals("yeah 00,553")){
                resultText.setText("Результат распознавания пусто");
            }
            else {
                resultText.setText("Результат распознавания " + obResList.get(0));
            }
        }

        //Mat mGray = new Mat();
        //Imgproc.cvtColor(currentCropImage,mGray,Imgproc.COLOR_RGB2GRAY);
        //recognize(Utils.matToGrayIntArray(mGray));
    }

    public void recognize(int[] image){
        DataBlock db = new DataBlock(mr.getSizeX(), mr.getSizeY(), 1, 0);
        db.addImageData(image, mr.getMaxvalue());
        net.forward(db, false);
        int prediction = net.getPrediction();
        double[] out = ((LossLayer)net.layers.get(net.layers.size()-1)).getOutAct().getWeights();
        Main.listImagesController.recognize(prediction, out);
    }


    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // init
                    blurredImage = new Mat();
                    hsvImage = new Mat();
                    biraryMask = new Mat();


                    // remove some noise
                    int blurValue = (int) blur.getValue();


                    Imgproc.blur(frame, blurredImage, new Size(blurValue, blurValue)); //TODO было 7 7 Добавить ползунок для Blur

                    // convert the frame to HSV
                    Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);


                    // get thresholding values from the UI
                    // remember: H ranges 0-180, S and V range 0-255
                    Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
                    Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());


                    // show the current selected HSV range
                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
                            + minValues.val[2] + "-" + maxValues.val[2];
                    Utils.onFXThread(this.hsvValuesProp, valuesToPrint);

                    // threshold HSV image
                    Core.inRange(hsvImage, minValues, maxValues, biraryMask);

                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(this.kernel.getValue(), this.kernel.getValue()));
                    Imgproc.morphologyEx(biraryMask, biraryMask, Imgproc.MORPH_CLOSE, kernel);
                    //Imgproc.erode(biraryMask, biraryMask, kernel);
                    //Imgproc.dilate(biraryMask, biraryMask, kernel);



					/*if(isTracking){
						onCameraFrame(biraryMask);
					}*/


                    // show the partial output
                    this.updateImageView(this.maskImage, Utils.mat2Image(biraryMask));


                    // find the contours and show them
                    frame = this.generateContours(biraryMask, frame);

                    //frame = cutFromBinary(frame);
                    if(tmp != 0){
                        //frame = histMask(frame);
                        frame = cutFromBinary(frame, biraryMask);
                        Imgproc.morphologyEx(frame, frame, Imgproc.MORPH_CLOSE, kernel);
                    }

                    //Core.subtract(frame, frame, biraryMask);

                }

            } catch (Exception e) {
                // log the (full) error
                System.err.print("Exception during the image elaboration...");
                e.printStackTrace();
            }
        }

        return frame;
    }

    public Mat cutFromBinary(Mat frame, Mat binary){
        Mat src1_mask = new Mat();
        Mat mask_out = new Mat();
        Imgproc.cvtColor(binary, src1_mask, Imgproc.COLOR_GRAY2BGR);
        Core.subtract(src1_mask, frame, mask_out);
        Core.subtract(src1_mask,mask_out, mask_out);
        return mask_out;
    }

    public void skinDetect(){
        /*Scalar min = new Scalar(0,133,77);
        Scalar max = new Scalar(255,173,127);

        // if the frame is not empty, process it
        if (!frame.empty()) {
            Mat skinRegion = new Mat();
            Imgproc.cvtColor(frame,imageYCrCb, Imgproc.COLOR_BGR2YCrCb);



            //# Find region with skin tone in YCrCb image
            //skinRegion = cv2.inRange(imageYCrCb,min_YCrCb,max_YCrCb)
            Core.inRange(frame,min,max,skinRegion);

            genCounters(skinRegion, frame);

                    *//*# Do contour detection on skin region
                    contours, hierarchy = cv2.findContours(skinRegion, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

                    # Draw the contour on the source image
                    for i, c in enumerate(contours):
                    area = cv2.contourArea(c)
                    if area > 1000:
                    cv2.drawContours(sourceImage, contours, i, (0, 255, 0), 3)*//*



        }*/
    }

    //helper method to find biggest contour
    private int findBiggestContour(List<MatOfPoint> contours) {
        int indexOfBiggestContour = -1;
        double sizeOfBiggestContour = 0;
        for (int i = 0; i < contours.size(); i++) {
            if (Imgproc.contourArea(contours.get(i)) > sizeOfBiggestContour) {
                sizeOfBiggestContour = Imgproc.contourArea(contours.get(i));
                indexOfBiggestContour = i;
            }
        }
        return indexOfBiggestContour;
    }

    public Mat genCounters(Mat maskedImage, Mat frame){
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		// if any contours exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
		{
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			{
				Imgproc.drawContours(frame, contours, idx, new Scalar(0, 255, 0));
            }
		}

		return frame;
    }


    /**
     * Given a binary image containing one or more closed surfaces, use it as a
     * biraryMask to find and highlight the objects contours
     *
     * @param maskedImage the binary image to be used as a biraryMask
     * @param frame       the original {@link Mat} image to be used for drawing the
     *                    objects contours
     * @return the {@link Mat} image with the objects contours framed
     */
    private Mat generateContours(Mat maskedImage, Mat frame) {
//		// init
//		List<MatOfPoint> contours = new ArrayList<>();
//		Mat hierarchy = new Mat();
//		
//		// find contours
//		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//		
//		// if any contours exist...
//		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
//		{
//			// for each contour, display it in blue
//			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//			{
//				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
//			}
//		}
//		
//		return frame;

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();


        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfInt> hull = new ArrayList<MatOfInt>();

        List<MatOfInt4> defects = new ArrayList<MatOfInt4>();
        for (int i = 0; i < contours.size(); i++) {
            hull.add(new MatOfInt());
            defects.add(new MatOfInt4());
        }

        //generate the convex hull and defects 
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.convexHull(contours.get(i), hull.get(i));
            Imgproc.convexityDefects(contours.get(i), hull.get(i), defects.get(i));
        }


        // Loop over all contours
        List<Point[]> hullpoints = new ArrayList<Point[]>();
        for (int i = 0; i < hull.size(); i++) {
            Point[] points = new Point[hull.get(i).rows()];

            // Loop over all points that need to be hulled in current contour
            for (int j = 0; j < hull.get(i).rows(); j++) {
                int index = (int) hull.get(i).get(j, 0)[0];
                points[j] = new Point(contours.get(i).get(index, 0)[0], contours.get(i).get(index, 0)[1]);
            }

            hullpoints.add(points);
        }

        // Convert Point arrays into MatOfPoint
        List<MatOfPoint> hullmop = new ArrayList<MatOfPoint>();
        for (int i = 0; i < hullpoints.size(); i++) {
            MatOfPoint mop = new MatOfPoint();
            mop.fromArray(hullpoints.get(i));
            hullmop.add(mop);
        }

        // Draw contours + hull results
        int biggestContourIndex = findBiggestContour(contours);
        int fingerCount = 1;


        /*Rectan
		if(trackingFrame == null){
			trackingFrame = new Rect(scene.cols()/2,scene.rows()/2,50,50);
		}
		//System.out.println(trackingFrame.size().toString());
		RotatedRect box =Video.CamShift(frame, trackingFrame, new TermCriteria(TermCriteria.EPS,10,1));
		Video.CamShift()*/
        Scalar color = new Scalar(0, 255, 0);   // Green
        Rect maxRect = null;
        for (int i = 0; i < contours.size(); i++) {

            //choose only the biggest contour
            if (i == biggestContourIndex) {
                //Imgproc.drawContours(frame, contours, i, new Scalar(0,0,255),2);
                Moments moment = Imgproc.moments(contours.get(i));
                if (moment.m00 != 0){
                    int cx = (int)(moment.m10 / moment.m00);
                    int cy = (int)(moment.m01 / moment.m00);
                    Imgproc.circle(frame, new Point(cx, cy), 7, new Scalar(255, 0, 0), 6);
                }
                //draw convex hull of biggest contour
                //Imgproc.drawContours(frame, hullmop, i, new Scalar(0,255,255),2);

            //MatOfPoint points = hullmop.get(0);
            maxRect = Imgproc.boundingRect(hullmop.get(0));//TODO можно попробовать сделать проверку что Area входит в область Tracking
            for (MatOfPoint matOfPoint : hullmop) {
                Rect rect = Imgproc.boundingRect(matOfPoint);
                if (maxRect.area() < rect.area()) {
                    maxRect = rect;
                }
            }


            //TODO нужно выбрать MatOfPoint с самой большой областью и оставить его
            // Get bounding rect of contour
            //Rect rect = Imgproc.boundingRect(points);


                //Imgproc.boundingRect(hullmop);
                //Imgproc.drawContours(frame, hullmop, i, new Scalar(0,255,255),2);\
                //Imgproc.rectangle(frame,maxRect, new Scalar(0,255,255), 3); //TODO


            } else //draw smaller contours in green
            {
                //Imgproc.drawContours(frame, contours, i, color);

            }
        }

        //Imgproc.Canny(biraryMask, frame, 50, 200, 3, false);

        //Imgproc.rectangle(frame, maxRect, new Scalar(0, 255, 255), 3);
        //Imgproc.rectangle(biraryMask,trackRectangle, new Scalar(255,255,0), 3);
        if (!isTracking) {
            Imgproc.rectangle(frame, trackRectangle, new Scalar(255, 255, 0), 3);
        } else {
            onCameraFrame(biraryMask, maxRect, frame);// TODO ЕСЛИ ЦЕНТРЫ НЕДАЛЕКО и квадрат больше(или же берем самые крайние точки) БЕРЕМ ЦЕНТР С ЗЕЛЕНОГО RECT
        }
		/*else{
			onCameraFrame(frame);
		}
*/

        //resultText.setText(fingerCount + " finger(s) detected");

        return frame;


    }

    private Mat histAndBackproj() {
       /* Mat hist = new Mat();
        int h_bins = 30;
        int s_bins = 32;

        // C++:
        //int histSize[] = { h_bins, s_bins };
        MatOfInt mHistSize = new MatOfInt (h_bins, s_bins);

        // C++:
        //float h_range[] = { 0, 179 };
        //float s_range[] = { 0, 255 };
        //const float* ranges[] = { h_range, s_range };
        //int channels[] = { 0, 1 };

        MatOfFloat mRanges = new MatOfFloat(0, 179, 0, 255);
        MatOfInt mChannels = new MatOfInt(0, 1);

        // C++:
        // calcHist( &hsv, 1, channels, mask, hist, 2, histSize, ranges, true, false );

        //check 'mask', it was mMat0 in ImageManipulationsActivity
        // 'mask' – Optional mask. If the matrix is not empty, it must be an 8-bit array of the same size as images[i] .
        // The non-zero mask elements mark the array elements counted in the histogram.

        List<Mat> lHSV = Arrays.asList(mHSV);

        boolean accumulate = false;
        Imgproc.calcHist(lHSV, mChannels, mask, hist, mHistSize, mRanges, accumulate);

        // C++:
        // normalize( hist, hist, 0, 255, NORM_MINMAX, -1, Mat() );
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX, -1, new Mat());

        // C++:
        // calcBackProject( &hsv, 1, channels, hist, backproj, ranges, 1, true );
        Mat backproj = new Mat();
        Imgproc.calcBackProject(lHSV, mChannels, hist, backproj, mRanges, 1);

        return backproj;*/
       return null;
    }



    //Pre: MatOfInt4 of defect list, MatOfPoint of hand contour, and index j of defect of interest
    private boolean isFinger(MatOfInt4 defect, MatOfPoint contour, int j) {

		Rect boundingRect= Imgproc.boundingRect(contour);
		int tolerance = boundingRect.height / 5;
		double angleTol = 95;	
		//store indexes of start, end, and far points
		int startid = defect.toList().get(j);
		//store the point on the contour as a Point object
		Point startPt = contour.toList().get(startid);
		int endid = defect.toList().get(j+1);
		Point endPt = contour.toList().get(endid);
		int farid = defect.toList().get(j+2);
		Point farPt = contour.toList().get(farid);
		
		if (distanceFormula(startPt,farPt)>tolerance && 
			distanceFormula(endPt,farPt)>tolerance && 
			getAngle(startPt,endPt,farPt) < angleTol &&
			endPt.y <= (boundingRect.y + boundingRect.height - boundingRect.height/4) &&
			startPt.y <= (boundingRect.y + boundingRect.height - boundingRect.height/4))
				return true;

        return false;
    }


    //use Law of Cosines to find angle between 3 points
    private double getAngle(Point start, Point end, Point far) {
        //distance between start and far
        double a = distanceFormula(start, far);
        //distance between end and far
        double b = distanceFormula(end, far);
        //distance between start and end (side c of triangle)
        double c = distanceFormula(start, end);
        //Law of Cosines
        double angle = Math.acos((a * a + b * b - c * c) / (2 * a * b));
        angle = angle * 180 / Math.PI;
        return angle;
    }

    private double distanceFormula(Point start, Point end) {
        return Math.sqrt(Math.abs(Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2)));
    }

    /**
     * Set typical {@link ImageView} properties: a fixed width and the
     * information to preserve the original image ration
     *
     * @param image     the {@link ImageView} to use
     * @param dimension the width of the image to set
     */
    private void imageViewProperties(ImageView image, int dimension) {
        // set a fixed width for the given ImageView
        image.setFitWidth(dimension);
        // preserve the image ratio
        image.setPreserveRatio(true);
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    public void setClosed() {
        this.stopAcquisition();
    }

    public void setProperties(Properties properties) {
        blur.setValue(properties.getBlur());
        hueStart.setValue(properties.getHueStart());
        hueStop.setValue(properties.getHueStop());
        saturationStart.setValue(properties.getSaturationStart());
        saturationStop.setValue(properties.getSaturationStop());
        valueStart.setValue(properties.getValueStart());
        valueStop.setValue(properties.getValueStop());
        /*Y_MAX.setValue(properties.getY_MAX());
        Y_MIN.setValue(properties.getY_MIN());
        Cr_MAX.setValue(properties.getCr_MAX());
        Cr_MIN.setValue(properties.getCr_MIN());
        Cb_MAX.setValue(properties.getCb_MAX());
        Cb_MIN.setValue(properties.getCb_MAX());*/
        kernel.setValue(properties.getKernel());
        //sigma.setValue(properties.getSigma());
    }


    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setBlur(blur.getValue());
        properties.setHueStart(hueStart.getValue());
        properties.setHueStop(hueStop.getValue());
        properties.setSaturationStart(saturationStart.getValue());
        properties.setSaturationStop(saturationStop.getValue());
        properties.setValueStart(valueStart.getValue());
        properties.setValueStop(valueStop.getValue());
        /*properties.setY_MAX(Y_MAX.getValue());
        properties.setY_MIN(Y_MIN.getValue());
        properties.setCr_MAX(Cr_MAX.getValue());
        properties.setCr_MIN(Cr_MIN.getValue());
        properties.setCb_MAX(Cb_MAX.getValue());
        properties.setCb_MIN(Cb_MIN.getValue());*/
        properties.setKernel(kernel.getValue());
        //properties.setSigma(sigma.getValue());
        return properties;
    }

}