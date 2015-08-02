package com.dexafree.materialList.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.events.BusProvider;
import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MaterialListAdapter extends RecyclerView.Adapter<MaterialListAdapter.ViewHolder> implements IMaterialListAdapter {
	private final List<Card> mCardList = new ArrayList<>();
    private final List<HeaderCardItem> mHeaderList = new ArrayList<>();


	public static class ViewHolder<T extends Card> extends RecyclerView.ViewHolder {
		private final CardItemView<T> view;

		public ViewHolder(View v) {
			super(v);
			view = (CardItemView<T>) v;
		}

		public void build(T card) {
			view.build(card);
		}

    }

	@Override
	public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.build(mCardList.get(position));
	}

	public ViewHolder onCreateHeaderViewHolder(ViewGroup parent, final int position) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(getHeaderItem(position).getCard().getLayout(), parent, false);
        return new ViewHolder(view);
	}

	public void onBindHeaderViewHolder(ViewHolder holder, int position) {
        holder.build(getHeaderItem(position).getCard());
	}


	@Override
	public int getItemCount() {
		return mCardList.size();
	}

	@Override
	public int getItemViewType(final int position) {
		return mCardList.get(position).getLayout();
	}


    /*sticky header*/
    public HeaderCardItem getHeaderItem(int position){
        int size = mHeaderList.size();
        if(size == 0) return null;
        for(int i = 0; i < size; i++){
            if(position < mHeaderList.get(i).getPosition()){
                if(i <= 0)  return null;
                else        return mHeaderList.get(i-1);
            }
        }
        return mHeaderList.get(size - 1);
    }

    public long getHeaderId(int position) {
        if(getHeaderItem(position) == null) return -1;
        return getHeaderItem(position).getPosition();
    }

    public Card getHeaderCard(int position) {
        if(getHeaderItem(position) == null) return null;
        return getHeaderItem(position).getCard();
    }

    public void addHeader(Card card){
        mHeaderList.add(new HeaderCardItem(mCardList.size(), card));
//        mHeaderList.put(mCardList.size(), card);
//        mHeaderList.add(card);
    }
    /*sticky header*/


	/*multi selection*/
	public boolean setSelect(int position){
		boolean s = mCardList.get(position).setSelectingToggle();
		notifyItemChanged(position);
		return s;
	}

	public boolean isSelectable(int position){
		if(position >= mCardList.size())
			return false;
		return mCardList.get(position).isSelectable();
	}

	/*multi selection*/


    public void addAtStart(Card card){
		mCardList.add(0, card);
		notifyItemChanged(0);
	}

	public void add(int position, Card card){
		if(position >= mCardList.size())
			return;
		mCardList.add(position, card);
		notifyItemChanged(position);
	}

	public void add(Card card) {
		mCardList.add(card);
		notifyItemChanged(mCardList.size()-1);
//		notifyDataSetChanged();
	}

	public void addAll(Card... cards) {
        addAll(Arrays.asList(cards));
	}

	public void addAll(Collection<Card> cards) {
		for (Card card : cards) {
			add(card);
		}
	}


	public void remove(Card card, boolean withAnimation) {
		if (card.isDismissible()) {
			if (withAnimation) {
				BusProvider.dismiss(card);
			} else {
				mCardList.remove(card);
				notifyDataSetChanged();
			}
		}
	}

    public void clear() {
		mHeaderList.clear();
		mCardList.clear();
		notifyDataSetChanged();
    }

	public boolean isEmpty() {
		return mCardList.isEmpty();
	}

	public Card getCard(int position) {
		return mCardList.get(position);
	}

	public int getPosition(Card card) {
		return mCardList.indexOf(card);
	}

    class HeaderCardItem{
        private int position;
        private Card card;

        public HeaderCardItem(int position, Card card) {
            this.position = position;
            this.card = card;
        }

        public int getPosition() {
            return position;
        }

        public Card getCard() {
            return card;
        }
    }
}
