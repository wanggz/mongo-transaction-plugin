package com.yudao.test;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.yudao.mongo.transaction.load.MongoURLConstants;

public class MongoBasicTest {

	@Test
	public void addNewDate2Test() throws Exception {
		DB db = Mongo
				.connect(new DBAddress(MongoURLConstants.MONGO_URL, "test"));
		DBCollection collection = db.getCollection("test");
		collection.save(new BasicDBObject().append("name", "jack"));
	}
}
