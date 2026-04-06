package dao;

import model.entity.Route;

public class RouteDAO extends BaseDAO<Route, String> {
    public RouteDAO() {
        super(Route.class);
    }
}
