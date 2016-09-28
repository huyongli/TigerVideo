package cn.ittiger.video.fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ittiger.video.R;
import cn.ittiger.video.adapter.VideoAdapter;
import cn.ittiger.video.bean.VideoData;
import cn.ittiger.video.http.DataType;
import cn.ittiger.video.factory.ResultParseFactory;
import cn.ittiger.video.player.VideoPlayerHelper;
import cn.ittiger.video.ui.recycler.CommonRecyclerView;
import cn.ittiger.video.ui.recycler.SpacesItemDecoration;
import cn.ittiger.video.util.CallbackHandler;
import cn.ittiger.video.util.UIUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * @author laohu
 * @site http://ittiger.cn
 */
public abstract class VideoFragment extends BaseFragment implements
        CommonRecyclerView.LoadMoreListener {
    @BindView(R.id.video_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.video_recycler_view)
    CommonRecyclerView mRecyclerView;
    @BindView(R.id.small_video_player_container)
    RelativeLayout mSmallVideoPlayerContainer;

    private View mFooterView;
    private VideoAdapter mVideoAdapter;
    private int mCurPage = 1;
    private boolean mIsFirstLoad = true;

    @Override
    public View getContentView(LayoutInflater inflater, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_video, null);
        ButterKnife.bind(this, view);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.d_10)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setOnLoadMoreListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshData();
            }
        });
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int curPlayPosition = VideoPlayerHelper.getInstance().getCurrPlayPosition();
                int lastPlayPosition = VideoPlayerHelper.getInstance().getLastPlayPosition();
                if (curPlayPosition != -1 && (curPlayPosition < mRecyclerView.getFirstVisiblePosition() ||
                        curPlayPosition > mRecyclerView.getLastVisiblePosition())) {
                    VideoPlayerHelper.getInstance().smallWindowPlay();//移除屏幕之后进入小窗口播放
                } else if (curPlayPosition == -1 && lastPlayPosition >= mRecyclerView.getFirstVisiblePosition()
                        && lastPlayPosition <= mRecyclerView.getLastVisiblePosition()) {
                    VideoPlayerHelper.getInstance().smallWindowToListPlay();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return view;
    }

    @OnClick(R.id.iv_video_close)
    public void onClickCloseVideo(View view) {

        VideoPlayerHelper.getInstance().stop();
    }

    @Override
    public void refreshData() {

        if(mIsFirstLoad) {
            VideoPlayerHelper.init(getContext(), mSmallVideoPlayerContainer);
            mIsFirstLoad = false;
        }
        mCurPage = 1;

        queryVideo(mCurPage, new CallbackHandler<List<VideoData>>() {
            @Override
            public void callback(List<VideoData> videos) {

                if (videos == null || videos.size() == 0) {
                    refreshFailed();
                    return;
                }
                if (mVideoAdapter == null) {
                    mVideoAdapter = new VideoAdapter(mContext, videos);
                    mVideoAdapter.enableFooterView();
                    mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_layout, mRecyclerView, false);
                    mVideoAdapter.addFooterView(mFooterView);
                    mRecyclerView.setAdapter(mVideoAdapter);
                } else {
                    mVideoAdapter.addAll(videos, 0);
                }
                mCurPage++;
                refreshSuccess();
            }
        });
    }

    @Override
    public void onLoadMore() {

        if(mFooterView.getVisibility() == View.GONE) {
            mFooterView.setVisibility(View.VISIBLE);
        }
        queryVideo(mCurPage, new CallbackHandler<List<VideoData>>() {
            @Override
            public void callback(List<VideoData> videos) {

                if (videos == null || videos.size() == 0) {
                    mFooterView.setVisibility(View.GONE);
                    UIUtil.showToast(mContext, mContext.getString(R.string.no_more_videos));
                    return;
                }
                mVideoAdapter.addAll(videos);
                if (videos.size() > 0) {
                    mCurPage++;
                }
            }
        });
    }

    private void queryVideo(int pageNum, final CallbackHandler<List<VideoData>> callback) {

        Call<String> call = getHttpCall(pageNum);
        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(Call<String> call, final Response<String> response) {

                if (response.isSuccessful()) {
                    queryVideoHandler(ResultParseFactory.parse(response.body(), getType()), callback);
                } else {
                    onFailure(call, new NullPointerException("not query videos"));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                queryVideoHandler(null, callback);
            }
        });
    }

    private void queryVideoHandler(final List<VideoData> videos, final CallbackHandler<List<VideoData>> callback) {

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (callback != null) {
                    callback.callback(videos);
                }
            }
        });
    }

    public abstract Call<String> getHttpCall(int curPage);

    public abstract DataType getType();

    @Override
    public void refreshFailed() {

        super.refreshFailed();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void refreshSuccess() {

        super.refreshSuccess();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {

        VideoPlayerHelper.getInstance().stop();
        super.onDestroyView();
        mVideoAdapter = null;
        mCurPage = 1;
    }
}
