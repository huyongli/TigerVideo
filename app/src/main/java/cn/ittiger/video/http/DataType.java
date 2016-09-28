package cn.ittiger.video.http;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public enum DataType {

    NET_EASY(1),
    WU5LI(2),
    TTKB(3),
    IFENG(4);

    int mValue;

    DataType(int value) {

        mValue = value;
    }

    public int value() {

        return mValue;
    }
}
