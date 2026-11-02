package com.kd.interviewcoach;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5174")
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
        return gemini.ask(
                "Ask ONE fresher-level Java interview question about "
                        + topic + ". Reply with only the question.", false);
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