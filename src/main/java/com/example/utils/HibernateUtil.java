package com.example.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HibernateUtil {

    @Getter
    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    @PostConstruct
    public static void init() {
        sessionFactory = new Configuration().configure().buildSessionFactory();
    }

    @PreDestroy
    public static void destroy() {
        sessionFactory.close();
    }
}