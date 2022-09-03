package com.sakh.telegrambot.service;

import com.sakh.telegrambot.config.BotConfig;
import com.sakh.telegrambot.model.User;
import com.sakh.telegrambot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig botConfig;

    final static String HELP_TEXT = "there is some info about bot and its commands\n\n";

    TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfBotCommands = new ArrayList<>();
        listOfBotCommands.add(new BotCommand("/help", "get info about this bot"));
        listOfBotCommands.add(new BotCommand("/start", "get welcoming message"));
        listOfBotCommands.add(new BotCommand("/getmydata", "get my data"));
        listOfBotCommands.add(new BotCommand("/deletemydata", "delete my data"));
        listOfBotCommands.add(new BotCommand("/settings", "get settings"));
        try {
            this.execute(new SetMyCommands(listOfBotCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error occurred in setting the bot's menu" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    log.info("unknown command:" + messageText);
                    sendMessage(chatId, "Command is not found. Try again!");

            }

        }
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            Long chatId = msg.getChatId();
            Chat chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user added" + user.toString());


        }
    }

    private void startCommandReceived(long chatId, String name) {

        String answer = "Hello, " + name + ", let's start!";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }

    }
}
