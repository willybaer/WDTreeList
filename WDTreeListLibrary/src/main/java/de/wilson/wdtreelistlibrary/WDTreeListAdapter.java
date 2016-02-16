package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;

import java.util.List;

import de.wilson.wdtreelistlibrary.exception.WDException;

/**
 * Created by Wilhelm Dewald on 09/03/15.
 * <p/>
 * Stay cool, stay calm.
 * <p/>
 * This class replaces the standard RecyclerView.Adapter and implements
 * a tree structure inspired by the NSOutlineView class for mac os applications.
 * <p/>
 * Because of the fact that the standard RecyclerView.Adapter works like a list, we
 * need a implementation that can handle both, a MapTree and a list behaviour.
 * So each element has on the one side a relation to its parent leaf and a relation to its previous
 * and its next leaf.
 * <p/>
 * We depict the RecyclerView.Adapter interface to our own interface implenentation.
 */
public abstract class WDTreeListAdapter<V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> implements WDTreeListAdapterStructure {

    protected WDTreeLeaf tree = new WDTreeLeaf(null, null);
    protected int mCount = 0;
    private boolean mInvalidates = true;

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
     * Here is where the magic starts. At this point the standard RecyclerView.Adapter behaviour starts
     * creating the list structure and at this point our adapter start to create the tree/list structure
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (mInvalidates) {
            mInvalidates = false;
            synchronized (tree) {
                tree = new WDTreeLeaf(null, null);
                mCount = 0;
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
     * New custom interface functions
     */
    public abstract int getItemCount(Object parent, int depth);

    public abstract boolean itemIsCollapsed(Object parent, int depth);

    public abstract Object getItemObject(Object parent, int pos, int depth);

    public abstract int getItemViewType(Object parent, int depth);

    public abstract void onBindViewHolder(V holder, Object leaf, int depth);

    /**
     * In this method we check how much children a parent leaf has.
     * And here we will count the amount of all existing leaf, because we need the amount
     * of leafs for recycler view adapter. We also generate the parent/children and list structure by
     * using our abstract functions.
     *
     * @param parent
     * @param depth
     * @param parentChildren
     */
    protected void generateStructure(WDTreeLeaf parent, int depth, List<WDTreeLeaf> parentChildren) {

        WDTreeLeaf currentParent = parent == null ? this.tree : parent; // We need the current leaf for each position
        currentParent.setPosition(mCount - 1); // Setup the position for each leaf

        int count = getItemCount(currentParent.mObject, currentParent.getDepth()); // calling the new itemCount function with the subtree depth
        boolean parentIsCollapsed = itemIsCollapsed(currentParent, currentParent.getDepth());
        currentParent.childrenCollapsed = !parentIsCollapsed;

        if (count < 1)
            return;

        // Subtree structure
        WDTreeLeaf previousLeaf = null;
        for (int i = 0; i < count; i++) {

            // Getting the children for the index
            Object newObject = getItemObject(currentParent.mObject, i, depth); // Here we have to copy the object
            if (newObject == null)
                throw new WDException(WDException.WDExceptionType.ITEM_OBJECT_CALLBACK_NULL_OBJECT);

            // Parent <-> Child relationship
            WDTreeLeaf leaf = new WDTreeLeaf(currentParent, newObject);
            leaf.setDepth(depth);
            leaf.childrenCollapsed = currentParent.childrenCollapsed;

            if (currentParent.childrenCollapsed)
                currentParent.getCollapsedChildren().add(leaf);
            else
                parentChildren.add(leaf);

            // Prev <-> next relationship only if expanded
            if (!currentParent.childrenCollapsed) {
                mCount++;

                if (i == 0) {
                    leaf.next = currentParent.next;
                    currentParent.next = leaf;
                    leaf.prev = currentParent;
                } else {
                    leaf.next = previousLeaf.next;
                    previousLeaf.next = leaf;
                    leaf.prev = previousLeaf;
                }
            }
            previousLeaf = leaf;

            // We need the information about the previous leaf for setting up the correct list relation
            generateStructure(leaf, depth + 1, leaf.getChildren());
        }

    }

    public void addRootChild(Object newObject) {
        addChildForParentPosition(-1, newObject);
    }

    public void addChildForParentPosition(int parentPosition, Object newObject) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf newItem = new WDTreeLeaf(parent, newObject);
        addChildForParent(parent, newItem);

        newItem.collapsed = parent.childrenCollapsed;
        if (parent.childrenCollapsed)
            parent.getCollapsedChildren().add(newItem);
        else
            parent.getChildren().add(newItem);

        mCount++;
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    public void addChildAfterChildPosition(int childPosition, Object newObject) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leafForPosition = getItemForPosition(childPosition);

        if (leafForPosition == null || leafForPosition.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf lastChildLeaf = lastChildrenForParent(leafForPosition); // We need the last leaf the the childs sub tree
        WDTreeLeaf parent = leafForPosition.parent; // Needs to be the same parent like the leaf at the given position

        // Setup new item
        WDTreeLeaf newItem = new WDTreeLeaf(parent, newObject);

        // setup new item relations
        newItem.next = lastChildLeaf.next;
        newItem.prev = lastChildLeaf;

        newItem.setPosition(lastChildLeaf.getPosition() + 1);
        newItem.setDepth(leafForPosition.getDepth()); // Needs to have the same depth like the leaf

        // setup relations of the existing items
        if (lastChildLeaf.next != null)
            lastChildLeaf.next.prev = newItem; // 1. tell old next item(if exists) to set his prev pointer to the new item
        lastChildLeaf.next = newItem; // 2. and then tell the current leaf to set its pointer pointer to the new item

        newItem.collapsed = parent.childrenCollapsed;
        if (parent.childrenCollapsed)
            parent.getCollapsedChildren().add(newItem);
        else
            parent.getChildren().add(newItem);

        mCount++;

        // update position
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    public void addChildBeforeChildPosition(int childPosition, Object newObject) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leaf = getItemForPosition(childPosition);

        if (leaf == null || leaf.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf parent = leaf.parent;
        WDTreeLeaf newItem = new WDTreeLeaf(parent, newObject);

        // setup new item relations
        newItem.next = leaf;
        newItem.prev = leaf.prev;

        newItem.setPosition(leaf.getPosition() - 1); // set new position
        newItem.setDepth(leaf.getDepth());

        // updating the relations of the old items
        leaf.prev.next = newItem; // 1. tell old prev item to set his next pointer to the new item
        leaf.prev = newItem; // 2. and then tell the current leaf to set its prev pointer to the new item

        newItem.collapsed = parent.childrenCollapsed;
        if (parent.childrenCollapsed)
            parent.getCollapsedChildren().add(newItem);
        else
            parent.getChildren().add(newItem);

        mCount++;

        // update position
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    public void removeChildForPosition(int childPosition) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leaf = getItemForPosition(childPosition);

        if (leaf == null || leaf.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf parent = leaf.parent;
        WDTreeLeaf prevLeaf = leaf.prev;

        // find next leaf
        WDTreeLeaf nextLeaf = lastChildrenForParent(leaf);
        if (nextLeaf == null)
            nextLeaf = leaf;

        // setup relations
        prevLeaf.next = nextLeaf.next;
        if (nextLeaf.next != null)
            nextLeaf.next.prev = prevLeaf; // setting up next leaf prev relation only if there is a next object

        // First, we have to do the animation
        // animate item + sub item to be removed
        removeAllChildrenIncParentAnimated(leaf);

        // remove leaf from the parent
        parent.getChildren().remove(leaf);

        // update position
        updatePositionAscending(parent);
    }

    public void removeAllChildrenForParentPosition(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if (parent == null || parent.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        removeAllChildrenRelationsForParent(parent);
        removeAllChildrenForParentFromListAnimated(parent);

        // Remove all children from parent forever
        parent.getChildren().clear();
        parent.getCollapsedChildren().clear();

        // Update the position
        updatePositionAscending(parent);
    }

    public Object getObjectForPosition(int position) {
        WDTreeLeaf leaf = getItemForPosition(position);
        return leaf != null ? leaf.mObject : null;
    }

    public boolean isParentCollapsed(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        return parent.childrenCollapsed;
    }

    public void setCollapsedForAllChildrenAndParentPosition(boolean collapse, int parentPosition) {
        if (collapse) {
            collapseAllChildrenForParentPosition(parentPosition);
        } else {
            expandAllChildrenForParentPosition(parentPosition);
        }
    }

    /**
     * Collapse all children for a parent position
     *
     * @param parentPosition
     */
    private void collapseAllChildrenForParentPosition(int parentPosition) {

        // First of all we need the parent tree leaf
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if (parent == null || parent.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        removeAllChildrenRelationsForParent(parent);
        removeAllChildrenForParentFromListAnimated(parent); // Animates the removal from List
        manageCollapsedChildrenForParent(parent);

        // Update the position
        updatePositionAscending(parent);

        parent.childrenCollapsed = true;
    }

    /**
     * Expand all children for a parent position
     *
     * @param parentPosition
     */
    private void expandAllChildrenForParentPosition(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        int currentCount = mCount;

        parent.childrenCollapsed = false;
        reAddChildrenForParent(parent, parent.getCollapsedChildren());
        parent.getCollapsedChildren().clear();
        updatePositionAscending(parent);

        // animate inserted items
        notifyItemRangeInserted(parentPosition + 1, mCount - currentCount);
    }

    private void reAddChildrenForParent(WDTreeLeaf parent, List<WDTreeLeaf> children) {
        if (children == null || children.size() == 0)
            return;

        for (WDTreeLeaf leaf : children) {
            leaf.collapsed = false;
            addChildForParent(parent, leaf);
            mCount++;
            if (!leaf.childrenCollapsed)
                reAddChildrenForParent(leaf, leaf.getCollapsedChildren());
        }
    }

    /**
     * Helper method for appending new items to a parents item
     *
     * @param parent
     * @param newItem
     */
    private void addChildForParent(WDTreeLeaf parent, WDTreeLeaf newItem) {
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        newItem.parent = parent; // setting up parent object relation

        if (parent.getChildren().size() == 0) {
            // Is first entry so nothing special to calculate
            newItem.next = parent.next;
            newItem.prev = parent;

            newItem.setPosition(parent.getPosition() + 1);
            newItem.setDepth(parent.getDepth() + 1);

            parent.next = newItem;
            if (newItem.next != null)
                newItem.next.prev = newItem; // setting next item prev relation to the new item

            parent.getChildren().add(newItem);
        } else {
            // Get the last children of the parent object
            WDTreeLeaf lastChildren = parent.getChildren().get(parent.getChildren().size() - 1);

            // Iterate down to the last children tree
            WDTreeLeaf lastItem = lastChildrenForParent(lastChildren);

            newItem.next = lastItem.next;
            newItem.prev = lastItem;

            newItem.setPosition(lastItem.getPosition() + 1);
            newItem.setDepth(parent.getDepth() + 1);

            lastItem.next = newItem;
            if (newItem.next != null)
                newItem.next.prev = newItem; // setting next item prev relation to the new item

            parent.getChildren().add(newItem);
        }
    }

    /**
     * Helper method to remove all children of a parent leaf
     *
     * @param parent
     */
    private void removeAllChildrenRelationsForParent(WDTreeLeaf parent) {
        if (parent.getChildren().size() == 0)
            return;

        WDTreeLeaf lastChildForParent = lastChildrenForParent(parent);
        parent.next = lastChildForParent.next;

        if (lastChildForParent.next != null)
            lastChildForParent.next.prev = parent;
    }

    /**
     * This function return the leaf for a given position.
     *
     * @param position
     * @return
     */
    private WDTreeLeaf getItemForPosition(int position) {
        if (position < -1)
            throw new WDException(WDException.WDExceptionType.FORBIDDEN_POSITION);

        if (position == -1)
            return tree;

        WDTreeLeaf currentItem = tree.next;

        while (currentItem != null) {
            if (currentItem.getPosition() == position)
                return currentItem;
            currentItem = currentItem.next;
        }

        return null;
    }

    /**
     * This functions are re managing the leaf positions after a successful add/remove of a leaf
     *
     * @param currentItem
     */
    private void updatePositionAscending(WDTreeLeaf currentItem) {
        if (currentItem.next != null) {
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

    /**
     * This function is need to find the last child of the sub tree of a given parent leaf.
     *
     * @param parent
     * @return
     */
    private WDTreeLeaf lastChildrenForParent(WDTreeLeaf parent) {
        if (parent == null)
            return null;

        if (parent.getChildren() == null || parent.getChildren().size() == 0)
            return parent;

        return lastChildrenForParent(parent.getChildren().get(parent.getChildren().size() - 1));
    }

    /**
     * Animates item deletion inside the recycler view list:
     * Here we animate the the deletion of the parent item and including its children
     *
     * @param parent
     */
    private void removeAllChildrenIncParentAnimated(WDTreeLeaf parent) {
        if (parent == null)
            return;

        int parentPosition = parent.getPosition();
        int lastChildPosition = lastChildrenForParent(parent).getPosition();
        int range = lastChildPosition - parentPosition + 1;
        notifyItemRangeRemoved(parentPosition, range);

        mCount -= range;
    }

    /**
     * Animates item deletion inside the recycler view list:
     * Here we animate the the deletion of children
     *
     * @param parent
     */
    private void removeAllChildrenForParentFromListAnimated(WDTreeLeaf parent) {
        if (parent == null)
            return;

        int parentPosition = parent.getPosition();
        int lastChildPosition = lastChildrenForParent(parent).getPosition();
        int range = lastChildPosition - parentPosition;
        notifyItemRangeRemoved(parentPosition + 1, range);

        mCount -= range;
    }

    private void manageCollapsedChildrenForParent(WDTreeLeaf parent) {
        if (parent == null || parent.getChildren() == null || parent.getChildren().size() == 0)
            return;

        // Copy children to new list
        parent.getCollapsedChildren().clear();
        parent.getCollapsedChildren().addAll(parent.getChildren());
        parent.getChildren().clear();

        for (WDTreeLeaf leaf : parent.getCollapsedChildren()) {
            leaf.collapsed = parent.childrenCollapsed;
            manageCollapsedChildrenForParent(leaf);
        }
    }
}
