package cn.ittiger.player.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.ittiger.player.FullScreenVideoPlayerView;
import cn.ittiger.player.PlayerManager;

/**
 * @author: ylhu
 * @time: 2017/12/5
 */

public class FullScreenActivity extends AppCompatActivity {
    FullScreenVideoPlayerView mVideoPlayerView;
    String mVideoUrl = "http://www.eywedu.com.cn/sanzijing/UploadFiles_2038/szj-01.mp4";
    String mVideoTitle = "三字经";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);
        mVideoPlayerView = (FullScreenVideoPlayerView) findViewById(R.id.video_player_view);
        mVideoPlayerView.bind(mVideoUrl, mVideoTitle);
        mVideoPlayerView.startPlayVideo();
    }

    @Override
    protected void onResume() {

        super.onResume();
        PlayerManager.getInstance().resume();
    }

    @Override
    protected void onPause() {

        super.onPause();
        PlayerManager.getInstance().pause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        PlayerManager.getInstance().release();
    }
}
