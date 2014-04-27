package com.yudao.mongo.transaction.exception;

/**
 * mongo事务操作异常 <br>
 * 在执行事务操作的过程中，抛出的异常
 * 
 * @author Austin
 * 
 */
public class MongoTransactionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MongoTransactionException(String message) {
		super(message);
	}

}
