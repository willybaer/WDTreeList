# Android Tree List Adapter
-----
### About
This library supports a tree list implementation for a RecyclerView. Inspired by the NSOUtlineView ( AppKit Framework ), this library is based on the RecyclerView.Adapter.

### Version
0.1.8

Modified the 'onBindViewHolder' function to be able access the parent object there.

### Gradle
```Java
compile 'com.github.willybaer.wdtreelist:WDTreeListLibrary:0.1.8'
```

### Example
Have a look inside the "app" directory for an example implementation.

<img style="-webkit-user-select: none; cursor: zoom-in;" src="https://cloud.githubusercontent.com/assets/3387249/15375126/d16ca114-1d4d-11e6-879e-052f49921fa6.gif" width="315" height="560">

------
### How To
The usage of the WDTreeListAdapter is similar to the standard RecyclerView.Adapter. Extend the WDTreeListAdapter and implement the necessary methods.

It is now possible to expand and collapse tree leafs. For this there are 
##### int getItemCount(T parent, int depth)
Here you have to return the number of children for the given parent object. To specify the root item in any of these methods, nil is sent as the method`s parent object.

##### Object getItemObject(T parent, int pos, int depth)
Return the related object for the given child position and its parent object.

##### int getItemViewType(T leaf, int depth)
Return the viewType for the children of the given parent object.

##### void onBindViewHolder(V holder, Object parent, Object leaf, int depth)
Setup the content of the ViewHolder.

##### boolean itemIsCollapsed(T parent, int depth)
Return true if the item should be collapsed when initialising the tree list.

------
### Helper functions
#### Append new entry
```Java
public void addRootChild(T newObject)
public void addChildForParentPosition(int parentPosition, T newObject)
public void addChildAfterChildPosition(int childPosition, T newObject)
public void addChildBeforeChildPosition(int childPosition, T newObject)
```
#### Remove entry
```Java
public void removeChildForPosition(int childPosition)
public  void removeAllChildrenForParent(int parentPosition)
```
#### Search functions
```Java
public T getObjectForPosition(int position)
```
#### Collaps/Expand functions
```Java
public boolean isParentCollapsed(int parentPosition)
public void setCollapsedForAllChildrenAndParentPosition(int parentPosition, boolean collapse)
```