package com.example.jb.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

public class LoadingScreenThread extends AsyncTask<Void, String, Void>  {


        Activity current_activity = null;

        ProgressDialog dialog;


        // Will be called from another activity
        public LoadingScreenThread(Activity cxt) {
            current_activity = cxt;

        }


       // Runs in UI before do in Background
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(current_activity, "", "Loading..");
        }

        @Override
        protected Void doInBackground(Void... unused) {



            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            current_activity.startActivity(new Intent(current_activity, NavBar.class));

            return (null);
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(dialog !=null) {
                dialog.dismiss();
            }
             current_activity.finish();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();


        }
}
