package com.unlimited_dream.demo.config;

import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DynamoDBConfig {
    @Value("${aws.dynamodb.accessKey}")
    private String accessKey;

    @Value("${aws.dynamodb.secretKey}")
    private String secretKey;

    @Value("${aws.dynamodb.endpoint}")
    private String endpoint;

    private final GenericApplicationContext context;

    @Bean
    public DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient dynamoDbEnhancedClient =  DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        registerTableBean(dynamoDbEnhancedClient);
        return dynamoDbEnhancedClient;
    }

    private void registerTableBean(DynamoDbEnhancedClient enhancedClient) {

        Set<Class<?>> dynamoDbClasses = findDynamoDbBeanClasses();
        for (Class<?> clazz : dynamoDbClasses) {
            String tableName = clazz.getSimpleName().toLowerCase();
            DynamoDbTable<?> table = enhancedClient.table(tableName, TableSchema.fromBean(clazz));
            context.registerBean(tableName + "Table", DynamoDbTable.class, () -> table);
        }
    }

    public static Set<Class<?>> findDynamoDbBeanClasses() {
        Reflections reflections = new Reflections("com.unlimited_dream.demo.entity");
        return reflections.getTypesAnnotatedWith(DynamoDbBean.class);
    }
}
