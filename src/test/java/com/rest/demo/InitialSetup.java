package com.rest.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.rest.demo.domain.*;
import com.rest.demo.service.UserService;

public class InitialSetup implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    UserService authUserService;

    @SneakyThrows
    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        synchronized (InitialSetup.class) {
            ApplicationContext springContext = SpringExtension.getApplicationContext(extensionContext);
            authUserService = springContext.getBean(UserService.class);

            User admin = new User(null, "admin name", "admin", "adminpass", "ROLE_USER,ROLE_ADMIN", "NONE");
            UserPrincipal adminPrincipal = new UserPrincipal(admin);
            authUserService.createUser(adminPrincipal, admin);

            User user = new User(null, "user name", "user", "userpass", "ROLE_USER", "NONE");
            authUserService.createUser(adminPrincipal, user);
        }
    }

    @Override
    public void close() {
        synchronized (InitialSetup.class) {
        }
    }
}