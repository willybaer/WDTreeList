package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilhelm Dewald on 09/03/15.
 * <p/>
 * Stay cool, stay calm.
 *
 * This class will replace the RecyclerView.Adapter and will implement a
 * structure like a the NSOutlineView for mac os
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
            type = getItemViewType(item.mObject, item.getDepth());
            item.viewType = type;
        }

        return type;
    }

    @Override
    public void onBindViewHolder(V holder, int position) {
        // Check if the object List has this object
        WDTreeLeaf item = getItemForPosition(position);
        onBindViewHolder(holder, item.mObject, item.getDepth());
    }

    /**
     *  Our own functionality of a recycler view adapter
     */
    public abstract int getItemCount(Object parent, int depth);
    public abstract Object getItemObject(Object parent, int pos, int depth);
    public abstract int getItemViewType(Object parent, int depth);
    public abstract void onBindViewHolder(V holder, Object treeView, int depth);

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
        int count = getItemCount(currentParent.mObject, currentParent.getDepth());
        if( count < 1 )
            return;

        // Here we generate the subtree structure
        for(int i = 0; i < count; i++){
            mCount++;

            // Getting the children for the index
            Object newObject = getItemObject(currentParent.mObject, i, depth); // Here we have to copy the object
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

    }

    /**
     * Function for appending new child for the parent item position
     *
     * @param parentPosition
     * @param newObject
     */
    public void addChildForParentPosition(int parentPosition, Object newObject) {

        // First of all we need the parent tree leaf
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if(parent == null)
            throw new NullPointerException("");

        WDTreeLeaf newItem = new WDTreeLeaf();
        newItem.mObject = newObject;

        if( parent.getChildren().size() == 0) {
            // Is first entry so nothing special to calculate
            newItem.next = parent.next;
            newItem.prev = parent;
            newItem.setPosition(parent.getPosition() + 1);
            newItem.setDepth(parent.getDepth() + 1);
            parent.next = newItem;
            parent.getChildren().add(newItem);

        } else {
            // Get the last children of the parent object
            WDTreeLeaf lastChildren = parent.getChildren().get(parent.getChildren().size() - 1);

            // Iterate down to the last children tree
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

    /**
     * Function for appending new object at the same depth like the current leafs position
     *
     * @param childPosition
     * @param newObject
     */
    public void addChildAfterChildPosition(int childPosition, Object newObject) {

        // Hier müssen wir nich von der leaf position ausgehen, sondern vom letzten innerhalb des Leaf`s

        // First of all we need the parent tree leaf
        WDTreeLeaf leafForPosition = getItemForPosition(childPosition);
        WDTreeLeaf leaf = lastChildrenForParent(leafForPosition);


        if(leaf == null || leafForPosition.parent == null)
            throw new NullPointerException("");

        WDTreeLeaf parent = leafForPosition.parent; // Needs to be the same parent like the leaf at the given position

        WDTreeLeaf newItem = new WDTreeLeaf();
        newItem.mObject = newObject;

        // setup new item relations
        newItem.next = leaf.next;
        newItem.prev = leaf;
        newItem.parent = parent;

        newItem.setPosition(leaf.getPosition() + 1);
        newItem.setDepth(leafForPosition.getDepth()); // Needs to have the same depth like the leaf

        // setup relations of the existing items
        if(leaf.next != null)
            leaf.next.prev = newItem; // 1. tell old next item(if exists) to set his prev pointer to the new item
        leaf.next = newItem; // 2. and then tell the current leaf to set its pointer pointer to the new item

        parent.getChildren().add(newItem);
        mCount++;

        // update position
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    /**
     * Function for appending new object at the same depth like the current leafs
     * position but add the new leaf before the given leaf
     *
     * @param childPosition
     * @param newObject
     */
    public void addChildBeforeChildPosition(int childPosition, Object newObject) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leaf = getItemForPosition(childPosition);

        if(leaf == null || leaf.parent == null)
            throw new NullPointerException("");

        WDTreeLeaf parent = leaf.parent;

        WDTreeLeaf newItem = new WDTreeLeaf();
        newItem.mObject = newObject;

        // setup new item relations
        newItem.next = leaf;
        newItem.prev = leaf.prev;
        newItem.parent = parent;

        newItem.setPosition(leaf.getPosition() - 1); // set new position
        newItem.setDepth(leaf.getDepth());

        // updating the relations of the old items
        leaf.prev.next = newItem; // 1. tell old prev item to set his next pointer to the new item
        leaf.prev = newItem; // 2. and then tell the current leaf to set its prev pointer to the new item

        parent.getChildren().add(newItem);
        mCount++;

        // update position
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    /**
     * Removes leaf for position
     * @param childPosition
     */
    public void removeChildForPosition(int childPosition) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leaf = getItemForPosition(childPosition);

        if(leaf == null || leaf.parent == null)
            throw new NullPointerException("");

        WDTreeLeaf parent = leaf.parent;
        WDTreeLeaf prevLeaf = leaf.prev;

        // setup relations
        prevLeaf.next = leaf.next;

        // remove leaf from the parent
        parent.getChildren().remove(leaf);

        // update position
        updatePositionAscending(parent);

        // animate item + sub item to be removed
        removeAllChildrenIncParentAnimated(leaf);
    }

    private void removeAllChildrenIncParentAnimated(WDTreeLeaf parent) {
        if( parent == null)
            return;

        for(WDTreeLeaf childLeaf : parent.getChildren()) {
            removeAllChildrenIncParentAnimated(childLeaf);
        }
        notifyItemRemoved(parent.getPosition());
        mCount--;
    }

    /**
     * Removes all children for a parent position
     * @param parentPosition
     */
    public  void removeAllChildrenForParent(int parentPosition) {

        // First of all we need the parent tree leaf
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if(parent == null || parent.parent == null)
            throw new NullPointerException("");

        // setup relations only if our parent object has more than 1 children
        if ( parent.parent.getChildren().size() > 1 ) {
            WDTreeLeaf nextParentChild = parent.parent.getChildren().get(parent.parent.getChildren().size() - 1);

            parent.next = nextParentChild;
            nextParentChild.prev = parent;

        } else
            parent.next = null;

        ArrayList<WDTreeLeaf> clearedChildren = new ArrayList<>(parent.getChildren()); // 1. Copy all cleared children
        parent.getChildren().clear(); // 2. Remove all items

        updatePositionAscending(parent);

        // animated it for each removed item
        for(WDTreeLeaf removedLeaf : clearedChildren) {
            removeAllChildrenIncParentAnimated(removedLeaf);
        }
    }

    /**
     * Returns the object for a given position, if there is no object for a position
     * @param position
     * @return
     */
    public Object getObjectForPosition(int position) {
        WDTreeLeaf leaf = getItemForPosition(position);
        return leaf != null ? leaf.mObject : null;
    }

    /**
     * Private helper functions
     */
    private WDTreeLeaf lastChildrenForParent(WDTreeLeaf parent) {

        if ( parent == null)
            return null;

        if(parent.getChildren() == null || parent.getChildren().size() == 0)
            return parent;

        // Get Last Children of parent
        return lastChildrenForParent(parent.getChildren().get(parent.getChildren().size() - 1));
    }

    private WDTreeLeaf getItemForPosition(int position) {
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
     * This functions are helping us to manage the the leaf positions after a succesful add/remove of a leaf
     * @param currentItem
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
