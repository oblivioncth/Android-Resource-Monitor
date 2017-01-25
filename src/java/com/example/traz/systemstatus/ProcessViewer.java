package com.example.traz.systemstatus;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static android.graphics.Color.BLUE;

public class ProcessViewer extends AppCompatActivity {

    List<AndroidAppProcess> processes; //Uses "AndroidProcesses" by - jaredrummler
    PackageManager pm;
    String pNameList[];
    String aNameList[];


    //Setup UI elements
    ListView processListView;
    Button buttonBack;
    ProgressBar taskProgress;
    TextView descriptorText;
    ImageView separatorOneImage;
    ImageView separatorTwoImage;


    //Setup activity context
    Activity mainContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_viewer);

        //Get activity context
        mainContext = this;

        //Set Title
        setTitle("Process List");

        //Set Load Text
        descriptorText = (TextView) findViewById(R.id.textProcesses);
        descriptorText.setText("PLEASE WAIT\nLoading process list...");

        //Set Back Button
        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Hide separator bars
        separatorOneImage = (ImageView) findViewById(R.id.imageSeparatorOne);
        separatorTwoImage = (ImageView) findViewById(R.id.imageSeparatorTwo);
        separatorOneImage.setVisibility(View.GONE);
        separatorTwoImage.setVisibility(View.GONE);

        //Start background asynctask
        new MyTask().execute();
    }


    private class MyTask extends AsyncTask<Void, Integer, Void> { //Asynctask custom class

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Grab progress bar
            taskProgress = (ProgressBar) findViewById(R.id.loadBar);
        }

        @Override
        protected Void doInBackground(Void... params) {

            //Get Process list
            processes = AndroidProcesses.getRunningAppProcesses();
            pm = getApplicationContext().getPackageManager();

            //Allocate informational arrays
            pNameList = new String[processes.size()];
            aNameList = new String[processes.size()];

            //Fill arrays
            for(int i = 0; i < processes.size(); i++)
            {
                //Grab process "i" and initialize info as null
                AndroidAppProcess process = processes.get(i);
                String processName = process.name;
                PackageInfo packageInfo = null;
                Stat stat = null;
                int PID;

                //Generate stat object, try statement required by android studio to compile
                try {
                    stat = process.stat();
                } catch (IOException ex){
                    ex.printStackTrace();
                }

                //Generate package info object, try statement required by android studio to compile
                try {
                    packageInfo = process.getPackageInfo(mainContext, 0);
                } catch (PackageManager.NameNotFoundException ex){
                    ex.printStackTrace();
                }

                //Get app name, if app name is the same as process name then mark it as a system process
                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                if(processName.compareTo(appName) == 0)
                    appName = "SYSTEM";

                //Get process ID, some system processes don't have one so set those to -1
                try {
                    PID = stat.getPid();
                } catch (NullPointerException ex){
                    PID = - 1;
                }

                //Populate arrays for listview, set processes with PID = -1 to PID = "NONE"
                pNameList[i] = processName;
                if(PID == -1)
                    aNameList[i] = appName + " (PID: NONE)";
                else
                    aNameList[i] = appName + " (PID: " + Integer.toString(PID) + ")";

                //Update progress bar for asynctask
                publishProgress(i);
            }

            //Unused - required for doInBackground override
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);

            //Update progress bar
            taskProgress = (ProgressBar) findViewById(R.id.loadBar);
            taskProgress.setMax(processes.size());
            taskProgress.setProgress(value[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //Prepare List
            processListView = (ListView) findViewById(R.id.listProcesses);

            //Change load text to descriptor text
            descriptorText.setText("App Name (PID)\nProcess Name");

            //Hide progress bar and reveal separators
            taskProgress.setVisibility(View.GONE);
            separatorOneImage.setVisibility(View.VISIBLE);
            separatorTwoImage.setVisibility(View.VISIBLE);

            //Create and override array adapter for two text views instead of one
            ArrayAdapter<String> adapter = new ArrayAdapter(mainContext, android.R.layout.simple_list_item_2, android.R.id.text1, pNameList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(aNameList[position]);
                    text2.setText(pNameList[position]);
                    return view;
                }
            };

            //Set adapter and end asynctask
            processListView.setAdapter(adapter);
        }
    }
}

