package org.example.client;

import org.example.Connection;
import org.example.ConsoleHelper;
import org.example.Message;
import org.example.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    protected volatile boolean clientConnected = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.run();
    }

    public void run() throws IOException, InterruptedException {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this){
            try {
                wait();
            }catch (Exception e){
                ConsoleHelper.writeMessage("ERROR!");
                return;
            }
        }
        if(clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        }else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        String msg = null;
        while (clientConnected){
            msg = ConsoleHelper.readString();
            if(msg.equals("exit")) break;

            if(shouldSendTextFromConsole()){
                sendTextMessage(msg);
            }
        }
    }

    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text) throws IOException {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        }catch (Exception e){
            ConsoleHelper.writeMessage("something was wrong with connection!");
            clientConnected = false;
        }

    }

    public class SocketThread extends Thread{
        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(address, port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (Exception e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT){
                    processIncomingMessage(message.getData());
                }else if(message.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }else if(message.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("User: " + userName + " has been connected to chat!");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("User: " + userName + " has been disconnected!");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            synchronized (Client.this){
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }
    }
}
