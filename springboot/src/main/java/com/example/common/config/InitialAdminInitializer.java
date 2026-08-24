package com.example.common.config;

import com.example.entity.Admin;
import com.example.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminInitializer.class);

    private final AdminService adminService;
    private final String username;
    private final String password;

    public InitialAdminInitializer(
            AdminService adminService,
            @Value("${app.bootstrap.admin.username:}") String username,
            @Value("${app.bootstrap.admin.password:}") String password) {
        this.adminService = adminService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank() || adminService.existsByUsername(username)) {
            return;
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setName(username);
        admin.setPassword(password);
        adminService.add(admin);
        log.info("Initial administrator '{}' was created. Remove the bootstrap environment variables now.", username);
    }
}
