package com.yudao.test;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.yudao.mongo.transaction.MongoTransaction;
import com.yudao.mongo.transaction.util.UUIDGenerator;

public class MongoTest
{

    @Test
    public void test2()
    {
        try
        {
            MongoTransaction.startTransaction();
            BasicDBObject query = new BasicDBObject();
            query.put("_id", "1");
            BasicDBObject conclusion = new BasicDBObject();
            conclusion.put("name", "austin");
            MongoTransaction.update("test", "test", query, conclusion);
            MongoTransaction.save("test", "test", new BasicDBObject().append("_id", "3").append("name", "lala"));

            MongoTransaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void test5()
    {
        String uuid = UUIDGenerator.getUUID();
        System.out.println(uuid);
    }

}
