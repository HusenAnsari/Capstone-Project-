package com.husenansari.theprompter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {

    private SearchActivity context;
    private JSONArray array = new JSONArray();

    SearchResultAdapter(SearchActivity context) {
        this.context = context;
    }

    public void setArray(JSONArray array)
    {
        this.array = array;
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.search_result_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        String title = "";
        try {
            JSONObject page = array.getJSONObject(position);
            title = page.getString("page_title");
            title = title.replace('_', ' ');
            final String finalTitle = title;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.fetchScript(finalTitle);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.title.setText(title);
    }

    @Override
    public int getItemCount() {
        return array.length();
    }

    public class VH extends RecyclerView.ViewHolder {

        TextView title;

        public VH(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.script_title);
        }
    }
}
