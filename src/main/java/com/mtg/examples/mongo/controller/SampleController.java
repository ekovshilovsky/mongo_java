package com.mtg.examples.mongo.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mtg.examples.mongo.daos.UserDao;
import com.mtg.examples.mongo.dto.User;

@Controller
@RequestMapping("/sample")
public class SampleController {
	
    @Resource
    private UserDao userDao;
	
	
    @RequestMapping(value="/user/{userID}", method=RequestMethod.GET)
    public @ResponseBody User getUser(@PathVariable String userID) throws IOException {
        User user = userDao.find(userID);
        return user;
    }
    
    @RequestMapping(value="/createuser/{username}", method=RequestMethod.GET)
    public @ResponseBody User saveUser(@PathVariable String username) throws IOException {
        User user = new User();
        user.setUsername(username);
        user.setUserId(new ObjectId());
        userDao.save(user);
        return user;
    }
    
    @RequestMapping(value="/users", method=RequestMethod.GET)
    public @ResponseBody List<User> getUsers() throws IOException {
        return userDao.findAll(); 
    }

    @RequestMapping(value="/shardRange", method=RequestMethod.GET)
    public @ResponseBody String getShardRange() {
        ObjectId id = new ObjectId();
        return "<b>FROM:</b>" + id._time() + "<br/><b>TO:</b>" + (id._time() + 0x3FFFF);
    }

}
