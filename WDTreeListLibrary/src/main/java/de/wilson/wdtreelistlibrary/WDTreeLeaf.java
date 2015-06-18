package de.wilson.wdtreelistlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilhelm Dewald on 29/04/15.
 * <p/>
 * Stay cool, stay calm.
 *
 * This object represents our tree leaf.
 *
 * For a better interaction with the recycler list adapter we
 * implement the basic logic of a doubly linked list data structure
 *
 * Have a look on the following wikipedia article for more informations
 * http://en.wikipedia.org/wiki/Linked_list
 *
 */
public class WDTreeLeaf{

    // Basic list information
    private int mDepth = -1;
    private int mPosition = -1;

    // Storing reference to all children of this leaf
    private final List<WDTreeLeaf> mChildren = new ArrayList<>();
    public Object mObject;
    public WDTreeLeaf parent;

    // Double linked list logic
    public WDTreeLeaf prev;
    public WDTreeLeaf next;

    // Other stuff
    public boolean visible = false;
    public int viewType = 0;

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

    public List<WDTreeLeaf> getChildren()
    {
        return mChildren;
    }

}
