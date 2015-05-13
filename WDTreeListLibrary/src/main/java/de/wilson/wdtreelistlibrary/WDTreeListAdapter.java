package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Wilhelm Dewald on 09/03/15.
 * <p/>
 * Stay cool, stay calm.
 */
public abstract class WDTreeListAdapter<VH extends RecyclerView.ViewHolder, TV extends WDTreeObject> extends RecyclerView.Adapter<VH> {

    protected WDTreeObject<TV> tree = new WDTreeObject<TV>();

    private boolean mInvalidates = true;

    private int mCount = 0;

    @Override
    public void onBindViewHolder(VH holder, int position) {
        // Check if the object List has this object
        TV view = getItemForPosition(position);
        onBindViewHolder(holder, view);
    }

    @Override
    public int getItemCount() {

        if(mInvalidates) {
            synchronized (tree) {
                tree = new WDTreeObject<TV>();
                tree.position = -1;
                mCount = 0;
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
            TV view = getItemForPosition(pos);
            type = getItemViewType(view);
            view.viewType = type;
        }

        return type;
    }

    /**
     * Wir zerpflücken den Scheiß
     */
    protected void generateStructure(TV parent, int depth, List<TV> objects) {

        // 1) we need number of element in this depth
        int count = getItemCount(parent);

        // 2) Generate List size
        //objects = new ArrayList<WDTreeView>(count);

        // 3) we need the object for each position
        TV currentParent = parent == null ? (TV) this.tree : parent;
        for(int i = 0; i < count; i++){
            mCount++;
            TV obj = getItemObject(parent, i, depth);
            obj.parent = currentParent;
            obj.position = mCount - 1;
            obj.depth = depth;

            if (i == 0) {
                // the parent object is the first position
                // and it next pointer points to the first object in its childrens list
                obj.next = currentParent.next;
                currentParent.next = obj;
                obj.prev = currentParent;
            } else {
                obj.prev = objects.get(i-1);
                obj.next = obj.prev.next;
                obj.prev.next = obj;
            }

            objects.add(obj);
        }

        // 4) Generate SubTree
        for(TV treeView : objects) {
            generateStructure(treeView, depth+1, treeView.getChildren());
        }

    }


    /**
     *
     *  Vererbungsgedöns
     *
     */

    public abstract int getItemCount(WDTreeObject parent);
    public abstract TV getItemObject(WDTreeObject parent, int pos, int depth);
    public abstract int getItemViewType(WDTreeObject parent);
    public abstract void onBindViewHolder(VH holder, WDTreeObject treeView);

    public final void notifyThatDataChanged() {

        synchronized (tree) {
            mCount = -1;
            mInvalidates = true;
            tree = null;
        }

        this.notifyDataSetChanged();

    }

    /**
     * Helferlein
     */
    private TV objectForPosition(TV rootitem, int currentPosition, int searchedPosition) {

        // if null
        if(rootitem == null)
            return null;

        // Check the normal shit
        if(currentPosition == searchedPosition) {
            rootitem.position = currentPosition;
            return rootitem;
        }

        if(rootitem.children.size() == 0) {
            rootitem.position = currentPosition;
            return rootitem;
        }

        int currPos = currentPosition;
        for( int i = 0; i < rootitem.children.size(); i++){
            TV it = objectForPosition((TV) rootitem.children.get(i), currPos + 1, searchedPosition);
            if(it.position == searchedPosition)
                return it;
            else
                currPos = it.position;
        }

        rootitem.position = currPos;
        return rootitem;
    }

    /**
     * Unser View holder :D
     */

    public void addItemAfter(TV currentItem, TV newItem) {

        newItem.prev = currentItem;
        newItem.next = currentItem.next;
        currentItem.next = newItem;
        int parentPosition = currentItem.parent.children.indexOf(currentItem);
        currentItem.parent.children.add(parentPosition + 1, newItem);

        // update positions
        updatePositionAscending(currentItem);

        // animate item
        notifyItemInserted(newItem.position);
    }

    private void updatePositionAscending(TV currentItem) {
        if(currentItem.next != null) {
            currentItem.next.position = currentItem.position + 1;
            updatePositionAscending((TV) currentItem.next);
        }
    }

    public void addItemBefore(TV currentItem, TV newItem) {

        newItem.next = currentItem;
        newItem.prev = currentItem.prev;
        currentItem.prev = newItem;
        int parentPosition = currentItem.parent.children.indexOf(currentItem);
        currentItem.parent.children.add(parentPosition, newItem);

        // update positions
        updatePositionDescending(currentItem);

        // animate item
        notifyItemInserted(newItem.position);
    }

    private void updatePositionDescending(TV currentItem) {
        if (currentItem.prev != null) {
            currentItem.prev.position = currentItem.position - 1;
            updatePositionDescending((TV) currentItem.prev);
        }
    }

    public void addChildForParent(WDTreeObject parent, TV newItem) {

        if( parent.children.size() == 0) {
            newItem.next = parent.next;
            newItem.prev = parent;
            newItem.position = parent.position + 1;
            newItem.depth = parent.depth + 1;
            parent.next = newItem;
            parent.children.add(newItem);
        } else {
            TV furtherItem = (TV) parent.children.get(parent.children.size() - 1);
            newItem.next = furtherItem.next;
            newItem.prev = furtherItem;
            newItem.position = furtherItem.position + 1;
            newItem.depth = furtherItem.depth;
            furtherItem.next = newItem;
            parent.children.add(newItem);
        }
        mCount++;
        updatePositionAscending((TV) parent);
        // animate item
        notifyItemInserted(newItem.position);
    }

    public TV getItemForPosition(int position) {
        if( position == -1 )
            return (TV) tree;
        TV view = tree.next;
        while( view != null) {
            if(view.position == position)
                return view;
            view = (TV) view.next;
        }
        return null;
    }


}
