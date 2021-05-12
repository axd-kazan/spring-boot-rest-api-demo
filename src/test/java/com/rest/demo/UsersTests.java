package com.rest.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import com.rest.demo.domain.User;
import com.rest.demo.domain.UserPrincipal;
import com.rest.demo.service.UserService;
import com.rest.demo.service.ExecService;
import com.rest.demo.service.TaskCardService;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ExtendWith(InitialSetup.class)
class UsersTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;
    @Autowired
    ExecService execService;
    @Autowired
    TaskCardService taskCardService;

    UserPrincipal adminPrincipal = new UserPrincipal(new User("admin name", "admin", "adminpass", "ROLE_USER,ROLE_ADMIN", "NONE"));

    @Test
    void checkTestUsers() throws Exception {

        log.info("TEST_1_1 AUTHORISE WITH ADMIN");

        mockMvc.perform(MockMvcRequestBuilders.get("/getCurrentUser")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("admin name")))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.authorities", is("ROLE_USER,ROLE_ADMIN")))
                .andExpect(jsonPath("$.data.unit", is("NONE")));

        log.info("TEST_1_2 AUTHORISE WITH USER");

        mockMvc.perform(MockMvcRequestBuilders.get("/getCurrentUser")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is("user name")))
                .andExpect(jsonPath("$.data.username", is("user")))
                .andExpect(jsonPath("$.data.authorities", is("ROLE_USER")))
                .andExpect(jsonPath("$.data.unit", is("NONE")));
    }

    @Test
    void test_CreateUser() throws Exception {

        User newUser = new User("new_user name", "new_user", "new_userpass", "ROLE_USER", "new_user_unit");

        log.info("TEST_2_1 ADD NEW USER WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(newUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_2_2 ADD NEW USER");

        mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(newUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is(newUser.getName())))
                .andExpect(jsonPath("$.data.username", is(newUser.getUsername())))
                .andExpect(jsonPath("$.data.authorities", is(newUser.getAuthorities())))
                .andExpect(jsonPath("$.data.unit", is(newUser.getUnit())));

        log.info("TEST_2_3 AUTHORISE WITH NEW USER");

        mockMvc.perform(MockMvcRequestBuilders.get("/getCurrentUser")
                .with(httpBasic(newUser.getUsername(), newUser.getPassword()))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is(newUser.getName())))
                .andExpect(jsonPath("$.data.username", is(newUser.getUsername())))
                .andExpect(jsonPath("$.data.authorities", is(newUser.getAuthorities())))
                .andExpect(jsonPath("$.data.unit", is(newUser.getUnit())));

        log.info("TEST_2_4 ADD USER THAT ALREADY EXIST");

        mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(newUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_2_5 ADD USER WITH EMPTY FIELDS");

        List<String> emptyUsers = Arrays.asList(
                createUserJson(new User("", "user1", "userpass1", "ROLE_USER", "unit1")),
                createUserJson(new User("user name2", "", "userpass2", "ROLE_USER", "unit2")),
                createUserJson(new User("user name3", "user3", "", "ROLE_USER", "unit3")),
                createUserJson(new User("user name4", "user4", "userpass4", "", "unit4")),
                createUserJson(new User("user name5", "user5", "userpass5", "ROLE_USER", ""))
        );

        for (String user : emptyUsers) {
            mockMvc.perform(MockMvcRequestBuilders.post("/createUser")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(user))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        newUser = ((UserPrincipal) userService.loadUserByUsername(newUser.getUsername())).getUser();
        userService.deleteUser(adminPrincipal, newUser.getId());

    }

    @Test
    void test_EditUser() throws Exception {

        User editUser = userService.createUser(
                adminPrincipal,
                new User("new_edit_user name", "edit_user", "new_edit_userpass", "ROLE_USER", "new_edit_user_unit")).getData();

        editUser.setName("edit_user name");
        editUser.setPassword("edit_user_pass");
        editUser.setAuthorities("ROLE_USER,ROLE_ADMIN");
        editUser.setUnit("edit_user_unit");

        log.info("TEST_3_1 EDIT USER WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.post("/editUser")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(editUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_3_2 EDIT USER");

        mockMvc.perform(MockMvcRequestBuilders.post("/editUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(editUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is(editUser.getName())))
                .andExpect(jsonPath("$.data.username", is(editUser.getUsername())))
                .andExpect(jsonPath("$.data.authorities", is(editUser.getAuthorities())))
                .andExpect(jsonPath("$.data.unit", is(editUser.getUnit())));

        log.info("TEST_3_3 AUTHORISE WITH EDITED USER");

        mockMvc.perform(MockMvcRequestBuilders.get("/getCurrentUser")
                .with(httpBasic(editUser.getUsername(), editUser.getPassword()))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.name", is(editUser.getName())))
                .andExpect(jsonPath("$.data.username", is(editUser.getUsername())))
                .andExpect(jsonPath("$.data.authorities", is(editUser.getAuthorities())))
                .andExpect(jsonPath("$.data.unit", is(editUser.getUnit())));

        log.info("TEST_3_4 EDIT USER WITH EMPTY FIELDS");

        List<String> emptyUsers = Arrays.asList(
                createUserJson(new User("", "edit_user1", "edit_userpass1", "ROLE_USER", "edit_unit1")),
                createUserJson(new User("edit_user name2", "", "edit_userpass2", "ROLE_USER", "edit_unit2")),
                createUserJson(new User("edit_user name3", "edit_user3", "", "ROLE_USER", "edit_unit3")),
                createUserJson(new User("edit_user name4", "edit_user4", "edit_userpass4", "", "edit_unit4")),
                createUserJson(new User("edit_user name5", "edit_user5", "edit_userpass5", "ROLE_USER", ""))
        );

        for (String user : emptyUsers) {
            mockMvc.perform(MockMvcRequestBuilders.post("/editUser")
                    .with(httpBasic("admin", "adminpass"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(user))
                    .andExpect(status().isOk())
                    .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                    .andExpect(jsonPath("$.status", is("error")));
        }

        log.info("TEST_3_5 EDIT USER THAT DOESN'T EXIST");

        User notExistUser = new User("not_exist_edit_user name", "not_exist_edit_user", "not_exist_edit_userpass", "ROLE_USER", "not_exist_edit_user_unit");

        mockMvc.perform(MockMvcRequestBuilders.post("/editUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createUserJson(notExistUser)))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        userService.deleteUser(adminPrincipal, editUser.getId());

    }

    @Test
    public void test_DeleteUser() throws Exception {

        User deleteUser = userService.createUser(
                adminPrincipal,
                new User("delete_user name", "delete_user", "delete_userpass", "ROLE_USER", "delete_user_unit")).getData();

        log.info("TEST_4_1 DELETE USER WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteUser")
                .with(httpBasic("user", "userpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteUser.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

        log.info("TEST_4_2 DELETE USER");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteUser.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")));

        log.info("TEST_4_3 AUTHORISE WITH DELETED USER");

        mockMvc.perform(MockMvcRequestBuilders.get("/getCurrentUser")
                .with(httpBasic(deleteUser.getUsername(), deleteUser.getPassword()))
                .accept(MediaType.ALL))
                .andExpect(status().isUnauthorized())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()));

        log.info("TEST_4_4 DELETE USER THAT DOESN'T EXIST");

        mockMvc.perform(MockMvcRequestBuilders.get("/deleteUser")
                .with(httpBasic("admin", "adminpass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteUser.getId())))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

    }

    @Test
    public void test_GetUsersList() throws Exception {
        log.info("TEST_5_1 GET USERS LIST");

        mockMvc.perform(MockMvcRequestBuilders.get("/getUsersList")
                .with(httpBasic("admin", "adminpass"))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[0].name", is("admin name")))
                .andExpect(jsonPath("$.data[1].name", is("user name")))
                .andExpect(jsonPath("$.data[0].username", is("admin")))
                .andExpect(jsonPath("$.data[1].username", is("user")))
                .andExpect(jsonPath("$.data[0].authorities", is("ROLE_USER,ROLE_ADMIN")))
                .andExpect(jsonPath("$.data[1].authorities", is("ROLE_USER")))
                .andExpect(jsonPath("$.data[0].unit", is("NONE")))
                .andExpect(jsonPath("$.data[1].unit", is("NONE")));

        log.info("TEST_5_2 GET USERS LIST WITH LOW AUTHORITIES");

        mockMvc.perform(MockMvcRequestBuilders.get("/getUsersList")
                .with(httpBasic("user", "userpass"))
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andDo(mvcResult -> log.info(mvcResult.getResponse().getStatus() + " " + mvcResult.getResponse().getContentAsString()))
                .andExpect(jsonPath("$.status", is("error")));

    }

    String createUserJson(User user) {
        return "{ \"id\" : " + user.getId() + ", \"name\" : \"" + user.getName() + "\", \"username\" : \"" + user.getUsername() +
                "\", \"password\" : \"" + user.getPassword() + "\", \"authorities\" : \"" + user.getAuthorities() +
                "\", \"unit\" : \"" + user.getUnit() + "\" }";
    }

}
