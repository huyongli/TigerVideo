package cn.ittiger.player.demo;

/**
 * @author: ylhu
 * @time: 17-9-18
 */

public class VideoBean {
    private String mVideoUrl;
    private String mVideoThumbUrl;
    private String mVideoTitle;

    public VideoBean(String videoUrl, String videoThumbUrl, String videoTitle) {

        mVideoUrl = videoUrl;
        mVideoThumbUrl = videoThumbUrl;
        mVideoTitle = videoTitle;
    }

    public String getVideoUrl() {

        return mVideoUrl;
    }

    public String getVideoThumbUrl() {

        return mVideoThumbUrl;
    }

    public String getVideoTitle() {

        return mVideoTitle;
    }
}
