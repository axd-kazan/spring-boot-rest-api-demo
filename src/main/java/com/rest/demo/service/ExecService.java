package com.rest.demo.service;

import com.rest.demo.domain.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rest.demo.domain.Executor;
import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.repository.ExecRepository;

import java.util.List;
import java.util.Optional;

import static org.hibernate.internal.util.StringHelper.isBlank;

@Service
@RequiredArgsConstructor
public class ExecService {

    private final ExecRepository execRepository;

    @Transactional
    public ServiceResponse<Executor> addExecutor(UserPrincipal userPrincipal, Executor executor) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            if (!isBlank(executor.getExecutorName()) && !isBlank(executor.getExecutorState())) {

                Optional<Executor> targetExec = execRepository.findByExecutorNameAndExecutorState(executor.getExecutorName(), executor.getExecutorState());

                if (!targetExec.isPresent()) {
                    Executor exec = execRepository.save(executor);

                    return new ServiceResponse<>("success", "Executor added.", exec);
                }
                return new ServiceResponse<>("error", "Executor already existed!", null);
            }
            return new ServiceResponse<>("error", "Executor has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<List<Executor>> getExecutors(UserPrincipal userPrincipal) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            List<Executor> listExecutors = execRepository.findByExecutorState("Executor");

            return new ServiceResponse<>("success", "Executors list.", listExecutors);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<List<Executor>> getCoexecutors(UserPrincipal userPrincipal) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            List<Executor> listCoexecutors = execRepository.findByExecutorState("Coexecutor");

            return new ServiceResponse<>("success", "Coexecutors list.", listCoexecutors);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<Executor> deleteExecutor(UserPrincipal userPrincipal, int execId) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            Optional<Executor> findedExec = execRepository.findById(execId);
            if (findedExec.isPresent()) {
                if (findedExec.get().getTaskCards().isEmpty()) {
                    execRepository.deleteById(execId);
                    return new ServiceResponse<>("success", "Executor deleted.", findedExec.get());
                }
                return new ServiceResponse<>("error", "Executor is linked to the task cards!", null);
            }
            return new ServiceResponse<>("error", "Executor not found!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

}
