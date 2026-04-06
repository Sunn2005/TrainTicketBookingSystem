package controller;

import dto.CreateTrainRequest;
import service.TrainService;

public class TrainController {
    private final TrainService trainService;

    public TrainController() {
        this.trainService = new TrainService();
    }

    public void createTrain(CreateTrainRequest request) {
        trainService.createTrain(request);
    }
}
