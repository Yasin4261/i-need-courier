package com.api.demo;

import com.api.demo.config.container.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestINeedCourierApplication {

        public static void main(String[] args) {
            SpringApplication.from(INeedCourierApplication::main)
                    .with(TestContainersConfiguration.class)
                    .run(args);
        }


}
