package cn.ittiger.video.ui.recycler;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * 通用RecyclerView，支持添加ItemClick和ItemLongClick事件，同时支持自动加载更多
 * Created by laohu on 16-7-21.
 */
public class CommonRecyclerView extends RecyclerView {

    private GestureDetector mGestureDetector;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private OnScrollListener mOnScrollListener;
    private LoadMoreListener mLoadMoreListener;
    private boolean mIsAutoLoadMore = true;//是否自动加载更多
    private HeaderAndFooterAdapter mHeaderAndFooterAdapter;
    private int mLastVisiblePosition = 0;

    public CommonRecyclerView(Context context) {

        this(context, null);
    }

    public CommonRecyclerView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public CommonRecyclerView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {

                super.onLongPress(e);
                if(mItemLongClickListener != null) {
                    View childView = findChildViewUnder(e.getX(), e.getY());
                    if(childView != null) {
                        int position = getChildLayoutPosition(childView);
                        if(!(mHeaderAndFooterAdapter.isHeaderViewPosition(position) ||
                            mHeaderAndFooterAdapter.isFooterViewPosition(position))) {
                            int headerViewCount = mHeaderAndFooterAdapter.getHeaderViewCount();
                            mItemLongClickListener.onItemLongClick(position - headerViewCount, childView);
                        }
                    }
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                if(mOnItemClickListener != null) {
                    View childView = findChildViewUnder(e.getX(),e.getY());
                    if(childView != null){
                        int position = getChildLayoutPosition(childView);
                        if(!(mHeaderAndFooterAdapter.isHeaderViewPosition(position) ||
                                mHeaderAndFooterAdapter.isFooterViewPosition(position))) {
                            int headerViewCount = mHeaderAndFooterAdapter.getHeaderViewCount();
                            mOnItemClickListener.onItemClick(position - headerViewCount, childView);
                        }
                        return true;
                    }
                }
                return super.onSingleTapUp(e);
            }
        });

        addOnItemTouchListener(new SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                if (mGestureDetector.onTouchEvent(e)) {//交由手势处理
                    return true;
                }
                return false;
            }
        });

        //设置加载更多处理
        super.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
                if(newState == SCROLL_STATE_IDLE && mIsAutoLoadMore && mLoadMoreListener != null && getAdapter() != null) {
                    if(mLastVisiblePosition + 1 == getAdapter().getItemCount()) {
                        mLoadMoreListener.onLoadMore();
                    }
                }
                if(mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                if(mIsAutoLoadMore && mLoadMoreListener != null) {
                    mLastVisiblePosition = getLastVisiblePosition();
                }
                if(mOnScrollListener != null) {
                    mOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }
        });
    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    public int getLastVisiblePosition() {

        int position;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else {
            position = getLayoutManager().getItemCount() - 1;
        }
        return position;
    }

    /**
     * 获取第一个显示的位置
     *
     * @return
     */
    public int getFirstVisiblePosition() {

        int position;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            position = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMinPosition(lastPositions);
        } else {
            position = 0;
        }
        return position;
    }

    /**
     * 获得最大的位置
     *
     * @param positions
     * @return
     */
    private int getMaxPosition(int[] positions) {

        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < positions.length; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    private int getMinPosition(int[] positions) {

        int minPosition = Integer.MAX_VALUE;
        for (int i = 0; i < positions.length; i++) {
            minPosition = Math.min(minPosition, positions[i]);
        }
        return minPosition;
    }

    /**
     * 设置是否允许自动加载更多，默认为true
     * 设置之后，还需要设置加载更多的监听
     *
     * @param autoLoadMore
     */
    public void setEnableAutoLoadMore(boolean autoLoadMore) {

        mIsAutoLoadMore = autoLoadMore;
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {

        this.mOnScrollListener = listener;
    }

    public void setAdapter(HeaderAndFooterAdapter adapter) {

        super.setAdapter(adapter);
        mHeaderAndFooterAdapter = adapter;
    }

    /**
     * 设置Item单击监听
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {

        this.mOnItemClickListener = itemClickListener;
    }

    /**
     * 设置Item长按监听
     *
     * @param itemLongClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {

        this.mItemLongClickListener = itemLongClickListener;
    }

    /**
     * 设置加载更多监听
     *
     * @param loadMoreListener
     */
    public void setOnLoadMoreListener(LoadMoreListener loadMoreListener) {

        mLoadMoreListener = loadMoreListener;
    }

    /**
     * Item项点击事件
     */
    public interface OnItemClickListener {

        void onItemClick(int position, View itemView);
    }

    /**
     * Item项长按点击事件
     */
    public interface OnItemLongClickListener {

        void onItemLongClick(int position, View itemView);
    }

    /**
     * 加载更多监听
     */
    public interface LoadMoreListener {

        /**
         * UI线程
         */
        void onLoadMore();
    }
}
