package com.rest.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Executor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic
    @NonNull
    private String executorName;

    @Basic
    @NonNull
    private String executorState;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "executors")
    @JsonIgnore
    private Set<TaskCard> taskCards;

}