package com.kd.interviewcoach;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class InterviewController {

    private final GeminiClient gemini;
    private final AttemptRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public InterviewController(GeminiClient gemini, AttemptRepository repo) {
        this.gemini = gemini;
        this.repo = repo;
    }

    @GetMapping("/question")
    public String question(@RequestParam String topic , @RequestParam(defaultValue = "medium") String difficulty) {

        String level= switch(difficulty.toLowerCase()){
            case "easy" -> "a very simple 'What is...' or 'What does...' question about ONE concept, with no comparisons, no performance trade-offs and no scenarios, that a beginner can answer in one or two sentences";
            case "hard" -> "a scenario-based question that needs a design decision or debugging reasoning";
            default ->  "a question that needs short explanation with one example";
        };


        String recent = repo.findAllByOrderByCreatedAtDesc().stream()
                .limit(10)
                .map(Attempt::getQuestion)
                .collect(Collectors.joining("\n- ", "- ", ""));

        String prompt = """
            Ask ONE fresher-level Java interview question about %s.
            Pick a random sub-topic within it. Do NOT repeat or closely
            resemble any of these earlier questions:
            %s
            Reply with only the question.
            """.formatted(topic, level, recent);

        return gemini.ask(prompt, false, 1.0);
    }

    @PostMapping(value = "/evaluate",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String evaluate(@RequestBody EvalRequest req) {
        String prompt = """
            You are a strict but kind interviewer. Evaluate the answer.
            Reply ONLY in JSON with keys: score (0-10),
            strengths (array), improvements (array), betterAnswer (string).

            QUESTION: %s
            ANSWER: %s
            """.formatted(req.question(), req.answer());

        String feedback = extractJson(gemini.ask(prompt, true));

        int score;
        try {
            score = mapper.readTree(feedback).path("score").asInt();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI returned an invalid format. Please try again.");
        }

        repo.save(new Attempt(req.question(), req.answer(), score, feedback));
        return feedback;
    }
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    @GetMapping("/history")
    public List<Attempt> history() {
        return repo.findAllByOrderByCreatedAtDesc();
    }
}