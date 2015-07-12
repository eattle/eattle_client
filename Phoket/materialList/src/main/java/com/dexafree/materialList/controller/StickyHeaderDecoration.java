package com.dexafree.materialList.controller;


import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * A sticky header decoration for android's RecyclerView.
 */
public class StickyHeaderDecoration extends RecyclerView.ItemDecoration {

    private Map<Long, RecyclerView.ViewHolder> mHeaderCache;

    private MaterialListAdapter mAdapter;

    /**
     * @param adapter the sticky header adapter to use
     */
    public StickyHeaderDecoration(RecyclerView.Adapter adapter) {
        mAdapter = (MaterialListAdapter)adapter;
        mHeaderCache = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        int headerHeight = 0;
        if (position != RecyclerView.NO_POSITION && hasHeader(position)) {
            View header = getHeader(parent, position).itemView;
            headerHeight = header.getHeight();
        }

        outRect.set(0, headerHeight, 0, 0);
    }


    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    public void clearHeaderCache() {
        mHeaderCache.clear();
    }

    private boolean hasHeader(int position) {
        long headId = mAdapter.getHeaderId(position);
        if(headId == -1){
            return false;
        }

        int previous = position - 1;
        return headId != mAdapter.getHeaderId(previous);
    }

    private RecyclerView.ViewHolder getHeader(RecyclerView parent, int position) {
        final long key = mAdapter.getHeaderId(position);

        if (mHeaderCache.containsKey(key)) {
            return mHeaderCache.get(key);
        } else {
            final MaterialListAdapter.ViewHolder holder;
            if(mAdapter.getHeaderItem(position) == null){
                holder = mAdapter.onCreateViewHolder(parent, mAdapter.getItemViewType(position));
                mAdapter.onBindViewHolder(holder, position);

            } else {
                holder = mAdapter.onCreateHeaderViewHolder(parent, position);
                mAdapter.onBindHeaderViewHolder(holder, position);
            }
            final View header = holder.itemView;

            //noinspection unchecked
//            mAdapter.remove(position);


            int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);

            int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.getPaddingLeft() + parent.getPaddingRight(), header.getLayoutParams().width);
            int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.getPaddingTop() + parent.getPaddingBottom(), header.getLayoutParams().height);

            header.measure(childWidth, childHeight);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());

            mHeaderCache.put(key, holder);

            return holder;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int count = parent.getChildCount();

        for (int layoutPos = 0; layoutPos < count; layoutPos++) {
            final View child = parent.getChildAt(layoutPos);

            final int adapterPos = parent.getChildAdapterPosition(child);

            if (adapterPos != RecyclerView.NO_POSITION && (layoutPos == 0 || hasHeader(adapterPos))) {
                View header = getHeader(parent, adapterPos).itemView;
                c.save();
                final int left = child.getLeft();
                final int top = getHeaderTop(parent, child, header, adapterPos, layoutPos);
                c.translate(left, top);
                header.draw(c);
                c.restore();
            }
        }
    }

    private int getHeaderTop(RecyclerView parent, View child, View header, int adapterPos, int layoutPos) {
        int top = getAnimatedTop(child) - header.getHeight();
        if (layoutPos == 0) {
            final int count = parent.getChildCount();
            final long currentId = mAdapter.getHeaderId(adapterPos);
            // find next view with header and compute the offscreen push if needed
            for (int i = 1; i < count; i++) {
                int adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(i));
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    long nextId = mAdapter.getHeaderId(adapterPosHere);
                    if (nextId != currentId) {
                        final View next = parent.getChildAt(i);
                        final int offset = getAnimatedTop(next) - (header.getHeight() + getHeader(parent, i).itemView.getHeight());
                        if (offset < 0) {
                            return offset;
                        } else {
                            break;
                        }
                    }
                }
            }

            top = Math.max(0, top);
        }

        return top;
    }

    private int getAnimatedTop(View child) {
        return child.getTop() + (int)child.getTranslationY();
    }
}
