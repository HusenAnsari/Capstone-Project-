package com.husenansari.theprompter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.husenansari.theprompter.data.PrompterContract;
import com.husenansari.theprompter.data.Script;
import com.husenansari.theprompter.data.ScriptsProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ScriptListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView scriptsRecyclerView;
    TextView noScriptsView;
    ScriptListAdapter adapter;

    private boolean dualScreenMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        ((Application) getActivity().getApplication()).startTracking();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

        dualScreenMode = ((MainActivity) getActivity()).isDualScreen();

        adapter = new ScriptListAdapter(getContext(), new ArrayList<Script>());
    }

    @Override
    public void onResume() {
        getLoaderManager().restartLoader(0, null, this);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.script_list_fragment, container, false);

        scriptsRecyclerView = (RecyclerView) root.findViewById(R.id.script_list);
        noScriptsView = (TextView) root.findViewById(R.id.no_scripts);

        scriptsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        scriptsRecyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ScriptItemTouchCallback());
        itemTouchHelper.attachToRecyclerView(scriptsRecyclerView);

        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.add_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddScriptActivity.class);
                startActivity(intent);
            }
        });

        AdView adView = (AdView) root.findViewById(R.id.script_list_banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), ScriptsProvider.SCRIPTS_BASE_URI, null, null, null, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.script_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_dummy_content:
                PrompterContract.addDummyContent(getContext());
                onResume();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRecyclerViewVisibility(boolean show)
    {
        if (show) {
            scriptsRecyclerView.setVisibility(View.VISIBLE);
            noScriptsView.setVisibility(View.GONE);
        } else {
            scriptsRecyclerView.setVisibility(View.GONE);
            noScriptsView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0) {
            scriptsRecyclerView.setVisibility(View.VISIBLE);
            noScriptsView.setVisibility(View.GONE);

            data.moveToFirst();
            ArrayList<Script> scripts = new ArrayList<>();

            do {
                scripts.add(Script.populate(data));
            } while (data.moveToNext());
            //data.close();

            adapter = new ScriptListAdapter(getContext(), scripts);
            scriptsRecyclerView.setAdapter(adapter);

            setRecyclerViewVisibility(true);
        } else {
            setRecyclerViewVisibility(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter = new ScriptListAdapter(getContext(), new ArrayList<Script>());
        scriptsRecyclerView.setAdapter(adapter);
    }

    private class ScriptListAdapter extends RecyclerView.Adapter<ScriptListAdapter.VH> {

        private Context context;
        private ArrayList<Script> scripts = new ArrayList<>();
        private Calendar calendar = new GregorianCalendar();
        private SimpleDateFormat format = new SimpleDateFormat("d MMM yy");

        private Handler handler = new Handler();

        private Script scriptBeingRemoved;
        private boolean undoShown = false;
        private Runnable runnable;

        private int selected = 0;

        ScriptListAdapter(Context context, ArrayList<Script> scripts)
        {
            this.context = context;
            this.scripts = scripts;
        }

        public boolean isUndoShown() {
            return undoShown;
        }

        public void remove(final int pos)
        {
            final Script script = scripts.get(pos);
            scriptBeingRemoved = script;
            notifyItemChanged(pos);
            undoShown = true;

            runnable = new Runnable() {
                @Override
                public void run() {
                    getContext().getContentResolver()
                            .delete(ScriptsProvider.SCRIPT_BASE_URI.buildUpon().appendPath(script.getId().toString()).build(), null, null);

                    scripts.remove(script);
                    scriptBeingRemoved = null;
                    undoShown = false;
                    notifyItemRemoved(pos);
                    setRecyclerViewVisibility(scripts.size() > 0);

                    if (selected == pos)
                    {
                        selected = 0;
                        notifyItemChanged(selected);
                    }

                    if (dualScreenMode)
                        ((MainActivity) getActivity()).onResume();
                }
            };

            handler.postDelayed(runnable, 2000);
        }

        @Override
        public ScriptListAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(context).inflate(R.layout.script_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            Script script = scripts.get(position);

            if (script.equals(scriptBeingRemoved)) {
                holder.contentContainer.measure(0, 0);
                holder.undo.getLayoutParams().height = holder.contentContainer.getMeasuredHeight();
                holder.undo.setVisibility(View.VISIBLE);
                holder.contentContainer.setVisibility(View.GONE);
                holder.undo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        handler.removeCallbacks(runnable);
                        scriptBeingRemoved = null;
                        undoShown = false;
                        notifyItemChanged(pos);
                    }
                });
            } else {
                if (dualScreenMode && selected == position) {
                    holder.contentContainer.setBackgroundColor(getResources().getColor(R.color.divider));
                } else {
                    holder.contentContainer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                holder.undo.setVisibility(View.GONE);
                holder.contentContainer.setVisibility(View.VISIBLE);
                holder.title.setText(script.getTitle());
                holder.excerpt.setText(script.getContent());

                calendar.setTimeInMillis(script.getTimestamp() * 1000);
                holder.date.setText(format.format(calendar.getTime()));

                holder.contentContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notifyItemChanged(selected);
                        selected = holder.getAdapterPosition();
                        notifyItemChanged(selected);
                        ((MainActivity) getActivity()).showDetailScreen(scripts.get(selected).getId().toString());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return scripts.size();
        }

        class VH extends RecyclerView.ViewHolder {

            public View contentContainer;
            public View undo;

            public TextView title;
            public TextView excerpt;
            public TextView date;

            public VH(View itemView) {
                super(itemView);

                contentContainer = itemView.findViewById(R.id.content_container);
                undo =  itemView.findViewById(R.id.undo);

                title = (TextView) itemView.findViewById(R.id.script_title);
                excerpt = (TextView) itemView.findViewById(R.id.script_content_excerpt);
                date = (TextView) itemView.findViewById(R.id.script_date);
            }
        }
    }

    private class ScriptItemTouchCallback extends ItemTouchHelper.SimpleCallback {

        Context context = ScriptListFragment.this.getContext();

        Drawable deleteIcon = context.getDrawable(R.drawable.delete_sweep);
        ColorDrawable bgColor = new ColorDrawable(context.getResources().getColor(R.color.colorAccent));
        int iconMargin = context.getResources().getDimensionPixelSize(R.dimen.swipe_delete_icon_margin);
        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.swipe_delete_icon_size);

        public ScriptItemTouchCallback() {
            super(0, ItemTouchHelper.LEFT);
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            ScriptListAdapter adapter = (ScriptListAdapter) recyclerView.getAdapter();
            if (adapter.isUndoShown()) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            ScriptListAdapter adapter = (ScriptListAdapter) scriptsRecyclerView.getAdapter();
            adapter.remove(pos);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View view = viewHolder.itemView;

            if (viewHolder.getAdapterPosition() == -1) return;

            bgColor.setBounds(view.getRight() + (int) dX, view.getTop(), view.getRight(), view.getBottom());
            bgColor.draw(c);

            int viewHeight = view.getBottom() - view.getTop();

            int iconRight = view.getRight() - iconMargin;
            int iconLeft = iconRight - iconSize;
            int iconTop = view.getTop() + (viewHeight - iconSize)/2;
            int iconBottom = iconTop + iconSize;

            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
