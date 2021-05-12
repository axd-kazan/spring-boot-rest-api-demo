package com.rest.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

import com.rest.demo.domain.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {

    @Query("SELECT e FROM Document e WHERE e.documentNumber = :documentNumber AND e.documentDate BETWEEN :startDocumentDate and :endDocumentDate")
    List<Document> findDocument(String documentNumber, LocalDate startDocumentDate, LocalDate endDocumentDate);

    @Query("SELECT e FROM Document e " +
            "WHERE (:documentName IS null OR :documentName = '' OR LOWER(e.documentName) LIKE LOWER(CONCAT('%', :documentName,'%'))) " +
            "AND (:documentNumber IS null OR :documentNumber = ''OR LOWER(e.documentNumber) LIKE LOWER(CONCAT('%', :documentNumber,'%'))) " +
            "AND (:documentDate IS null OR (e.documentDate = :documentDate)) " +
            "AND (:fullDocumentName IS null OR :fullDocumentName = ''OR LOWER(e.fullDocumentName) LIKE LOWER(CONCAT('%', :fullDocumentName,'%')))")
    Page<Document> findDocument(String documentName, String documentNumber, LocalDate documentDate, String fullDocumentName, Pageable pageable);

}
