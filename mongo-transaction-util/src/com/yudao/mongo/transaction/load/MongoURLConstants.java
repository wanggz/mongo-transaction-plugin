package com.yudao.mongo.transaction.load;

import java.util.ResourceBundle;

/**
 * mongo数据库连接信息
 * 
 * @author Austin
 * 
 */
public class MongoURLConstants {

	private final static ResourceBundle bundle = ResourceBundle
			.getBundle("mongo-url");

	/** mongo url */
	public final static String MONGO_URL = bundle.getString("mongo_url");
	/** mongo user */
	public final static String MONGO_USER = bundle.getString("mongo_user");
	/** mongo password */
	public final static String MONGO_PWD = bundle.getString("mongo_pwd");
}
