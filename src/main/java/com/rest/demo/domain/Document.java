package com.rest.demo.domain;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"documentNumber", "documentDate"})})
public class Document {

    public Document(String documentName, String documentNumber, LocalDate documentDate) {
        if (documentName != null && documentNumber != null && documentDate != null) {
            this.documentName = documentName;
            this.documentNumber = documentNumber;
            this.documentDate = documentDate;
            buildFullDocName();
        }
    }

    void buildFullDocName() {
        if (this.documentName != null && documentNumber != null && documentDate != null)
            this.fullDocumentName = this.documentName + " " + this.documentNumber + " " + this.documentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public void setDocumentName(@NotEmpty String documentName) {
        if (documentName != null)
            this.documentName = documentName;
        buildFullDocName();
    }

    public void setDocumentNumber(@NotEmpty String documentNumber) {
        if (documentNumber != null)
            this.documentNumber = documentNumber;
        buildFullDocName();
    }

    public void setDocumentDate(@NotEmpty LocalDate documentDate) {
        if (documentDate != null)
            this.documentDate = documentDate;
        buildFullDocName();
    }

    public Set<TaskCard> getTaskCards() {
        return new HashSet<>(taskCards);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic
    @NotEmpty
    @Setter(AccessLevel.NONE)
    private String documentName;

    @Basic
    @NotEmpty
    @Setter(AccessLevel.NONE)
    private String documentNumber;

    @Basic
    @Setter(AccessLevel.NONE)
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Column(columnDefinition = "DATE")
    private LocalDate documentDate;

    // Full document name contain name + number + date
    @Basic
    @NotEmpty
    @Setter(AccessLevel.NONE)
    private String fullDocumentName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "taskReasonDocument")
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private Set<TaskCard> taskCards;

}
