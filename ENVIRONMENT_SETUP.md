# Environment Setup

## Prerequisites
1. Copy `.env.example` to `.env`
2. Update the values in `.env` with your actual configuration

## Environment Variables

Create a `.env` file in the project root with the following variables:

```bash
# Application Configuration
SPRING_APPLICATION_NAME=finalPorject
SERVER_PORT=8085

# Firebase Configuration
FIREBASE_CREDENTIALS_PATH=path/to/your/firebase-credentials.json
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com

# Logging Configuration
LOGGING_LEVEL_COM_GOOGLE_FIREBASE=DEBUG
LOGGING_LEVEL_ORG_SCITECHDEV_FINALPROJECT=DEBUG

# Spring Boot Configuration
SPRING_AUTOCONFIGURE_EXCLUDE=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

## Setup Instructions

1. Clone the repository
2. Copy `.env.example` to `.env`: `cp .env.example .env`
3. Update the `.env` file with your Firebase credentials and other settings
4. Run the application: `mvn spring-boot:run`

## Security Notes

- Never commit `.env` files to version control
- The `.env.example` file shows the required structure without sensitive data
- Firebase credentials should be kept secure and not shared publicly
