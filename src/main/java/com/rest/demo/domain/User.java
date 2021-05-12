package com.rest.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    @NotBlank
    private String name;

    @NonNull
    @NotBlank
    @Column(unique = true)
    private String username;

    @NonNull
    @NotBlank
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String password;

    @NonNull
    @NotBlank
    private String authorities;

    @NonNull
    @NotBlank
    private String unit;

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

}
