package com.kd.interviewcoach;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
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
    public String question(@RequestParam String topic) {
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
            """.formatted(topic, recent);

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

        String feedback = gemini.ask(prompt, true);
        int score = mapper.readTree(feedback).path("score").asInt();
        repo.save(new Attempt(req.question(), req.answer(), score, feedback));
        return feedback;
    }

    @GetMapping("/history")
    public List<Attempt> history() {
        return repo.findAllByOrderByCreatedAtDesc();
    }
}