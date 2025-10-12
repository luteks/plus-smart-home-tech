package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);
        AnalyzerStarter analyzer = context.getBean(AnalyzerStarter.class);
        analyzer.start();
    }
}