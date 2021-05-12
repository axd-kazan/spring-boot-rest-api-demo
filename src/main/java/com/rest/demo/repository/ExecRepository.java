package com.rest.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import com.rest.demo.domain.Executor;

public interface ExecRepository extends JpaRepository<Executor, Integer> {

    List<Executor> findByExecutorState(String executorState);

    Optional<Executor> findByExecutorNameAndExecutorState(String executorName, String executorState);

}
