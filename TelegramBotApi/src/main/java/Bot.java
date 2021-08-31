import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;


public class Bot extends TelegramLongPollingBot {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void onUpdateReceived(Update update) {     //переписать в свич
        Model model = new Model();
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            if (message.getText().startsWith("!смена")) {
                String date = message.getText().trim().substring(7);
                if (!(date.matches("^\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d$")))
                    sendMsg(message, "Неправильно указана дата, вводи в формате \"дд.мм.гггг\"");
                else {
                    sendMsg(message, getDateStatus(date));
                }

            } else if (message.getText().startsWith("!напомни")) {
                System.out.println(message.getText());
                String[] strings = message.getText().split(" ", 3);
                int time = 0;
                try {
                    time = Integer.parseInt(strings[1]);
                    System.out.println(time);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sendMsg(message, "Неверно указано время, не буду напоминать.");
                }
                String text = strings[2];
                if (time > 0) {
                    napominalka napominalka = new napominalka(time, message, text);
                    napominalka.start();
                }


            } else if (message.getText().startsWith("!график")) {
                String date = message.getText().trim().substring(8);
                if (!(date.matches("^\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d$")))
                    sendMsg(message, "Неправильно указана дата, вводи в формате \"дд.мм.гггг\"");
                else {
                    sendMsg(message, getSchedule(date));
                }
            } else if (message.getText().startsWith("!погода")) {
                String city = message.getText().trim().substring(8);
                try {
                    sendMsg(message, Weather.getWeather(city, model));
                } catch (IOException exception) {
                    sendMsg(message, "Неверно указан город");
                }


            } else if (message.getText().startsWith("!анекдот")){
                try {
                    sendMsg(message, Anekdot.getAnekdot());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

            }
        }

    }

    public String getDateStatus(String string) { //ГРАФИК БЛЯТЬ РАБОТАЕТ НЕПРАВИЛЬНО
        Calendar calendar = new GregorianCalendar();
        int day = Integer.parseInt(string.split("\\.")[0]);  //переписать красивее
        int month = Integer.parseInt(string.split("\\.")[1]); //переписать красивее
        int year = Integer.parseInt(string.split("\\.")[2]); //переписать красивее
        if (month > 12 || month < 1) return "Нет такой даты";
        if (day < 1
                || day > 31
                || month == 2 && !visokosniy(year) && day > 28
                || month == 2 && visokosniy(year) && day > 29
                || (month == 4 && day > 30)
                || (month == 6 && day > 30)
                || (month == 9 && day > 30)
                || (month == 11 && day > 30)) return "Нет такой даты";
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        if ((calendar.getTimeInMillis() / 1000 / 3600 / 24) % 3 == 2) return ("Вторая смена");
        else if ((calendar.getTimeInMillis() / 1000 / 3600 / 24) % 3 == 1) return ("Выходной");
        else return ("Первая смена");

    }

    public String getBotUsername() {
        return "wokko16Schedule_bot";
    }
    public String getBotToken() {
        return "1641813398:AAF8P-MEpMxglymVX8dsdhMlpYLlsqAcUv0";
    }

    public boolean visokosniy(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }

    public class napominalka extends Thread {
        int minutes;
        Message message;
        String text;

        public napominalka(int minutes, Message message, String text) {
            this.minutes = minutes;
            this.message = message;
            this.text = text;
        }

        public void run() {
            sendMsg(message, "Запомнил, напомню через " + minutes + " минут.");
            try {
                Thread.sleep(1000 * minutes * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendMsg(message, "Напоминаю:  " + text);
            this.interrupt();
        }
    }

    public String getSchedule(String date) {
        if (getDateStatus(date).equals("Нет такой даты")) return "Нет такой даты";
        String[] dates = date.split("\\.");
        String day = dates[0];
        String month = dates[1];
        String year = dates[2];
        File file = new File("C:\\Users\\di-pe\\Desktop\\TelegramBotApi\\src\\main\\resources\\WorkingDates" + month + year);
        Scanner scanner;
        String line = "";
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException exception) {
            return "Не смог открыть файл с графиками, скорее всего графика нет";
        }
        System.out.println(day);
        System.out.println(month);
        System.out.println(year);
        while (scanner.hasNext()) {
            System.out.println(line);
            if (line.startsWith(day)) {
                System.out.println("hehe");
                String[] workers = line.split(" ");
                return "График на " + date + ":\n" +
                        "На норме в обычных - " + workers[1] + ", помогает " + workers[3] + "\n" +
                        "На норме в персе - " + workers[2] + ", помогает " + workers[4] + "\n" +
                        "Видеовериф - " + workers[5];
            }
            line = scanner.nextLine();
        }
        if (line.startsWith(day)) {
            System.out.println("hehe");
            String[] workers = line.split(" ");
            return "График на " + date + ":\n" +
                    "На норме в обычных - " + workers[1] + ", помогает " + workers[3] + "\n" +
                    "На норме в персе - " + workers[2] + ", помогает " + workers[4] + "\n" +
                    "Видеовериф - " + workers[5];
        }
        scanner.close();
        return "Выходной, никто не работает";

    }
}

