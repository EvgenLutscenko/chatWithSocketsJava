package org.example.client;

import org.example.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + ((int) (Math.random() * 100));
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            String name = null;
            String mes = "";
            if(message.indexOf(':') != -1){
                name = message.substring(0, message.indexOf(':'));
                mes = message.substring(message.indexOf(':') + 2);
            }


            Calendar now = new GregorianCalendar();
            try {
                if (mes.equals("дата")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("d.MM.YYYY").format(now.getTime()));
                } else if (mes.equals("день")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("d").format(now.getTime()));
                } else if (mes.equals("месяц")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("MMMM").format(now.getTime()));
                } else if (mes.equals("год")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("yyyy").format(now.getTime()));
                } else if (mes.equals("время")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("H:mm:ss").format(now.getTime()));
                } else if (mes.equals("час")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("H").format(now.getTime()));
                } else if (mes.equals("минуты")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("m").format(now.getTime()));
                } else if (mes.equals("секунды")) {
                    sendTextMessage("Информация для " + name + ": " + new SimpleDateFormat("s").format(now.getTime()));
                }
            } catch (Exception e) {
                ConsoleHelper.writeMessage("ERROR");
            }

        }
    }
}
