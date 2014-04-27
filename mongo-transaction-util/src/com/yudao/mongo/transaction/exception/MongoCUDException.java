package com.yudao.mongo.transaction.exception;

/**
 * mongo数据库操作异常 <br>
 * 插入、更新、删除操作失败引起的异常
 * 
 * @author Austin
 * 
 */
public class MongoCUDException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MongoCUDException(String message) {
		super(message);
	}
}
