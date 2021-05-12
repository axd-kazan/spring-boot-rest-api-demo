package com.rest.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isBlank;

import com.rest.demo.domain.*;
import com.rest.demo.repository.TaskCardRepository;

@Service
@RequiredArgsConstructor
public class TaskCardService {
    
    private final TaskCardRepository taskCardRepository;

    @Transactional
    // Task card could contain multiple executors, in that case service will create separate card for every executor
    public ServiceResponse<List<TaskCard>> addTaskCard(UserPrincipal userPrincipal, TaskCard taskCard) {

        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            if (taskCard.getExecutors() != null) {
                // Filter executors from coexecutors
                List<Executor> executors = taskCard.getExecutors().stream().filter(exc -> (exc.getExecutorState().equals("Executor"))).collect(Collectors.toList());

                // Check that task card doesn't have empty fields
                if (!executors.isEmpty() && taskCard.getDeadlineDate() != null && taskCard.getTaskReasonDocument() != null
                        && !isBlank(taskCard.getParagraph()) && !isBlank(taskCard.getContent())) {

                    List<TaskCard> newTaskCardList = new ArrayList<>();

                    // Search for same task card for every executor
                    for (Executor executor : executors) {
                        List<TaskCard> sameTaskCard = taskCardRepository.searchTaskCard(executor.getExecutorName(), taskCard.getTaskReasonDocument().getFullDocumentName(), taskCard.getParagraph());
                        if (!sameTaskCard.isEmpty())
                            return new ServiceResponse<>("error", "Task card already existed!", null);
                    }

                    // Create task card for every executor
                    for (Executor executor : executors) {
                        TaskCard newTaskCard = taskCard.toBuilder().build();

                        newTaskCard.setExecutors(taskCard.getExecutors());
                        newTaskCard.setTaskReasonDocument(taskCard.getTaskReasonDocument());
                        newTaskCard.setTaskCardOwner(executor.getExecutorName());

                        newTaskCard = taskCardRepository.save(newTaskCard);

                        newTaskCardList.add(newTaskCard);
                    }
                    return new ServiceResponse<>("success", "Task card added.", newTaskCardList);

                }
            }
            return new ServiceResponse<>("error", "Task card has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<TaskCard> editTaskCard(UserPrincipal userPrincipal, TaskCard taskCard) {

        boolean adminAuthorityAllowed = userPrincipal.isHaveAuthority("ROLE_ADMIN");
        boolean userAuthorityAllowed = userPrincipal.isHaveAuthority("ROLE_USER");

        if (adminAuthorityAllowed || userAuthorityAllowed) {

            if (taskCard.getId() != null && taskCard.getId() > 0) {

                Optional<TaskCard> editTaskCard = taskCardRepository.findByIdFetch(taskCard.getId());

                if (editTaskCard.isPresent()) {

                    // Actions allowed for admin
                    if (adminAuthorityAllowed) {

                        // Check that task card doesn't have empty fields
                        if (taskCard.getDeadlineDate() != null && taskCard.getTaskReasonDocument() != null
                                && !isBlank(taskCard.getParagraph()) && !isBlank(taskCard.getContent())) {

                            editTaskCard.get().setDeadlineDate(taskCard.getDeadlineDate());
                            editTaskCard.get().setTaskReasonDocument(taskCard.getTaskReasonDocument());
                            editTaskCard.get().setParagraph(taskCard.getParagraph());
                            editTaskCard.get().setContent(taskCard.getContent());
                            editTaskCard.get().setTaskCompleted(taskCard.getTaskCompleted());

                        } else
                            return new ServiceResponse<>("error", "Task card have empty fields!", null);
                    }

                    // Actions allowed for user
                    if (userAuthorityAllowed) {

                        if (!isBlank(taskCard.getState()) && !isBlank(taskCard.getTaskRealizationDocumentName())
                                && !isBlank(taskCard.getTaskRealizationDocumentNumber()) && taskCard.getTaskRealizationDocumentDate() != null) {

                            if (editTaskCard.get().getTaskCardOwner() == userPrincipal.getUser().getUnit() || adminAuthorityAllowed) {

                                editTaskCard.get().setTaskRealizationDocumentName(taskCard.getTaskRealizationDocumentName());
                                editTaskCard.get().setTaskRealizationDocumentNumber(taskCard.getTaskRealizationDocumentNumber());
                                editTaskCard.get().setTaskRealizationDocumentDate(taskCard.getTaskRealizationDocumentDate());
                                editTaskCard.get().setState(taskCard.getState());
                                editTaskCard.get().setExecutorComplete(true);

                            } else
                                return new ServiceResponse<>("error", userPrincipal.getUser().getUnit() +
                                        " is not task card owner " + editTaskCard.get().getTaskCardOwner() + " !", null);
                        } else
                            return new ServiceResponse<>("error", "Task card have empty fields!", null);
                    }

                    TaskCard editedTaskCard = taskCardRepository.save(editTaskCard.get());
                    return new ServiceResponse<>("success", "Task card changed.", editedTaskCard);
                }
            }
            return new ServiceResponse<>("error", "Task card doesn't exists!", null);

        }
        return new ServiceResponse<>("error", "Wrong user credentials!", null);
    }

    @Transactional
    public ServiceResponse<Page<TaskCard>> findTaskCards(UserPrincipal userPrincipal, String executor, String coexecutor, LocalDate deadlineDateStart, LocalDate deadlineDateEnd,
                                                         String taskReasonDocument, Boolean taskCompleted, int page, int size) {

        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            // Split query in two parts in a purpose of performance
            Page<Integer> foundTaskCardsId = taskCardRepository.searchTaskCardsIds(executor, coexecutor, deadlineDateStart, deadlineDateEnd,
                    taskReasonDocument, "", taskCompleted, PageRequest.of(page, size));
            List<TaskCard> foundTaskCards = taskCardRepository.searchTaskCardsByIds(foundTaskCardsId.toList().toArray(new Integer[0]));
            Page<TaskCard> pageable = new PageImpl<>(foundTaskCards, foundTaskCardsId.getPageable(), foundTaskCardsId.getTotalElements());

            if (!pageable.getContent().isEmpty())
                return new ServiceResponse<>("success", "Task cards list.", pageable);

            return new ServiceResponse<>("error", "Task cards not found!", null);
        }

        if (userPrincipal.isHaveAuthority("ROLE_USER")) {

            Page<Integer> foundTaskCardsId = taskCardRepository.searchTaskCardsIdsForExecutors(userPrincipal.getUser().getUnit(), PageRequest.of(page, size));
            List<TaskCard> foundTaskCards = taskCardRepository.searchTaskCardByIdsForExecutors(foundTaskCardsId.toList().toArray(new Integer[0]));
            Page<TaskCard> pageable = new PageImpl<>(foundTaskCards, foundTaskCardsId.getPageable(), foundTaskCardsId.getTotalElements());

            return new ServiceResponse<>("success", "Task cards list.", pageable);

        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);

    }

    @Transactional
    public ServiceResponse<TaskCard> deleteTaskCard(UserPrincipal userPrincipal, int taskCardId) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            if (taskCardId != 0) {
                Optional<TaskCard> delTaskCard = taskCardRepository.findByIdFetch(taskCardId);

                if (delTaskCard.isPresent()) {
                    taskCardRepository.delete(delTaskCard.get());
                    return new ServiceResponse<>("success", "Task card deleted!", delTaskCard.get());
                }

            }
            return new ServiceResponse<>("error", "Task card not found!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

}
