package com.rest.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import com.rest.demo.domain.*;
import com.rest.demo.service.ExecService;
import com.rest.demo.service.UserService;
import com.rest.demo.service.TaskCardService;
import com.rest.demo.service.DocumentService;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ExtendWith(InitialSetup.class)
class DocsTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService authUserService;
    @Autowired
    DocumentService documentService;
    @Autowired
    TaskCardService taskCardService;
    @Autowired
    ExecService execService;

    UserPrincipal adminPrincipal = new UserPrincipal(new User(null, "admin name", "admin", "adminpass", "ROLE_USER,ROLE_ADMIN", "NONE"));

    @Test
    void test_CreateDoc() throws Exception {

        Document newDoc = new Document("NewDocName", "New10000", LocalDate.now());

        log.info("TEST_1_1 CREATE NEW DOCUMENT");

        mockMvc.perform(MockMvcRequestBuilders.post("/createDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.documentName", is(newDoc.getDocumentName())))
                .andExpect(jsonPath("$.data.documentNumber", is(newDoc.getDocumentNumber())))
                .andExpect(jsonPath("$.data.documentDate", is(newDoc.getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.fullDocumentName", is(newDoc.getFullDocumentName())));

        log.info("TEST_1_2 ADD NEW DOCUMENT WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/createDocument")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_3 ADD DOCUMENT THAT ALREADY EXIST");

        mockMvc.perform(MockMvcRequestBuilders.post("/createDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_4 ADD DOCUMENT WITH EMPTY FIELDS");

        List<Document> emptyDocs = Arrays.asList(
                new Document("", "00001", LocalDate.now()),
                new Document("DocName02", "", LocalDate.now()),
                new Document("DocName03", "00003", null));

        for (Document doc : emptyDocs) {
            mockMvc.perform(post("/createDocument")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(doc)))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        newDoc = documentService.findDocument(adminPrincipal, newDoc.getDocumentName(), newDoc.getDocumentNumber(), newDoc.getDocumentDate(),
                null, 0, 100).getData().iterator().next();
        documentService.deleteDocument(adminPrincipal, newDoc.getId());

    }

    @Test
    void test_EditDoc() throws Exception {

        Document editDoc = documentService.createDocument(
                adminPrincipal,
                new Document("NewEditDocName", "NewEdit10000", LocalDate.now())).getData();

        editDoc.setDocumentName("EditDocName");
        editDoc.setDocumentNumber("Edit10000");
        editDoc.setDocumentDate(LocalDate.now().plus(1, ChronoUnit.MONTHS));

        log.info("TEST_2_1 EDIT DOCUMENT WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/editDocument")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_2_2 EDIT DOCUMENT");

        mockMvc.perform(MockMvcRequestBuilders.post("/editDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.documentName", is(editDoc.getDocumentName())))
                .andExpect(jsonPath("$.data.documentNumber", is(editDoc.getDocumentNumber())))
                .andExpect(jsonPath("$.data.documentDate", is(editDoc.getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.fullDocumentName", is(editDoc.getFullDocumentName())));

        log.info("TEST_2_3 EDIT DOCUMENT WITH EMPTY FIELDS");

        List<Document> emptyDocs = Arrays.asList(
                new Document("", "00001", LocalDate.now()),
                new Document("DocName02", "", LocalDate.now()),
                new Document("DocName03", "00003", null));

        for (Document doc : emptyDocs) {
            mockMvc.perform(MockMvcRequestBuilders.post("/editDocument")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(doc)))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        log.info("TEST_2_4 EDIT DOCUMENT WITH NUMBER AND CREATION YEAR THAT ALREADY EXISTS");

        mockMvc.perform(MockMvcRequestBuilders.post("/editDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_2_5 EDIT DOCUMENT THAT DOESN'T EXIST");

        Document notExistDoc = new Document(123, "NewEditDocName", "NewEdit10000", LocalDate.now(), null, null);

        mockMvc.perform(MockMvcRequestBuilders.post("/editDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notExistDoc)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        documentService.deleteDocument(adminPrincipal, editDoc.getId());

    }

    @Test
    void test_DeleteDoc() throws Exception {

        Document delDoc = documentService.createDocument(
                adminPrincipal,
                new Document("DelDocName", "Del0001", LocalDate.now().plus(50, ChronoUnit.DAYS))).getData();

        Document linkedDelDoc = documentService.createDocument(
                adminPrincipal,
                new Document("LinkedDelDocName", "LinkedDelDoc0001", LocalDate.now().plus(60, ChronoUnit.DAYS))).getData();

        Executor exec = execService.addExecutor(adminPrincipal, new Executor("DelDocNameUnit1", "Executor")).getData();
        Executor coexec = execService.addExecutor(adminPrincipal, new Executor("DelDocNameUnit2", "Coexecutor")).getData();
        HashSet<Executor> execSet = new HashSet<>(Arrays.asList(exec, coexec));
        TaskCard taskCard = taskCardService.addTaskCard(adminPrincipal, new TaskCard(execSet, LocalDate.now().plus(10, ChronoUnit.DAYS), linkedDelDoc, "DelDocPara", "DelDocContent")).getData().iterator().next();

        log.info("TEST_3_1 DELETE DOCUMENT WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteDocument")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delDoc.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_2 DELETE DOCUMENT");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delDoc.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")));

        log.info("TEST_3_3 DELETE DOCUMENT WITH LINKED TASK CARD");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkedDelDoc.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_4 DELETE DOCUMENT THAT DOESN'T EXIST");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteDocument")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delDoc.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        taskCardService.deleteTaskCard(adminPrincipal, taskCard.getId());
        documentService.deleteDocument(adminPrincipal, linkedDelDoc.getId());
        execService.deleteExecutor(adminPrincipal, exec.getId());
        execService.deleteExecutor(adminPrincipal, coexec.getId());

    }

    @Test
    void test_FindDoc() throws Exception {
        // Add doc's to find
        List<Document> findDocList = new ArrayList<>(100);
        for (int i = 0; i < 4; i++) {
            findDocList.add(documentService.createDocument(
                    adminPrincipal,
                    new Document("FindDocName" + String.valueOf(i), "Find" + String.valueOf(i), LocalDate.now().plus(i, ChronoUnit.DAYS))).getData());
        }

        log.info("TEST_4_1 FIND ALL DOCUMENTS");

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(4))));

        log.info("TEST_4_2 FIND DOCUMENTS");

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("documentName", findDocList.get(findDocList.size() - 1).getDocumentName().toLowerCase())
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content[0].documentName", is(findDocList.get(findDocList.size() - 1).getDocumentName())))
                .andExpect(jsonPath("$.data.content[0].documentNumber", is(findDocList.get(findDocList.size() - 1).getDocumentNumber())))
                .andExpect(jsonPath("$.data.content[0].documentDate", is(findDocList.get(findDocList.size() - 1).getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.content[0].fullDocumentName", is(findDocList.get(findDocList.size() - 1).getFullDocumentName())));

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("documentNumber", findDocList.get(findDocList.size() - 2).getDocumentNumber().toLowerCase())
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content[0].documentName", is(findDocList.get(findDocList.size() - 2).getDocumentName())))
                .andExpect(jsonPath("$.data.content[0].documentNumber", is(findDocList.get(findDocList.size() - 2).getDocumentNumber())))
                .andExpect(jsonPath("$.data.content[0].documentDate", is(findDocList.get(findDocList.size() - 2).getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.content[0].fullDocumentName", is(findDocList.get(findDocList.size() - 2).getFullDocumentName())));

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("documentDate", findDocList.get(findDocList.size() - 3).getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content[0].documentName", is(findDocList.get(findDocList.size() - 3).getDocumentName())))
                .andExpect(jsonPath("$.data.content[0].documentNumber", is(findDocList.get(findDocList.size() - 3).getDocumentNumber())))
                .andExpect(jsonPath("$.data.content[0].documentDate", is(findDocList.get(findDocList.size() - 3).getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.content[0].fullDocumentName", is(findDocList.get(findDocList.size() - 3).getFullDocumentName())));

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("fullDocumentName", findDocList.get(findDocList.size() - 4).getFullDocumentName().toLowerCase())
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content[0].documentName", is(findDocList.get(findDocList.size() - 4).getDocumentName())))
                .andExpect(jsonPath("$.data.content[0].documentNumber", is(findDocList.get(findDocList.size() - 4).getDocumentNumber())))
                .andExpect(jsonPath("$.data.content[0].documentDate", is(findDocList.get(findDocList.size() - 4).getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.content[0].fullDocumentName", is(findDocList.get(findDocList.size() - 4).getFullDocumentName())));

        log.info("TEST_4_3 FIND DOCUMENT THAT DOESN'T EXISTS");

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("documentName", "DocThatDoesntExists")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_4_4 FIND DOCUMENT WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/findDocument")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.ALL)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        for (Document doc : findDocList)
            documentService.deleteDocument(adminPrincipal, doc.getId());

    }

}
