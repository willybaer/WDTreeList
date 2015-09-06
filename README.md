# Android Tree List Adapter
-----
### About
This library spports a tree list implementation for a RecyclerView. Inspired by the NSOUtlineView ( AppKit Framework ), this library is based on the RecyclerView.Adapter.

### Version
0.1.4

### Gradle
```Java
compile 'com.github.willybaer.wdtreelist:WDTreeListLibrary:0.1.4'
```

### Example
Have a look inside the "app" directory for an example implemenation.

------
### How To
The usage of the WDTreeListAdapter is similiar to the standard RecyclerView.Adapter. Only extend the WDTreeListAdapter and implement the necessary methods.

##### int getItemCount(Object parent, int depth)
Here you have to return the number of children for the given parent object. To specify the root item in any of these methods, nil is sent as the method`s parent object.

##### Object getItemObject(Object parent, int pos, int depth)
Return the related object for the given child position and its parent object.

##### int getItemViewType(Object parent, int depth)
Return the viewType for the children of the given parent object.

##### void onBindViewHolder(V holder, Object leaf, int depth)
Setup the content of the ViewHolder.

------
### Helper functions
#### Append new entry
```Java
public void addRootChild(Object newObject)
public void addChildForParentPosition(int parentPosition, Object newObject)
public void addChildAfterChildPosition(int childPosition, Object newObject)
public void addChildBeforeChildPosition(int childPosition, Object newObject)
```
#### Remove entry
```Java
public void removeChildForPosition(int childPosition)
public  void removeAllChildrenForParent(int parentPosition)
```
#### Search functions
```Java
public Object getObjectForPosition(int position)
```