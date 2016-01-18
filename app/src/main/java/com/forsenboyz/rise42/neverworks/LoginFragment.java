package com.forsenboyz.rise42.neverworks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class LoginFragment extends Fragment {

    public static String IP = "192.168.0.30";
    public static int PORT = 1488;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    View fragmentHolder;    //xml root
    EditText editLogin, editPassword;
    Button buttonLogin;

    private volatile boolean switchFragment = false; //indicates, whether login is successful

    private String currentUser;


    //Required by default
    public LoginFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentHolder = inflater.inflate(R.layout.fragment_login, container, false);

        editLogin = (EditText) fragmentHolder.findViewById(R.id.editLogin);
        editPassword = (EditText) fragmentHolder.findViewById(R.id.editPassword);
        buttonLogin = (Button) fragmentHolder.findViewById(R.id.buttonLogin);

        //to0 lazy
        editLogin.setText(R.string.loginKappa);
        editPassword.setText(R.string.passKappa);

        // NEXT -> editPassword to focus
        editLogin.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editPassword.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                    return true;
                }
                return false;
            }
        });

        // launch logging on GO
        editPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    new MyLogger().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    return true;
                }
                return false;
            }
        });

        // launch logging on button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyLogger().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        log("LoginFragment created");

        return fragmentHolder;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public boolean isSwitch() {
        return switchFragment;
    }

    public static void log(String str) {
        Log.d("MY_TAG", str);
    }

    public String getCurrentUser() {
        return currentUser;
    }


    class MyLogger extends AsyncTask<Void, Boolean, Void> {

        //blocking views to prevent launching parallel logging thread
        MyLogger() {
            log("Thread created");
            buttonLogin.setEnabled(false);
            editLogin.setEnabled(false);
            editPassword.setEnabled(false);
        }


        @Override
        protected Void doInBackground(Void... params) {
            log("Thread started");
            publishProgress(loggingIn());
            log("Thread ended");
            return null;
        }


        @Override
        protected void onProgressUpdate(Boolean... values) {
            if (values[0]) {
                //switching to messaging fragment
                log("Changing fragment");
                switchFragment = true;
            } else {
                unlockLogging();
            }
        }

        //Sending login and pass and receiving feedback
        private boolean loggingIn() {
            String login = editLogin.getText().toString();
            String password = editPassword.getText().toString();

            try {
                socket = new Socket();
                log("opening socket");
                socket.connect(new InetSocketAddress(IP, PORT), 3000);

                log("opening streams");
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                log("sending login: " + login);
                out.writeUTF("_login_" + login);
                log("sending password: " + password);
                out.writeUTF("_pass_" + password);

                if(in.readBoolean()) {
                    currentUser = login;
                    return true;
                } else {
                    closeAll();
                    return false;
                }

            }  catch (SocketTimeoutException s){
                log("Connection time expired");
                s.printStackTrace();

                closeAll();

                return false;

            } catch (IOException e) {
                log("Connection failed");
                e.printStackTrace();

                closeAll();

                return false;
            }

        }

        private void closeAll(){
            try{
                log("Closing socket and streams");
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex){
                log("Closing exception, mystery!!1");
            }
        }


        //unlocking button
        private void unlockLogging() {
            Toast.makeText(getActivity(), "Logging failed", Toast.LENGTH_SHORT).show();
            log("Unlocking buttons");
            buttonLogin.setEnabled(true);
            editLogin.setEnabled(true);
            editPassword.setEnabled(true);
            editPassword.setText("");
        }

    }
}