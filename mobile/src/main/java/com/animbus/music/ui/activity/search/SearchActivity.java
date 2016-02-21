package com.animbus.music.ui.activity.search;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.animbus.music.R;
import com.animbus.music.media.Library;
import com.animbus.music.ui.custom.activity.ThemeActivity;
import com.animbus.music.ui.list.ListAdapter;

import java.util.List;

public class SearchActivity extends ThemeActivity {
    SearchView mSearchView;

    @Override
    protected void init() {
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void setVariables() {

    }

    @Override
    protected void setUp() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Search
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                SearchActivity.this.supportFinishAfterTransition();
                return false;
            }
        });
        menu.findItem(R.id.action_search).expandActionView();

        //Search
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                String suggestion = getSuggestion(position);
                searchView.setQuery(suggestion, true); // submit query now
                return true; // replace default search manager behaviour
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
        this.mSearchView = searchView;
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()) && intent.hasExtra(SearchManager.QUERY)) {
            search(intent.getStringExtra(SearchManager.QUERY));
        } else {
            search("");
        }
    }

    private void search(String query) {
        //Stops if the query is empty
        if (TextUtils.isEmpty(query)) {
            findViewById(R.id.search_empty_textview).setVisibility(View.GONE);
            findViewById(R.id.recycler).setVisibility(View.GONE);
            return;
        }
        //Fetches results
        List<SearchResult> results = Library.search(query);

        //Resets
        findViewById(R.id.search_empty_textview).setVisibility(!results.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.recycler).setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
        if (results.isEmpty()) return;

        ListAdapter adapter = new ListAdapter(ListAdapter.TYPE_SEARCH, results, this);
        adapter.withTransitionActivity(this);
        RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setAdapter(adapter);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private String getSuggestion(int position) {
        Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(
                position);
        return cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
    }

}
