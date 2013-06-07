package com.mtg.examples.mongo.daos;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.mapping.MappedClass;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.google.code.morphia.utils.LongIdEntity;
import com.mtg.examples.mongo.annotations.ShardKey;

/**
 * 
 */
public class ShardBasicDAO<T, K> extends BasicDAO<T, K> {

    // Disable on the child classes to make sure that all calls go through this
    private final Datastore ds;

    Mapper mapper = new Mapper();

    protected ShardBasicDAO(Datastore ds) {
        super(ds);
        this.ds = ds;
    }

    protected Long createAutoIncrement() {
        String collName = getCollection().getName();
        Query<LongIdEntity.StoredId> q = ds.find(LongIdEntity.StoredId.class, "_id", collName);
        UpdateOperations<LongIdEntity.StoredId> uOps = ds.createUpdateOperations(LongIdEntity.StoredId.class).inc("value");
        LongIdEntity.StoredId newId = ds.findAndModify(q, uOps);
        if (newId == null) {
            newId = new LongIdEntity.StoredId(collName);
            ds.save(newId);
        }
        return newId.getValue();
    }

    public T findAndModify(Query<T> q, UpdateOperations<T> ops) {
        final T ret = ds.findAndModify(q, ops, false);
        return ret;
    }

    public UpdateResults<T> update(T ent, UpdateOperations<T> ops) {
        return ds.update(ent, ops);
    }

    public UpdateResults<T> update(T ent, Query<T> q, UpdateOperations<T> ops) {
        q = createIDQuery(ent, q);
        UpdateResults<T> updateResults = ds.update(q, ops);
        return updateResults;
    }

    public Query<T> createIDQuery(T ent) {
        MappedClass mc = mapper.getMappedClass(ent);
        @SuppressWarnings("unchecked")
        Query<T> q = (Query<T>) ds.createQuery(mc.getClazz());
        return createIDQuery(ent, q);
    }

    protected Query<T> createIDQuery(T ent, Query<T> q) {
        MappedClass mc = mapper.getMappedClass(ent);
        q.disableValidation().filter(Mapper.ID_KEY, mapper.getId(ent));
        if (mc.getFieldsAnnotatedWith(ShardKey.class).size() > 0) {
            MappedField shardKeyMF = mc.getFieldsAnnotatedWith(ShardKey.class).get(0);
            Integer shardKey = (Integer) shardKeyMF.getFieldValue(ent);
            q.filter(shardKeyMF.getNameToStore(), shardKey);
        }
        return q;
    }
}
