package cn.ittiger.video.ui.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author laohu
 */
public abstract class HeaderAndFooterAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private static final int TYPE_HEADER = 100000;
    private static final int TYPE_FOOTER = 200000;

    protected List<T> mList;
    private SparseArray<View> mHeaderViews = new SparseArray<>(0);
    private SparseArray<View> mFooterViews = new SparseArray<>(0);

    private boolean mIsHeaderViewEnable = false;
    private boolean mIsFooterViewEnable = false;

    public HeaderAndFooterAdapter(List<T> list) {

        mList = list;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(isHeaderViewEnable() && mHeaderViews.get(viewType) != null) {
            return new ViewHolder(mHeaderViews.get(viewType));
        } else if(isFooterViewEnable() && mFooterViews.get(viewType) != null) {
            return new ViewHolder(mFooterViews.get(viewType));
        }
        return onCreateItemViewHolder(parent, viewType);
    }

    public abstract ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindItemViewHolder(ViewHolder holder, int position, T item);

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {

        if(isFooterView(position) || isHeaderView(position)) {
            return;
        }
        T item = getItem(position - getHeaderViewCount());
        onBindItemViewHolder(holder, position, item);
    }

    @Override
    public final int getItemViewType(int position) {

        if(isHeaderView(position)) {//FooterView
            return mHeaderViews.keyAt(position);
        }
        if(isFooterView(position)){//HeaderView
            return mFooterViews.keyAt(position - getHeaderViewCount() - getItemDataCount());
        }
        return getItemViewTypeForData(position);
    }

    /**
     * 展示的总数据数(包括HeaderView和FooterView)
     *
     * @return
     */
    @Override
    public final int getItemCount() {

        return getItemDataCount() + getHeaderViewCount() + getFooterViewCount();
    }

    /**
     * 要展示的有效数据数(不包括HeaderView和FooterView)
     *
     * @return
     */
    public int getItemDataCount() {

        return mList == null ? 0 : mList.size();
    }

    /**
     * 获取position位置的数据
     *
     * @param position
     * @return
     */
    public T getItem(int position) {

        return mList == null ? null : mList.get(position);
    }

    public void addAll(List<T> list) {

        int positionStart = getHeaderViewCount();
        if(mList == null) {
            mList = list;
        } else {
            positionStart += mList.size();
            mList.addAll(list);
        }
        notifyItemRangeInserted(positionStart, list.size());
    }

    public void addAll(List<T> list, int position) {

        int positionStart = getHeaderViewCount() + position;
        if(mList == null) {
            mList = list;
        } else {
            mList.addAll(positionStart, list);
        }
        notifyDataSetChanged();
    }

    public void add(T item) {

        if(mList == null) {
            mList = new ArrayList<>(1);
        }
        int size = getItemDataCount();
        mList.add(item);
        notifyItemInserted(size);
    }

    public void add(T item, int position) {

        if(mList == null) {
            mList = new ArrayList<>();
        }
        mList.add(position, item);
        notifyItemInserted(position);
    }

    public void update(T item , int position) {

        if(mList == null) {
            mList = new ArrayList<>();
        }
        mList.set(position, item);
        notifyItemChanged(position);
    }

    public void reset(List<T> list) {

        mList = list;
        notifyDataSetChanged();
    }

    /**
     * 获取待展示的position索引处数据的viewType
     *
     * @param position
     * @return
     */
    public int getItemViewTypeForData(int position) {

        return super.getItemViewType(position);
    }

    /**
     * 判断position位置是否为FooterView的索引
     *
     * @param position
     * @return
     */
    public boolean isFooterViewPosition(int position) {

        return position >= getItemDataCount() + getHeaderViewCount();
    }

    /**
     * 判断position位置是否为HeaderView的索引
     *
     * @param position
     * @return
     */
    public boolean isHeaderViewPosition(int position) {

        return position < getHeaderViewCount();
    }

    /**
     * 获取HeaderView的总数
     *
     * @return
     */
    public int getHeaderViewCount() {

        return isHeaderViewEnable() ? mHeaderViews.size() : 0;
    }

    /**
     * 获取FooterView的总数
     *
     * @return
     */
    public int getFooterViewCount() {

        return isFooterViewEnable() ? mFooterViews.size() : 0;
    }

    /**
     * HeaderView是否启用,默认不启用
     *
     * @return
     */
    public boolean isHeaderViewEnable() {

        return mIsHeaderViewEnable;
    }

    /**
     * 启用HeaderView
     */
    public void enableHeaderView() {

        mIsHeaderViewEnable = true;
    }

    /**
     * 禁用HeaderView
     */
    public void disableHeaderView() {

        mIsHeaderViewEnable = false;
    }

    /**
     * FooterView是否启用,默认不启用
     *
     * @return
     */
    public boolean isFooterViewEnable() {

        return mIsFooterViewEnable;
    }

    /**
     * 启用FooterView
     */
    public void enableFooterView() {

        mIsFooterViewEnable = true;
    }

    /**
     * 禁用FooterView
     */
    public void disableFooterView() {

        mIsFooterViewEnable = false;
    }

    /**
     * 判断position位置是否为FooterView
     *
     * @param position
     * @return
     */
    public boolean isFooterView(int position) {

        return isFooterViewEnable() && isFooterViewPosition(position);
    }

    /**
     * 判断position位置是否为HeaderView
     *
     * @param position
     * @return
     */
    public boolean isHeaderView(int position) {

        return isHeaderViewEnable() && isHeaderViewPosition(position);
    }

    /**
     * 添加一个HeaderView
     *
     * @param headerView
     */
    public void addHeaderView(View headerView) {

        if(headerView == null) {
            throw new NullPointerException("headerView is null");
        }
        mHeaderViews.put(TYPE_HEADER + getHeaderViewCount(), headerView);
        notifyItemInserted(getHeaderViewCount() - 1);
    }

    /**
     * 添加一个FooterView
     *
     * @param footerView
     */
    public void addFooterView(View footerView) {

        if(footerView == null) {
            throw new NullPointerException("footerView is null");
        }
        mFooterViews.put(TYPE_FOOTER + getFooterViewCount(), footerView);
        notifyItemInserted(getHeaderViewCount() + getItemDataCount() + getFooterViewCount() - 1);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    return getNewSpanSize(((GridLayoutManager) layoutManager).getSpanCount(), position);
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {

        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if(isHeaderView(position) || isFooterView(position)) {
            final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if(layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }

    private int getNewSpanSize(int spanCount, int position) {

        if(isHeaderView(position) || isFooterView(position)) {
            return spanCount;
        }

        return 1;
    }
}

