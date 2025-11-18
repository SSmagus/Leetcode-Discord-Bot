# ⚔️ LeetHost — LeetCode Discord Bot

LeetHost is a Discord bot that brings competitive coding, daily LeetCode tracking, and 1v1 duels directly into your Discord server.

---

## 🎥 Demo

https://github.com/user-attachments/assets/7244ad6f-724c-477e-8ad7-105d95784cca


---

## 🚀 Features

### 👥 User Commands
- `>register` — Register yourself
  - `>done` - To verify the completion of registration request task  
- `>profile` — View your stats
- `>stalk <user>` — View another user's profile

### ⚔️ Duel System
- `>duel <@user> <difficulty>` — Challenge someone  
- `>accept` — Accept a duel  
- `>decline` — Decline a duel  
- Duel requests automatically expire  

### 🔧 Utility
- `>ping` — Check if the bot is online  
- `>help` — Show all commands  

---

## 🧩 Tech Stack
- Java 17+  
- Spring Boot  
- Discord4J  
- MySQL  
- Hibernate / JPA  
- LeetCode GraphQL API  
- ScheduledExecutorService (duel expiry timers)

---

## 🔧 How to Run the Bot (For Developers)

### 1. Clone the repository
```bash
git clone https://github.com/SSmagus/Leetcode-Discord-Bot.git
cd discord-bot
```

### 2. Set environment variables  
Do NOT commit real tokens or passwords.

#### Windows (PowerShell)
```powershell
setx DB_PASSWORD "your_db_password"
setx DISCORD_BOT_TOKEN "your_discord_token"
```

#### Mac/Linux
```bash
export DB_PASSWORD=your_db_password
export DISCORD_BOT_TOKEN=your_discord_token
```

#### IntelliJ (Recommended)

Go to:  
Run → Edit Configurations → Environment Variables

Add:
```text
DB_PASSWORD=your_db_password;DISCORD_BOT_TOKEN=your_discord_token
```

### 3. MySQL Setup
Create a database named `discord_bot`:
```sql
CREATE DATABASE discord_bot;
```

### 4. Build the project
```bash
mvn clean install
```

### 5. Run the bot
```bash
mvn spring-boot:run
```

Bot is now active and listens for commands using the `>` prefix.

---

## 📌 Future Features
- Leaderboards  
- Web dashboard  
- Match history  
- New duel modes  

---

## ⭐ Support
If the project helps you, consider giving it a ⭐ on GitHub!
