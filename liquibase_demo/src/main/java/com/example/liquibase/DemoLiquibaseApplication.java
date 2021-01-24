package com.example.liquibase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.IOException;

// DataSourceAutoConfiguration会自动加载, 所以排除此类的自动配置
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class DemoLiquibaseApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(DemoLiquibaseApplication.class, args);
		System.in.read();
	}

}
