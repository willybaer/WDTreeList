package de.wilson.wdtreelist;

import de.wilson.wdtreelistlibrary.WDTreeLeaf;

/**
 * Created by Wilhelm Dewald on 14/05/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class TestObject extends WDTreeLeaf<TestObject> {
    public String title = "Title" ;
    public TestObject(String t) {
        title += t;
    }
}
