package com.github.rocketmax.rpiconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    SocketVideo sv = new SocketVideo(MainActivity.this, "192.168.1.114", 65432);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonClicked(View v){
        int id = v.getId();

        if(id == R.id.bConnect)
            sv.startSocket();

        else if(id == R.id.bDisconnect)
            sv.stopSocket();

        else if(id == R.id.bStart){
            if(!sv.startStream()) {
                TextView tv = findViewById(R.id.tv);
                tv.setText("Err");
            }
        }

        else if(id == R.id.bStop)
            sv.endStream();
    }
}
