package cn.ittiger.database;

import cn.ittiger.database.util.ValueUtil;

import android.content.Context;

import java.util.HashMap;

/**
 * 数据库管理工厂
 * @author: huylee
 * @time:	2015-8-12下午10:20:59
 */
public class SQLiteDBFactory {
	/**
	 * 多个数据库集合对象<dbName, {@link}SQLiteDB>
	 */
	private static HashMap<String, SQLiteDB> dbMap = new HashMap<String, SQLiteDB>();
	
	/**
	 * 生成一个名为dnName的数据库，目录为默认目录(参考SQLiteDBConfig里面的目录设置)}
	 * Author: hyl
	 * Time: 2015-8-23下午6:36:34
	 * @param context
	 * @param dbName		要生成的数据库名称
	 * @return
	 */
	public static SQLiteDB createSQLiteDB(Context context, String dbName) {
		SQLiteDBConfig confing = new SQLiteDBConfig(context);
		confing.setDbName(dbName);
		return createSQLiteDB(confing);
	}
	
	/**
	 * 在默认目录下生成默认名称的数据库
	 * Author: hyl
	 * Time: 2015-8-23下午6:37:55
	 * @param context
	 * @return
	 */
	public static SQLiteDB createSQLiteDB(Context context) {
		return createSQLiteDB(new SQLiteDBConfig(context));
	}
	
	/**
	 * 根据自定义配置生成数据库
	 * Author: hyl
	 * Time: 2015-8-23下午6:38:15
	 * @param config
	 * @return
	 */
	public static SQLiteDB createSQLiteDB(SQLiteDBConfig config) {
		if(config.getVersion() < 0) {
			config.setVersion(SQLiteDBConfig.DEFAULT_VERSION);
		}
		if(ValueUtil.isEmpty(config.getDbName())) {
			config.setDbName(SQLiteDBConfig.DEFAULT_DB_NAME);
		}
		if(ValueUtil.isEmpty(config.getDbDirectoryPath())) {
			config.setDbDirectoryPath(SQLiteDBConfig.DEFAULT_DB_DIRECTORY_PATH);
		}
		if(!dbMap.containsKey(config.getDbName())) {
			synchronized (SQLiteDBFactory.class) {
				if(!dbMap.containsKey(config.getDbName())) {
					dbMap.put(config.getDbName(), new SQLiteDB(config));
				}
			}
		}
		SQLiteDB db = dbMap.get(config.getDbName());
		if(!db.isOpen()) {
			db.reOpen();
		}
		return dbMap.get(config.getDbName());
	}
}
