package com.example.traz.systemstatus;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button wirelessButton;
    Button ramcpuButton;
    Button proccessButton;
    ProgressBar intmem;
    ProgressBar extmem;
    TextView intname,intused,intmax;
    TextView extname,extused,extmax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intmem=(ProgressBar) findViewById(R.id.internalbar);
        extmem=(ProgressBar) findViewById(R.id.externalBar);

        intname=(TextView) findViewById(R.id.InternalName);
        intused=(TextView) findViewById(R.id.InternalUsed);
        intmax=(TextView) findViewById(R.id.InternalMax);
        extname=(TextView) findViewById(R.id.ExternalName);
        extused=(TextView) findViewById(R.id.ExternalUsed);
        extmax=(TextView) findViewById(R.id.ExternalMAX);

        //Set title bar color
        View titleView = getWindow().findViewById(android.R.id.title);
        if (titleView != null) {
            ViewParent parent = titleView.getParent();
            if (parent != null && (parent instanceof View)) {
                View parentView = (View)parent;
                parentView.setBackgroundColor(Color.parseColor("#585c63"));
            }
        }


        //get dir with read permissions
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free_memory = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        long total_memory =stat.getBlockCountLong()* stat.getBlockSizeLong();
        long avalible_memory=total_memory-free_memory;
        //do it in MB to avoid overflow
        intmem.setMax((int)(total_memory/1048576));
        intmem.setProgress((int)(avalible_memory/1048576));


        //get device name
        intname.setText(Build.MODEL+":");
        intused.setText(String.format("%.2fGB Available",(float) free_memory / 1073741824f));
        intmax.setText(String.format("%.2fGB Total",(float) total_memory / 1073741824f));


        // SD CARD

        String sdstate= Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdstate)||Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
            path = Environment.getExternalStorageDirectory();

            stat = new StatFs(path.getPath());
            free_memory = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            total_memory =stat.getBlockCountLong()* stat.getBlockSizeLong();
            avalible_memory=total_memory-free_memory;


            extmem.setMax((int)(total_memory/1048576));
            extmem.setProgress((int)(avalible_memory/1048576));

            extname.setText("External Memory Card:");
            extused.setText(String.format("%.2fGB Available",(float) free_memory / 1073741824f));
            extmax.setText(String.format("%.2fGB Total",(float) total_memory / 1073741824f));


        }
        else{
            extmem.setVisibility(View.INVISIBLE);
            extname.setText("No SD card found. This may be erroneous");
            extused.setVisibility(View.INVISIBLE);
            extmax.setVisibility(View.INVISIBLE);
        }

        wirelessButton = (Button) findViewById(R.id.buttonWireless);
        ramcpuButton = (Button) findViewById(R.id.buttonRAMCPU);
        proccessButton = (Button) findViewById(R.id.buttonProcess);


        wirelessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Wireless.class);
                startActivity(intent);
            }
        });
        ramcpuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RAMCPUViewer.class);
                startActivity(intent);
            }
        });
        proccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ProcessViewer.class);
                startActivity(intent);
            }
        });


    }

}
