package de.wilson.wdtreelist;

import java.util.ArrayList;
import java.util.List;

import de.wilson.wdtreelistlibrary.WDTreeLeaf;

/**
 * Created by Wilhelm Dewald on 14/05/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class TestObject{
    public String title = "Title" ;
    private List<TestObject> mChildren;

    public TestObject(String t) {
        title += t;
    }

    public List<TestObject> getChildren() {
        if(mChildren == null)
            mChildren = new ArrayList<>();
        return mChildren;
    }

    public void setChildren(List<TestObject> mChildren) {
        this.mChildren = mChildren;
    }
}
