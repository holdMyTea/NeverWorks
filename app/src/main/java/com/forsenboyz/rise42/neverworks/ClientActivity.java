package com.forsenboyz.rise42.neverworks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CursorAdapter;
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
    private volatile boolean send = false;
    private volatile String message;
    EditText editText;
    ListView listView;
    DataBaseHandler dbHandler;
    ListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        log("Client started");

        dbHandler = new DataBaseHandler(this);

        listView = (ListView) findViewById(R.id.listView);

        adapter = new ListAdapter(this,dbHandler.getAllRows());
        listView.setAdapter(adapter);

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
                        dbHandler.insertOutcome(message);
                        //TODO: HERE THE MAGIC COMES (prbbl not)
                        ClientActivity.log("Cursor magic");
                        ClientActivity.log("DataBasing outcome");
                        adapter.changeCursor(dbHandler.getAllRows());
                        send = true;
                        editText.setText("");
                    }
                }
                return false;
            }
        });


        IP = getIntent().getStringExtra("ip");
        new MessageSender().execute();
    }

    private class MessageSender extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
            try {
                ClientActivity.log("Connecting: " + IP + " to " + 1488);
                Socket socket = new Socket(InetAddress.getByName(IP), 1488);
                ClientActivity.log("Connected");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                new Thread(new MessageReicever(out)).start();

                while (true) {
                    response = in.readUTF();
                    if (!response.isEmpty()) {
                        ClientActivity.log("DataBasing income");
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
            log("Income update");
            dbHandler.insertOutcome(values[0]);
            adapter.changeCursor(dbHandler.getAllRows());
        }
    }

    private class MessageReicever implements Runnable {
        DataOutputStream out;

        MessageReicever(DataOutputStream out) {
            this.out = out;
        }

        @Override
        public void run() {
            ClientActivity.log("Runned send stream");
            try {
                while (true) {
                    if (send) {
                        ClientActivity.log("Sending: " + message);
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

    private class ListAdapter extends CursorAdapter{

        public ListAdapter(Context context,Cursor c){
            super(context,c,0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.list_item,parent,false);
            log(v.toString());
            return v;
            //return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        }

        @Override
        public void bindView(View v, Context context, Cursor cursor) {
            TextView textId = (TextView) v.findViewById(R.id.textID);
            TextView textMessage = (TextView) v.findViewById(R.id.textMessage);

            String id = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseCreator.ID_COLUMN)));
            String text = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseCreator.MESSAGE_COLUMN));

            textId.setText(id);
            textMessage.setText(text);
        }
    }

    public static void log(String str) {
        Log.d("MY_TAG", str);
    }

}