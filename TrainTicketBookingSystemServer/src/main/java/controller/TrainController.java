package controller;

import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;
import model.entity.Train;
import service.TrainService;

import java.util.List;

public class TrainController {
    private final TrainService trainService;

    public TrainController() {
        this.trainService = new TrainService();
    }


    public void updateTrain(UpdateTrainRequest request) {
        trainService.updateTrain(request);
    }

    public void deleteTrain(String trainID) {
        trainService.deleteTrain(trainID);
    }

    // Thêm method này
    public List<Train> getAllTrains() {
        return trainService.getAllTrains();
    }

    // Thêm method này
    public Train getTrainById(String trainId) {
        return trainService.getTrainById(trainId);
    }

    // Sửa method để return Train
    public Train createTrain(CreateTrainRequest request) {
        return trainService.createTrain(request);  // trainService đã return Train rồi
    }
}
