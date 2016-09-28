package cn.ittiger.database.listener;

import android.database.sqlite.SQLiteDatabase;

/**
 * 数据监听(数据库第一次创建，版本变更时的监听)
 * Author: hyl
 * Time: 2015-8-13下午1:51:59
 */
public interface IDBListener {
	/**
	 * 数据库版本变更的时候执行,在数据库DB打开时会进行版本号判断，当版本号不同的时候会执行该监听处理函数(只会执行一次)
	 * 在此方法中执行数据库操作不会出现(java.lang.IllegalStateException: getDatabase called recursively)异常，已解决
	 * @author: huylee
	 * @time:	2015-8-12下午10:32:02
	 * @param db			数据库
	 * @param oldVersion	旧版本号
	 * @param newVersion	新版本号
	 */
	public void onUpgradeHandler(SQLiteDatabase db, int oldVersion, int newVersion);
	/**
	 * 数据库文件第一次创建时的监听响应函数，已经存在的数据库不会执行此方法
	 * 在此方法中执行数据库操作不会出现(java.lang.IllegalStateException: getDatabase called recursively)异常，已解决
	 * @author: huylee
	 * @time:	2015-8-12下午10:36:06
	 * @param db	数据库
	 */
	public void onDbCreateHandler(SQLiteDatabase db);
}
