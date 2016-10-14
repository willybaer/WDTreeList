package de.wilson.wdtreelistlibrary;

/**
 * Created by Wilhelm Dewald on 16/02/16.
 */
public interface WDTreeListAdapterStructure<T> {

    /**
     * Helper function to manage our own data reload
     */
    void notifyThatDataChanged();

    /**
     * Helper for appending a new root child. If there is an empty tree.
     *
     * @param newObject
     */
    void addRootChild(T newObject);

    /**
     * Function for appending new child to the parents child list.
     * The new child gets added to the last chil position.
     *
     * @param parentPosition
     * @param newObject
     */
    void addChildForParentPosition(int parentPosition, T newObject);

    /**
     * Function for appending new object at the same depth like the current leafs position and
     * the one position after the child position.
     *
     * @param childPosition
     * @param newObject
     */
    void addChildAfterChildPosition(int childPosition, T newObject);

    /**
     * Function for appending new object at the same depth like the current leafs
     * position, but adds the new leaf before the given leaf
     *
     * @param childPosition
     * @param newObject
     */
    void addChildBeforeChildPosition(int childPosition, T newObject);

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
    T getObjectForPosition(int position);

    /**
     * Returns the position of the parent object
     * Returns -1 if the child is the root element
     *
     * @param childPosition
     * @return
     */
    int getParentPositionForChildPosition(int childPosition);

    /**
     * Toggle the collapsing and the expanding of a parent leaf
     *
     * @param parentPosition
     */
    void setCollapsedForAllChildrenAndParentPosition(int parentPosition, boolean collapse);


    /**
     * Return the the leaf state if it is collapes or not
     *
     * @param parentPosition
     * @return
     */
    boolean isParentCollapsed(int parentPosition);


}
