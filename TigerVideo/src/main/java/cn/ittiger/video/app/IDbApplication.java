package cn.ittiger.video.app;

import cn.ittiger.database.SQLiteDBConfig;

/**
 * 使用本地数据库必须实现此接口，只有在用到此数据库开始创建连接数据库时，相关的配置获取方法才会调用
 * @auther: laohu
 */
public interface IDbApplication {
	/**
	 * 系统全局数据库配置
	 * @return
	 */
	SQLiteDBConfig getGlobalDbConfig();
}
