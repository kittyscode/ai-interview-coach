package com.kd.interviewcoach;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private int score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Attempt() {}

    public Attempt(String question, String answer, int score, String feedback) {
        this.question = question;
        this.answer = answer;
        this.score = score;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getScore() { return score; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
