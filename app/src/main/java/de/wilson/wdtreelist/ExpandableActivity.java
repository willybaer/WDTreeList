package de.wilson.wdtreelist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.wilson.wdtreelistlibrary.WDTreeListAdapter;
//import de.wilson.wdtreelistlibrary.expandable.MyLinearLayoutManager;
//import de.wilson.wdtreelistlibrary.expandable.WDExpandableViewHolder;

/**
 * Created by Wilhelm Dewald on 27/07/15.
 * <p/>
 * Stay cool, stay calm.
 */
public class ExpandableActivity extends Activity {

    public TestAdapter mAdapter;
    @InjectView(R.id.list_view)
    RecyclerView mList;
    TestObject testObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        testObject = new TestObject("root");

        // Three Children
        TestObject child1 = new TestObject("child1");

        TestObject child2 = new TestObject("child2");
        // two children
        TestObject child21 = new TestObject("child21");
        TestObject child22 = new TestObject("child22");
        child2.getChildren().add(child21);
        child2.getChildren().add(child22);
        TestObject child3 = new TestObject("child3");
        // two children
        TestObject child31 = new TestObject("child31");
        TestObject child32 = new TestObject("child32");
        child3.getChildren().add(child31);
        child3.getChildren().add(child32);
        testObject.getChildren().add(child1);
        testObject.getChildren().add(child2);
        testObject.getChildren().add(child3);

        mAdapter = new TestAdapter(testObject);
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(new DefaultItemAnimator());

    }


    public class TestAdapter extends WDTreeListAdapter<TestAdapter.ViewHolder, Object> {

        public TestObject object;

        // Provide a suitable constructor (depends on the kind of data set)
        public TestAdapter(TestObject object) {
            this.object = object;
        }


        @Override
        public int getItemCount(Object parent, int depth) {
            if (parent == null)
                return 1;
            else
                return ((TestObject) parent).getChildren().size();
        }

        @Override
        public boolean itemIsCollapsed(Object parent, int depth) {
            return false;
        }

        @Override
        public Object getItemObject(Object parent, int pos, int depth) {
            if (parent == null)
                return object;
            else
                return ((TestObject) parent).getChildren().get(pos);

        }

        @Override
        public int getItemViewType(Object parent, int depth) {
            return 0;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, Object parent, Object leaf, int depth) {


            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mText.getLayoutParams();

            int margin = 20 * (depth + 1);
            params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
            holder.mText.setLayoutParams(params);
            holder.mText.setText("Dept: " + depth);


            /*if(depth <= 1)
                holder.setExpanded(true);
            else
                holder.setExpanded(false);*/

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_text_item_two, parent, false);
            return new ViewHolder(itemView);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            // each data item is just a string in this case
            @InjectView(R.id.test_text)
            public TextView mText;

            public Object leaf;

            public ViewHolder(View layout) {
                super(layout);
                ButterKnife.inject(this, layout);
            }

            @OnClick(R.id.collapse_button)
            public void onButton1(View view) {
                boolean isCollapsed = mAdapter.isParentCollapsed(getAdapterPosition());
                mAdapter.setCollapsedForAllChildrenAndParentPosition(getAdapterPosition(), !isCollapsed);
            }

            @OnClick(R.id.add_children_before_children)
            public void onButton2(View view) {
                mAdapter.addChildAfterChildPosition(getAdapterPosition(), new TestObject("newLastChild"));
            }

            @OnClick(R.id.add_children)
            public void onButton3(View view) {
                mAdapter.addChildForParentPosition(getAdapterPosition(), new TestObject("newLastChild"));
            }

            @OnClick(R.id.remove_children)
            public void onButton4(View view) {
                mAdapter.removeChildForPosition(getAdapterPosition());
            }

            @OnClick(R.id.remove_all_children)
            public void onButton5(View view) {
                mAdapter.removeAllChildrenForParentPosition(getAdapterPosition());
            }


        }
    }


}
