package application;

import application.controller.ListImagesController;
import application.db.Hand;
import application.db.NeuroProperties;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private static final int HIDDEN_LAYERS = 2;

    private static final int COUNT_HIDDEN_NEURON = 70 + 1;
    private static final int COUNT_INPUT_NEURON = 2500 + 1;//Из-за нейрона смещения + 1
    public static final int COUNT_OUTPUT_NEURON = 9;//Из-за кол-ва букв

    private static final int COUNT_LAYERS = HIDDEN_LAYERS + 2;// Скрытые слои + входной и выходной слой

    private static final int COUNT_EDGE = COUNT_LAYERS - 1;// Количество слоев ребер = Количество слоев нейронов - 1

    private static final int COUNT_EPOCH = 100;

    private static final double BIAS_NEURON = 1;// Нейрон смещения /*их можно размещать на входном слое и всех скрытых слоях, но никак не на выходном слое*/

    private static final double d = 1; // параметр наклона сигмоидальной функции

    private static final double SPEED = 0.06; // коэффициент скорости обучения
    //private static final double SPEED = 0.03; // коэффициент скорости обучения
    private static final double ALPHA = 0.00005;//α (альфа) — момент
    //private static final double ALPHA = 0.0005;//α (альфа) — момент

    private static final double SENSITIVITY = 0.0000000000002;

    private int iteration; //Сколько раз нейросеть прошла по trainingSet

    private double[][][] weights;//[Номер слоя ребра][Номер левого нейрона][Номер правого нейрона]
    private double[][][] deltaWeights;//[Номер слоя ребра][Номер левого нейрона][Номер правого нейрона]

    private double[][] neurons;//[Номер слоя][Номер нейрона]
    private double[] input;// Входные значения
    private int[] layersLength;// Массив хранящий длину каждого слоя

    private int trainingSet;//Нужно рандомизировать подачу тренировочных данных
    private int verifyingSet;

    public NeuralNetwork() {
        layersLength = new int[COUNT_LAYERS];
        layersLength[0] = COUNT_INPUT_NEURON;
        for (int i = 1; i < HIDDEN_LAYERS + 1; i++) {
            layersLength[i] = COUNT_HIDDEN_NEURON;
        }
        layersLength[layersLength.length - 1] = COUNT_OUTPUT_NEURON;
    }

    public void load() {
        if (NeuroProperties.isSaveProperty()) {
            NeuroProperties neuroProperties = NeuroProperties.loadProperty();
            weights = neuroProperties.getWeightsArray();
            iteration = neuroProperties.getIteration();
        } else {
            weights = new double[COUNT_EDGE][maxLengthLayers()][COUNT_HIDDEN_NEURON];
            NeuralNetwork.fillRandomDouble(weights, layersLength);
            NeuroProperties.saveProperty(new NeuroProperties(weights, iteration));
        }
    }

    private static int maxLengthLayers() {
        return COUNT_HIDDEN_NEURON >= COUNT_INPUT_NEURON ? COUNT_HIDDEN_NEURON : COUNT_INPUT_NEURON;
    }

    public void calculate(int[][] imageArray) {
        calculate(convertBinaryArrayToSingle(imageArray));
    }

    public void calculate(int[] imageArray) {
        calculate(intArrayToDouble(imageArray));
    }

    public void calculate(double[] imageArray) {
        loadInput(imageArray);//Инициированы входные значения, там пропускаем через функцию активации
        neurons = new double[COUNT_LAYERS][maxLengthLayers()];
        neurons[0] = input;
        double res; // Переменная для подсчета взвешенной суммы
        for (int i = 1; i < layersLength.length; i++) {// Пробегаемся по всем слоям, начиная со 2
            neurons[i - 1][0] = BIAS_NEURON;
            for (int j = 0; j < layersLength[i]; j++) {//Пробегаемся по всем нейронам слоя
                res = 0;
                /**
                 * Считаем для данного нейрона взвешенную сумму
                 * пробегаясь по нейронам левого слоя
                 * */
                for (int x = 0; x < layersLength[i - 1]; x++) {
                    res += neurons[i - 1][x] * weights[i - 1][x][j];
                }
                /**
                 * Из-за того что res получался всегда 1
                 * нужна использовать нормализацию*/
                //double norm = normalization(res, i);
                neurons[i][j] = activate(res);//Прогоняем результат через активационную функцию
            }
        }
        //System.out.println();

    }

    /**
     * Используется метод обратного распространения //Возможно их несколько вариантов
     */
    public void study(List<Hand> hands) {
        double[] expectedResult;//Ожидаемый результат
        double errEpoch;
        double percent = 0;
        double step = 100D / COUNT_EPOCH;
        long ms = System.currentTimeMillis();
        boolean time = true;
        long hour;
        long minutes;
        long seconds;
        String errorText;
        DecimalFormat decimalFormatter = new DecimalFormat("###.###");
        decimalFormatter.setMinimumFractionDigits(3);
        decimalFormatter.setMinimumIntegerDigits(2);
        XYChart.Series series = ListImagesController.getController().series1;
        ObservableList<XYChart.Data> graphic = FXCollections.observableArrayList();

        deltaWeights = new double[COUNT_EDGE][maxLengthLayers()][maxLengthLayers()];
        for (int epoch = 0; epoch < COUNT_EPOCH; epoch++) {
            errEpoch = 0;
            for (Hand ch : hands) {
                calculate(ch.getImageArray());//TODO тут возможна ошибка
                int lastLayer = layersLength.length - 1;// номер последнего слоя
                double weights_delta;//Дельта весов (ошибка*производную активационной функции)

                expectedResult = getExpectedResult(ch.getNum());
                /**
                 * смысл в том, что мы сначала рассчитываем ошибку для конечного выходного нейрона, а потом передаем ее обратно
                 * рассчитывая значения для предыдущих нейронов, на основе этого изменяем веса
                 * */

                boolean normalRes = false;

                for (int i = 0; i < layersLength[lastLayer]; i++) {
                    if (Math.abs(neurons[lastLayer][i] - expectedResult[i]) <= SENSITIVITY) {
                        normalRes = false;
                    } else {
                        normalRes = true;
                        break;
                    }
                }

                double delta = 0;
                double error = 0;

                for (int i = 0; i < expectedResult.length; i++) {
                    delta = neurons[lastLayer][i] - expectedResult[i];
                    error += delta * delta;
                }

                errEpoch += Math.sqrt(error / expectedResult.length);

                if (normalRes) {
                    double[] deltRightLayer = new double[layersLength[lastLayer]];
                    double[] deltLeftLayer = new double[layersLength[lastLayer - 1]];

                    for (int j = lastLayer; j >= 0; j--) {//Начинаем с конечного слоя. Текущий (правый) слой
                        for (int i = 0; i < layersLength[j]; i++) {//Пробегаемся по всем нейронам СНАЧАЛА текущего(правого) ПОТОМ ЛЕВОГО слоя
                            if (j == lastLayer) {
                                deltRightLayer[i] = (expectedResult[i] - neurons[lastLayer][i]) * activateDerivative(neurons[lastLayer][i]);// TODO ВОЗМОЖНО НУЖНА ОТДЕЛЬНАЯ ФУНКЦИЯ
                                /**
                                 * ошибка_левого_нейрона_версии_2 = (ожидаемый - текущий) * текущий * (1 - текущий)
                                 * текущий - результат уже пропущенный через активационную функцию для нейрона у которого рассчитываем ошибку
                                 * http://microtechnics.ru/obuchenie-nejronnoj-seti-algoritm-obratnogo-rasprostraneniya-oshibok/?fdx_switcher=true
                                 * */
                                if (Math.abs(deltRightLayer[i]) <= SENSITIVITY)
                                    break;// Если верно не корректируем веса TODO ОШИБКА СЧИТАЕТСЯ ОБЩАЯ У МЕНЯ ОШИБКА ЭТО ДЕЛЬТА ЭТО УСЛОВИЕ ДОЛЖНО БЫТЬ ВЫШЕ
                            } else {
                                /**
                                 * ошибка для внутренних нейронов рассчитывается
                                 * error = вес_ребра_до_нейрона_справа(1)*ошибка_нейрона_справа(1) ... + вес_ребра_до_нейрона_справа(n)*ошибка_нейрона_справа(n)
                                 * http://robocraft.ru/blog/algorithm/560.html
                                 *
                                 * ошибка_левого_нейрона_версии_2 =  сумма_их_всех((ошибка_нейрона_справа_K*вес_до_нейрона_справа_К)) * текущий * (1 - текущий)
                                 * http://microtechnics.ru/obuchenie-nejronnoj-seti-algoritm-obratnogo-rasprostraneniya-oshibok/?fdx_switcher=true
                                 * дельта_весов = коэффициент_скорости_обучения * ошибка_правого_нейрона_версии_2 * результат_левого_нейрона_после_активационной_функции(текущий)
                                 * новый вес = вес + дельта_весов;
                                 * */
                                if (j != 0) {
                                    for (int k = 0; k < deltRightLayer.length; k++) {// Считаем сумму дельт
                                        deltLeftLayer[i] += deltRightLayer[k] * weights[j][i][k];
                                    }
                                    deltLeftLayer[i] *= activateDerivative(neurons[j][i]);
                                }
                                for (int k = 0; k < deltRightLayer.length; k++) {
                                    weights_delta = SPEED * deltRightLayer[k] * neurons[j][i] + ALPHA * deltaWeights[j][i][k];//TODO И ПОХОЖЕ КОРРЕКТИРОВКУ ВЕСОВ МЕНЯТЬ
                                    deltaWeights[j][i][k] = weights_delta;
                                    weights[j][i][k] += weights_delta;
                                    /**
                                     *формула для расчета нового веса   newWeight = oldWeight + коэффициент_скорости_обучения * значения_выходного_нейрона(слева) * дельту_весов(нейрона справа)
                                     * */
                                }
                            }
                        }
                        if (j < lastLayer) {
                            deltRightLayer = deltLeftLayer;
                            if (j - 1 > 0) {
                                deltLeftLayer = new double[layersLength[j - 1]];
                            }
                        }
                    }
                }
            }
            if (time) {
                ms = System.currentTimeMillis() - ms;
                long msTosec = (ms * COUNT_EPOCH / 1000);
                hour = msTosec / (60 * 60);
                long tmp = msTosec % (60 * 60);
                minutes = tmp / (60);
                seconds = tmp % (60);
                System.out.println("Примерное время расчета = [" + hour + ":" + minutes + ":" + seconds + "]");
                time = false;
            }
            errEpoch /= COUNT_INPUT_NEURON;
            percent += step;
            //ListImagesController.getController().percent.setText(decimalFormatter.format(percent));

            errorText = "Ошибка эпохи:" + errEpoch + "  [" + decimalFormatter.format(percent) + "]";

            graphic.add(new XYChart.Data(epoch, errEpoch));

            //labelError.setText(errorText);

            /*Platform.runLater(new Runnable() {
                @Override
                public void run() {

                }
            });*/


            //ListImagesController.getController().convergence.getData().addAll(series);

            System.out.println(errorText);

            if (errEpoch < SENSITIVITY) {
                break;
            }
            //save();
            Collections.shuffle(hands);
        }
        series.getData().addAll(graphic);
    }

    public double[] getExpectedResult(int ch) {
        double[] output = new double[COUNT_OUTPUT_NEURON];
        for (int i = 0; i < output.length; i++) {
            output[i] = 0.0D;
        }
        output[ch] = 1.0D;
        return output;
    }

    private void loadInput(double[] newInput) {
        input = newInput;
        for (int i = 0; i < newInput.length; i++) {
            input[i] = activate(newInput[i]);//Специально использовал функцию активации, хоть значения 1 и 0, чтобы потом не забыть

        }
    }

    public double normalization(double x, int numLayer) {
        return x / (layersLength[numLayer]);
    }

    private double activate(double x) {// Функция активации (Нормальзации значения [0;1])
        return (1 / (1 + Math.pow(Math.E, -(d * x))));//Сигмоид
    }

    private double activateDerivative(double x) {// Производная активационной функции
        return (1 - x) * x;//Сигмоид
    }

    private double[] convertBinaryArrayToSingle(int[][] imageArray) {
        int k = 0;
        double[] tmpArray = new double[COUNT_INPUT_NEURON];//Возможно заменить на imageArray.length * imageArray[0].length
        //tmpArray[0] = BIAS_NEURON;//TODO тут идет Инициализация нейрона смещения
        for (int i = 0; i < imageArray.length; i++) {
            for (int j = 0; j < imageArray[i].length; j++) {
                tmpArray[++k] = (double) imageArray[i][j];
            }
        }
        return tmpArray;
    }

    private double[] intArrayToDouble(int[] imageArray) {
        int k = 0;
        double[] tmpArray = new double[COUNT_INPUT_NEURON];//Возможно заменить на imageArray.length * imageArray[0].length
        try{
            //tmpArray[0] = BIAS_NEURON;//TODO тут идет Инициализация нейрона смещения
            for (int i = 0; i < imageArray.length; i++) {
                tmpArray[++k] = (double) imageArray[i];
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return tmpArray;
    }

    private static void fillRandomDouble(double[][][] weightsArray, int[] layersLength) {
        double rangeMin = -0.5f;
        double rangeMax = 0.5f;
        Random r = new Random();
        for (int i = 0; i < COUNT_LAYERS - 1; i++) {//Бежим по слоям
            for (int j = 0; j < layersLength[i]; j++) {
                for (int k = 0; k < layersLength[i + 1]; k++) {
                    weightsArray[i][j][k] = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
                    //System.out.print(weightsArray[i][j][k]);
                }
                //System.out.println();
            }
            /*System.out.println();
            System.out.println();
            System.out.println();*/
        }
    }

    public static String getOutputChar(int number) {
        switch (number) {
            case 0:
                return "fist";
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            case 5:
                return "hand";
            case 6:
                return "ok";
            case 7:
                return "class";
            case 8:
                return "yeah";
            case 9:
                return "none";
        }
        throw new IllegalArgumentException("Выход за пределы массива");
    }

    public static int getOutputNumber(String hand) {
        switch (hand) {
            case "fist":
                return 0;
            case "one":
                return 1;
            case "two":
                return 2;
            case "three":
                return 3;
            case "four":
                return 4;
            case "hand":
                return 5;
            case "ok":
                return 6;
            case "class":
                return 7;
            case "yeah":
                return 8;
            case "none":
                return 9;
        }
        throw new IllegalArgumentException("Нет такого жеста");
    }

    public void save() {
        NeuroProperties.saveProperty(new NeuroProperties(weights, iteration));
    }

    public void writeResultsForSym(int[][] imageArray) {
        calculate(imageArray);//TODO тут возможна ошибка
        DecimalFormat decimalFormatter = new DecimalFormat("###.###");
        for (int i = 0; i < COUNT_OUTPUT_NEURON; i++) {
            System.out.println(getOutputChar(i) + " " + decimalFormatter.format(getError(i)));
        }
    }

    public double getError(int ch) {
        double[] excpectedResult = getExpectedResult(ch);

        double delta = 0;
        double error = 0;

        for (int i = 0; i < excpectedResult.length; i++) {
            delta = neurons[layersLength.length - 1][i] - excpectedResult[i];// Считаем разницу между тем что должно быть и есть
            error += delta * delta;//
        }
        error /= excpectedResult.length;
        error = Math.sqrt(error);
        error = (1 - error) * 100;

        return error;
    }

}
