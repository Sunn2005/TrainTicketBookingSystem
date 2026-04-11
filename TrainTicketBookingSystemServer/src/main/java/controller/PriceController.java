package controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.BasePrice;

import java.io.File;
import java.io.IOException;
public class PriceController {
    private static final String FILE_PATH = "json/basePrice.json";
    private final ObjectMapper mapper = new ObjectMapper();
    public BasePrice getBasePrice() {
        try {
            return mapper.readValue(new File(FILE_PATH), BasePrice.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean updateBasePrice(BasePrice newPrice) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), newPrice);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
