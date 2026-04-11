package iuh.fit.controller;

import controller.TrainController;
import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;

public class TrainClientController {
    private final TrainController delegate;

    public TrainClientController() {
        this.delegate = new TrainController();
    }

    public TrainClientController(TrainController delegate) {
        this.delegate = delegate;
    }

    public void createTrain(CreateTrainRequest request) {
        delegate.createTrain(request);
    }

    public void updateTrain(UpdateTrainRequest request) {
        delegate.updateTrain(request);
    }

    public void deleteTrain(String trainId) {
        delegate.deleteTrain(trainId);
    }
}