package iuh.fit.socketconfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SocketClient {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9999;
    public String sendMessage(String host, int port, String message) throws IOException {
        List<String> responses = sendMessages(host, port, List.of(message));
        return responses.isEmpty() ? "No response" : responses.getFirst();
    }

    public String sendRawMessage(String host, int port, String message) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            reader.readLine();
            writer.write(message);
            writer.newLine();
            writer.flush();
            return reader.readLine();
        }
    }

    public List<String> sendMessages(String host, int port, List<String> messages) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String greeting = reader.readLine();
            List<String> responses = new ArrayList<>();

            for (String message : messages) {
                writer.write(message);
                writer.newLine();
                writer.flush();

                String response = reader.readLine();
                responses.add(response);
            }
            return responses;
        }
    }
}