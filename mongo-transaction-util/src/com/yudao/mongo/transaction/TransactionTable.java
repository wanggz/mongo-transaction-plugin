package com.yudao.mongo.transaction;

import com.mongodb.DBObject;

/**
 * 事务表 <br>
 * 主要用于记录需要事务操作的执行语句 <br>
 * 
 * @author Austin
 */
public class TransactionTable {

	private String _id;

	private String dbName;

	private String collectionName;

	private String operationType;

	private DBObject dbobject1;

	private DBObject dbobject2;

	private String status;

	/**
	 * 事务表状态常量
	 * 
	 * @author Austin
	 * 
	 */
	public class Status {
		/** 待插入状态 */
		public static final String BEGING = "beging";
		/** 操作成功状态 */
		public static final String SUCCEED = "succeed";
		/** 操作失败状态 */
		public static final String FAILURE = "failure";
		/** 提交完成状态 */
		public static final String FINISHED = "finished";
	}

	public String get_id() {
		return this._id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getDbName() {
		return this.dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getCollectionName() {
		return this.collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getOperationType() {
		return this.operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public DBObject getDbobject1() {
		return this.dbobject1;
	}

	public void setDbobject1(DBObject dbobject1) {
		this.dbobject1 = dbobject1;
	}

	public DBObject getDbobject2() {
		return this.dbobject2;
	}

	public void setDbobject2(DBObject dbobject2) {
		this.dbobject2 = dbobject2;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
