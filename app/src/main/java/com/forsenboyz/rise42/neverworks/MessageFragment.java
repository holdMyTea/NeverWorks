package com.forsenboyz.rise42.neverworks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class MessageFragment extends Fragment {

    View fragmentHolder;
    ListView listView;
    EditText editText;
    Button sendButton;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    DataBaseHandler dbHandler;
    ListAdapter adapter;

    private String currentUser;


    // Required empty public constructor
    public MessageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHandler = new DataBaseHandler(getActivity());
        dbHandler.setCurrentUser(currentUser);

        fragmentHolder = inflater.inflate(R.layout.fragment_message, container, false);

        listView = (ListView) fragmentHolder.findViewById(R.id.listView);
        adapter = new ListAdapter(getActivity(),dbHandler.getAllRows());
        listView.setAdapter(adapter);

        editText = (EditText) fragmentHolder.findViewById(R.id.editTextMessage);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    sendMessage();
                }
                return false;
            }
        });

        sendButton = (Button) fragmentHolder.findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        new MessageReceiver().execute();

        return  fragmentHolder;
    }

    private void sendMessage(){
        String buff = editText.getText().toString();
        if (buff.isEmpty()) {
            Toast.makeText(getActivity(), "Not sent, nothing to send", Toast.LENGTH_SHORT).show();
        } else {
            new Thread(new MessageSender(buff)).start();
            editText.setText("");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_message,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menuDropDB){
            dbHandler.dropTable();

            adapter.changeCursor(dbHandler.getAllRows()); //updating cursor for adapter
            listView.setAdapter(adapter); //without this wrong params applied
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        log("Message user: "+this.currentUser);
    }

    public static void log(String str) {
        Log.d("MY_TAG", str);
    }


    //just sends the message, verification is sent by server
    private class MessageSender implements Runnable{
        String message;

        MessageSender(String message){
            this.message = message;
        }

        @Override
        public void run() {
            try {
                message = "_"+currentUser+"_"+message;
                log("Sending message");
                out.writeUTF(message);
            } catch(Exception e){
                log("Sending failed");
                e.printStackTrace();
            }
        }
    }


    private class MessageReceiver extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
            try {
                while (true) {
                    response = in.readUTF();
                    log(response);
                    if (!response.isEmpty()) {
                        log("Income update: "+response);
                        publishProgress(response);
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            log("Updating database");

            int messageBeginning = values[0].indexOf('_',2);

            String from = values[0].substring(1, messageBeginning);
            String message = values[0].substring(messageBeginning+1);

            log("Is "+from+" equals to "+currentUser+"?");
            if(from.equals(currentUser)){
                log("Inserting outcome: "+message);
                dbHandler.insertOutcome(message);
            }
            else{
                log("Inserting income from "+from+": "+message);
                dbHandler.insertRow(from,message);
            }
            adapter.changeCursor(dbHandler.getAllRows()); //updating cursor for adapter
            listView.setAdapter(adapter); //without this wrong params applied
        }
    }


    private class ListAdapter extends CursorAdapter {

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
            TextView textSender = (TextView) v.findViewById(R.id.textSender);

            String from = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseCreator.SENDER_COLUMN));
            String message = cursor.getString(cursor.getColumnIndexOrThrow(DataBaseCreator.MESSAGE_COLUMN));

            //log("Is "+from+" equals to "+currentUser+"?");

            if(currentUser.equals(from)){
                //log("Yes, Applying outcome params");
                RelativeLayout.LayoutParams incomeParams = new RelativeLayout.LayoutParams(textMessage.getLayoutParams());
                incomeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textMessage.setLayoutParams(incomeParams);

                textSender.setVisibility(View.GONE);
            } else{
                //log("No, Applying income params");
                RelativeLayout.LayoutParams outcomeParams = new RelativeLayout.LayoutParams(textSender.getLayoutParams());
                outcomeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                textSender.setLayoutParams(outcomeParams);
            }

            textSender.setText(from);
            textMessage.setText(message);
        }
    }
}