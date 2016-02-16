package de.wilson.wdtreelistlibrary;

/**
 * Created by Wilhelm Dewald on 16/02/16.
 */
public interface WDTreeListAdapterStructure {

    /**
     * Helper function to manage our own data reload
     */
    void notifyThatDataChanged();

    /**
     * Helper for appending a new root child. If there is an empty tree.
     *
     * @param newObject
     */
    void addRootChild(Object newObject);

    /**
     * Function for appending new child to the parents child list.
     * The new child gets added to the last chil position.
     *
     * @param parentPosition
     * @param newObject
     */
    void addChildForParentPosition(int parentPosition, Object newObject);

    /**
     * Function for appending new object at the same depth like the current leafs position and
     * the one position after the childs position.
     *
     * @param childPosition
     * @param newObject
     */
    void addChildAfterChildPosition(int childPosition, Object newObject);

    /**
     * Function for appending new object at the same depth like the current leafs
     * position, but adds the new leaf before the given leaf
     *
     * @param childPosition
     * @param newObject
     */
    void addChildBeforeChildPosition(int childPosition, Object newObject);

    /**
     * Removes all children for a parent position
     *
     * @param parentPosition
     */
    void removeAllChildrenForParentPosition(int parentPosition);

    /**
     * Removes leaf for the given position.
     *
     * @param childPosition
     */
    void removeChildForPosition(int childPosition);

    /**
     * Returns the object for a given position.
     *
     * @param position
     * @return
     */
    Object getObjectForPosition(int position);

    /**
     * Toggle the collapsing and the expanding of a parent leaf
     *
     * @param parentPosition
     */
    void setCollapsedForAllChildrenAndParentPosition(boolean collapse, int parentPosition);

    boolean isParentCollapsed(int parentPosition);
}
