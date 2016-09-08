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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.wilson.wdtreelistlibrary.WDTreeListAdapter;

/**
 * Created by Wilhelm Dewald on 18/03/16.
 */
public class JsonActivity extends Activity {

    public TestAdapter mAdapter;
    @InjectView(R.id.list_view)
    RecyclerView mList;

    TestObject testObject;

    private String json = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "\n" +
            "  \"definitions\": {\n" +
            "    \"address\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"street_address\": { \"type\": \"string\" },\n" +
            "        \"city\":           { \"type\": \"string\" },\n" +
            "        \"state\":          { \"type\": \"string\" }\n" +
            "      },\n" +
            "      \"required\": [\"street_address\", \"city\", \"state\"]\n" +
            "    }\n" +
            "  },\n" +
            "\n" +
            "  \"type\": \"object\",\n" +
            "\n" +
            "  \"properties\": {\n" +
            "    \"billing_address\": { \"$ref\": \"#/definitions/address\" },\n" +
            "    \"shipping_address\": {\n" +
            "      \"allOf\": [\n" +
            "        { \"$ref\": \"#/definitions/address\" },\n" +
            "        { \"properties\":\n" +
            "          { \"type\": { \"enum\": [ \"residential\", \"business\" ] } },\n" +
            "          \"required\": [\"type\"]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    private JSONObject mJSONObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        try {
            mJSONObject = (JSONObject) new JSONTokener(json).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        mAdapter = new TestAdapter(mJSONObject);
        mList.setAdapter(mAdapter);
        mList.setItemAnimator(new DefaultItemAnimator());

    }


    public class TestAdapter extends WDTreeListAdapter<TestAdapter.ViewHolder, JSONObject> {

        public JSONObject object;

        // Provide a suitable constructor (depends on the kind of data set)
        public TestAdapter(JSONObject object) {
            this.object = object;
        }


        @Override
        public int getItemCount(JSONObject parent, int depth) {
            if (parent == null)
                return 0;
            else
                return 0; //((JSONObject) parent).getChildren().size();
        }

        @Override
        public boolean itemIsCollapsed(JSONObject parent, int depth) {
            return depth >= 0;
        }

        @Override
        public JSONObject getItemObject(JSONObject parent, int pos, int depth) {
            if (parent == null)
                return object;
            else
                return null;

        }

        @Override
        public int getItemViewType(JSONObject parent, int depth) {
            return 0;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, JSONObject parent, JSONObject leaf, int depth) {


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
                mAdapter.addChildAfterChildPosition(getAdapterPosition(), null);
            }

            @OnClick(R.id.add_children)
            public void onButton3(View view) {
                mAdapter.addChildForParentPosition(getAdapterPosition(), null);
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

