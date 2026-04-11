package iuh.fit.controller;

import controller.PriceController;
import model.entity.BasePrice;

public class PriceClientController {
    private final PriceController delegate;

    public PriceClientController() {
        this.delegate = new PriceController();
    }

    public PriceClientController(PriceController delegate) {
        this.delegate = delegate;
    }

    public BasePrice getBasePrice() {
        return delegate.getBasePrice();
    }

    public boolean updateBasePrice(BasePrice basePrice) {
        return delegate.updateBasePrice(basePrice);
    }
}