package com.rest.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

import java.util.List;

import com.rest.demo.domain.Executor;
import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.service.ExecService;
import com.rest.demo.domain.UserPrincipal;

@Controller
@RequiredArgsConstructor
public class ExecController {

    @Autowired
    private final ExecService execService;

    @PostMapping("/addExecutor")
    public ResponseEntity addExecutor(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Executor executor) {
        ServiceResponse<Executor> response = execService.addExecutor(userPrincipal, executor);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/deleteExecutor")
    public ResponseEntity deleteExecutor(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody int execId) {
        ServiceResponse<Executor> response = execService.deleteExecutor(userPrincipal, execId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getExecutors")
    public ResponseEntity getExecutors(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ServiceResponse<List<Executor>> response = execService.getExecutors(userPrincipal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getCoexecutors")
    public ResponseEntity getCoexecutors(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ServiceResponse<List<Executor>> response = execService.getCoexecutors(userPrincipal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}




