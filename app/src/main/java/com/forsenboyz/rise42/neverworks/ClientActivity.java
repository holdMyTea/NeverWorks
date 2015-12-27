package com.forsenboyz.rise42.neverworks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientActivity extends AppCompatActivity {

    private String IP = "93.73.130.108";
    volatile private boolean send = false;
    volatile private String message;
    EditText editText;
    ListView listView;
    DataBaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        listView = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.editTextMessage);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    String buff = editText.getText().toString();
                    if (buff == "") {
                        message = "";
                        Toast.makeText(ClientActivity.this, "Wrt smth, asshl", Toast.LENGTH_SHORT).show();
                        send = false;
                    } else {
                        message = buff;
                        send = true;
                        editText.setText("");
                    }
                }
                return false;
            }
        });


        IP = getIntent().getStringExtra("ip");
        new Running().execute();
    }

    private class Running extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
            try {
                log("Connecting: " + IP + " to " + 1488);
                Socket socket = new Socket(InetAddress.getByName(IP), 1488);
                log("Connected");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                new Thread(new MessageReicever(out)).start();

                while (true) {
                    response = in.readUTF();
                    if (!response.isEmpty()) {
                        publishProgress(response);
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //TODO: some shit with listView
            //listView.setText(values[0]);
        }
    }

    private class MessageReicever implements Runnable {
        DataOutputStream out;

        MessageReicever(DataOutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            log("Runned send stream");
            try {
                while (true) {
                    if (send) {
                        log("Sending: " + message);
                        out.writeUTF(message);
                        out.flush();
                        message = "";
                        send = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String str) {
        Log.d("MY_TAG", str);
    }

}