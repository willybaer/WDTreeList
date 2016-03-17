package de.wilson.wdtreelistlibrary.helper;

/**
 * Created by Wilhelm Dewald on 17/03/16.
 */
public class WDListPositionWithRange extends WDListPosition {
    public int range;

    public WDListPositionWithRange(int position, int range) {
        super(position);
        this.range = range;
    }
}
