package com.mtg.examples.mongo.datastore;

import java.util.ConcurrentModificationException;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.DatastoreImpl;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.VersionHelper;
import com.google.code.morphia.annotations.Version;
import com.google.code.morphia.mapping.MappedClass;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.QueryException;
import com.google.code.morphia.query.QueryImpl;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mtg.examples.mongo.annotations.ShardKey;

/**
 * Created to override the tryVersionedUpdate so it also uses the shardKey as
 * part of the update function
 */
public class DatastoreShardImpl extends DatastoreImpl implements Datastore {

    private static String SHARD_KEY_FIELDNAME = "shardKey";

    public DatastoreShardImpl(Morphia morphia, Mongo mongo, String dbName) {
        super(morphia, mongo, dbName);
        MappedClass.interestingAnnotations.add(ShardKey.class);
        MappedField.interestingAnnotations.add(ShardKey.class);
    }

    //TODO: Override find to also support shard key annotation
    
    @Override
    protected <T> WriteResult tryVersionedUpdate(DBCollection dbColl, T entity, DBObject dbObj, WriteConcern wc, DB db, MappedClass mc) {
        WriteResult wr = null;
        if (mc.getFieldsAnnotatedWith(Version.class).isEmpty())
            return wr;

        MappedField mfVersion = mc.getFieldsAnnotatedWith(Version.class).get(0);
        String versionKeyName = mfVersion.getNameToStore();
        Long oldVersion = (Long) mfVersion.getFieldValue(entity);
        long newVersion = VersionHelper.nextValue(oldVersion);
        dbObj.put(versionKeyName, newVersion);
        if (oldVersion != null && oldVersion > 0) {
            Object idValue = dbObj.get(Mapper.ID_KEY);
            Object shardKey = dbObj.get(SHARD_KEY_FIELDNAME);
            @SuppressWarnings("unchecked")
            final Query<T> filter = find((Class<T>) entity.getClass(), Mapper.ID_KEY, idValue).filter(versionKeyName, oldVersion);
            if (shardKey != null) {
                filter.filter(SHARD_KEY_FIELDNAME, shardKey);
            }
            UpdateResults<T> res = update(filter, dbObj, false, false, wc);

            wr = res.getWriteResult();

            if (res.getUpdatedCount() != 1)
                throw new ConcurrentModificationException("Entity of class " + entity.getClass().getName() + " (id='" + idValue + "',version='" + oldVersion + "') was concurrently updated.");
        } else if (wc == null)
            wr = dbColl.save(dbObj);
        else
            wr = dbColl.save(dbObj, wc);

        // update the version.
        mfVersion.setFieldValue(entity, newVersion);
        return wr;
    }

    private <T> UpdateResults<T> update(Query<T> query, DBObject u, boolean createIfMissing, boolean multi, WriteConcern wc) {
        QueryImpl<T> qi = (QueryImpl<T>) query;

        DBCollection dbColl = qi.getCollection();

        if (qi.getSortObject() != null && qi.getSortObject().keySet() != null && !qi.getSortObject().keySet().isEmpty())
            throw new QueryException("sorting is not allowed for updates.");
        if (qi.getOffset() > 0)
            throw new QueryException("a query offset is not allowed for updates.");
        if (qi.getLimit() > 0)
            throw new QueryException("a query limit is not allowed for updates.");

        DBObject q = qi.getQueryObject();
        if (q == null)
            q = new BasicDBObject();

        WriteResult wr;
        if (wc == null)
            wr = dbColl.update(q, u, createIfMissing, multi);
        else
            wr = dbColl.update(q, u, createIfMissing, multi, wc);

        throwOnError(wc, wr);

        return new UpdateResults<T>(wr);
    }

    @Override
    public <T> UpdateResults<T> update(T ent, UpdateOperations<T> ops) {
        MappedClass mc = mapr.getMappedClass(ent);
        @SuppressWarnings("unchecked")
        Query<T> q = (Query<T>) createQuery(mc.getClazz());
        q.disableValidation().filter(Mapper.ID_KEY, mapr.getId(ent));
        if (mc.getFieldsAnnotatedWith(ShardKey.class).size() > 0) {
            MappedField shardKeyMF = mc.getFieldsAnnotatedWith(ShardKey.class).get(0);
            Integer shardKey = (Integer) shardKeyMF.getFieldValue(ent);
            q.filter(shardKeyMF.getNameToStore(), shardKey);
        }
        MappedField versionMF = null;
        long newVersion = 0;
        if (mc.getFieldsAnnotatedWith(Version.class).size() > 0) {
            versionMF = mc.getFieldsAnnotatedWith(Version.class).get(0);
            Long oldVer = (Long) versionMF.getFieldValue(ent);
            q.filter(versionMF.getNameToStore(), oldVer);
            newVersion = VersionHelper.nextValue(oldVer);
            ops.set(versionMF.getNameToStore(), newVersion);
        }
        UpdateResults<T> updateResults = update(q, ops);
        if (updateResults.getUpdatedCount() > 0 && versionMF != null) {
            versionMF.setFieldValue(ent, newVersion);
        }
        return updateResults;
    }

}
