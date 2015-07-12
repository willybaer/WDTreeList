package de.wilson.wdtreelistlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilhelm Dewald on 12/07/15.
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
