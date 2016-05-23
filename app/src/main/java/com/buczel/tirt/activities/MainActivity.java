package com.buczel.tirt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.buczel.tirt.R;
import com.buczel.tirt.SenderClass;
import com.buczel.tirt.VideoStream;

public class MainActivity extends Activity {

    Button videoStream, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        videoStream = (Button) findViewById(R.id.videoStream);
        settings = (Button) findViewById(R.id.settings);

        final SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String ipValueLocal = sharedPref.getString("ipValue", "192.168.43.249");
        SenderClass.setIP(ipValueLocal);

        videoStream.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoStream.class);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

    }
}
