package org.example;

import org.example.Connection;
import org.example.ConsoleHelper;
import org.example.Message;
import org.example.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server has been activated");


        while (true) {
            try {
                Socket socket = serverSocket.accept();

                Thread thread = new Handler(socket);
                thread.start();
            } catch (Exception e) {
                serverSocket.close();
                e.printStackTrace();
                break;
            }

        }

    }

    public static void sendBroadcastMessage(Message message) {
        for (String string : connectionMap.keySet()) {
            try {
                connectionMap.get(string).send(message);
            } catch (Exception e) {
                System.out.println("Message wasn't sent");
            }

        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("a new connection address: " + socket.getRemoteSocketAddress() + "has been set");
            String name = null;
            try (Connection connection = new Connection(socket)) {
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                ConsoleHelper.writeMessage("Connection with " + socket.getRemoteSocketAddress() + " was closed");
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Error");
            }

        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            MessageType messageType = null;
            Message message = null;
            while (messageType != MessageType.USER_NAME) {
                while (messageType != MessageType.USER_NAME) {
                    connection.send(new Message(MessageType.NAME_REQUEST));
                    message = connection.receive();
                    messageType = message.getType();
                }
                if (message.getData().trim().length() == 0 || connectionMap.containsKey(message.getData())) {
                    messageType = null;
                }
            }

            connectionMap.put(message.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));

            return message.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String string : connectionMap.keySet()) {
                if (!string.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, string));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message newMessage = new Message(MessageType.TEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(newMessage);
                } else {
                    ConsoleHelper.writeMessage("ERROR: message is not a TEXT type");
                }
            }

        }
    }
}
