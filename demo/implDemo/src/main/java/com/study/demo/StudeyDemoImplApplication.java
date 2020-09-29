package com.study.demo;

import lombok.extern.slf4j.Slf4j;
//import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.study.dao.mapper")
@Slf4j
public class StudeyDemoImplApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudeyDemoImplApplication.class,args);
        log.debug("++++++++++++StudeyDemoImplApplication run success ++++++++++++");
    }
}
