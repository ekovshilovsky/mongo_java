package com.mtg.examples.mongo.daos;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.mtg.examples.mongo.dto.User;
import com.mtg.examples.mongo.repository.UserRepository;

/**
 * 
 */
public class UserDaoMongoImpl implements UserDao {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User find(String userID) {
        return find(new ObjectId(userID));
    }

    @Override
    public User find(ObjectId userID) {
        return userRepository.get(userID);
    }

    @Override
    public User save(User user) {
            userRepository.save(user);
            return user;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
