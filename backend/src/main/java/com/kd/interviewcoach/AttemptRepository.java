package com.kd.interviewcoach;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findAllByOrderByCreatedAtDesc();
}
