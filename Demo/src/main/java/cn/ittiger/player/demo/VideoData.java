package cn.ittiger.player.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: ylhu
 * @time: 17-9-18
 */

public class VideoData {
    private static List<VideoBean> sVideoList;
    static {
        String[] videoUrls = AppContext.getInstance().getResources().getStringArray(R.array.video_url);
        String[] videoCovers = AppContext.getInstance().getResources().getStringArray(R.array.video_cover);
        String[] videoTitles = AppContext.getInstance().getResources().getStringArray(R.array.video_title);
        sVideoList = new ArrayList<>();
        for(int i = 0; i < videoUrls.length; i++) {
            sVideoList.add(new VideoBean(videoUrls[i], videoCovers[i], videoTitles[i]));
        }
        for(int i = 0; i < videoUrls.length; i++) {
            sVideoList.add(new VideoBean(videoUrls[i], videoCovers[i], videoTitles[i]));
        }
        for(int i = 0; i < videoUrls.length; i++) {
            sVideoList.add(new VideoBean(videoUrls[i], videoCovers[i], videoTitles[i]));
        }
        for(int i = 0; i < videoUrls.length; i++) {
            sVideoList.add(new VideoBean(videoUrls[i], videoCovers[i], videoTitles[i]));
        }
    }

    public static List<VideoBean> getVideoList() {

        return sVideoList;
    }

    public static VideoBean getVideo() {

        return sVideoList.get(0);
    }
}
