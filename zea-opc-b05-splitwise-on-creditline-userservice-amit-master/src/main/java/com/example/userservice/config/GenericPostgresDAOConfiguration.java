package com.example.userservice.config;

import in.zeta.springframework.boot.commons.postgres.GenericPostgresDAO;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenericPostgresDAOConfiguration {

    @Bean
    public GenericPostgresDAO genericPostgresDAO(
            BasicDataSource basicDataSource,
            @Value("${spring.datasource.max-total}") int maxTotal) {
        return new GenericPostgresDAO(basicDataSource, maxTotal);
    }

    @Bean
    public BasicDataSource defineBasicDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {

        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setDriverClassName(driverClassName);

        return basicDataSource;
    }
}
