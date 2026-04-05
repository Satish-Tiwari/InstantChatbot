# 📖 User Guide

Welcome to **Instant Chatbot**! This guide will help you create your first AI-powered chatbot in minutes.

## Step 1: Create an Account
1. Open the dashboard at http://localhost:3000.
2. Click on **Sign Up** and enter your details.
3. Log in to access your dashboard.

## Step 2: Create a New Project
1. Click the **"New Project"** button.
2. Give your project a name (e.g., "Company Knowledge Base").
3. Enter the URL of the website you want the chatbot to learn from.
4. Click **"Create Project"**.

## Step 3: Train Your Chatbot
1. On the project dashboard, click **"Start Crawl"**.
2. Wait for the system to:
    - Discover pages from your website.
    - Extract and clean the text content.
    - Generate semantic embeddings and store them.
3. You can monitor the progress (pages found, chunks created) in real-time.

## Step 4: Test Your Chatbot
1. Once the status turns to **READY**, click on the **"Chat Playground"**.
2. Send messages like "What are your services?" or "How can I contact support?".
3. The chatbot will answer using information exclusively from your website, providing sources for its answers.

## Step 5: Deploy Your Chatbot
1. Click the **"Download Package"** button.
2. You will receive a ZIP file containing:
    - A standalone Node.js/Python server.
    - The processed knowledge base embeddings.
    - An embeddable HTML/JS widget for your website.
3. Follow the `README.md` inside the ZIP to deploy your bot in your own infrastructure.

---

## Tips for Best Results
- **URL Selection**: Start with a root URL that has a clear `sitemap.xml` if possible.
- **Content Quality**: The chatbot is only as good as the content it reads. Ensure your website has up-to-date and clear information.
- **Rate Limits**: Large websites may take longer to crawl.
