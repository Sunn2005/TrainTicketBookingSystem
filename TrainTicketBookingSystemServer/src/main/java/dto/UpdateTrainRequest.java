package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainRequest {
    private String trainID;
    private String trainName;
    // example: {key: 1, value: "20-SOFT_SEAT"}
    private Map<Integer, String> detail;
}
