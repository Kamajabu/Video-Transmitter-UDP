package com.buczel.tirt.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.buczel.tirt.R;
import com.buczel.tirt.SenderClass;

public class SettingsActivity extends Activity {

    private Button btnAccept;
    private Button btnCancel;
    private EditText inputIP;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        inputIP = (EditText) findViewById(R.id.IP_address);

        btnAccept = (Button) findViewById(R.id.btnAccept);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        final SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        String ipValueLocal = sharedPref.getString("ipValue", "192.168.43.249");
        SenderClass.setIP(ipValueLocal);

        inputIP.setText(SenderClass.getIP());

        btnAccept.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String IPadress = inputIP.getText().toString();

              if (Patterns.IP_ADDRESS.matcher(IPadress).matches()){
                  editor.putString("ipValue", IPadress);
                  editor.commit();
                  SenderClass.setIP(IPadress);
                  finish();

              } else
                  Toast.makeText(SettingsActivity.this, "Wrong IP format", Toast.LENGTH_SHORT).show();

            }

        });


        btnCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
//                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
//                startActivity(intent);
                finish();
            }

        });
    }


}