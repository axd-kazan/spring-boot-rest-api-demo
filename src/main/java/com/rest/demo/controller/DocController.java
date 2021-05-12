package com.rest.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.rest.demo.domain.Document;
import com.rest.demo.service.DocumentService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.domain.UserPrincipal;

@RestController
@RequiredArgsConstructor
public class DocController {

    @Autowired
    private final DocumentService documentService;

    @PostMapping("/createDocument")
    public ResponseEntity createDocument(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Document document) {
        ServiceResponse<Document> response = documentService.createDocument(userPrincipal, document);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/editDocument")
    public ResponseEntity changeDocumentAttributes(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody Document document) {
        ServiceResponse<Document> response = documentService.editDocument(userPrincipal, document);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/deleteDocument")
    public ResponseEntity deleteDocument(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody int docId) {
        ServiceResponse<Document> response = documentService.deleteDocument(userPrincipal, docId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/findDocument")
    public ResponseEntity findDocumentByAttributes(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam(required = false) String documentName,
                                                   @RequestParam(required = false) String documentNumber, @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate documentDate,
                                                   @RequestParam(required = false) String fullDocumentName, @RequestParam int page, @RequestParam int size) {
        ServiceResponse<Page<Document>> response = documentService.findDocument(userPrincipal, documentName, documentNumber, documentDate, fullDocumentName, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
}
