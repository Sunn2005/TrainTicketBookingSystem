package iuh.fit.service;

import controller.PriceController;
import model.entity.BasePrice;

public class PriceClientService {
    private final PriceController delegate;

    public PriceClientService() {
        this.delegate = new PriceController();
    }

    public PriceClientService(PriceController delegate) {
        this.delegate = delegate;
    }

    public BasePrice getBasePrice() {
        return delegate.getBasePrice();
    }

    public boolean updateBasePrice(BasePrice basePrice) {
        return delegate.updateBasePrice(basePrice);
    }
}