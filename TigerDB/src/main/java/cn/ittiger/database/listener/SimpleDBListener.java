package cn.ittiger.database.listener;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库监听空实现
 * Author: hyl
 * Time: 2015-8-13下午1:53:11
 */
public class SimpleDBListener implements IDBListener {

	@Override
	public void onUpgradeHandler(SQLiteDatabase db, int oldVersion,
			int newVersion) {

	}

	@Override
	public void onDbCreateHandler(SQLiteDatabase db) {

	}

}
