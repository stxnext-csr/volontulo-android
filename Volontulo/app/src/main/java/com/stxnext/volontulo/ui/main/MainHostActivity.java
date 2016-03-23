package com.stxnext.volontulo.ui.main;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.stxnext.volontulo.R;
import com.stxnext.volontulo.VolontuloBaseActivity;
import com.stxnext.volontulo.ui.offers.OfferListFragment;

import butterknife.Bind;

public class MainHostActivity extends VolontuloBaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.navigation_menu)
    protected NavigationView navigationMenu;

    @Bind(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    protected ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        init(R.string.app_name);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        navigationMenu.setNavigationItemSelectedListener(this);
        navigationMenu.setCheckedItem(R.id.menu_action_list);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
            .replace(R.id.content, new OfferListFragment())
            .commit();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawers();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = NavigationDrawerFragmentFactory.create(item.getItemId());
        if (fragment != null) {
            fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
            return true;
        }
        return false;
    }
}