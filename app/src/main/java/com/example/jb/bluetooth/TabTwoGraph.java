package com.example.jb.bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TabTwoGraph extends AppCompatActivity {

    View v;
    List<Float> ble = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_two_graph);

       // Intent intent = getParent().getIntent();
       // v = intent.getParcelableExtra("view");

        Intent intent = getIntent();
        ble.addAll((Collection<? extends Float>) intent.getSerializableExtra("m"));

        Button b = (Button) findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(TabTwoGraph.this, AlertPage.class);
                i.putExtra("m", (Serializable) ble);
               startActivity(i);
            }
        });

    }





    @Override
    public void onBackPressed() {

    }

}
