package ch.papers.androidcommunicationbenchmark.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ch.papers.androidcommunicationbenchmark.R;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.UuidObjectStorage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UuidObjectStorage.getInstance().init(this);
        Preferences.getInstance().init(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);        // Drawer object Assigned to the view
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();


        final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, BenchmarkResultListFragment.newInstance());
        transaction.commit();


        NavigationView navigationView = (NavigationView) this.findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.navigation_list:
                        transaction.replace(R.id.container, BenchmarkResultListFragment.newInstance());
                        break;
                    case R.id.navigation_benchmark_bluetooth_rfcomm:
                        transaction.replace(R.id.container, BenchmarkRunnerFragment.newInstance(BenchmarkResult.ConnectionTechonology.BLUETOOTH_RFCOMM));
                        break;
                    case R.id.navigation_benchmark_wifi:
                        transaction.replace(R.id.container, BenchmarkRunnerFragment.newInstance(BenchmarkResult.ConnectionTechonology.WIFI));
                        break;
                    case R.id.navigation_benchmark_nfc:
                        transaction.replace(R.id.container, BenchmarkRunnerFragment.newInstance(BenchmarkResult.ConnectionTechonology.NFC));
                        break;
                    case R.id.navigation_benchmark_bluetooth_le:
                        transaction.replace(R.id.container, BenchmarkRunnerFragment.newInstance(BenchmarkResult.ConnectionTechonology.BLUETOOTH_LE));
                        break;
                    case R.id.navigation_preferences:
                        transaction.replace(R.id.container, PreferencesFragment.newInstance());
                        break;
                }
                transaction.commit();
                drawer.closeDrawers();
                return true;
            }
        });
    }
}


