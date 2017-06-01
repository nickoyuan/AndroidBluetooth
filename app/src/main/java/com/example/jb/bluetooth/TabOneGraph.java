package com.example.jb.bluetooth;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class TabOneGraph extends AppCompatActivity {

   LineChart mChart;

   // private final String[] mLabels = {"1", "2", "3", "4", "5", "6", "7", "8", "9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30"};

   // private final float[] mValues = {3.5f, 4.7f, 4.3f, 8f, 6.5f, 9.9f, 7f, 8.3f, 7.0f,1.0f,2.0f,3.1f,5.0f,3.0f,12.0f,9.0f,1.9f,20f,1.2f,2.0f,3.0f,22f,2.3f,23f,1.2f,12f,12f,12f,13f,13f};

  //  private final float[] mValue = {3.0f, 3f, 3f, 4f, 4.5f, 4.9f, 3f, 3.3f, 3.0f,3.0f,3.0f,3.1f};


    List<Double> ble = new ArrayList<>();

    LineData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_one_graph);

        TextView timerlayout = (TextView) findViewById(R.id.resettxt);
        float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        timerlayout.getLayoutParams().height = (int) ht_px;


        mChart = (LineChart) findViewById(R.id.line);

        List<Entry> entries = new ArrayList<Entry>();

        List<String> numbers = new ArrayList<String>();


        Intent intent = getIntent();
        ble.addAll((Collection<? extends Double>) intent.getSerializableExtra("m"));

        for(int i=0;i< ble.size();i++)
        {
            entries.add(new Entry(ble.get(i).floatValue(),i));

           // numbers.add(Integer.toString(i));
        }


        for(int x=0; x< ble.size()+1; x++)
        {
            numbers.add(Integer.toString(x));
        }



        String colourstr = "#14B9D6";
        int color = Integer.parseInt(colourstr.replaceFirst("^#",""), 16);

        String line_colour = "#1976D2";
        int strs = Integer.parseInt(colourstr.replaceFirst("^#",""), 16);



        LineDataSet dataSet = new LineDataSet(entries,"Data No.");

        dataSet.setDrawCubic(false);

        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        dataSet.setColor(Color.rgb(19,215,205));
        dataSet.setCircleColor(Color.WHITE);

        // Width of the line thickness above the previous line
        dataSet.setLineWidth(0);

        dataSet.setCircleSize(3f);
        dataSet.setDrawCircles(false);
        dataSet.setFillAlpha(255);
        // Filling of the Graph Color
        dataSet.setDrawFilled(true);

        // Line of the graph
        dataSet.setFillColor(color);
        dataSet.setDrawCircleHole(false);



         // Drawing the DATA Sets !!!
        dataSet.setDrawValues(false);




         data = new LineData(numbers,dataSet);



        mChart.setData(data);
        mChart.getXAxis().setDrawGridLines(false); // disable grid lines for the XAxis
        mChart.getAxisLeft().setDrawGridLines(false); // disable grid lines for the left YAxis
        mChart.getAxisRight().setDrawGridLines(false); // disable grid lines for the right YAxis
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Moved XAxis position to bottom
        mChart.getAxisRight().setDrawLabels(false);

        // Legends
        mChart.getLegend().setEnabled(true);
        mChart.setDescription(" ");


        data.notifyDataChanged();
        mChart.notifyDataSetChanged(); // let the chart know it's data changed or else APP crashes
        mChart.fitScreen();


        // Animation
        //mChart.animateX(2000);
       // mChart.animateY(2000);


        // Setting Min and Max scalling for Y Axis
        //mChart.setAutoScaleMinMaxEnabled(true);

        // Allow Zooming when pinching
        mChart.setScaleEnabled(true);
        mChart.setDoubleTapToZoomEnabled(false);


        //Enable gestures on the chart
        mChart.setTouchEnabled(true);







        CustomMarkerView mv = new CustomMarkerView(this, R.layout.custom_marker);
        mChart.setMarkerView(mv);

        mChart.getAxisRight().setEnabled(false);
        /*
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                // display msg when value selected
                if (e == null)
                    return;

                mChart.highlightValue(e.getXIndex(), 0);
            }

            @Override
            public void onNothingSelected() {

            }
        });
        */


        LimitLine limit_y = new LimitLine(1f,"Lower Limit");
        limit_y.setLineWidth(4f);
        limit_y.enableDashedLine(10f,10f,10f);
        limit_y.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        limit_y.setTextSize(10f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(limit_y);
        leftAxis.setDrawAxisLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(false);

        leftAxis.setValueFormatter(new PercentFormatter());
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);


        Legend l = mChart.getLegend();
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setForm(Legend.LegendForm.LINE); // set what type of form/shape should be used
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_CENTER);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis



    }



  public void reset_zoom(View v){

      mChart.fitScreen();
      mChart.invalidate(); // refresh
      mChart.animateX(1000);
      mChart.animateY(1000);
  }

    // Do nothing if you press the back button below the phone !
    // Makes sure that it doesn't freeze or crash
    @Override
    public void onBackPressed() {

    }


}





