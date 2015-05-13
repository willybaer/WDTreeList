package de.wilson.wdtreelistlibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wilhelm Dewald on 29/04/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class WDTreeObject<TV extends WDTreeObject> extends Object {

    public boolean visible = false;
    public int depth = -1;
    public int viewType = 0;
    public int position;

    // Tree list and linear list handling
    public List<TV> children = new ArrayList<>();
    public TV parent;

    public TV prev;
    public TV next;

    public List<TV> getChildren()
    {
        if ( children == null )
            children = new ArrayList<>();
        return children;
    }

    // Instantiate a Generic Type
    public static <TV> TV getInstance(Class<TV> _class)
    {
        try
        {
            return _class.newInstance();
        }
        catch (Exception _ex)
        {
            _ex.printStackTrace();
        }
        return null;
    }



}
