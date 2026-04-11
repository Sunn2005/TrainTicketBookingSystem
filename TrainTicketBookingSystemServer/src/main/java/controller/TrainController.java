package controller;

import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;
import service.TrainService;

public class TrainController {
    private final TrainService trainService;

    public TrainController() {
        this.trainService = new TrainService();
    }

    public void createTrain(CreateTrainRequest request) {
        trainService.createTrain(request);
    }

    public void updateTrain(UpdateTrainRequest request) {
        trainService.updateTrain(request);
    }

    public void deleteTrain(String trainID) {
        trainService.deleteTrain(trainID);
    }
}
