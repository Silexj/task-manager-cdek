package ru.silex.tasktracker.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("ru.silex.tasktracker.persistence.mybatis")
public class MyBatisMapperConfig {
}
