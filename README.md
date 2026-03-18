# E2E Test Agent With Gemini and Spring AI

Minimalistic E2E test agent powered by Google Gemini, Spring AI, and Selenide for browser automation. Controllable via Telegram bot.

## Tech Stack

- Spring Boot 3.5.10
- Spring AI 1.1.3 (Google GenAI)
- Groovy 4.0.30
- Selenide 7.14.0
- Java 24
- Telegram Bot API

## Setup for Local Usage

`SDKMan` installed locally is required

Create a `.env` file in the project root with the following required keys:

```bash
GEMINI_API_KEY=your_gemini_api_key_here
TELEGRAM_BOT_API_KEY=your_telegram_bot_api_key_here
```

## Running Locally

```bash
sdk env
./gradlew build
docker-compose up
```

## Testing

Unit and integration testing is out of scope for this example.

## Note

This project represents a minimalistic version of the E2E test agent.
