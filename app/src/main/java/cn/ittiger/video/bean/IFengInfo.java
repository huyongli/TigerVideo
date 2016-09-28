package cn.ittiger.video.bean;

import cn.ittiger.database.annotation.PrimaryKey;
import cn.ittiger.video.util.DBManager;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class IFengInfo {
    @PrimaryKey
    private int tabId;
    private String itemId;

    public IFengInfo() {

    }

    public IFengInfo(int tabId, String itemId) {

        this.tabId = tabId;
        this.itemId = itemId;
    }

    public int getTabId() {

        return tabId;
    }

    public void setTabId(int tabId) {

        this.tabId = tabId;
    }

    public String getItemId() {

        return itemId;
    }

    public void setItemId(String itemId) {

        this.itemId = itemId;
    }

    public static void save(IFengInfo iFengInfo) {

        boolean isExist = DBManager.getInstance().getSQLiteDB().queryIfExist(IFengInfo.class, String.valueOf(iFengInfo.getTabId()));
        if(isExist) {
            DBManager.getInstance().getSQLiteDB().update(iFengInfo);
        } else {
            DBManager.getInstance().getSQLiteDB().save(iFengInfo);
        }
    }
}
