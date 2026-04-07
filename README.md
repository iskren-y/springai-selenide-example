# E2E Test Agent With Ollama and Spring AI

Minimalistic E2E test agent powered by Ollama (local LLM), Spring AI, and Selenide for browser automation. Controllable via Telegram bot.

## Tech Stack

- Spring Boot 3.5.10
- Spring AI 2.0.0-M4 (Ollama)
- Groovy 4.0.30
- Selenide 7.14.0
- Java 24
- Telegram Bot API

## Prerequisites

- [Ollama](https://ollama.com/) installed and running locally on port 11434
- Pull the default model: `ollama pull gemma4:26b`

## Setup for Local Usage

Export the following environment variables in your shell:

```bash
export TELEGRAM_BOT_API_KEY=your_telegram_bot_api_key_here
```

## Running Locally

```bash
./gradlew build
docker-compose up
```

## Testing

Unit and integration testing is out of scope for this example.

## Note

This project represents a minimalistic version of the E2E test agent.
