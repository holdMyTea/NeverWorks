package com.forsenboyz.rise42.neverworks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHandler = new DataBaseHandler(getActivity());

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
        dbHandler.setCurrentUser(currentUser);
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
                        log("Income update");
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

            log("message from "+from+" : "+message);

            if(from.equals(currentUser)){
                // ':' indicates that message was sent by this client
                dbHandler.insertOutcome(message);
            }
            else{
                //TODO: different user messages
                dbHandler.insertRow(from,message);
            }
            adapter.changeCursor(dbHandler.getAllRows());
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

            boolean outcome = (cursor.getString(cursor.getColumnIndexOrThrow(DataBaseCreator.SENDER_COLUMN))).equals("_me");

            if(outcome){
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(textMessage.getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textMessage.setLayoutParams(params);
            } else{
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(textSender.getLayoutParams());
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                textSender.setLayoutParams(params);
            }

            textSender.setText(from);
            textMessage.setText(message);
        }
    }
}