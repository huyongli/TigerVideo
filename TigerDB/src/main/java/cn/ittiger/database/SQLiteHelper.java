package cn.ittiger.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 继承自SQLiteOpenHelper，扩展实现自定义db的生成路径
 * Author: hyl
 * Time: 2015-8-15下午1:52:02
 */
public class SQLiteHelper extends SQLiteOpenHelper {
	/**
	 * 默认db，解决在onCreate，onUpgrade中执行其他操作数据库操作时出现的异常(java.lang.IllegalStateException: getDatabase called recursively)
	 */
	private SQLiteDatabase mDefaultSQLiteDatabase = null;
	/**
	 * 数据库配置
	 */
	private SQLiteDBConfig mConfig;
	
	public SQLiteHelper(SQLiteDBConfig config) {
		super(new SQLiteContext(config.getContext(), config), config.getDbName(), null, config.getVersion());
		this.mConfig = config;
	}
	
	@Override
	public SQLiteDatabase getWritableDatabase() {
		if(mDefaultSQLiteDatabase != null) {
			return mDefaultSQLiteDatabase;
		}
		return super.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.mDefaultSQLiteDatabase = db;
		if(mConfig.getDbListener() != null) {
			mConfig.getDbListener().onDbCreateHandler(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		this.mDefaultSQLiteDatabase = db;
		if(mConfig.getDbListener() != null) {
			mConfig.getDbListener().onUpgradeHandler(db, oldVersion, newVersion);
		}
	}

}
