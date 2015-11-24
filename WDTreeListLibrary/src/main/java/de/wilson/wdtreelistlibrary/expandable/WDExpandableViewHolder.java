package de.wilson.wdtreelistlibrary.expandable;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;


/**
 * Created by Wilhelm Dewald on 23/11/15.
 */
public class WDExpandableViewHolder extends RecyclerView.ViewHolder implements Animation.AnimationListener {

    // Constants
    private static final int ANIMATION_DURATION = 300; // 0.3s

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

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mRootView.measure(widthMeasureSpec, heightMeasureSpec);
        mRootViewHeight = mRootView.getMeasuredHeight();
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
            mExpandAnimation = new AlphaAnimation(0.0f, 1.0f);
            mExpandAnimation.setFillAfter(true);
            mExpandAnimation.setDuration(ANIMATION_DURATION);
            mExpandAnimation.setAnimationListener(this);

        }
        return mExpandAnimation;
    }

    private Animation getCollapseAnimation() {
        if (mCollapseAnimation == null) {

            mCollapseAnimation = new AlphaAnimation(1.0f, 0.0f);
            mCollapseAnimation.setFillAfter(true);
            mCollapseAnimation.setDuration(ANIMATION_DURATION);
            mCollapseAnimation.setAnimationListener(this);

        }
        return mCollapseAnimation;
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
