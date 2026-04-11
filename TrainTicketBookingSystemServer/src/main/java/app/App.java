package app;

public class App {
    public static void main(String[] args) {
        int port = SocketServer.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port. Use default port " + SocketServer.DEFAULT_PORT);
            }
        }

        new SocketServer().start(port);
    }
}
