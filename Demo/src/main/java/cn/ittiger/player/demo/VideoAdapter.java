package cn.ittiger.player.demo;

import com.bumptech.glide.Glide;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.ittiger.player.VideoPlayerView;

import java.util.List;

/**
 * @author: ylhu
 * @time: 17-9-18
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private Context mContext;
    private List<VideoBean> mVideoList;
    private int mScreenWidth;

    public VideoAdapter(Context context) {

        mContext = context;
        mVideoList = VideoData.getVideoList();
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item_view, parent, false);
        view.getLayoutParams().width = mScreenWidth;
        view.getLayoutParams().height = (int) (mScreenWidth * 1.0f / 16 * 9 + 0.5f);

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

        VideoBean video = mVideoList.get(position);
        holder.mPlayerView.bind(video.getVideoUrl(), video.getVideoTitle());
        holder.mPlayerView.getThumbImageView().setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(mContext).load(video.getVideoThumbUrl()).into(holder.mPlayerView.getThumbImageView());
    }

    @Override
    public int getItemCount() {

        return mVideoList.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoPlayerView mPlayerView;

        public VideoViewHolder(View itemView) {

            super(itemView);
            mPlayerView = (VideoPlayerView)itemView.findViewById(R.id.video_player_view);
        }
    }
}
