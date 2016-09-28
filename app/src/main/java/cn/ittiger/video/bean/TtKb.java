package cn.ittiger.video.bean;

import cn.ittiger.database.annotation.PrimaryKey;
import cn.ittiger.database.annotation.Table;
import cn.ittiger.video.util.DBManager;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
@Table(name = "TTKB")
public class TtKb {
    @PrimaryKey(isAutoGenerate = true)
    private long id;
    private String newkey;
    private String endkey;
    private int isRefresh;//1:refresh,0:loadMore

    public TtKb() {

    }

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getNewkey() {

        return newkey;
    }

    public void setNewkey(String newkey) {

        this.newkey = newkey;
    }

    public String getEndkey() {

        return endkey;
    }

    public void setEndkey(String endkey) {

        this.endkey = endkey;
    }

    public int getIsRefresh() {

        return isRefresh;
    }

    public void setIsRefresh(int isRefresh) {

        this.isRefresh = isRefresh;
    }

    public static void save(String newkey, String endkey, int isRefresh) {

        TtKb ttKb = DBManager.getInstance().getSQLiteDB().queryOne(TtKb.class, "isRefresh=?", new String[]{String.valueOf(isRefresh)});
        if(ttKb == null) {
            ttKb = new TtKb();
            ttKb.setEndkey(endkey);
            ttKb.setNewkey(newkey);
            ttKb.setIsRefresh(isRefresh);
            DBManager.getInstance().getSQLiteDB().save(ttKb);
            if(isRefresh == 1) {
                ttKb.setIsRefresh(0);
                DBManager.getInstance().getSQLiteDB().save(ttKb);
            }
        } else {
            ttKb.setEndkey(endkey);
            ttKb.setNewkey(newkey);
            ttKb.setIsRefresh(isRefresh);
            DBManager.getInstance().getSQLiteDB().update(ttKb);
        }
    }
}
