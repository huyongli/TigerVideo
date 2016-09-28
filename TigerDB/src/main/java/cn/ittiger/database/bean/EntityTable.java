package cn.ittiger.database.bean;

import java.util.LinkedHashMap;

/**
 * 实体类信息
 * @author: huylee
 * @time:	2015-8-13下午10:48:41
 */
public final class EntityTable {
	/**
	 * 实体类Class对象
	 */
	private Class<?> mClass;
	/**
	 * 实体类对应的表名
	 */
	private String mTableName;
	/**
	 * 实体类对应的表主键
	 */
	private PrimaryKey mPrimaryKey;
	/**
	 * 实体类对应的表字段集合<column(字段名), {@link Property}>，不包括主键
	 */
	private LinkedHashMap<String, Property> mColumnMap;
	
	public EntityTable(Class<?> mClass) {
		super();
		this.mClass = mClass;
		mColumnMap = new LinkedHashMap<String, Property>();
	}

	public String getTableName() {
		return mTableName;
	}

	public void setTableName(String mTableName) {
		this.mTableName = mTableName;
	}

	public LinkedHashMap<String, Property> getColumnMap() {
		return mColumnMap;
	}

	public PrimaryKey getPrimaryKey() {
		return mPrimaryKey;
	}

	public void setPrimaryKey(PrimaryKey mPrimaryKey) {
		this.mPrimaryKey = mPrimaryKey;
	}

	public Class<?> getEntityClass() {
		return mClass;
	}
}
