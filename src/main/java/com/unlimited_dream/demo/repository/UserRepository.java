package com.unlimited_dream.demo.repository;

import com.unlimited_dream.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    @Autowired
    DynamoDbTable<User> userTable;

    public User saveUser (User user) {
        userTable.putItem(user);
        return user;
    }

    public User getUserById (String id) {
        return userTable.getItem(Key.builder().partitionValue(id).build());
    }

    public User deleteById (String id) {
        return userTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public List<User> scanAllUser() {
        PageIterable<User> pages = userTable.scan();
        return convertUsers(pages);
    }

    private List<User> convertUsers (PageIterable<User> userPageIterable) {
        List<User> allUsers = new ArrayList<>();
        for (Page<User> page : userPageIterable) {
            allUsers.addAll(page.items());
        }
        return allUsers;
    }

    public List<User> queryUser(String name, String lastName) {
        Expression filterExpression = Expression.builder()
                .expression("contains(#name, :name) AND contains(#lastName, :lastName)")
                .putExpressionName("#name", "name")
                .putExpressionName("#lastName", "lastName")
                .putExpressionValue(":name", AttributeValue.builder().s(name).build())
                .putExpressionValue(":lastName", AttributeValue.builder().s(lastName).build())
                .build();
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder().filterExpression(filterExpression).build();
        PageIterable<User> pages = userTable.scan(scanEnhancedRequest);
        return convertUsers(pages);
    }

}
