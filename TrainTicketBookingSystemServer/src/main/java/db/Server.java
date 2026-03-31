package db;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        System.out.println("connected" + JPAUtil.getEntityManager());

    }
}
