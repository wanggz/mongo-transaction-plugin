package com.yudao.mongo.transaction;

import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.yudao.mongo.transaction.exception.MongoCUDException;
import com.yudao.mongo.transaction.exception.MongoTransactionException;
import com.yudao.mongo.transaction.load.MongoURLConstants;
import com.yudao.mongo.transaction.util.UUIDGenerator;

/**
 * 处理事务核心类 <br>
 * 包含开始事务，提交事务，回滚的方法。封装了mongo添加，修改，删除操作方法。<br>
 * 
 * @author Austin
 * 
 */
public class MongoTransaction {

	private static final ThreadLocal<Transaction> local = new ThreadLocal<Transaction>();

	/**
	 * 开启事务
	 * 
	 * @throws Exception
	 */
	public static void startTransaction() throws Exception {
		Transaction tran = local.get();
		// 判断此事务是否属于顶层事务
		if (tran == null) {
			tran = new Transaction();

			// DBCollection
			DB db = Mongo.connect(new DBAddress(MongoURLConstants.MONGO_URL,
					"transactionDB"));
			DBCollection collection = db.getCollection(local.toString());
			tran.setDbCollection(collection);
			DBCollection rollBackCollection = db.getCollection("$"
					+ local.toString());
			tran.setDbRollBackCollection(rollBackCollection);
			tran.setCommitCount(0);
			tran.setTransCount(1);
			tran.setTransDeep(1);

			local.set(tran);
		} else {
			// 事务已经开启，将嵌套层次深度加一，将事务次数加一
			tran.setTransCount(tran.getTransCount() + 1);
			tran.setTransDeep(tran.getTransDeep() + 1);
		}
	}

	/**
	 * 事务操作方法指定常量类型
	 * 
	 * @author Austin
	 * 
	 */
	class OperationTypeConstants {
		/** 保存 */
		public static final String SAVE = "save";
		/** 更新 */
		public static final String UPDATE = "update";
		/** 删除 */
		public static final String DELETE = "delete";
	}

	/**
	 * 带事务的保存操作
	 * 
	 * @param dbName
	 *            待保存数据库名字
	 * @param collectionName
	 *            待保存集合名字
	 * @param conclusion
	 *            待保存结果
	 * @throws MongoCUDException
	 */
	public static void save(String dbName, String collectionName,
			DBObject conclusion) throws MongoCUDException {

		String _id = UUIDGenerator.getUUID();
		TransactionTable transactionTable = new TransactionTable();
		transactionTable.set_id(_id); // UUID id
		transactionTable.setDbName(dbName);
		transactionTable.setCollectionName(collectionName);
		transactionTable.setOperationType(OperationTypeConstants.SAVE);
		transactionTable.setDbobject1(conclusion);
		transactionTable.setStatus(TransactionTable.Status.BEGING);

		Gson gson = new Gson();

		DBCollection collection = local.get().getDbCollection();
		WriteResult result1 = collection.save((DBObject) JSON.parse(gson
				.toJson(transactionTable)));
		if (result1.getError() != null) {
			throw new MongoCUDException("mongo保存出错！");
		}

		// 将待回滚后数据存入另一个库中
		TransactionTable rollBackTransactionTable = new TransactionTable();
		rollBackTransactionTable.set_id(_id);
		rollBackTransactionTable.setDbName(dbName);
		rollBackTransactionTable.setCollectionName(collectionName);
		rollBackTransactionTable
				.setOperationType(OperationTypeConstants.DELETE);
		rollBackTransactionTable.setDbobject1(conclusion);
		rollBackTransactionTable.setStatus(TransactionTable.Status.BEGING);

		DBCollection rollBackCollection = local.get().getDbRollBackCollection();
		WriteResult result2 = rollBackCollection.save((DBObject) JSON
				.parse(gson.toJson(rollBackTransactionTable)));
		if (result2.getError() != null) {
			throw new MongoCUDException("mongo保存出错！");
		}
	}

	/**
	 * 带事务的更新操作
	 * 
	 * @param dbName
	 *            待更新数据库名字
	 * @param collectionName
	 *            待更新集合名字
	 * @param query
	 *            待更新查询条件
	 * @param conclusion
	 *            待更新结果
	 * @throws MongoCUDException
	 * @throws UnknownHostException 
	 */
	public static void update(String dbName, String collectionName,
			DBObject query, DBObject conclusion) throws MongoCUDException, UnknownHostException {

		String _id = UUIDGenerator.getUUID();
		TransactionTable transactionTable = new TransactionTable();
		transactionTable.set_id(_id); // UUID id
		transactionTable.setDbName(dbName);
		transactionTable.setCollectionName(collectionName);
		transactionTable.setOperationType(OperationTypeConstants.UPDATE);
		transactionTable.setDbobject1(query);
		transactionTable.setDbobject2(conclusion);
		transactionTable.setStatus(TransactionTable.Status.BEGING);

		Gson gson = new Gson();

		DBCollection collection = local.get().getDbCollection();
		WriteResult result1 = collection.save((DBObject) JSON.parse(gson
				.toJson(transactionTable)));
		if (result1.getError() != null) {
			throw new MongoCUDException("mongo更新出错！");
		}

		// 根据ID，找出所有待更新的数据，将记录插入到库中，多条存数组格式
		DB db = Mongo
				.connect(new DBAddress(MongoURLConstants.MONGO_URL, dbName));
		DBCollection collection4update = db.getCollection(collectionName);
		DBCursor cursor = collection4update.find(query);
		BasicDBList dbList = new BasicDBList();
		while(cursor.hasNext()){
			DBObject next = cursor.next();
			dbList.add(next);
		}
		TransactionTable rollBackTransactionTable = new TransactionTable();
		rollBackTransactionTable.set_id(_id);
		rollBackTransactionTable.setDbName(dbName);
		rollBackTransactionTable.setCollectionName(collectionName);
		rollBackTransactionTable
				.setOperationType(OperationTypeConstants.UPDATE);
		rollBackTransactionTable.setDbobject1(dbList); //TODO 将数据存入此字段会不会有问题？
		rollBackTransactionTable.setStatus(TransactionTable.Status.BEGING);
		
		DBCollection rollBackCollection = local.get().getDbRollBackCollection();
		WriteResult result2 = rollBackCollection.save((DBObject) JSON
				.parse(gson.toJson(rollBackTransactionTable)));
		if (result2.getError() != null) {
			throw new MongoCUDException("mongo更新出错！");
		}
	}

	/**
	 * 带事务的删除操作
	 * 
	 * @param dbName
	 *            待删除数据库名字
	 * @param collectionName
	 *            待删除集合名字
	 * @param conclusion
	 *            待删除条件
	 * @throws UnknownHostException 
	 * @throws MongoCUDException 
	 */
	public static void delete(String dbName, String collectionName,
			DBObject query) throws UnknownHostException, MongoCUDException {
		
		String _id = UUIDGenerator.getUUID();
		TransactionTable transactionTable = new TransactionTable();
		transactionTable.set_id(_id); // UUID id
		transactionTable.setDbName(dbName);
		transactionTable.setCollectionName(collectionName);
		transactionTable.setOperationType(OperationTypeConstants.DELETE);
		transactionTable.setDbobject1(query);
		transactionTable.setStatus(TransactionTable.Status.BEGING);

		Gson gson = new Gson();

		DBCollection collection = local.get().getDbCollection();
		WriteResult result1 = collection.save((DBObject) JSON.parse(gson.toJson(transactionTable)));
		if (result1.getError() != null) {
			throw new MongoCUDException("mongo更新出错！");
		}
		
		// 根据ID，找出所有待删除的数据，将记录插入到库中，多条存数组格式
		DB db = Mongo
				.connect(new DBAddress(MongoURLConstants.MONGO_URL, dbName));
		DBCollection collection4update = db.getCollection(collectionName);
		DBCursor cursor = collection4update.find(query);
		BasicDBList dbList = new BasicDBList();
		while(cursor.hasNext()){
			DBObject next = cursor.next();
			dbList.add(next);
		}
		TransactionTable rollBackTransactionTable = new TransactionTable();
		rollBackTransactionTable.set_id(_id);
		rollBackTransactionTable.setDbName(dbName);
		rollBackTransactionTable.setCollectionName(collectionName);
		rollBackTransactionTable.setOperationType(OperationTypeConstants.SAVE);
		rollBackTransactionTable.setDbobject1(dbList);
		rollBackTransactionTable.setStatus(TransactionTable.Status.BEGING);
		
		DBCollection rollBackCollection = local.get().getDbRollBackCollection();
		WriteResult result2 = rollBackCollection.save((DBObject) JSON
				.parse(gson.toJson(rollBackTransactionTable)));
		if (result2.getError() != null) {
			throw new MongoCUDException("mongo删除出错！");
		}
	}

	/**
	 * 事务提交
	 * 
	 * @throws MongoTransactionException
	 * @throws MongoCUDException
	 * @throws UnknownHostException
	 * 
	 * @throws Exception
	 */
	public static void commit() throws UnknownHostException, MongoCUDException,
			MongoTransactionException {

		DBCollection collection = local.get().getDbCollection();
		DBCursor dbCursor = collection.find();
		while (dbCursor.hasNext()) {
			DBObject next = dbCursor.next();
			String operationType = (String) next.get("operationType");
			if (operationType.equals(OperationTypeConstants.SAVE)) {
				saveOperation(next);
			} else if (operationType.equals(OperationTypeConstants.UPDATE)) {
				updateOperation(next);
			} else if (operationType.equals(OperationTypeConstants.DELETE)) {
				deleteOperation(next);
			} else {
				throw new MongoTransactionException("执行事务提交出错！");
			}
		}

		// 提交成功后，判断表中所有事务记录是否为succeed。如果全部是succeed则事务操作成功，如果有失败则事务操作失败，需回滚
		DBCursor dbCursor2 = collection.find();
		while (dbCursor2.hasNext()) {
			DBObject next = dbCursor2.next();
			String status = (String) next.get("status");
			if (TransactionTable.Status.BEGING.equals(status)
					|| TransactionTable.Status.FAILURE.equals(status)) {
				throw new MongoTransactionException("执行事务提交出错！");
			}
		}

		// 删除当前事务表
		collection.drop();
	}

	public static void rollback() {

		// TODO 事务的回滚操作

		/*
		 * 查看状态 ， 如果更新成功了要回滚 。如果是更新失败的原始数据应该没变。 如果是begin，则没开始操作。
		 * 
		 * 原始数据如何存储？
		 * 
		 * save---delete update---update delete---save
		 */

		DBCollection collection = local.get().getDbCollection();
		DBCursor dbCursor = collection.find();
		while (dbCursor.hasNext()) {
			DBObject next = dbCursor.next();
			String status = (String) next.get("status");
			if (TransactionTable.Status.SUCCEED.equals(status)) {
				
			}
		}

	}

	private static void saveOperation(DBObject next)
			throws UnknownHostException, MongoCUDException,
			MongoTransactionException {

		DBCollection collection = getDBCollection(next);

		DBObject dbobject1 = (DBObject) next.get("dbobject1");

		WriteResult result = collection.save(dbobject1);

		if (result.getError() == null) {
			updateTransactionTableStatus(next, TransactionTable.Status.SUCCEED);
		} else {
			updateTransactionTableStatus(next, TransactionTable.Status.FAILURE);
			throw new MongoCUDException("数据保存失败！");
		}
	}

	private static void updateOperation(DBObject next)
			throws UnknownHostException, MongoCUDException,
			MongoTransactionException {

		DBCollection collection = getDBCollection(next);

		DBObject dbobject1 = (DBObject) next.get("dbobject1");
		DBObject dbobject2 = (DBObject) next.get("dbobject2");

		WriteResult result = collection.update(dbobject1, dbobject2);

		if (result.getError() == null) {
			updateTransactionTableStatus(next, TransactionTable.Status.SUCCEED);
		} else {
			updateTransactionTableStatus(next, TransactionTable.Status.FAILURE);
			throw new MongoCUDException("数据更新失败！");
		}
	}

	private static void deleteOperation(DBObject next)
			throws UnknownHostException, MongoCUDException,
			MongoTransactionException {

		DBCollection collection = getDBCollection(next);

		DBObject dbobject1 = (DBObject) next.get("dbobject1");

		WriteResult result = collection.remove(dbobject1);

		if (result.getError() == null) {
			updateTransactionTableStatus(next, TransactionTable.Status.SUCCEED);
		} else {
			updateTransactionTableStatus(next, TransactionTable.Status.SUCCEED);
			throw new MongoCUDException("数据删除失败！");
		}
	}

	private static DBCollection getDBCollection(DBObject next)
			throws UnknownHostException {

		String dbName = (String) next.get("dbName");
		String collectionName = (String) next.get("collectionName");

		DB db = Mongo
				.connect(new DBAddress(MongoURLConstants.MONGO_URL, dbName));
		DBCollection collection = db.getCollection(collectionName);

		return collection;
	}

	private static void updateTransactionTableStatus(DBObject next,
			String status) throws MongoTransactionException {

		BasicDBObject updateStatus = new BasicDBObject();
		updateStatus.put("status", status);

		WriteResult result = local.get().getDbCollection()
				.update(next, new BasicDBObject().append("$set", updateStatus));

		if (result.getError() != null) {
			throw new MongoTransactionException("更新事务状态出错！");
		}
	}
}
