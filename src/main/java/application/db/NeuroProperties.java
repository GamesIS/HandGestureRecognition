package application.db;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

import static application.Main.RESOURCES_PATH;

@XmlRootElement(name = "Neuro_Properties")
@XmlAccessorType(XmlAccessType.FIELD)
public class NeuroProperties {
    @XmlElement(name = "iteration")
    private int iteration;

    @XmlElement(name = "weight")
    private double[][][] weightsArray;

    private static final String FILE_PATH = RESOURCES_PATH + "/neuro";
    private static final String FILE_NAME = "neuroProperties.xml";

    private NeuroProperties() {
    }

    public NeuroProperties(double[][][] weights, int iteration) {
        this.weightsArray = cloneArray(weights);
        this.iteration = iteration;
    }

    public static boolean isSaveProperty() {
        File dir = new File(FILE_PATH);
        if (dir.isDirectory()) {
            // получаем все вложенные объекты в каталоге
            for (File item : dir.listFiles()) {
                if (item.getName().equals(FILE_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void saveProperty(NeuroProperties neuroProperties) {
        try {
            //NeuroProperties neuroProperties = new NeuroProperties(weightsArray, iteration);
            //System.out.println(isSaveProperty());
            File file = new File(FILE_PATH + File.separator + FILE_NAME);
            JAXBContext context = JAXBContext.newInstance(NeuroProperties.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(neuroProperties, file);
            //marshaller.marshal(neuroProperties, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static NeuroProperties loadProperty() {
        NeuroProperties neuroProperties = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NeuroProperties.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            neuroProperties = (NeuroProperties) jaxbUnmarshaller.unmarshal(
                    new File(FILE_PATH + File.separator + FILE_NAME));

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return neuroProperties;
    }

    private static double[][][] cloneArray(double[][][] oldArray) {
        double[][][] newArray = new double[oldArray.length][oldArray[0].length][oldArray[0][0].length];
        for (int i = 0; i < oldArray.length; i++) {
            for (int j = 0; j < oldArray[i].length; j++) {
                for (int k = 0; k < oldArray[i][j].length; k++) {
                    newArray[i][j][k] = oldArray[i][j][k];
                }
            }
        }
        return newArray;

    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public double[][][] getWeightsArray() {
        return cloneArray(weightsArray);
    }

    public void setWeightsArray(double[][][] weightsArray) {
        this.weightsArray = cloneArray(weightsArray);
    }
}
