package com.yudao.mongo.transaction.util;

import java.util.UUID;

public class UUIDGenerator {

	public UUIDGenerator() {

	}

	/**
	 * 获得一个UUID
	 * 
	 * @return String UUID
	 */
	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		// 去掉“-”符号
		String uuid = s.substring(0, 8) + s.substring(9, 13)
				+ s.substring(14, 18) + s.substring(19, 23) + s.substring(24);

		return uuid.toUpperCase();
	}

}