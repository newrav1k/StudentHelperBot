package com.example.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    @Value("${database.url}")
    private String url;

    @Value("${database.name}")
    private String name;

    @Value("${database.password}")
    private String password;

    public void saveToDatabaseUsername(Update update) {
        Message message = update.getMessage();
        try (Connection connection = DriverManager.getConnection(url, name, password)) {
            String sql = "INSERT INTO users (user_name) VALUES ('?')";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, message.getChat().getUserName());
                preparedStatement.executeQuery();
            }
        } catch (SQLException exception) {
            log.error(exception.getMessage());
        }
    }
}