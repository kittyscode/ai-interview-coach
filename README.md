# AI Interview Coach

Practise Java interview questions and get an AI score with feedback.

## Features
- Generates interview questions by topic
- Scores your answer and suggests a better one
- Saves every attempt so you can track progress

## Tech
- Backend: Spring Boot 4, Spring Data JPA, PostgreSQL, Gemini API
- Frontend: React (Vite)

## Run locally
1. Create a PostgreSQL database named `interview_coach`
2. Backend: set `GEMINI_API_KEY` and `DB_PASSWORD`, then run
   `InterviewCoachApplication` from `/backend` (port 8081)
3. Frontend: in `/frontend` run `npm install` and `npm run dev`

## API
- `GET /question?topic=Spring Boot`
- `POST /evaluate`
- `GET /history`