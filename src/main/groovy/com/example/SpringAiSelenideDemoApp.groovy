package com.example

import com.pengrad.telegrambot.TelegramBot
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SpringBootApplication
class SpringAiSelenideDemoApp {

    @Value('${telegram.bot.apiKey}')
    String telegramBotApiKey

    static void main(String[] args) {
        SpringApplication.run(SpringAiSelenideDemoApp, args)
    }

    /**
     * Creates the TelegramBot bean.
     *
     * @return configured TelegramBot instance
     */
    @Bean
    TelegramBot telegramBot() {
        return new TelegramBot.Builder(telegramBotApiKey).build()
    }

    @Bean
    ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor()
    }
}
