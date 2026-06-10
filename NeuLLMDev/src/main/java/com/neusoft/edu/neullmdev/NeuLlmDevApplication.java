package com.neusoft.edu.neullmdev;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({
        "com.neusoft.edu.neullmdev.mapper.reminder",
        "com.neusoft.edu.neullmdev.mapper.hotel",
        "com.neusoft.edu.neullmdev.mapper.travel",
        "com.neusoft.edu.neullmdev.mapper.profile",
        "com.neusoft.edu.neullmdev.mapper.communication",
        "com.neusoft.edu.neullmdev.mapper.auth",
        "com.neusoft.edu.neullmdev.mapper.classroom",
        "com.neusoft.edu.neullmdev.mapper.notification"
})
public class NeuLlmDevApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuLlmDevApplication.class, args);
    }

}
