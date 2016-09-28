package cn.ittiger.video.util;

import cn.ittiger.database.SQLiteDB;
import cn.ittiger.database.SQLiteDBConfig;
import cn.ittiger.database.SQLiteDBFactory;
import cn.ittiger.video.app.IDbApplication;

/**
 * 本地数据库管理类
 * @author laohu
 */
public class DBManager {
    /**
     * 管理器单例
     */
    private static DBManager sDBInstance;
    /**
     * 数据库配置上下文
     */
    private IDbApplication mDbApplication;
    /**
     * 全局数据库
     */
    private SQLiteDB mDB;


    private DBManager() {

        mDbApplication = ApplicationHelper.getInstance().getDbApplication();
    }

    public static DBManager getInstance() {

        if(sDBInstance == null) {
            synchronized (DBManager.class) {
                if(sDBInstance == null) {
                    sDBInstance = new DBManager();
                }
            }
        }
        return  sDBInstance;
    }

    /**
     * 获取全局数据库操作对象
     * @return
     */
    public SQLiteDB getSQLiteDB() {

        if(mDB == null) {
            synchronized (this) {
                if(mDB == null) {
                    SQLiteDBConfig config = mDbApplication.getGlobalDbConfig();
                    mDB = SQLiteDBFactory.createSQLiteDB(config);
                }
            }
        }
        return mDB;
    }

    /**
     * 关闭数据库
     */
    public void closeSQLiteDB() {
        if(this.mDB != null) {
            this.mDB.close();
        }
        this.mDB = null;
    }
}
