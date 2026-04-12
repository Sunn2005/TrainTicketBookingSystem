package iuh.fit.service;

import controller.TrainController;
import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;

public class TrainClientService {
    private final TrainController delegate;

    public TrainClientService() {
        this.delegate = new TrainController();
    }

    public TrainClientService(TrainController delegate) {
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