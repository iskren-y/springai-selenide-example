package com.example

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import com.pengrad.telegrambot.UpdatesListener
import jakarta.annotation.PreDestroy
import org.springaicommunity.tool.search.ToolSearchToolCallAdvisor
import org.springaicommunity.tool.search.ToolSearcher
import org.springaicommunity.tool.searcher.LuceneToolSearcher
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.metadata.Usage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.ollama.api.OllamaChatOptions
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

import java.util.concurrent.ExecutorService

/**
 * Handles incoming Telegram messages.
 * Only responds to private text messages from non-bot users using AI-powered responses.
 */
@Component
class TelegramBotListener {

    private final TelegramBot telegramBot
    private final ChatModel chatModel
    private final BrowserTools browserTools
    private final ApiTools apiTools
    private final ToolSearcher toolSearcher
    private final ChatClient chatClient
    private final ExecutorService executorService

    private final String systemInstruction = """
You are an expert AI Telegram Quality Assurance engineer, specializing in Selenium browser automation. 
Treat each user message as a test case you need to execute. 
For each user message, think step-by-step: analyze intent, plan step executions, use tools (see TOOLS DESCRIPTION) and verify requirement. 
Be precise, helpful, and concise in responses. Your response length MUST not exceed the Telegram message limit. It must be in plain text (no markdown).
"""

    /**
     * Checks if the update is a valid private text message from a non-bot user.
     *
     * @param update the incoming Telegram update
     * @return true if the message should be processed
     */
    private static boolean isValidPrivateMessage(Update update) {
        def message = update.message()
        if (message == null) {
            return false
        }
        if (message.from()?.isBot()) {
            return false
        }
        if (message.chat().type() != Chat.Type.Private) {
            return false
        }
        if (message.text() == null || message.text().isBlank()) {
            return false
        }
        return true
    }

    TelegramBotListener(TelegramBot telegramBot,
                        ChatModel chatModel,
                        BrowserTools browserTools,
                        ApiTools apiTools,
                        ExecutorService executorService) {

        this.telegramBot = telegramBot
        this.chatModel = chatModel
        this.browserTools = browserTools
        this.apiTools = apiTools
        this.executorService = executorService
        this.toolSearcher = new LuceneToolSearcher()
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemInstruction)
                .defaultTools(browserTools, apiTools)
                .build()
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown()
    }

    /**
     * Starts the updates listener after the Spring application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    void init() {
        telegramBot.setUpdatesListener({ updates ->
            updates.each { Update update ->
                if (isValidPrivateMessage(update)) {
                    executorService.submit({ -> handleMessage(update) })
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL
        }, { exception ->
            println "[TelegramBot] Exception: ${exception.message}"
            if (exception.response()) {
                println "[TelegramBot] Error: ${exception.response().errorCode()} - ${exception.response().description()}"
            }
        })
        println "[TelegramBot] Started listening for messages"
    }

    /**
     * Processes the message and sends an AI-generated response.
     *
     * @param update the incoming Telegram update
     */
    private void handleMessage(Update update) {
        def chatId = update.message().chat().id()
        def userMessage = update.message().text()

        try {



            // Dynamic tool discovery, provided by Christian Tzolov and the Spring AI team
            // https://spring.io/blog/2025/12/11/spring-ai-tool-search-tools-tzolov
            ToolSearchToolCallAdvisor toolSearchToolAdvisor = ToolSearchToolCallAdvisor.builder()
                    .toolSearcher(toolSearcher)
                    .build()

            final List<Advisor> advisors = List.of(toolSearchToolAdvisor)


            ChatResponse response = chatClient.prompt()
                    .advisors(advisors)
                    .user(userMessage)
                    .call()
                    .chatResponse()

            Usage usage = response?.metadata?.usage

            println("OUTPUT: ${usage?.completionTokens}; INPUT: ${usage?.promptTokens}; TOTAL: ${usage?.totalTokens}")

            sendMessage(chatId, response?.result?.output?.text)
        } catch (Exception e) {
            println "[TelegramBot] Error processing message: ${e.message}"
            sendMessage(chatId, "Sorry, I encountered an error processing your message.")
        }
    }

    /**
     * Sends a text message to the specified chat.
     *
     * @param chatId the target chat ID
     * @param text the message text to send
     */
    private void sendMessage(Long chatId, String text) {
        if (text == null || text.isBlank()) {
            return
        }
        def request = new SendMessage(chatId, text).parseMode(ParseMode.HTML)
        SendResponse response = telegramBot.execute(request)
        if (!response.isOk()) {
            println "[TelegramBot] Send error: ${response.errorCode()} - ${response.description()}"
        }
    }
}
