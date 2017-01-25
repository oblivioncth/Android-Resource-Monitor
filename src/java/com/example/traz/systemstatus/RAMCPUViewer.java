package com.example.traz.systemstatus;

import android.app.ActivityManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RAMCPUViewer extends AppCompatActivity {

    //Setup Main UI
    TextView usedRAMText;
    TextView totalRAMText;
    TextView percentRAMText;
    TextView percentCPUText;
    TextView speedCPUText;
    ProgressBar percentRAMBar;
    ProgressBar percentCPUBar;
    Button buttonBack;
    LineChart percentCPUChart; //Uses "MPAndroidChart" by - PhilJay

    //Setup repeat Handler
    int mInterval = 1000; //1 second
    Handler mHandler;

    //Mem Info Setup
    ActivityManager.MemoryInfo MemInfo;
    ActivityManager activityManager;
    float availableMegs;
    float totalMegs;
    float usedMegs;
    float percentUsed;

    //CPU Usage Setup
    float cpuUsage;

    //Chart setup
    List<Entry> cpuChartEntries;
    LineDataSet cpuDataSet;
    LineData cpuData;
    int iNumOfPoints = 31; //Number of points for CPU usage graph

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ramcpuviewer);

        //Set Title
        setTitle("RAM & CPU Info");

        //Grab Text Fields, Bars, Buttons, and Chart
        usedRAMText = (TextView) findViewById(R.id.textUsedRam);
        totalRAMText = (TextView) findViewById(R.id.textTotalRam);
        percentRAMText = (TextView) findViewById(R.id.textRamPerc);
        percentCPUText = (TextView) findViewById(R.id.textCpuPerc);
        speedCPUText = (TextView) findViewById(R.id.textCPUFreq);
        percentRAMBar = (ProgressBar) findViewById(R.id.RamBar);
        percentCPUBar = (ProgressBar) findViewById(R.id.CpuBar);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        percentCPUChart = (LineChart) findViewById(R.id.chartCPUUsage);

        //Setup RAM Info Grabber
        MemInfo = new ActivityManager.MemoryInfo();
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        //Get Hardware Info
        float fSpeed = getMaxCPUFreqGHz();
        speedCPUText.setText(String.format("Max Freq: %.3fGHz",fSpeed));

        //Initialize chart with 0s
        cpuChartEntries = new ArrayList<>();
        for(int i = iNumOfPoints-1; i > -1; i--)
            cpuChartEntries.add(new Entry(-i,0));
        cpuDataSet = new LineDataSet(cpuChartEntries, "");

        //Finalize chart setup (mostly formatting and styling)
        cpuData = new LineData(cpuDataSet);
        percentCPUChart.setData(cpuData);
        Description description = new Description();
        description.setText("");
        percentCPUChart.setDescription(description);
        percentCPUChart.getAxisRight().setDrawLabels(false);
        percentCPUChart.getAxisRight().setDrawGridLines(false);
        percentCPUChart.getAxisLeft().setAxisMinimum(0);
        percentCPUChart.getAxisLeft().setAxisMaximum(100);
        percentCPUChart.getLegend().setEnabled(false);
        cpuDataSet.setDrawValues(false);
        cpuDataSet.setColors(new int[] { R.color.colorAccent}, this);
        cpuDataSet.setDrawFilled(true);
        cpuDataSet.setDrawCircles(false);
        cpuDataSet.setFillColor(R.color.colorAccent); //Bug with chart library makes it primary app color instead of red but it looks ok so whatever
        XAxis xAxis = percentCPUChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        percentCPUChart.invalidate();

        //Initialize repeat Handler and start repeating task
        mHandler = new Handler();
        startRepeatingTask();

    }

    @Override
    public void onDestroy() {    //Keeps polling from continuing on activity exit
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus(); //Page update function
            } finally {
                //Pushes change even if exception (for safety, not covering up bug)
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    void updateStatus()
    {
        //Read RAM
        activityManager.getMemoryInfo(MemInfo);
        availableMegs = MemInfo.availMem / 1048576L;
        totalMegs = MemInfo.totalMem / 1048576L;
        usedMegs = totalMegs - availableMegs;
        percentUsed = usedMegs/ totalMegs;

        //Read CPU
        cpuUsage = readCPUUsage();

        //Update Main
        usedRAMText.setText(String.format("%.0fMB Used",usedMegs));
        totalRAMText.setText(String.format("%.0fMB Total",totalMegs));
        percentRAMBar.setMax((int) totalMegs);
        percentRAMBar.setProgress((int) usedMegs);
        percentRAMText.setText(String.format("Utilization: %.1f%%",percentUsed * 100));

        percentCPUBar.setMax(100);
        percentCPUBar.setProgress((int) (cpuUsage*100));
        percentCPUText.setText(String.format("Utilization: %.1f%%",cpuUsage * 100));

        //Update Chart
        for(int i = 0; i < iNumOfPoints - 1; i++)
        {
            float fY= cpuChartEntries.get(i+1).getY();
            cpuChartEntries.get(i).setY(fY);
        }
        cpuChartEntries.get(iNumOfPoints-1).setY(cpuUsage * 100);
        percentCPUChart.invalidate();
    }

    float readCPUUsage() {
        try {
            //Acess process statistics file
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            //Read file and measure time + idle time
            String[] toks = load.split(" ");
            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            //Wait 0.360 seconds
            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            //Reset to front of file and set reader again
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");

            //Read file again and measure new time + idle time
            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            //Compare time spent on system tasks in two different instances to abstract CPU usage
            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) { //For safety
            ex.printStackTrace();
        }

        return 0;
    }

    static float getMaxCPUFreqGHz() { //Reads system CPU state stats for get max frequency

        //Initialize max frequency as -1
        int maxFreq = -1;

        try {
            //Select processor states frequency stats file
            RandomAccessFile reader = new RandomAccessFile( "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state", "r" );
            boolean done = false;

            //Read lines till end of file
            while ( ! done ) {
                String line = reader.readLine();
                if ( null == line ) {
                    done = true;
                    break;
                }

                //Parse file format
                String[] splits = line.split( "\\s+" );
                assert ( splits.length == 2 );
                int timeInState = Integer.parseInt( splits[1] );

                //Ignore lines that report 0 time spent in that power state
                if ( timeInState > 0 ) {
                    int freq = Integer.parseInt( splits[0] ) / 1000;

                    //Only update max frequency if its higher than a previous power state
                    if ( freq > maxFreq ) {
                        maxFreq = freq;
                    }
                }
            }

        } catch ( IOException ex ) { //For safety
            ex.printStackTrace();
        }

        //Format to GHz
        float fSpeedGHz = ((float) maxFreq)/1000;
        return fSpeedGHz;
    }

}