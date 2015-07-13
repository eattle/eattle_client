package com.dexafree.materialList.model;

/**
 * The Card is the base class of all Card Models.
 */
public abstract class Card {

    private Object tag;

    private boolean mDismissible;

    private boolean mSelectable = false;

    private boolean mSelecting;

    public boolean isSelecting() {
        return mSelecting;
    }

    public void setSelectingToggle() {
        this.mSelecting = !this.mSelecting;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean canSelect) {
        this.mSelectable = canSelect;
        this.mSelecting = false;
    }

    public boolean isDismissible() {
        return mDismissible;
    }

    public void setDismissible(boolean canDismiss) {
        this.mDismissible = canDismiss;
    }

    public abstract int getLayout();

    public Object getTag(){
        return tag;
    }

    public void setTag(Object tag){
        this.tag = tag;
    }
}
