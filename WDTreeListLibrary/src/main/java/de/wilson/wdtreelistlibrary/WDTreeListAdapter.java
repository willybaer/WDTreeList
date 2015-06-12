package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
public abstract class WDTreeListAdapter<V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {

    // This object represents our tree
    protected WDTreeLeaf tree = new WDTreeLeaf();

    private boolean mInvalidates = true;
    private int mCount = 0;


    // Helper objects for generating the structure
    private WDTreeLeaf mPreviousLeaf = null;

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
            mInvalidates = false;
            synchronized (tree) {
                tree = new WDTreeLeaf();
                mCount = 0;
                mPreviousLeaf = null;
                generateStructure(null, 0, tree.getChildren());
            }
        }

        return mCount;
    }

    @Override
    public int getItemViewType(int pos) {
        int type;

        synchronized (tree) {
            WDTreeLeaf item = getItemForPosition(pos);
            type = getItemViewType(item.mObject);
            item.viewType = type;
        }

        return type;
    }

    @Override
    public void onBindViewHolder(V holder, int position) {
        // Check if the object List has this object
        WDTreeLeaf item = getItemForPosition(position);
        onBindViewHolder(holder, item.mObject);
    }

    /**
     *  Our own functionality of a recycler view adapter
     */
    public abstract int getItemCount(Object parent);
    public abstract Object getItemObject(Object parent, int pos, int depth);
    public abstract int getItemViewType(Object parent);
    public abstract void onBindViewHolder(V holder, Object treeView);

    /**
     * This method helps us to check how much children a paren leaf has
     * And here we will count the amount of all existing leaf, because we need the amount
     * of leafs for recycler view adapter
     *
     * @param parent
     * @param depth
     * @param parentChildren
     */
    protected void generateStructure(WDTreeLeaf parent, int depth, List<WDTreeLeaf> parentChildren) {

        // We need the object for each position
        WDTreeLeaf currentParent = parent == null ? this.tree : parent;

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
            Object newObject = getItemObject(parent, i, depth); // Here we have to copy the object
            WDTreeLeaf leaf = new WDTreeLeaf();
            leaf.mObject = newObject;

            leaf.getChildren().clear();
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

        String bla = "bla";
    }

    /**
     * Funktionierende add child methoden
     *
     * @param parentPosition
     * @param newObject
     */
    public void addChildForParentPosition(int parentPosition, Object newObject) {

        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if(parent == null)
            throw new NullPointerException("");

        WDTreeLeaf newItem = new WDTreeLeaf();
        newItem.mObject = newObject;

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
            WDTreeLeaf lastChildren = parent.getChildren().get(parent.getChildren().size() - 1);

            // Iterate down the last childrens tree
            WDTreeLeaf lastItem = lastChildrenForParent(parent);

            newItem.next = lastItem.next;
            newItem.prev = lastItem;
            newItem.setPosition(lastItem.getPosition() + 1); // That is the problem

            newItem.setDepth(lastChildren.getDepth());
            lastItem.next = newItem;

            parent.getChildren().add(newItem);
        }
        mCount++;
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    private WDTreeLeaf lastChildrenForParent(WDTreeLeaf parent) {

        if ( parent == null)
            return null;

        if(parent.getChildren() == null || parent.getChildren().size() == 0)
            return parent;

        // Get Last Children of parent
        return lastChildrenForParent(parent.getChildren().get(parent.getChildren().size() - 1));
    }

    public WDTreeLeaf getItemForPosition(int position) {
        if( position == -1 )
            return tree;
        WDTreeLeaf currentItem = tree.next;
        while( currentItem != null) {
            if(currentItem.getPosition() == position)
                return currentItem;
            currentItem = currentItem.next;
        }
        return null;
    }



    /**
     * Helferlein
     */

    /**
     * Unser View holder :D
     */

    private void updatePositionAscending(WDTreeLeaf currentItem) {
        if(currentItem.next != null) {
            currentItem.next.setPosition(currentItem.getPosition() + 1);
            updatePositionAscending(currentItem.next);
        }
    }

    private void updatePositionDescending(WDTreeLeaf currentItem) {
        if (currentItem.prev != null) {
            currentItem.prev.setPosition(currentItem.getPosition() - 1);
            updatePositionDescending(currentItem.prev);
        }
    }





}
