package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Wilhelm Dewald on 09/03/15.
 * <p/>
 * Stay cool, stay calm.
 *
 * This class will replacen the RecyclerView.Adapter and will implement a
 * structure like a the nsoutlineview for mac os
 *
 */
public abstract class WDTreeListAdapter<V extends RecyclerView.ViewHolder, T extends WDTreeLeaf> extends RecyclerView.Adapter<V> {

    // This object represents our tree
    protected WDTreeLeaf<T> tree = new WDTreeLeaf();

    private boolean mInvalidates = true;
    private int mCount = 0;


    // Helper objects for generating the structure
    private T mPreviousLeaf = null;

    /**
     * Helper function to manage our own data reload
     */
    public final void notifyThatDataChanged() {

        synchronized (tree) {
            mCount = -1;
            mInvalidates = true;
            tree = null;
        }

        this.notifyDataSetChanged();
    }


    /**
     * Here we handle the interaction between the recycler views functions
     * and our new functionality
     */

    // 1. Here is the magic starts -> first of all the size of entries is important
    @Override
    public int getItemCount() {

        if(mInvalidates) {
            synchronized (tree) {
                tree = new WDTreeLeaf();
                mCount = 0;
                mPreviousLeaf = null;
                generateStructure(null, 0, tree.getChildren());
            }
            mInvalidates = false;
        }

        return mCount;
    }

    @Override
    public int getItemViewType(int pos) {
        int type;

        synchronized (tree) {
            T view = getItemForPosition(pos);
            type = getItemViewType(view);
            view.viewType = type;
        }

        return type;
    }

    @Override
    public void onBindViewHolder(V holder, int position) {

        // Check if the object List has this object
        T view = getItemForPosition(position);
        onBindViewHolder(holder, view);
    }

    /**
     *  Our own functionality of a recycler view adapter
     */
    public abstract int getItemCount(WDTreeLeaf parent);
    public abstract T getItemObject(WDTreeLeaf parent, int pos, int depth);
    public abstract int getItemViewType(WDTreeLeaf parent);
    public abstract void onBindViewHolder(V holder, WDTreeLeaf treeView);

    /**
     * This method helps us to check how much children a paren leaf has
     * And here we will count the amount of all existing leaf, because we need the amount
     * of leafs for recycler view adapter
     *
     * @param parent
     * @param depth
     * @param parentChildren
     */
    protected void generateStructure(T parent, int depth, List<T> parentChildren) {

        // We need the object for each position
        T currentParent = parent == null ? (T) this.tree : parent;

        // Setup the data of the parent leaf
        currentParent.setPosition(mCount - 1);

        // We need number of elements at the current depth
        int count = getItemCount(parent);
        if( count < 1 )
            return;

        // Here we generate the subtree structure
        for(int i = 0; i < count; i++){
            mCount++;

            // Getting the children for the index
            T leaf = getItemObject(parent, i, depth);
            if(leaf == null) {
                continue;
            }

            // Climb down the subtree
            parentChildren.add(leaf);
            leaf.parent = currentParent;
            leaf.setDepth(depth);
            if (i == 0) {
                leaf.next = currentParent.next;
                currentParent.next = leaf;
                leaf.prev = currentParent;
            } else {
                leaf.next = mPreviousLeaf.next;
                mPreviousLeaf.next = leaf;
                leaf.prev = mPreviousLeaf;
            }
            mPreviousLeaf = leaf;
            generateStructure(leaf, depth + 1, leaf.getChildren());

        }

    }






    /**
     * Helferlein
     */
    private T objectForPosition(T rootitem, int currentPosition, int searchedPosition) {

        // if null
        if(rootitem == null)
            return null;

        // Check the normal shit
        if(currentPosition == searchedPosition) {
            rootitem.setPosition(currentPosition);
            return rootitem;
        }

        if(rootitem.getChildren().size() == 0) {
            rootitem.setPosition(currentPosition);
            return rootitem;
        }

        int currPos = currentPosition;
        for( int i = 0; i < rootitem.getChildren().size(); i++){
            T it = objectForPosition((T) rootitem.getChildren().get(i), currPos + 1, searchedPosition);
            if(it.getPosition() == searchedPosition)
                return it;
            else
                currPos = it.getPosition();
        }

        rootitem.setPosition(currPos);
        return rootitem;
    }

    /**
     * Unser View holder :D
     */

    public void addItemAfter(T currentItem, T newItem) {

        newItem.prev = currentItem;
        newItem.next = currentItem.next;
        currentItem.next = newItem;
        int parentPosition = currentItem.parent.getChildren().indexOf(currentItem);
        currentItem.parent.getChildren().add(parentPosition + 1, newItem);

        // update positions
        updatePositionAscending(currentItem);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    private void updatePositionAscending(T currentItem) {
        if(currentItem.next != null) {
            currentItem.next.setPosition(currentItem.getPosition() + 1);
            updatePositionAscending((T) currentItem.next);
        }
    }

    public void addItemBefore(T currentItem, T newItem) {

        newItem.next = currentItem;
        newItem.prev = currentItem.prev;
        currentItem.prev = newItem;
        int parentPosition = currentItem.parent.getChildren().indexOf(currentItem);
        currentItem.parent.getChildren().add(parentPosition, newItem);

        // update positions
        updatePositionDescending(currentItem);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    private void updatePositionDescending(T currentItem) {
        if (currentItem.prev != null) {
            currentItem.prev.setPosition(currentItem.getPosition() - 1);
            updatePositionDescending((T) currentItem.prev);
        }
    }

    public void addChildForParent(WDTreeLeaf parent, T newItem) {

        if( parent.getChildren().size() == 0) {
            // Is first entry so nothing sepecial to calculate
            newItem.next = parent.next;
            newItem.prev = parent;
            newItem.setPosition(parent.getPosition() + 1);
            newItem.setDepth(parent.getDepth() + 1);
            parent.next = newItem;
            parent.getChildren().add(newItem);
        } else {
            // Get the last children of the parent object
            T lastChildren = (T) parent.getChildren().get(parent.getChildren().size() - 1);

            // Iterate down the last childrens tree
            T lastItem = lastChildrenForParent((T) parent);

            newItem.next = lastItem.next;
            newItem.prev = lastItem;
            newItem.setPosition(lastItem.getPosition() + 1); // That is the problem

            newItem.setDepth(lastChildren.getDepth());
            lastItem.next = newItem;

            parent.getChildren().add(newItem);
        }
        mCount++;
        updatePositionAscending((T) parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    private T lastChildrenForParent(T parent) {

        if ( parent == null)
            return null;

        if(parent.getChildren() == null || parent.getChildren().size() == 0)
            return parent;

        // Get Last Children of
        return lastChildrenForParent((T) parent.getChildren().get(parent.getChildren().size() - 1));
    }

    public T getItemForPosition(int position) {
        if( position == -1 )
            return (T) tree;
        T view = tree.next;
        while( view != null) {
            if(view.getPosition() == position)
                return view;
            view = (T) view.next;
        }
        return null;
    }



}
