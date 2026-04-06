package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.Carriage;

import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrainRequest {
    private String trainName;

    //example: {key: 1, value: "20-SOFT_SEAT"}
    private Map<Integer, String> detail;

    public CreateTrainRequest(Map<Integer, String> detail, String trainName) {
        this.detail = detail;
        this.trainName = trainName;
    }


}
