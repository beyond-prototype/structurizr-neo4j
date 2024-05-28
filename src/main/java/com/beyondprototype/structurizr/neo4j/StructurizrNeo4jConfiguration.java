package com.beyondprototype.structurizr.neo4j;

import io.github.cdimascio.dotenv.Dotenv;
import org.neo4j.driver.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StructurizrNeo4jConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public Dotenv dotenv(){
        Dotenv dotenv = Dotenv.configure()
                .directory("/Users/xwp/github/beyond-prototype/structurizr-neo4j")
                .filename(".env")
                .load();

        return dotenv;
    }

    @Bean
    @ConditionalOnMissingBean
    public Session session(Dotenv dotenv) {
        String neo4jUri = dotenv.get("neo4j.uri");
        String username = dotenv.get("neo4j.authentication.username");
        String password = dotenv.get("neo4j.authentication.password");
        String database = dotenv.get("neo4j.database");

        Driver driver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password));

        driver.verifyConnectivity();

        Session session = driver.session(SessionConfig.builder().withDatabase(database == null?"neo4j":database).build());
        return session;
    }
}
