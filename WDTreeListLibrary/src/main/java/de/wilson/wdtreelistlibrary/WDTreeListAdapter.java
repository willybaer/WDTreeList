package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;

import java.util.List;

import de.wilson.wdtreelistlibrary.exception.WDException;
import de.wilson.wdtreelistlibrary.helper.WDListPositionWithRange;

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
 * We depict the RecyclerView.Adapter interface to our own interface implementation.
 */
public abstract class WDTreeListAdapter<V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<V> implements WDTreeListAdapterStructure {

    protected WDTreeLeaf tree = new WDTreeLeaf(null);
    protected int mCount = 0;
    protected boolean mInvalidates = true;

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

    /*
     * Here is where the magic starts. At this point the standard RecyclerView.Adapter behaviour starts
     * creating the list structure and at this point our adapter start to create the tree/list structure
     */
    @Override
    public int getItemCount() {
        if (mInvalidates) {
            mInvalidates = false;
            synchronized (tree) {
                tree = new WDTreeLeaf(null);
                mCount = 0;
                generateStructure(null, 0);
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
     * New custom abstract functions will replace the default recyclerview functions
     */
    public abstract int getItemCount(Object parent, int depth);

    public abstract boolean itemIsCollapsed(Object parent, int depth);

    public abstract Object getItemObject(Object parent, int pos, int depth);

    public abstract int getItemViewType(Object parent, int depth);

    public abstract void onBindViewHolder(V holder, Object leaf, int depth);

    /*
     * In this method we check how much children a parent leaf has.
     * And here we will count the amount of all existing leaf, because we need the amount
     * of leafs for recycler view adapter. We also generate the parent/children and list structure by
     * using our abstract functions.
     */
    protected void generateStructure(WDTreeLeaf parent, int depth) {
        WDTreeLeaf currentParent = parent == null ? this.tree : parent; // We need the current leaf for each position
        currentParent.setPosition(mCount - 1); // Setup the position for each leaf

        // Asking for children count
        int count = getItemCount(currentParent.mObject, currentParent.getDepth()); // calling the new itemCount function with the subtree depth
        if (count < 1)
            return;

        // Asking for collapsed state
        if (!currentParent.isChildrenCollapsed() && currentParent != tree) {
            boolean parentIsCollapsed = itemIsCollapsed(currentParent, currentParent.getDepth());
            currentParent.setChildrenCollapsed(parentIsCollapsed);
        }

        // Subtree structure
        for (int i = 0; i < count; i++) {

            // Getting the children for the index
            Object newObject = getItemObject(currentParent.mObject, i, depth); // Here we have to copy the object
            if (newObject == null)
                throw new WDException(WDException.WDExceptionType.ITEM_OBJECT_CALLBACK_NULL_OBJECT);

            // Create new leaf entry
            WDTreeLeaf newLeaf = new WDTreeLeaf(newObject);
            newLeaf.parent = currentParent;
            newLeaf.setDepth(depth);
            newLeaf.setChildrenCollapsed(currentParent.isChildrenCollapsed());

            // Setup relations
            addChildToParentAndSetUpRelations(currentParent, newLeaf);

            // Go on with the subtree
            generateStructure(newLeaf, depth + 1);
        }
    }

    /**
     * Helper function to add a root Child
     *
     * @param newObject
     */
    public void addRootChild(Object newObject) {
        addChildForParentPosition(-1, newObject);
    }

    /**
     * Helper function to add a child to a parent object for a given parent position.
     * Child gets pushed to the end of the list
     *
     * @param parentPosition
     * @param newObject
     */
    public void addChildForParentPosition(int parentPosition, Object newObject) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        // Create new Item
        WDTreeLeaf newItem = new WDTreeLeaf(newObject);
        newItem.parent = parent;
        newItem.setDepth(parent.getDepth() + 1);

        // Setup relations
        addChildToParentAndSetUpRelations(parent, newItem);

        // Update list items position need for the recycler view
        updatePositionAscending(parent);

        // Animate item
        notifyItemInserted(parent, newItem.getPosition());
    }

    /**
     * Helper function to add a child after a given child position;
     * Must be on the same depth and should append after the child inside the
     * Child gets pushed to the end of the list
     *
     * @param childPosition
     * @param newObject
     */
    public void addChildAfterChildPosition(int childPosition, Object newObject) {
        WDTreeLeaf leafForPosition = getItemForPosition(childPosition);

        if (leafForPosition == null || leafForPosition.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf lastChildLeaf = lastChildrenForParent(leafForPosition); // We need the last leaf the the childs sub tree
        WDTreeLeaf parent = leafForPosition.parent; // Needs to be the same parent like the leaf at the given position

        // Setup new item
        WDTreeLeaf newItem = new WDTreeLeaf(newObject);
        newItem.parent = parent;

        // Setup new item relations
        if (!parent.isChildrenCollapsed()) {
            newItem.next = lastChildLeaf.next;
            newItem.prev = lastChildLeaf;

            newItem.setPosition(lastChildLeaf.getPosition() + 1);
            newItem.setDepth(leafForPosition.getDepth()); // Needs to have the same depth like the leaf

            // Setup relations of the existing items
            if (lastChildLeaf.next != null)
                lastChildLeaf.next.prev = newItem; // 1. tell old next item(if exists) to set his prev pointer to the new item
            lastChildLeaf.next = newItem; // 2. and then tell the current leaf to set its pointer pointer to the new item
        }

        // Append new item to
        parentAppendNewChildAfterChild(parent, leafForPosition, newItem);

        // Update positions
        updatePositionAscending(parent);

        // Animate item
        notifyItemInserted(parent, newItem.getPosition());
    }

    private void parentAppendNewChildAfterChild(WDTreeLeaf parentLeaf, WDTreeLeaf afterChild, WDTreeLeaf newItem) {
        if (parentLeaf.isChildrenCollapsed()) {
            int childPosition = parentLeaf.getCollapsedChildren().indexOf(afterChild);
            parentLeaf.getCollapsedChildren().add(childPosition + 1, newItem);
        } else {
            int childPosition = parentLeaf.getChildren().indexOf(afterChild);
            parentLeaf.getChildren().add(childPosition + 1, newItem);
            this.mCount++;
        }
    }

    /**
     * Helper function to add a child before a given child position;
     * Must be on the same depth and should append after the child inside the
     * Child gets pushed to the end of the list
     *
     * @param childPosition
     * @param newObject
     */
    public void addChildBeforeChildPosition(int childPosition, Object newObject) {

        // First of all we need the parent tree leaf
        WDTreeLeaf leafForPosition = getItemForPosition(childPosition);

        if (leafForPosition == null || leafForPosition.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_LEAF_FOR_GIVEN_POSITION);

        WDTreeLeaf parent = leafForPosition.parent;
        WDTreeLeaf newItem = new WDTreeLeaf(newObject);
        newItem.parent = parent;
        newItem.setDepth(leafForPosition.getDepth());

        if (!parent.isChildrenCollapsed()) {
            // setup new item relations
            newItem.next = leafForPosition;
            newItem.prev = leafForPosition.prev;

            newItem.setPosition(leafForPosition.getPosition() - 1); // set new position

            // updating the relations of the old items
            leafForPosition.prev.next = newItem; // 1. tell old prev item to set his next pointer to the new item
            leafForPosition.prev = newItem; // 2. and then tell the current leaf to set its prev pointer to the new item
        }

        // Append new item to
        parentAppendNewChildBeforeChild(parent, leafForPosition, newItem);

        // update position
        updatePositionAscending(parent);

        // animate item
        notifyItemInserted(newItem.getPosition());
    }

    private void parentAppendNewChildBeforeChild(WDTreeLeaf parentLeaf, WDTreeLeaf afterChild, WDTreeLeaf newItem) {
        if (parentLeaf.isChildrenCollapsed()) {
            int childPosition = parentLeaf.getCollapsedChildren().indexOf(afterChild);
            parentLeaf.getCollapsedChildren().add(childPosition, newItem);
        } else {
            int childPosition = parentLeaf.getChildren().indexOf(afterChild);
            parentLeaf.getChildren().add(childPosition, newItem);
            this.mCount++;
        }
    }

    /**
     * Removes child for a given child position
     *
     * @param childPosition
     */
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

        // We need the range for the list animation
        WDListPositionWithRange animationRange = getRangeForChildrenIncParent(leaf);

        // remove leaf from the parent
        parent.getChildren().remove(leaf);

        // update position
        updatePositionAscending(parent);

        // Animate item range removed
        notifyItemRangeRemoved(animationRange.position, animationRange.range);
    }

    /**
     * Removes all children for a given paren position
     */
    public void removeAllChildrenForParentPosition(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if (parent == null || parent.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        // We need the range for the animation
        WDListPositionWithRange animationRange = getRangeForChildren(parent);

        // Remove relations
        removeAllChildrenRelationsForParent(parent);

        // Remove all children from parent forever
        parent.getChildren().clear();
        parent.getCollapsedChildren().clear();

        // Update the position
        updatePositionAscending(parent);

        // Animate item range
        notifyItemRangeRemoved(animationRange.position, animationRange.range);
    }

    private void removeAllChildrenRelationsForParent(WDTreeLeaf parent) {
        if (parent.getChildren().size() == 0)
            return;

        WDTreeLeaf lastChildForParent = lastChildrenForParent(parent);
        parent.next = lastChildForParent.next;

        if (lastChildForParent.next != null)
            lastChildForParent.next.prev = parent;
    }

    /**
     * Helps to find an related object for a given position
     *
     * @param position
     * @return
     */
    public Object getObjectForPosition(int position) {
        WDTreeLeaf leaf = getItemForPosition(position);
        return leaf != null ? leaf.mObject : null;
    }

    /**
     * Helper to ask for the collapsed state
     *
     * @param parentPosition
     * @return
     */
    public boolean isParentCollapsed(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        return parent.isChildrenCollapsed();
    }

    /**
     * Collapse or expand for a parent position
     *
     * @param collapse
     * @param parentPosition
     */
    public void setCollapsedForAllChildrenAndParentPosition(boolean collapse, int parentPosition) {
        if (collapse) {
            collapseAllChildrenForParentPosition(parentPosition);
        } else {
            expandAllChildrenForParentPosition(parentPosition);
        }
    }

    private void collapseAllChildrenForParentPosition(int parentPosition) {

        // First of all we need the parent tree leaf
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if (parent == null || parent.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        // We need the range for the animation
        WDListPositionWithRange animationRange = getRangeForChildren(parent);

        // Setup relations
        parent.setChildrenCollapsed(true);
        removeAllChildrenRelationsForParent(parent);
        copyChildrenToCollapsedList(parent);

        // Update the position
        updatePositionAscending(parent);

        // Animate item range
        notifyItemRangeRemoved(animationRange.position, animationRange.range);
    }

    private void copyChildrenToCollapsedList(WDTreeLeaf parent) {
        if (parent == null || parent.getChildren() == null || parent.getChildren().size() == 0)
            return;

        // Copy children to new list
        parent.getCollapsedChildren().clear();
        parent.getCollapsedChildren().addAll(parent.getChildren());
        parent.getChildren().clear();

        for (WDTreeLeaf leaf : parent.getCollapsedChildren()) {
            copyChildrenToCollapsedList(leaf);
        }
    }

    private void expandAllChildrenForParentPosition(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        int currentCount = mCount;

        parent.setChildrenCollapsed(false);
        reAddChildrenForParent(parent, parent.getCollapsedChildren());
        updatePositionAscending(parent);

        // animate inserted items
        notifyItemRangeInserted(parentPosition + 1, mCount - currentCount);
    }

    private void reAddChildrenForParent(WDTreeLeaf parent, List<WDTreeLeaf> children) {
        if (children == null || children.size() == 0)
            return;

        for (WDTreeLeaf leaf : children) {
            addChildToParentAndSetUpRelations(parent, leaf);
            if (!leaf.isChildrenCollapsed())
                reAddChildrenForParent(leaf, leaf.getCollapsedChildren());
        }

        if (!parent.isChildrenCollapsed())
            parent.getCollapsedChildren().clear();
    }

    /*
     * Helper method for appending new items to a parents children
     * Adds the child to the end of the child list.
     */
    private void addChildToParentAndSetUpRelations(WDTreeLeaf parent, WDTreeLeaf newItem) {
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        newItem.parent = parent; // setting up parent object relation

        if (parent.isChildrenCollapsed() && parent != this.tree) {
            if (!parent.getCollapsedChildren().contains(newItem))
                parent.getCollapsedChildren().add(newItem);
            return;
        }

        if (parent.getChildren().size() == 0) {
            // Is first entry so nothing special to calculate
            newItem.next = parent.next;
            newItem.prev = parent;

            newItem.setPosition(parent.getPosition() + 1);
            newItem.setDepth(parent.getDepth() + 1);

            parent.next = newItem;
            if (newItem.next != null)
                newItem.next.prev = newItem; // setting next item prev relation to the new item
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
        }
        if (!parent.getChildren().contains(newItem)) {
            parent.getChildren().add(newItem);
            this.mCount++;
        }

    }

    /*
     * Helper function for notify item inserted
     */
    private void notifyItemInserted(WDTreeLeaf parent, int childPosition) {
        if (!parent.isChildrenCollapsed())
            notifyItemInserted(childPosition);
    }

    /*
     * This function return the leaf for a given position.
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

    /*
     * This functions are re managing the leaf positions after a successful add/remove of a leaf
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

    /*
     * This function is need to find the last child of the sub tree of a given parent leaf.
     */
    private WDTreeLeaf lastChildrenForParent(WDTreeLeaf parent) {
        if (parent == null)
            return null;

        if (parent.getChildren() == null || parent.getChildren().size() == 0)
            return parent;

        return lastChildrenForParent(parent.getChildren().get(parent.getChildren().size() - 1));
    }

    /*
     * Returns the range from the parent position (inc parent position)
     * until the last child position inside the parents subtree
     */
    private WDListPositionWithRange getRangeForChildrenIncParent(WDTreeLeaf parent) {
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.PARENT_OR_CHILD_LEAF_ARE_NULL);

        int parentPosition = parent.getPosition();
        int lastChildPosition = lastChildrenForParent(parent).getPosition();
        int range = lastChildPosition - parentPosition + 1;
        this.mCount -= range;

        return new WDListPositionWithRange(parentPosition, range);
    }

    /*
     * Returns the range from the parent position (NOT inc parent position)
     * until the last child position inside the parents subtree
     */
    private WDListPositionWithRange getRangeForChildren(WDTreeLeaf parent) {
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.PARENT_OR_CHILD_LEAF_ARE_NULL);

        int parentPosition = parent.getPosition();
        int lastChildPosition = lastChildrenForParent(parent).getPosition();
        int range = lastChildPosition - parentPosition;
        this.mCount -= range;

        return new WDListPositionWithRange(parentPosition + 1, range);
    }
}
