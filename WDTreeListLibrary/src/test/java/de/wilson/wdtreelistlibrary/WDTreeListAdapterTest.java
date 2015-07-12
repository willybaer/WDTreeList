package de.wilson.wdtreelistlibrary;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Created by Wilhelm Dewald on 11/06/15.
 * <p/>
 * Stay cool, stay calm.
 */


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class WDTreeListAdapterTest {

    private WDTreeLeaf root;
    private TestAdapter testAdapter;

    @Before
    public void setUp() throws Exception {

        // Setup the root TreeLeaf
        root = new WDTreeLeaf();

            // Three Children
            WDTreeLeaf child1 = new WDTreeLeaf();
            WDTreeLeaf child2 = new WDTreeLeaf();

                // two children
                WDTreeLeaf child21 = new WDTreeLeaf();
                WDTreeLeaf child22 = new WDTreeLeaf();
                child2.getChildren().add(child21);
                child2.getChildren().add(child22);

            WDTreeLeaf child3 = new WDTreeLeaf();

                // two children
                WDTreeLeaf child31 = new WDTreeLeaf();
                WDTreeLeaf child32 = new WDTreeLeaf();

                child3.getChildren().add(child31);
                child3.getChildren().add(child32);
            root.getChildren().add(child1);
            root.getChildren().add(child2);
            root.getChildren().add(child3);
    }

    @Test
    public void testGenerateStructure() throws Exception {

    }

    public class TestAdapter extends WDTreeListAdapter<TestAdapter.ViewHolder> {

        public TestObject object;

        // Provide a suitable constructor (depends on the kind of dataset)
        public TestAdapter(TestObject object) {
            this.object = object;
        }


        @Override
        public int getItemCount(Object parent, int depth) {
            if(parent == null)
                return 1;
            else
                return ((TestObject)parent).getChildren().size();
        }

        @Override
        public Object getItemObject(Object parent, int pos, int depth) {
            if(parent == null)
                return object;
            else
                return ((TestObject)parent).getChildren().get(pos);

        }

        @Override
        public int getItemViewType(Object parent, int depth) {
            return 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, Object treeView, int depth) {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }



        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View layout) {
                super(layout);

            }
        }
    }
}