package cn.ittiger.video.bean;

import cn.ittiger.database.annotation.PrimaryKey;
import cn.ittiger.database.annotation.Table;

/**
 * @author: laohu on 2016/8/24
 * @site: http://ittiger.cn
 */
@Table(name = "VideoTable")
public class VideoData {


    private String id;//视频id
    private String duration;//视频时长
    private String title;//视频标题
    private String imageUrl;//视频预览图地址
    private String videoUrl;//视频播放地址
    private int mDataType;//视频数据类型

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getDuration() {

        return duration;
    }

    public void setDuration(String duration) {

        this.duration = duration;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getImageUrl() {

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {

        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {

        this.videoUrl = videoUrl;
    }

    public int getDataType() {

        return mDataType;
    }

    public void setDataType(int dataType) {

        mDataType = dataType;
    }
}
