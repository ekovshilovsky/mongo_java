package com.mtg.examples.mongo.repository;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.mongodb.WriteResult;
import com.mtg.examples.mongo.daos.ShardBasicDAO;
import com.mtg.examples.mongo.dto.User;

/**
 */
public class UserRepository extends ShardBasicDAO<User, ObjectId> {

    protected UserRepository(Datastore ds) {
        super(ds);
    }

    @Override
    public User get(ObjectId userId) {
        final Query<User> query = createUserIDQuery(userId);
        return super.findOne(query);
    }

    @Override
    public WriteResult delete(User entity) {
        final Query<User> query = createIDQuery(entity);
        return super.deleteByQuery(query);
    }

    private Query<User> createUserIDQuery(ObjectId userId) {
        return createQuery().filter("_id", userId).filter("shardKey", User.getObjectIdShardKey(userId));
    }

    public List<User> findAll() {
        final Query<User> query = createQuery().queryNonPrimary();
        return find(query).asList();
    }

}
