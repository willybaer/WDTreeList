package de.wilson.wdtreelist;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static de.wilson.wdtreelist.TestUtils.withRecyclerView;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ExpandableActivityTest {

    // https://github.com/dannyroa/espresso-samples/blob/master/RecyclerView/app/src/androidTest/java/com/dannyroa/espresso_samples/recyclerview/RecyclerViewMatcher.java

    @Rule
    public ActivityTestRule<ExpandableActivity> mExpandableActivity = new ActivityTestRule<>(
            ExpandableActivity.class);


    /*
     * TREE:
      * - 0
      *    - 1
      *    - 1
      *        - 2
      *        - 2
      *    - 1
      *        - 2
      *        - 2
     */
    @Test
    public void testingExpandCollapse() {
        testRootChildFunction();
        testExpandCollapse();
        testAddRemoveItem();
        testAddChildAfterAndBeforeChild();
        testExpandCollapseWithAddedingNewItems();
    }

    private void testRootChildFunction() {
        // Test 1: testing collapse -> should collapse two items
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.addRootChild(null);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(8, R.id.test_text)).check(matches(withText("Dept: 0")));
    }

    private void testExpandCollapse() {
        // Test 1: testing collapse -> should collapse two items
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.setCollapsedForAllChildrenAndParentPosition(true, 2);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(3, R.id.test_text)).check(matches(withText("Dept: 1")));

        // Test 2: Testing expand
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.setCollapsedForAllChildrenAndParentPosition(false, 2);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(3, R.id.test_text)).check(matches(withText("Dept: 2")));
    }

    private void testAddRemoveItem() {
        // Test 1: testing add item
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.addChildForParentPosition(1, null);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(2, R.id.test_text)).check(matches(withText("Dept: 2")));

        // Test 2: Testing expand
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.removeChildForPosition(2);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(2, R.id.test_text)).check(matches(withText("Dept: 1")));
    }

    private void testAddChildAfterAndBeforeChild() {
        // Test 1: testing add item after child
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.addChildAfterChildPosition(3, null);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(5, R.id.test_text)).check(matches(withText("Dept: 2")));

        // Test 2: testing add item before child
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.addChildBeforeChildPosition(3, null);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(3, R.id.test_text)).check(matches(withText("Dept: 2")));


    }

    private void testExpandCollapseWithAddedingNewItems() {
        // Test 1: testing collapse -> should collapse two items
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.setCollapsedForAllChildrenAndParentPosition(true, 2);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(3, R.id.test_text)).check(matches(withText("Dept: 1")));

        // Test 2: Add new Item
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.addChildForParentPosition(2, null);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(3, R.id.test_text)).check(matches(withText("Dept: 1")));

        // Test 3: Testing expand
        mExpandableActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mExpandableActivity.getActivity().mAdapter.setCollapsedForAllChildrenAndParentPosition(false, 2);
            }
        });

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withRecyclerView(R.id.list_view)
                .atPositionOnView(7, R.id.test_text)).check(matches(withText("Dept: 2")));
    }


}