package com.rest.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.rest.demo.domain.TaskCard;

public interface TaskCardRepository extends JpaRepository<TaskCard, Integer> {

    @Query(value = "SELECT tc FROM TaskCard tc " +
            "JOIN FETCH tc.taskReasonDocument trd " +
            "WHERE ( trd.fullDocumentName = :taskReasonDocument ) " +
            "AND ( tc.taskCardOwner = :executor ) " +
            "AND ( tc.paragraph = :paragraph)")
    List<TaskCard> searchTaskCard(String executor, String taskReasonDocument, String paragraph);

    @Query(value = "SELECT DISTINCT tc.id FROM TaskCard tc " +
            "JOIN tc.taskReasonDocument trd " +
            "JOIN tc.executors tex " +
            "WHERE ( :coexecutor IS null OR :coexecutor = '' OR ( tex.executorName = :coexecutor AND tex.executorState = 'Coexecutor' ) ) " +
            "AND ( :taskReasonDocument IS null OR :taskReasonDocument = '' OR trd.fullDocumentName = :taskReasonDocument ) " +
            "AND ( :executor IS null OR :executor = '' OR tc.taskCardOwner = :executor ) " +
            "AND ( :deadlineDateStart IS null OR tc.deadlineDate >= :deadlineDateStart ) " +
            "AND ( :deadlineDateEnd IS null OR tc.deadlineDate <= :deadlineDateEnd ) " +
            "AND ( :paragraph IS null OR :paragraph = '' OR LOWER(tc.paragraph) LIKE LOWER(CONCAT('%', :paragraph,'%'))) " +
            "AND ( :taskCompleted IS null OR tc.taskCompleted = :taskCompleted )",
            countQuery = "SELECT COUNT(DISTINCT tc.id) FROM TaskCard tc")
    Page<Integer> searchTaskCardsIds(String executor, String coexecutor, LocalDate deadlineDateStart, LocalDate deadlineDateEnd,
                                     String taskReasonDocument, String paragraph, Boolean taskCompleted, Pageable pageable);

    @Query(value = "SELECT DISTINCT tc FROM TaskCard tc " +
            "JOIN FETCH tc.taskReasonDocument trd " +
            "JOIN FETCH tc.executors tex " +
            "WHERE tc.id in :ids " +
            "ORDER BY tc.deadlineDate",
            countQuery = "SELECT COUNT(DISTINCT tc.id) FROM TaskCard tc")
    List<TaskCard> searchTaskCardsByIds(Integer[] ids);

    @Query(value = "SELECT DISTINCT tc.id FROM TaskCard tc " +
            "JOIN tc.taskReasonDocument trd " +
            "JOIN tc.executors tex " +
            "WHERE ( tex.executorName = :executor AND tex.executorState = 'Coexecutor' ) " +
            "OR ( tc.taskCardOwner = :executor ) ",
            countQuery = "SELECT COUNT(tc) FROM TaskCard tc")
    Page<Integer> searchTaskCardsIdsForExecutors(String executor, Pageable pageable);

    @Query(value = "SELECT DISTINCT tc FROM TaskCard tc " +
            "JOIN FETCH tc.taskReasonDocument trd " +
            "JOIN FETCH tc.executors tex " +
            "WHERE tc.id in :ids " +
            "ORDER BY tc.deadlineDate",
            countQuery = "SELECT COUNT(tc) FROM TaskCard tc")
    List<TaskCard> searchTaskCardByIdsForExecutors(Integer[] ids);

    @Query(value = "SELECT tc FROM TaskCard tc LEFT JOIN FETCH tc.taskReasonDocument INNER JOIN FETCH tc.executors WHERE tc.id = :id")
    Optional<TaskCard> findByIdFetch(int id);

}
