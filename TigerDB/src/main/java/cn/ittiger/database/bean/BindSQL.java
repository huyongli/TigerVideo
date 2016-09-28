package cn.ittiger.database.bean;

/**
 * Bind SQL信息，包括执行SQL语句以及执行参数
 * Author: hyl
 * Time: 2015-8-16下午9:11:35
 */
public class BindSQL {
	/**
	 * 执行语句(参数使用占位符)，表名不能使用占位符
	 */
	private String sql;
	/**
	 * 占位符绑定参数
	 */
	private Object[] bindArgs;
	
	public BindSQL() {
	}
	
	public BindSQL(String sql) {
		super();
		this.setSql(sql);
	}

	public BindSQL(String sql, Object[] bindArgs) {
		super();
		this.setSql(sql);
		this.setBindArgs(bindArgs);
	}

	public Object[] getBindArgs() {
		return bindArgs;
	}

	public void setBindArgs(Object[] bindArgs) {
		this.bindArgs = bindArgs;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	
	
}
