package com.rest.demo;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import com.rest.demo.domain.*;
import com.rest.demo.service.DocumentService;
import com.rest.demo.service.ExecService;
import com.rest.demo.service.TaskCardService;
import com.rest.demo.service.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = "spring.h2.console.enabled=true")
@AutoConfigureMockMvc
@Slf4j
@ExtendWith(InitialSetup.class)
public class TaskCardTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DocumentService documentService;
    @Autowired
    ExecService execService;
    @Autowired
    TaskCardService taskCardService;
    @Autowired
    UserService userService;

    UserPrincipal adminPrincipal = new UserPrincipal(new User(null, "admin name", "admin", "adminpass", "ROLE_USER,ROLE_ADMIN", "NONE"));

    @Test
    void test_CreateTaskCard() throws Exception {
        List<TaskCard> newCards = new ArrayList<>();
        List<Set<Executor>> execSets = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        createTaskCards("CreateTaskCard", newCards, execSets, docs);

        log.info("TEST_1_1 ADD NEW TASK CARD WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/addTaskCard")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCards.get(newCards.size() - 1))))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_2 CREATE NEW TASK CARD");

        for (TaskCard card : newCards) {

            mockMvc.perform(MockMvcRequestBuilders.post("/addTaskCard")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("success")))
                    .andExpect(jsonPath("$.data[0].executors.length()", is(card.getExecutors().size())))
                    .andExpect(jsonPath("$.data[0].taskReasonDocument.documentName", is(card.getTaskReasonDocument().getDocumentName())))
                    .andExpect(jsonPath("$.data[0].taskReasonDocument.documentNumber", is(card.getTaskReasonDocument().getDocumentNumber())))
                    .andExpect(jsonPath("$.data[0].taskReasonDocument.documentDate", is(card.getTaskReasonDocument().getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                    .andExpect(jsonPath("$.data[0].paragraph", is(card.getParagraph())))
                    .andExpect(jsonPath("$.data[0].content", is(card.getContent())));
        }

        log.info("TEST_1_3 ADD TASK CARD THAT ALREADY EXIST");

        mockMvc.perform(MockMvcRequestBuilders.post("/addTaskCard")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCards.get(newCards.size() - 1))))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_1_4 ADD TASK CARD WITH EMPTY FIELDS");

        List<TaskCard> emptyTaskCards = Arrays.asList(
                new TaskCard(null, LocalDate.now(), docs.get(execSets.size() - 1), "EmptyTestPara", "EmptyTestContent"),
                new TaskCard(execSets.get(execSets.size() - 1), null, docs.get(execSets.size() - 1), "EmptyTestPara", "EmptyTestContent"),
                new TaskCard(execSets.get(execSets.size() - 1), LocalDate.now(), null, "EmptyTestPara", "EmptyTestContent"),
                new TaskCard(execSets.get(execSets.size() - 1), LocalDate.now(), docs.get(execSets.size() - 1), null, "EmptyTestContent"),
                new TaskCard(execSets.get(execSets.size() - 1), LocalDate.now(), docs.get(execSets.size() - 1), "EmptyTestPara", null));

        for (TaskCard card : emptyTaskCards) {
            mockMvc.perform(MockMvcRequestBuilders.post("/addTaskCard")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        cleanTestData();
    }

    @Test
    void test_FindTaskCard() throws Exception {
        List<TaskCard> findCards = new ArrayList<>();
        List<Set<Executor>> execSets = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        createTaskCards("FindTaskCard", findCards, execSets, docs);

        for (TaskCard card : findCards)
            taskCardService.addTaskCard(adminPrincipal, card);

        List<TaskCard> taskCards = taskCardService.findTaskCards(adminPrincipal, null, null, null, null, null, null, 0, 100).getData().getContent();
        TaskCard findCard = taskCards.get(taskCards.size() - 1);

        log.info("TEST_2_1 FIND TASK CARD (ADMIN)");

        mockMvc.perform(MockMvcRequestBuilders.get("/findTaskCards")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(30))));

        mockMvc.perform(MockMvcRequestBuilders.get("/findTaskCards")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("executor", findCard.getTaskCardOwner())
                .param("deadlineDateStart", findCard.getDeadlineDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .param("deadlineDateEnd", findCard.getDeadlineDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .param("taskReasonDocument", findCard.getTaskReasonDocument().getFullDocumentName())
                .param("taskCompleted", (findCard.getTaskCompleted() == null) ? "" : String.valueOf(findCard.getTaskCompleted()))
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].id", is(findCard.getId())))
                .andExpect(jsonPath("$.data.content[0].taskCardOwner", is(findCard.getTaskCardOwner())))
                .andExpect(jsonPath("$.data.content[0].executors.length()", is(findCard.getExecutors().size())))
                .andExpect(jsonPath("$.data.content[0].deadlineDate", is(findCard.getDeadlineDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.content[0].taskReasonDocument.fullDocumentName", is(findCard.getTaskReasonDocument().getFullDocumentName())))
                .andExpect(jsonPath("$.data.content[0].taskCompleted", is(findCard.getTaskCompleted())));

        log.info("TEST_2_2 FIND TASK CARD (USER)");

        User user = new User("find user name", "finduser", "finduserpass", "ROLE_USER", findCard.getExecutors().iterator().next().getExecutorName());
        userService.createUser(adminPrincipal, user);

        mockMvc.perform(MockMvcRequestBuilders.get("/findTaskCards")
                .with(httpBasic("finduser", "finduserpass"))
                .accept(MediaType.ALL)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")));

        log.info("TEST_2_3 FIND NON EXISTENT TASK CARD");

        mockMvc.perform(MockMvcRequestBuilders.get("/findTaskCards")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("executor", "NonExistExec")
                .param("taskReasonDocument", "NonExistDoc")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        userService.deleteUser(adminPrincipal, user.getId());

        cleanTestData();
    }

    @Test
    void test_EditTaskCard() throws Exception {
        List<TaskCard> editCards = new ArrayList<>();
        List<Set<Executor>> execSets = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        createTaskCards("EditTaskCard", editCards, execSets, docs);

        for (TaskCard card : editCards)
            taskCardService.addTaskCard(adminPrincipal, card);

        editCards = taskCardService.findTaskCards(adminPrincipal, null, null, null, null, null, null, 0, 100).getData().getContent();
        TaskCard editCardUser = editCards.get(editCards.size() - 1);

        Document editCardUserDoc = editCardUser.getTaskReasonDocument();
        editCardUserDoc.setDocumentName("EditTestTaskCardDocName");
        editCardUserDoc.setDocumentNumber("EditTestTaskCardNumber");
        editCardUserDoc.setDocumentDate(LocalDate.now().plus(200, ChronoUnit.DAYS));

        Set<Executor> editCardUserExec = editCardUser.getExecutors();
        editCardUserExec.add(new Executor("EditTestTaskCardUnit", "Executor"));

        User user = new User("test user name", "testuser", "testuserpass", "ROLE_USER", editCardUser.getTaskCardOwner());
        userService.createUser(adminPrincipal, user);

        log.info("TEST_3_1 EDIT TASK CARD (USER)");

        editCardUser.setExecutors(editCardUserExec);
        editCardUser.setTaskReasonDocument(editCardUserDoc);
        editCardUser.setDeadlineDate(LocalDate.now().plus(200, ChronoUnit.DAYS));
        editCardUser.setParagraph("EditTestPara");
        editCardUser.setContent("EditTestContent");
        editCardUser.setState("EditTestState");
        editCardUser.setTaskRealizationDocumentName("EditTestTaskReasonName");
        editCardUser.setTaskRealizationDocumentNumber("EditTestTaskReasonNum");
        editCardUser.setTaskRealizationDocumentDate(LocalDate.now().plus(200, ChronoUnit.DAYS));
        editCardUser.setExecutorComplete(true);
        editCardUser.setTaskCompleted(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                .with(httpBasic("testuser", "testuserpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editCardUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.executors.length()", not(editCardUser.getExecutors().size())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentName", not(editCardUser.getTaskReasonDocument().getDocumentName())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentNumber", not(editCardUser.getTaskReasonDocument().getDocumentNumber())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentDate", not(editCardUser.getTaskReasonDocument().getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.paragraph", not(editCardUser.getParagraph())))
                .andExpect(jsonPath("$.data.content", not(editCardUser.getContent())))
                .andExpect(jsonPath("$.data.taskCompleted", not(editCardUser.getTaskCompleted())))
                .andExpect(jsonPath("$.data.state", is(editCardUser.getState())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentName", is(editCardUser.getTaskRealizationDocumentName())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentNumber", is(editCardUser.getTaskRealizationDocumentNumber())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentDate", is(editCardUser.getTaskRealizationDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.executorComplete", is(editCardUser.getExecutorComplete())));

        log.info("TEST_3_2 EDIT TASK CARD (ADMIN)");

        TaskCard editCardAdmin = editCards.get(editCards.size() - 2);

        Document editCardAdminDoc = editCardUser.getTaskReasonDocument();
        editCardUserDoc.setDocumentName("EditTestTaskCardDocName");
        editCardUserDoc.setDocumentNumber("EditTestTaskCardNumber");
        editCardUserDoc.setDocumentDate(LocalDate.now().plus(200, ChronoUnit.DAYS));

        Set<Executor> editCardAdminExec = editCardUser.getExecutors();
        editCardUserExec.add(new Executor("EditTestTaskCardUnit", "Executor"));

        editCardAdmin.setExecutors(editCardAdminExec);
        editCardAdmin.setTaskReasonDocument(editCardAdminDoc);
        editCardAdmin.setDeadlineDate(LocalDate.now().plus(200, ChronoUnit.DAYS));
        editCardAdmin.setParagraph("EditTestPara");
        editCardAdmin.setContent("EditTestContent");
        editCardAdmin.setState("EditTestState");
        editCardAdmin.setTaskRealizationDocumentName("EditTestTaskReasonName");
        editCardAdmin.setTaskRealizationDocumentNumber("EditTestTaskReasonNum");
        editCardAdmin.setTaskRealizationDocumentDate(LocalDate.now().plus(200, ChronoUnit.DAYS));
        editCardAdmin.setExecutorComplete(true);
        editCardAdmin.setTaskCompleted(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editCardAdmin)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.executors.length()", not(editCardAdmin.getExecutors().size())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentName", is(editCardAdmin.getTaskReasonDocument().getDocumentName())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentNumber", is(editCardAdmin.getTaskReasonDocument().getDocumentNumber())))
                .andExpect(jsonPath("$.data.taskReasonDocument.documentDate", is(editCardAdmin.getTaskReasonDocument().getDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.paragraph", is(editCardAdmin.getParagraph())))
                .andExpect(jsonPath("$.data.content", is(editCardAdmin.getContent())))
                .andExpect(jsonPath("$.data.taskCompleted", is(editCardAdmin.getTaskCompleted())))
                .andExpect(jsonPath("$.data.state", is(editCardAdmin.getState())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentName", is(editCardAdmin.getTaskRealizationDocumentName())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentNumber", is(editCardAdmin.getTaskRealizationDocumentNumber())))
                .andExpect(jsonPath("$.data.taskRealizationDocumentDate", is(editCardAdmin.getTaskRealizationDocumentDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))))
                .andExpect(jsonPath("$.data.executorComplete", is(editCardAdmin.getExecutorComplete())));

        log.info("TEST_3_3 EDIT NON EXISTENT TASK CARD");

        TaskCard notExistCard = new TaskCard(editCardAdminExec, LocalDate.now().plus(200, ChronoUnit.DAYS), editCardAdminDoc, "NotExistPara", "NotExistContent");

        mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notExistCard)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_4 EDIT TASK CARD WITH EMPTY FIELDS (ADMIN)");

        List<TaskCard> emptyTaskCards = Arrays.asList(
                editCardUser.toBuilder().deadlineDate(null).build(),
                editCardUser.toBuilder().taskReasonDocument(null).build(),
                editCardUser.toBuilder().paragraph(null).build(),
                editCardUser.toBuilder().content(null).build(),
                editCardUser.toBuilder().state(null).build(),
                editCardUser.toBuilder().taskRealizationDocumentName(null).build(),
                editCardUser.toBuilder().taskRealizationDocumentNumber(null).build(),
                editCardUser.toBuilder().taskRealizationDocumentDate(null).build()
        );

        for (TaskCard card : emptyTaskCards) {
            mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        log.info("TEST_3_5 EDIT TASK CARD WITH EMPTY FIELDS (USER)");

        for (int i = 4; i < 8; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                    .with(httpBasic("testuser", "testuserpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyTaskCards.get(i))))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        log.info("TEST_3_6 EDIT TASK CARD WITH WRONG OWNER");

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/editTaskCard")
                    .with(httpBasic("user", "userpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(emptyTaskCards.get(i))))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        userService.deleteUser(adminPrincipal, user.getId());

        cleanTestData();
    }

    @Test
    void test_DeleteTaskCard() throws Exception {
        List<TaskCard> delCards = new ArrayList<>();
        List<Set<Executor>> execSets = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        createTaskCards("DeleteTaskCard", delCards, execSets, docs);

        for (TaskCard card : delCards)
            taskCardService.addTaskCard(adminPrincipal, card);

        delCards = taskCardService.findTaskCards(adminPrincipal, null, null, null, null, null, null, 0, 100).getData().getContent();
        TaskCard delCard = delCards.get(delCards.size() - 1);

        log.info("TEST_4_1 DELETE TASK CARD WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteTaskCard")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.ALL)
                .param("taskCardId", String.valueOf(delCard.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_4_2 DELETE NON EXISTENT TASK CARD");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteTaskCard")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("taskCardId", String.valueOf(0)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_4_3 DELETE TASK CARD");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteTaskCard")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL)
                .param("taskCardId", String.valueOf(delCard.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")));

        cleanTestData();
    }

    void createTaskCards(String testName, List<TaskCard> cards, List<Set<Executor>> execSets, List<Document> docs) {

        List<Executor> execs = new ArrayList<>();
        List<Executor> coexecs = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            execs.add(execService.addExecutor(adminPrincipal, new Executor(testName + "Unit" + String.valueOf(i), "Executor")).getData());
            coexecs.add(execService.addExecutor(adminPrincipal, new Executor(testName + "Unit" + String.valueOf(i), "Coexecutor")).getData());
            docs.add(documentService.createDocument(adminPrincipal, new Document(testName + "DocName" + String.valueOf(i), testName + "Num" + String.valueOf(i), LocalDate.now())).getData());

            HashSet<Executor> tempSet = new HashSet<>();
            for (int j = 0; j <= i; j++) {
                if (j % 2 == 0) tempSet.add(execs.get(j));
                else tempSet.add(coexecs.get(j));
            }
            execSets.add(tempSet);

            cards.add(new TaskCard(tempSet, LocalDate.now().plus(i, ChronoUnit.DAYS), docs.get(i), testName + "Para" + String.valueOf(i), testName + "Content" + String.valueOf(i)));
        }
    }

    void cleanTestData() {
        List<TaskCard> taskCards = taskCardService.findTaskCards(adminPrincipal, null, null, null, null, null, null, 0, 100).getData().getContent();
        List<Document> docs = documentService.findDocument(adminPrincipal, null, null, null, null, 0, 100).getData().getContent();

        for (TaskCard card : taskCards)
            taskCardService.deleteTaskCard(adminPrincipal, card.getId());
        for (Document doc : docs)
            documentService.deleteDocument(adminPrincipal, doc.getId());

        List<Executor> execs = execService.getExecutors(adminPrincipal).getData();
        List<Executor> сoexecs = execService.getCoexecutors(adminPrincipal).getData();

        for (Executor exec : execs)
            execService.deleteExecutor(adminPrincipal, exec.getId());

        for (Executor exec : сoexecs)
            execService.deleteExecutor(adminPrincipal, exec.getId());
    }

}
