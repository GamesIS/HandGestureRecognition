package application.utils;

import application.NeuralNetwork;
import application.db.Hand;

import java.util.List;

public class StudyingThread implements Runnable {
    private NeuralNetwork neuralNetwork;
    private List<Hand> hands;

    public StudyingThread(NeuralNetwork neuralNetwork, List<Hand> hands) {
        this.neuralNetwork = neuralNetwork;
        this.hands = hands;
    }

    @Override
    public void run() {
        neuralNetwork.study(hands);
        neuralNetwork.save();
    }
}
