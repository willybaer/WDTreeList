package de.wilson.wdtreelistlibrary.expandable;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;


/**
 * Created by Wilhelm Dewald on 23/11/15.
 */
public class WDExpandableViewHolder extends RecyclerView.ViewHolder implements Animation.AnimationListener {

    // Constants
    private static final int ANIMATION_DURATION = 1000; // 1s

    // Attributes
    private View mRootView;
    private int mRootViewHeight;
    private boolean mIsExpanded = false;

    // Animations
    private Animation mExpandAnimation;
    private Animation mCollapseAnimation;

    public WDExpandableViewHolder(View itemView, boolean isExpanded) {
        super(itemView);
        mRootView = itemView.getRootView();
        mRootViewHeight = itemView.getMeasuredHeight();
        setIsExpanded(isExpanded);
    }

    public void expand() {
        setIsExpanded(true);
    }

    public void collapse() {
        setIsExpanded(false);
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    private void setIsExpanded(boolean isExpanded) {
        this.mIsExpanded = isExpanded;
        mRootView.startAnimation(isExpanded ? getExpandAnimation() : getCollapseAnimation());
    }

    private Animation getExpandAnimation() {
        if (mExpandAnimation == null) {
            mExpandAnimation = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    0f, 1f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 1f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
            mExpandAnimation.setFillAfter(true);
            mExpandAnimation.setDuration(ANIMATION_DURATION);
            mExpandAnimation.setAnimationListener(this);

        }
        return mExpandAnimation;
    }

    private Animation getCollapseAnimation() {
        if (mCollapseAnimation == null) {
            mCollapseAnimation = new ScaleAnimation(
                    1f, 1f, // Start and end values for the X axis scaling
                    1f, 0f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 1f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
            mCollapseAnimation.setFillAfter(true);
            mCollapseAnimation.setDuration(ANIMATION_DURATION);
            mCollapseAnimation.setAnimationListener(this);

        }
        return mExpandAnimation;
    }

    /*
     * Animation Callbacks
     */
    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();
        if (animation == mExpandAnimation) {
            layoutParams.height = mRootViewHeight;
            mRootView.setLayoutParams(layoutParams);
        } else if (animation == mCollapseAnimation) {
            layoutParams.height = 0;
            mRootView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
