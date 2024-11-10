package com.unlimited_dream.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DynamoDBTableConfig {

    final DynamoDbClient dynamoDbClient;
    final DynamoDbEnhancedClient enhancedClient;


    @PostConstruct
    private void initDatabase() {
        Set<Class<?>> dynamoDbClasses = DynamoDBConfig.findDynamoDbBeanClasses();
        for (Class<?> clazz : dynamoDbClasses) {
            String tableName = clazz.getSimpleName().toLowerCase();
            createTableIfNotExists(enhancedClient, tableName, clazz);
        }
    }

    private static Set<Class<?>> findDynamoDbBeanClasses() {
        Reflections reflections = new Reflections("com.unlimited_dream.demo.entity");
        return reflections.getTypesAnnotatedWith(DynamoDbBean.class);
    }

    private void createTableIfNotExists(DynamoDbEnhancedClient enhancedClient, String tableName, Class<?> clazz) {

        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
        } catch (ResourceNotFoundException e) {
            // Table doesn't exist, create it
            CreateTableEnhancedRequest request = CreateTableEnhancedRequest.builder()
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .build();

            enhancedClient.table(tableName, TableSchema.fromBean(clazz))
                    .createTable(request);

            // Wait for table to be active
            waitForTableToBecomeActive(dynamoDbClient, tableName);
        }
    }

    private void waitForTableToBecomeActive(DynamoDbClient ddb, String tableName) {
        String status = null;
        int attempts = 0;
        do {
            try {
                attempts++;
                DescribeTableResponse tableDescription = ddb.describeTable(
                        DescribeTableRequest.builder().tableName(tableName).build());
                status = tableDescription.table().tableStatus().toString();
                if (!"ACTIVE".equals(status)) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for table to become active", e);
            }
        } while (!"ACTIVE".equals(status) && attempts < 30);

        if (!"ACTIVE".equals(status)) {
            throw new RuntimeException("Table " + tableName + " never became active");
        }
    }
}
