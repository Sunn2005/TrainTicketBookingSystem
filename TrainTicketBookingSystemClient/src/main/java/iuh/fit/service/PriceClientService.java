//package iuh.fit.service;
//
//import controller.PriceController;
//import model.entity.BasePrice;
//
//public class PriceClientService {
//    private final PriceController delegate;
//
//    public PriceClientService() {
//        this.delegate = new PriceController();
//    }
//
//    public PriceClientService(PriceController delegate) {
//        this.delegate = delegate;
//    }
//
//    public BasePrice getBasePrice() {
//        return delegate.getBasePrice();
//    }
//
//    public boolean updateBasePrice(BasePrice basePrice) {
//        return delegate.updateBasePrice(basePrice);
//    }
//}
package iuh.fit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.socketconfig.SocketClient;
import model.entity.BasePrice;

public class PriceClientService {

    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public BasePrice getBasePrice() {
        try {
            String res = socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    "GET_BASE_PRICE"
            );

            if (res == null || res.startsWith("ERROR")) return null;

            return mapper.readValue(res, BasePrice.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String updateBasePrice(BasePrice basePrice) {
        try {
            String json = mapper.writeValueAsString(basePrice);

            return socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    "UPDATE_BASE_PRICE|" + json
            );

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
}