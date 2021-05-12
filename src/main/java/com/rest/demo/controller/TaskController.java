package com.rest.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.domain.TaskCard;
import com.rest.demo.domain.UserPrincipal;
import com.rest.demo.service.TaskCardService;

@RestController
@RequiredArgsConstructor
public class TaskController {

    @Autowired
    private final TaskCardService taskCardService;

    @PostMapping("/addTaskCard")
    public ResponseEntity addTaskCard(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody TaskCard taskCard) {
        ServiceResponse<List<TaskCard>> response = taskCardService.addTaskCard(userPrincipal, taskCard);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/editTaskCard")
    public ResponseEntity editTaskCard(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody TaskCard taskCard) {
        ServiceResponse<TaskCard> response = taskCardService.editTaskCard(userPrincipal, taskCard);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findTaskCards")
    public ResponseEntity findTaskCards(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(required = false) String executor, @RequestParam(required = false) String coexecutor,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate deadlineDateStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate deadlineDateEnd,
                                        @RequestParam(required = false) String taskReasonDocument, @RequestParam(required = false) Boolean taskCompleted,
                                        @RequestParam int page, @RequestParam int size) {
        ServiceResponse<Page<TaskCard>> response = taskCardService.findTaskCards(userPrincipal, executor, coexecutor, deadlineDateStart, deadlineDateEnd, taskReasonDocument, taskCompleted, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/deleteTaskCard")
    public ResponseEntity deleteTaskCard(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam int taskCardId) {
        ServiceResponse<TaskCard> response = taskCardService.deleteTaskCard(userPrincipal, taskCardId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
