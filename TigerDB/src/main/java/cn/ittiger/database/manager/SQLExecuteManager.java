package cn.ittiger.database.manager;

import cn.ittiger.database.bean.BindSQL;
import cn.ittiger.database.log.DBLog;
import cn.ittiger.database.util.DateUtil;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.io.Serializable;
import java.util.Date;

/**
 * SQL语句执行器
 * Author: hyl
 * Time: 2015-8-14下午10:26:54
 */
public class SQLExecuteManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * SQLite中的关键字 
	 */
	public static final String[] SQLITE_KEYWORDS = { "ABORT", "ACTION",
		"ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC",
		"ATTACH", "AUTOINCREMENT", "BEFORE", "BEGIN", "BETWEEN", "BY",
		"CASCADE", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "COMMIT",
		"CONFLICT", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_DATE",
		"CURRENT_TIME", "CURRENT_TIMESTAMP", "DATABASE", "DEFAULT",
		"DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DETACH", "DISTINCT",
		"DROP", "EACH", "ELSE", "END", "ESCAPE", "EXCEPT", "EXCLUSIVE",
		"EXISTS", "EXPLAIN", "FAIL", "FOR", "FOREIGN", "FROM", "FULL",
		"GLOB", "GROUP", "HAVING", "IF", "IGNORE", "IMMEDIATE", "IN",
		"INDEX", "INDEXED", "INITIALLY", "INNER", "INSERT", "INSTEAD",
		"INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "KEY", "LEFT", "LIKE",
		"LIMIT", "MATCH", "NATURAL", "NO", "NOT", "NOTNULL", "NULL", "OF",
		"OFFSET", "ON", "OR", "ORDER", "OUTER", "PLAN", "PRAGMA",
		"PRIMARY", "QUERY", "RAISE", "REFERENCES", "REGEXP", "REINDEX",
		"RELEASE", "RENAME", "REPLACE", "RESTRICT", "RIGHT", "ROLLBACK",
		"ROW", "SAVEPOINT", "SELECT", "SET", "TABLE", "TEMP", "TEMPORARY",
		"THEN", "TO", "TRANSACTION", "TRIGGER", "UNION", "UNIQUE",
		"UPDATE", "USING", "VACUUM", "VALUES", "VIEW", "VIRTUAL", "WHEN",
		"WHERE" };
	
	/**
	 * 数据库操作类
	 */
	private SQLiteDatabase mSQLiteDataBase;

	public SQLExecuteManager(SQLiteDatabase mSQLiteDataBase) {
		super();
		this.mSQLiteDataBase = mSQLiteDataBase;
	}
	
	/**
	 * 开启一个事务(事务开始)
	 * 在事务代码执行完成后，必须要执行successTransaction()将事务标记为成功
	 * 在代码的最后必须要执行endTransaction()来结束当前事务，如果事务成功则提交事务，否则回滚事务
	 * @author: huylee
	 * @time:	2015-8-20下午9:18:06
	 */
	public void beginTransaction() {
		this.mSQLiteDataBase.beginTransaction();
	}
	
	/**
	 * 标记当前事务成功
	 * @author: huylee
	 * @time:	2015-8-20下午9:21:08
	 */
	public void successTransaction() {
		this.mSQLiteDataBase.setTransactionSuccessful();
	}
	
	/**
	 * 结束当前事务，当事物被标记成功后，此操作会提交事务，否则会回滚事务
	 * @author: huylee
	 * @time:	2015-8-20下午9:21:25
	 */
	public void endTransaction() {
		this.mSQLiteDataBase.endTransaction();
	}

	/**
	 * 执行指定无返回值的单条SQL语句，如建表、创建数据库等
	 * Author: hyl
	 * Time: 2015-8-14下午11:05:03
	 * @param sql
	 */
	public void execSQL(String sql) {
		DBLog.debug(sql);
		this.mSQLiteDataBase.execSQL(sql);
	}
	
	/**
	 * 插入一条记录，返回该记录的rowId
	 * Author: hyl
	 * Time: 2015-8-17上午11:55:55
	 * @param sql
	 * @param args
	 * @return				插入失败返回-1，成功返回rowId
	 */
	public long insert(String sql, Object[] args) {
		long rowId = -1;
		SQLiteStatement statement = this.mSQLiteDataBase.compileStatement(sql);
		try {
			if(args != null) {
				for(int i = 0; i < args.length; i++) {
					bindArgs(statement, i + 1, args[i]);
				}
			}
			rowId = statement.executeInsert();
			DBLog.debug(sql, args);
		} finally {
			statement.close();
		}
		return rowId;
	}
	
	/**
	 * 根据BindSQL进行插入数据
	 * Author: hyl
	 * Time: 2015-8-17下午9:55:36
	 * @param bindSQL
	 * @return
	 * @throws Exception
	 */
	public long insert(BindSQL bindSQL) {
		return insert(bindSQL.getSql(), bindSQL.getBindArgs());
	}
	
	/**
	 * 绑定参数
	 * Author: hyl
	 * Time: 2015-8-17上午10:42:43
	 * @param statement
	 * @param position
	 * @param args
	 */
	private void bindArgs(SQLiteStatement statement, int position, Object args) {
		int type = FieldTypeManager.getValueType(args);
		switch(type) {
			case FieldTypeManager.VALUE_TYPE_NULL:
				statement.bindNull(position);
				break;
			case FieldTypeManager.BASE_TYPE_BYTE_ARRAY:
				statement.bindBlob(position, (byte[])args);
				break;
			case FieldTypeManager.BASE_TYPE_CHAR:
			case FieldTypeManager.BASE_TYPE_STRING:
				statement.bindString(position, args.toString());
				break;
			case FieldTypeManager.BASE_TYPE_DATE:
				statement.bindString(position, DateUtil.formatDatetime((Date) args));
				break;
			case FieldTypeManager.BASE_TYPE_DOUBLE:
			case FieldTypeManager.BASE_TYPE_FLOAT:
				statement.bindDouble(position, Double.parseDouble(args.toString()));
				break;
			case FieldTypeManager.BASE_TYPE_INT:
			case FieldTypeManager.BASE_TYPE_LONG:
			case FieldTypeManager.BASE_TYPE_SHORT:
				statement.bindLong(position, Long.parseLong(args.toString()));
				break;
			case FieldTypeManager.NOT_BASE_TYPE:
				throw new IllegalArgumentException("未知参数类型，请检查绑定参数");
		}
	}
	
	/**
	 * 删除指定表
	 * Author: hyl
	 * Time: 2015-8-16下午8:17:39
	 * @param tableName
	 * @throws Exception 
	 */
	public void dropTable(String tableName) {
		String sql = "DROP TABLE IF EXISTS " + tableName;
		execSQL(sql);
	}
	
	/**
	 * 删除,表名不能使用占位符
	 * @author: huylee
	 * @time:	2015-8-20下午10:21:18
	 * @param bindSQL	
	 */
	public void delete(BindSQL bindSQL) {
		updateOrDelete(bindSQL.getSql(), bindSQL.getBindArgs());
	}
	
	/**
	 * 删除,表名不能使用占位符
	 * @author: huylee
	 * @time:	2015-8-20下午10:18:01
	 * @param sql		删除语句(参数使用占位符)
	 * @param args		占位符参数
	 */
	@SuppressLint("NewApi")
	public void updateOrDelete(String sql, Object[] args) {
		SQLiteStatement statement = mSQLiteDataBase.compileStatement(sql);
		try {
			if(args != null) {
				for(int i = 0; i < args.length; i++) {
					bindArgs(statement, i + 1, args[i]);
				}
			}
			DBLog.debug(sql, args);
			statement.executeUpdateDelete();
		} finally {
			statement.close();
		}
	}
	
	/**
	 * 删除(对于表名需要动态获取的，此方法非常适合)
	 * @author: huylee
	 * @time:	2015-8-20下午10:22:32
	 * @param tableName			要删除的数据表
	 * @param whereClause		where后面的条件句(delete from XXX where XXX)，参数使用占位符
	 * @param whereArgs			where子句后面的占位符参数
	 */
	public void delete(String tableName, String whereClause, String[] whereArgs) {
		DBLog.debug("{SQL：DELETE FROM " + tableName + " WHERE " + whereClause + "，PARAMS：" + whereArgs + "}");
		mSQLiteDataBase.delete(tableName, whereClause, whereArgs);
	}
	
	/**
	 * 更新
	 * @author: huylee
	 * @time:	2015-8-20下午10:47:16
	 * @param bindSQL
	 */
	public void update(BindSQL bindSQL) {
		updateOrDelete(bindSQL.getSql(), bindSQL.getBindArgs());
	}
	
	/**
	 * 根据SQL进行查询
	 * Author: hyl
	 * Time: 2015-8-17下午9:56:58
	 * @param sql
	 * @return
	 */
	public Cursor query(String sql) {
		return query(sql, null);
	}
	
	/**
	 * 执行绑定语句
	 * Author: hyl
	 * Time: 2015-8-16下午8:26:34
	 * @param sql
	 * @param whereArgs
	 * @return
	 */
	public Cursor query(String sql, String[] whereArgs) {
		DBLog.debug("{SQL：" + sql + "，PARAMS：" + whereArgs + "}");
		return this.mSQLiteDataBase.rawQuery(sql, whereArgs); 
	}

	/**
	 * 根据BindSQL查询
	 * Author: hyl
	 * Time: 2015-8-21上午10:55:46
	 * @param bindSQL
	 * @return
	 */
	public Cursor query(BindSQL bindSQL) {
		return query(bindSQL.getSql(), (String[])bindSQL.getBindArgs());
	}
}
