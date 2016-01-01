package com.forsenboyz.rise42.neverworks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    Socket socket;
    private volatile boolean send = false;
    private volatile String message;
    EditText editText;
    ListView listView;
    Toolbar toolbar;
    DataBaseHandler dbHandler;
    ListAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menuClient_cleanDB){
            dbHandler.dropTable();
        }
        return super.onOptionsItemSelected(item);
    }

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

        toolbar.
        setSupportActionBar(toolbar);

        IP = getIntent().getStringExtra("ip");
        new MessageSender().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();

        try{
            socket.close();
            dbHandler.close();
        } catch (Exception e){
            e.printStackTrace();
            log("EXCEPTED");
        }

        log("onStop()");
    }

    private class MessageSender extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
            try {
                ClientActivity.log("Connecting: " + IP + " to " + 1488);
                socket = new Socket(InetAddress.getByName(IP), 1488);
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
            dbHandler.insertIncome(values[0]);
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
            return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        }

        @Override
        public void bindView(View v, Context context, Cursor cursor) {
            TextView textMessage = (TextView) v.findViewById(R.id.textMessage);

            String text = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseCreator.MESSAGE_COLUMN));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(textMessage.getLayoutParams());

            boolean income = (cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseCreator.INCOME_COLUMN))) > 0;

            log("Params = "+Boolean.toString(income));

            if(income){
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            } else{
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            textMessage.setLayoutParams(params);

            textMessage.setText(text);
        }
    }

    public static void log(String str) {
        Log.d("MY_TAG", str);
    }

}