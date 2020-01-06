package com.dinfo.robotea;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.dinfo.robotea.mapper")
@EnableAutoConfiguration
@EnableFeignClients(basePackages="com.dinfo.robotea.http")
public class RoboteaApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoboteaApplication.class, args);
	}

}
