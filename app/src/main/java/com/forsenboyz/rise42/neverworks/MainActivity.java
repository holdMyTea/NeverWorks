package com.forsenboyz.rise42.neverworks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonClient = (Button) findViewById(R.id.buttonClient);
        Button buttonServer = (Button) findViewById(R.id.buttonServer);
        editText = (EditText) findViewById(R.id.editText2);

        buttonClient.setOnClickListener(this);
        buttonServer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int v = view.getId();
        Intent intent;
        if(v==R.id.buttonClient){
            intent = new Intent(this, ClientActivity.class);
            intent.putExtra("ip",editText.getText().toString());
            startActivity(intent);
        }
        else if(v==R.id.buttonServer){
            intent = new Intent(this, ServerActivity.class);
            startActivity(intent);
        }
    }
}