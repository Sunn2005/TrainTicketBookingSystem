package dao;

import model.entity.Train;

public class TrainDAO extends BaseDAO<Train, String> {
    public TrainDAO() {
        super(Train.class);
    }
}
