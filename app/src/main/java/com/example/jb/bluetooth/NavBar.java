package com.example.jb.bluetooth;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.example.jb.bluetooth.bleCheck.BluetoothCheck;


public class NavBar extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    TabHost tabHost;

    LocalActivityManager mLocalActivityManager;

    Context context;

   // BluetoothDevice selected_ble;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = NavBar.this;

        // Setting the Title of the NAV BAR
        getSupportActionBar().setTitle("M100 Treatment Tracker ");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        // Start Service intent
        Intent intent = new Intent(NavBar.this,BleServiceBackground.class);
        startService(intent);

        // Get BLE Device
        //selected_ble = getIntent().getExtras().getParcelable("ble_device");



        if(savedInstanceState == null) {

            DeviceSetting setting = new DeviceSetting();
            FragmentManager manager = getSupportFragmentManager();
            getSupportActionBar().setTitle("Info or Change");
            manager.beginTransaction().add(R.id.content_nav_bar,setting,"settings");

            EmsFragment ems = new EmsFragment();
            manager = getSupportFragmentManager();
            getSupportActionBar().setTitle("Electrode Sensitivity");
            manager.beginTransaction().add(R.id.content_nav_bar,ems,"ems");

            TestFragment test_f = new TestFragment();
             manager = getSupportFragmentManager();
            getSupportActionBar().setTitle("M100 Treatment Tracker");
            manager.beginTransaction().add(R.id.content_nav_bar,test_f,"main").commit();
        }




   }


    @Override
    public void onBackPressed() {

        /* Do nothing if back button is Pressed

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        */

        //moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {



        //    return true;
       // }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {

            // Main Fragment
            if(getSupportFragmentManager().findFragmentByTag("main")== null)
            {
                TestFragment test_f = new TestFragment();
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("M100 Treatment Tracker");
                manager.beginTransaction().add(R.id.content_nav_bar,test_f,"main").commit();
            }
            else{
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("M100 Treatment Tracker");
                manager.beginTransaction().show(getSupportFragmentManager().findFragmentByTag("main")).commit();
            }
          // EMS Fragment Detach
            if(getSupportFragmentManager().findFragmentByTag("ems") != null)
            {
                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("ems")).commit();
            }
           // Settings
            if(getSupportFragmentManager().findFragmentByTag("settings") != null)
            {
                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("settings")).commit();
            }

        }
        else if (id == R.id.nav_view) {
            // EMS Fragment Detach
            if(getSupportFragmentManager().findFragmentByTag("ems")== null)
            {
                EmsFragment ems = new EmsFragment();
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("Electrode Sensitivity");
                manager.beginTransaction().add(R.id.content_nav_bar,ems,"ems").commit();
            }
            else{
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("Electrode Sensitivity");
                manager.beginTransaction().show(getSupportFragmentManager().findFragmentByTag("ems")).commit();
            }
           // Main Fragment
            if(getSupportFragmentManager().findFragmentByTag("main") != null)
            {

                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("main")).commit();
            }
           // Settings
            if(getSupportFragmentManager().findFragmentByTag("settings") != null)
            {
                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("settings")).commit();
            }

        }


        else if (id== R.id.nav_settings)
        {
            // Settings
            if(getSupportFragmentManager().findFragmentByTag("settings")== null)
            {
                DeviceSetting setting = new DeviceSetting();
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("Info or Change");
                manager.beginTransaction().add(R.id.content_nav_bar,setting,"settings").commit();
            }
            else{
                FragmentManager manager = getSupportFragmentManager();
                getSupportActionBar().setTitle("Info or Change");
                manager.beginTransaction().show(getSupportFragmentManager().findFragmentByTag("settings")).commit();
            }
            // Main fragment
            if(getSupportFragmentManager().findFragmentByTag("main") != null)
            {
                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("main")).commit();
            }

            // EMS Fragment Detach
            if(getSupportFragmentManager().findFragmentByTag("ems") != null)
            {
                FragmentManager manager =  getSupportFragmentManager();
                manager.beginTransaction().hide(getSupportFragmentManager().findFragmentByTag("ems")).commit();
            }

        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        BluetoothCheck objBle = new BluetoothCheck();
        if(!objBle.isBluetoothOn(context))
        {
            new AlertDialog.Builder(context)
                    .setTitle("BLUETOOTH")
                    .setMessage("Please turn on your Phone Bluetooth")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                           finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        //mLocalActivityManager.dispatchResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        //mLocalActivityManager.dispatchPause(isFinishing());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager.findFragmentByTag("ems") != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("ems")).commit();
        }
        if(fragmentManager.findFragmentByTag("main") != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("main")).commit();
        }
        if(fragmentManager.findFragmentByTag("settings") != null){
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag("settings")).commit();
        }

    }



}


