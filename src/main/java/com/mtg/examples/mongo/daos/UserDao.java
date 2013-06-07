package com.mtg.examples.mongo.daos;

import java.util.List;

import org.bson.types.ObjectId;

import com.mtg.examples.mongo.dto.User;


/**
 * Interface for CRUD operations for user
 */
public interface UserDao {

    public User find(String userID);
    public User find(ObjectId userID);
    public List<User> findAll();
    public User save(User user);
}
