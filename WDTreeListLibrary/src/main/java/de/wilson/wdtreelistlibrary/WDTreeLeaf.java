package de.wilson.wdtreelistlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilhelm Dewald on 29/04/15.
 * <p/>
 * Stay cool, stay calm.
 * <p/>
 * This object represents our tree leaf.
 * <p/>
 * For a better interaction with the recycler list adapter we
 * implement the basic logic of a doubly linked list data structure and
 * the map tree logic
 * <p/>
 * Have a look on the following wikipedia article for more informations
 * http://en.wikipedia.org/wiki/Linked_list
 */
public class WDTreeLeaf {

    // Need for the map tree logic
    private final List<WDTreeLeaf> mChildren = new ArrayList<>();
    private final List<WDTreeLeaf> mCollapsedChildren = new ArrayList<>();
    public WDTreeLeaf parent;

    // Double linked list logic
    public WDTreeLeaf prev;
    public WDTreeLeaf next;

    // Other stuff
    public int viewType = 0;
    public Object mObject;

    // Basic list information
    private int mDepth = -1;
    private int mPosition = -1;
    private boolean mChildrenCollapsed = false;

    public WDTreeLeaf(Object newObject) {
        this.mObject = newObject;
    }

    /*
     * Getter and setter for the main information
     */
    public int getDepth() {
        return mDepth;
    }

    public void setDepth(int depth) {
        this.mDepth = depth;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public List<WDTreeLeaf> getChildren() {
        return mChildren;
    }

    public List<WDTreeLeaf> getCollapsedChildren() {
        return mCollapsedChildren;
    }

    public boolean isChildrenCollapsed() {
        return mChildrenCollapsed;
    }

    public void setChildrenCollapsed(boolean childrenCollapsed) {
        mChildrenCollapsed = childrenCollapsed;
    }
}
