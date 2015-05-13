package de.wilson.wdtreelist;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.wilson.wdtreelistlibrary.WDTreeListAdapter;
import de.wilson.wdtreelistlibrary.WDTreeObject;


public class MainActivity extends Activity {

    @InjectView(R.id.list_view)
    RecyclerView mList;

    TestObject testObject;
    private TestAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mList.setLayoutManager(new LinearLayoutManager(this));
        testObject = new TestObject();
        mAdapter = new TestAdapter(testObject);
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(new DefaultItemAnimator());

    }


    public class TestAdapter extends WDTreeListAdapter<TestAdapter.ViewHolder, TestObject> {

        public TestObject object;

        // Provide a suitable constructor (depends on the kind of dataset)
        public TestAdapter(TestObject object) {
            this.object = object;
        }

        @Override
        public int getItemCount(WDTreeObject parent) {

            if(parent == null)
                return 1;
            else
                return object.getChildren().size();
        }

        @Override
        public TestObject getItemObject(WDTreeObject parent, int pos, int depth) {

            if(parent == null)
                return object;
            else
                return object.getChildren().get(pos);

        }

        @Override
        public int getItemViewType(WDTreeObject parent) {
            return 0;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, WDTreeObject treeView) {
            String pos = treeView.next != null ? ""+treeView.next.position : "None";

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.mText.getLayoutParams();
            int margin = 20 * (treeView.depth + 1);
            params.setMargins(margin, 0, 0, 0); //substitute parameters for left, top, right, bottom
            holder.mText.setLayoutParams(params);

            holder.mText.setText("Dept: "+treeView.depth);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_test_item, parent, false);
            return new ViewHolder(itemView);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            // each data item is just a string in this case
            @InjectView(R.id.test_text) public TextView mText;

            public ViewHolder(View layout) {
                super(layout);
                ButterKnife.inject(this, layout);

            }

            @OnClick(R.id.test_button)
            public void onButton(View view) {
                WDTreeObject entry = mAdapter.getItemForPosition(getAdapterPosition());
                mAdapter.addChildForParent(entry, new TestObject());
            }

        }
    }


}
