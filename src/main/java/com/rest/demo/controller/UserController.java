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

import com.rest.demo.domain.UserPrincipal;
import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.domain.User;
import com.rest.demo.service.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @GetMapping("/getCurrentUser")
    public ResponseEntity getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ServiceResponse<User> response = userService.getCurrentUser(userPrincipal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/createUser")
    public ResponseEntity createUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody User userToCreate) {
        ServiceResponse<User> response = userService.createUser(userPrincipal, userToCreate);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/editUser")
    public ResponseEntity editUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody User editedUser) {
        ServiceResponse<User> response = userService.editUser(userPrincipal, editedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/deleteUser")
    public ResponseEntity deleteUser(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody int userId) {
        ServiceResponse<User> response = userService.deleteUser(userPrincipal, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getUsersList")
    public ResponseEntity getUsersList(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        ServiceResponse<List<User>> response = userService.getUsersList(userPrincipal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
