package cn.ittiger.video.bean;

import cn.ittiger.database.annotation.PrimaryKey;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public class VideoTabData {

    @PrimaryKey(isAutoGenerate = true)
    private long id;
    private int type;
    private int tabId;
    private String tabName;

    public VideoTabData() {

    }

    public VideoTabData(int tabId, String tabName, int type) {

        this.tabId = tabId;
        this.tabName = tabName;
        this.type = type;
    }

    public int getTabId() {

        return tabId;
    }

    public void setTabId(int tabId) {

        this.tabId = tabId;
    }

    public String getTabName() {

        return tabName;
    }

    public void setTabName(String tabName) {

        this.tabName = tabName;
    }

    public int getType() {

        return type;
    }

    public void setType(int type) {

        this.type = type;
    }
}
