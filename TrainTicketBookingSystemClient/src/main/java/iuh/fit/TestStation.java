package iuh.fit;

import controller.StationController;

public class TestStation {
    public static void main(String[] args) {
        try {
            StationController sc = new StationController();
            System.out.println(sc.getAllStations());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
