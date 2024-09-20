package com.example.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.*;

@Component
public class DatabaseConnection {

    @Value("${database.url}")
    private String url;

    @Value("${database.name}")
    private String name;

    @Value("${database.password}")
    private String password;

    public void saveToDatabaseWithoutData(Update update) {
        try {
            Connection connection = DriverManager.getConnection(
                    url,
                    name,
                    password
            );
            // Создаем объект Statement
            Statement statement = connection.createStatement();

            // Выполняем SQL-запрос
            ResultSet resultSet = statement.executeQuery(String.format("INSERT INTO users (user_name) VALUES ('%s')", update.getMessage().getChat().getFirstName()));

            // Закрываем соединение
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException ignore) {

        }
    }
}