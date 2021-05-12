package com.rest.demo;

import com.rest.demo.domain.*;
import com.rest.demo.service.DocumentService;
import com.rest.demo.service.ExecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import com.rest.demo.service.TaskCardService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ExtendWith(InitialSetup.class)
public class ExecutorTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TaskCardService taskCardService;
    @Autowired
    ExecService execService;
    @Autowired
    DocumentService documentService;

    UserPrincipal adminPrincipal = new UserPrincipal(new User(null, "admin name", "admin", "adminpass", "ROLE_USER,ROLE_ADMIN", "NONE"));

    @Test
    void test_CreateExec() throws Exception {

        Executor newExec = new Executor("NewExecUnit", "Executor");

        log.info("TEST_1_1 ADD NEW EXECUTOR WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/addExecutor")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newExec)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_2 ADD NEW EXECUTOR");

        mockMvc.perform(MockMvcRequestBuilders.post("/addExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newExec)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.executorName", is(newExec.getExecutorName())))
                .andExpect(jsonPath("$.data.executorState", is(newExec.getExecutorState())));

        log.info("TEST_1_3 ADD EXECUTOR THAT ALREADY EXIST");

        mockMvc.perform(MockMvcRequestBuilders.post("/addExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newExec)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_4 ADD EXECUTOR WITH EMPTY FIELDS");

        Executor emptyExec = new Executor("", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/addExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyExec)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        List<Executor> execList = execService.getExecutors(adminPrincipal).getData();
        execService.deleteExecutor(adminPrincipal, execList.iterator().next().getId());

    }

    @Test
    void test_GetLisOfExecs() throws Exception {

        for (int i = 0; i < 10; i++) {
            execService.addExecutor(adminPrincipal, new Executor("NewUnitName" + String.valueOf(i), "Executor"));
            execService.addExecutor(adminPrincipal, new Executor("NewUnitName" + String.valueOf(i), "Coexecutor"));
        }

        log.info("TEST_2_1 GET LIST OF EXECUTORS WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/getExecutors")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        mockMvc.perform(MockMvcRequestBuilders.get("/getCoexecutors")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_2_2 GET LIST OF EXECUTORS");

        mockMvc.perform(MockMvcRequestBuilders.get("/getExecutors")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.length()", is(10)));

        mockMvc.perform(MockMvcRequestBuilders.get("/getCoexecutors")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.length()", is(10)));

        List<Executor> execList = execService.getExecutors(adminPrincipal).getData();
        List<Executor> coexecList = execService.getCoexecutors(adminPrincipal).getData();

        for (int i = 0; i < 10; i++) {
            execService.deleteExecutor(adminPrincipal, execList.get(i).getId());
            execService.deleteExecutor(adminPrincipal, coexecList.get(i).getId());
        }

    }

    @Test
    void test_DeleteExec() throws Exception {

        Executor delExec = execService.addExecutor(adminPrincipal, new Executor("DelUnitName", "Executor")).getData();

        Document linkedDelDoc = documentService.createDocument(
                adminPrincipal,
                new Document("LinkedDelDocName", "LinkedDelDoc0001", LocalDate.now().plus(60, ChronoUnit.DAYS))).getData();

        Executor linkedExec = execService.addExecutor(adminPrincipal, new Executor("LinkedDelDocUnitName", "Executor")).getData();
        HashSet<Executor> execSet = new HashSet<>(Arrays.asList(linkedExec));
        TaskCard taskCard = taskCardService.addTaskCard(adminPrincipal, new TaskCard(execSet, LocalDate.now().plus(10, ChronoUnit.DAYS), linkedDelDoc, "DelDocPara", "DelDocContent")).getData().iterator().next();

        log.info("TEST_3_1 DELETE EXECUTOR WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteExecutor")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delExec.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_2 DELETE EXECUTOR");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delExec.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")));

        log.info("TEST_3_3 DELETE EXECUTOR WITH LINKED TASK CARD");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(linkedExec.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_4 DELETE EXECUTOR THAT DOESN'T EXIST");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteExecutor")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delExec.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        taskCardService.deleteTaskCard(adminPrincipal, taskCard.getId());
        documentService.deleteDocument(adminPrincipal, linkedDelDoc.getId());
        execService.deleteExecutor(adminPrincipal, linkedExec.getId());

    }

}
