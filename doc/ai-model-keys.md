# 🔑 Getting API Keys for AI Models

This guide explains how to obtain API keys and project identifiers for the supported AI models in **Instant Chatbot**.

---

## 1. Google Gemini (Free Tier) 🌟
Google offers the most generous free tier for developers via **Google AI Studio**.

### Option A: Google AI Studio (Easiest)
1. Go to [Google AI Studio (aistudio.google.com)](https://aistudio.google.com/).
2. Click on **"Get API key"** in the sidebar.
3. Create a new API key in a new or existing project.
4. **Note:** While the backend uses the `vertex-ai-gemini` starter for official integration, you can also use your Gemini API key with OpenAI-compatible clients by setting the `base-url` to `https://generativelanguage.googleapis.com/v1beta/openai/`.

### Option B: Vertex AI (Production/Standard)
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new Project.
3. Enable the **Vertex AI API**.
4. Set your `GOOGLE_PROJECT_ID` in the `.env` file.
5. Authenticate your local environment using the [Google Cloud CLI](https://cloud.google.com/sdk/docs/install): `gcloud auth application-default login`.

---

## 2. Anthropic Claude 🧠
Claude (by Anthropic) is known for its high reasoning capabilities.

1. Create an account at the [Anthropic Console](https://console.anthropic.com/).
2. Navigate to **"Get API Keys"**.
3. Create a new key and add it to `ANTHROPIC_API_KEY` in your `.env`.
4. **Free Credits:** Anthropic occasionally provides $5 in free credits to new developer accounts for testing.

---

## 3. OpenAI (GPT-4o / GPT-4o-mini) 🤖
The industry standard for RAG pipelines.

1. Go to the [OpenAI Platform](https://platform.openai.com/api-keys).
2. Create a new API key.
3. **Usage:** OpenAI requires a minimum deposit (e.g., $5) to activate the API for most users. The `gpt-4o-mini` model is extremely cheap (~$0.15 per 1M input tokens).

---

## 💡 Pro Tip: Switch Providers Instantly
You can toggle between different providers in your `.env` file without changing any code:

```env
# Switch between: openai, anthropic, google
AI_PROVIDER=google
```

The system will automatically swap the underlying chat engine while maintaining your RAG context.
