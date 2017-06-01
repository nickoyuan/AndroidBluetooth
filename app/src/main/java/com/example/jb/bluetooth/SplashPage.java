package com.example.jb.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Set;

public class SplashPage extends AppCompatActivity  {

    TextView sis;
    Context context;
    BluetoothAdapter mBluetoothAdapter;

    boolean isErrorFree = true;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    String changeFirmware = "1.0";

    // Remote Config key
    private static final String LOADING_KEY = "firmware_update_enable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_page);

        context = SplashPage.this;

        /*
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
       else{
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            // Remember that you should never show the action bar if the
            // status bar is hidden, so hide that too if necessary.
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
          //Activity is inheriting from the new android.support.v7.app.ActionBarActivity.
            // You should be using a call to getSupportActionBar() instead of getActionBar().

            actionBar.hide();
        }
        */

        sis = (TextView) findViewById(R.id.sis);
        Circle circle = (Circle) findViewById(R.id.circle);

        float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 270, getResources().getDisplayMetrics());
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 270, getResources().getDisplayMetrics());

        // Setting Height and Width Depending upon the Screen Size
         circle.getLayoutParams().height = (int) ht_px;
         circle.getLayoutParams().width = (int) wt_px;



        float sis_txt = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 130, getResources().getDisplayMetrics());
        sis.setTextSize(sis_txt);




        CircleAngleAnimation animation = new CircleAngleAnimation(circle, 360);
        animation.setDuration(2000);
        circle.startAnimation(animation);



        TextView iv = (TextView) findViewById(R.id.sis);

        AnimationSet mAnimationSet = new AnimationSet(false);
        // Translate animation
        Animation animfade = AnimationUtils.loadAnimation(this, R.anim.alpha);
        Animation translateanim = AnimationUtils.loadAnimation(this, R.anim.translate);


        animfade.reset();
        translateanim.reset();

        iv.clearAnimation();
        mAnimationSet.addAnimation(animfade);
        mAnimationSet.addAnimation(translateanim);

        iv.startAnimation(mAnimationSet);

        TextView website = (TextView) findViewById(R.id.website);
        Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(4000);
        website.startAnimation(in);




        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            isErrorFree = false;
            // If no BLE
            new AlertDialog.Builder(context)
                    .setTitle("Phone Too Outdated!")
                    .setMessage("Sorry! Your Phone does not SUPPORT BLE")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Quit activity
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // IF BLUETOOTH IS TURNED ON !
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            isErrorFree = false;
            // If no BLE
            new AlertDialog.Builder(context)
                    .setTitle("BLUETOOTH")
                    .setMessage("Please turn on your Phone and M100 Bluetooth")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            int REQUEST_PERMISSION_PHONE_STATE=1;

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    isErrorFree = false;
                    // We need this Permission
                    new AlertDialog.Builder(context)
                            .setTitle("BLUETOOTH PERMISSION")
                            .setMessage("Please go into your phone settings to PERMIT ACCESS TO THIS APP")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_PHONE_STATE);
                }

            } else {
                Toast.makeText(SplashPage.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            int REQUEST_PERMISSION_PHONE_STATE=1;

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                    isErrorFree = false;
                    // We need this Permission
                    new AlertDialog.Builder(context)
                            .setTitle("BLUETOOTH PERMISSION")
                            .setMessage("Please go into your phone settings to PERMIT ACCESS TO THIS APP")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_PHONE_STATE);
                }

            } else {
                Toast.makeText(SplashPage.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
            }
        }





        if(isErrorFree)
        {

            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build();
            mFirebaseRemoteConfig.setConfigSettings(configSettings);

            long cacheExpiration = 3600; // 1 hour in seconds.
            // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
            // retrieve values from the service.
            if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                cacheExpiration = 0;
            }

            // [START fetch_config_with_callback]
            // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
            // will use fetch data from the Remote Config service, rather than cached parameter values,
            // if cached parameter values are more than cacheExpiration seconds old.
            mFirebaseRemoteConfig.fetch(cacheExpiration)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // After config data is successfully fetched, it must be activated before newly fetched
                                // values are returned.
                                mFirebaseRemoteConfig.activateFetched();

                                // Remote data is Bool value
                                changeFirmware  =  mFirebaseRemoteConfig.getString(LOADING_KEY);

                                // If it isn't version 1.0
                                if(!changeFirmware.equals("1.0")) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("NEW FIRMWARE M100 AVALIABLE ")
                                            .setMessage("Please download new app version from Google Play Store")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                }
                                startProcedure();
                            } else {
                                  // Fetch failed, maybe no internet !
                                startProcedure();
                            }
                        }
                    });
        }
    }


    public void startProcedure(){
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {


                // This method will be executed once the timer is over
                // Start your app main activity



                boolean trigger = false;


                final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();


                mBluetoothAdapter.cancelDiscovery();

                if (bondedDevices.size() > 0) {

                    for (BluetoothDevice device : bondedDevices) {
                        String device_name = device.getName().toLowerCase();

                        if (device_name.contains("electromed")) {
                            trigger = true;
                            break;
                        }
                    }

                }



                if (trigger) {

                    Intent i = new Intent(SplashPage.this, NavBar.class);
                    goToNextActivity(i, R.anim.fadein, R.anim.fadeout);
                    //startActivity(i);
                    // close this activity

                } else {
                    Intent i = new Intent(SplashPage.this, MainActivity.class);
                    goToNextActivity(i, R.anim.fadein, R.anim.fadeout);
                }
                finish();

            }


        }, 2000);

    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }



    private void goToNextActivity(Intent intent, int animationIn, int animationOut) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //Call immediately after one of the flavors of startActivity(Intent) or finish() to
        // specify an explicit transition animation to perform next.
        overridePendingTransition(animationIn, animationOut);
    }


    /*
       If APP is in Foreground and then the User re-enters after enabling Bluetooth
    */
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}


