package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;

import java.util.List;

import de.wilson.wdtreelistlibrary.exception.WDException;

/**
 * Created by Wilhelm Dewald on 16/02/16.
 * <p/>
 * Abstract helper class where the collapse and expanding animation are implemented.
 * Based on the WDTreeListAdapter
 */
public abstract class WDTreeExpandableListAdapter<V extends RecyclerView.ViewHolder> extends WDTreeListAdapter<V> {

    public boolean isParentCollapsed(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        return parent.childrenCollapsed;
    }

    /**
     * Collapse all children for a parent position
     *
     * @param parentPosition
     */
    public void collapseAllChildrenForParentPosition(int parentPosition) {
        // First of all we need the parent tree leaf
        WDTreeLeaf parent = getItemForPosition(parentPosition);

        if (parent == null || parent.parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        removeAllChildrenRelationsForParent(parent);
        removeAllChildrenForParentFromListAnimated(parent); // Animates the removal from List

        parent.getCollapsedChildren().clear();
        parent.getCollapsedChildren().addAll(parent.getChildren());
        parent.getChildren().clear();

        // Update the position
        updatePositionAscending(parent);

        parent.childrenCollapsed = true;
    }

    /**
     * Expand all children for a parent position
     *
     * @param parentPosition
     */
    public void expandAllChildrenForParentPosition(int parentPosition) {
        WDTreeLeaf parent = getItemForPosition(parentPosition);
        if (parent == null)
            throw new WDException(WDException.WDExceptionType.NO_PARENT_LEAF_FOR_GIVEN_POSITION);

        int currentCount = mCount;

        reAddChildrenForParent(parent, parent.getCollapsedChildren());
        parent.getCollapsedChildren().clear();
        updatePositionAscending(parent);

        // animate inserted items
        notifyItemRangeInserted(parentPosition + 1, mCount - currentCount);
        parent.childrenCollapsed = false;
    }

    private void reAddChildrenForParent(WDTreeLeaf parent, List<WDTreeLeaf> children) {
        if (children == null || children.size() == 0)
            return;

        for (WDTreeLeaf leaf : children) {
            addChildForParent(parent, leaf);
            mCount++;
            reAddChildrenForParent(leaf, leaf.getChildren());
        }
    }
}
