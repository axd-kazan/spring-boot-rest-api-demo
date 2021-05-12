package com.rest.demo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
@Setter
public class TaskCard {

    public TaskCard(Set<Executor> executors, LocalDate deadlineDate, Document taskReasonDocument, String paragraph, String content) {
        this.executors = executors;
        this.deadlineDate = deadlineDate;
        this.taskReasonDocument = taskReasonDocument;
        this.paragraph = paragraph;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Unit which owns task card
    @Basic
    private String taskCardOwner;

    // Units that perform and help to perform task
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Executor> executors;

    // Task deadline date
    @Basic
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Column(columnDefinition = "DATE")
    private LocalDate deadlineDate;

    // Document which is the reason for creating the task
    @ManyToOne(fetch = FetchType.LAZY)
    private Document taskReasonDocument;

    // Paragraph of the document that corresponds to the task
    @Basic
    private String paragraph;

    // Content of the task
    @Basic
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // Current state of the task
    @Basic
    @Column(columnDefinition = "LONGTEXT")
    private String state;

    // Document which is the reason for closing the task (name, number and date)
    @Basic
    private String taskRealizationDocumentName;

    @Basic
    private String taskRealizationDocumentNumber;

    @Basic
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Column(columnDefinition = "DATE")
    private LocalDate taskRealizationDocumentDate;

    // Task completion by executor flag
    @Basic
    private Boolean executorComplete;

    // Task completion by inspector flag
    @Basic
    private Boolean taskCompleted;

}
