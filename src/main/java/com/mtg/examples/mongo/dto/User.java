package com.mtg.examples.mongo.dto;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.mtg.examples.mongo.annotations.ShardKey;
import com.mtg.examples.mongo.json.ObjectIdAsStringDeserializer;
import com.mtg.examples.mongo.json.ObjectIdAsStringSerializer;

/**
 * User DTO
 */
public class User implements Serializable {
    private static final long serialVersionUID = -1893077623585176643L;

    @Id
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL, using = ObjectIdAsStringSerializer.class)
    @JsonDeserialize(using = ObjectIdAsStringDeserializer.class)
    private ObjectId userId;

    /**
     * Shard Key
     */
    @Indexed
    @ShardKey
    private int shardKey;

    /**
     * Username for user
     */
    @Indexed
    private String username;

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId id) {
        this.userId = id;
        shardKey = getShardKey();
    }

    public int getShardKey() {
        if (shardKey == 0) {
            shardKey = getObjectIdShardKey(userId);
        }
        return shardKey;
    }

    public void setShardKey(int shardKey) {
        this.shardKey = shardKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    static public int getObjectIdShardKey(ObjectId userId) {
        // Return null if id isn't set
        if (userId == null)
            return 0;
        // Grab the first 20 bit from the md5
        final int first20Bit = Integer.parseInt(DigestUtils.md5Hex(userId.toStringMongod()).substring(0, 5), 16);
        // Truncate the 20 bit to 18 bit for 3 day granularity and add that to
        // the time
        return userId._time() + (0x3FFFF & first20Bit);
    }

}
