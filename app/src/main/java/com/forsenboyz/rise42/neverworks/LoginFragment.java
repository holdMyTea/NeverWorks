package com.forsenboyz.rise42.neverworks;

import android.content.Context;
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

    public UserSample[] samples = new UserSample[]{
            new UserSample(1488, "Kappa", "passKappa"),
            new UserSample(1489, "Keepo", "passKeepo"),
            new UserSample(1490, "Kippa", "passKeppa")
    };

    public String IP = "192.168.0.30";
    public int PORT = samples[0].port;    //maybe change to smth appropriate

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    View fragmentHolder;    //xml root
    EditText editIp, editLogin, editPassword;
    Button buttonIp, buttonLogin;

    MenuItem menuItemIP;

    private volatile boolean switchFragment = false; //indicates, whether login is successful

    private String currentUser;


    //Required by default
    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentHolder = inflater.inflate(R.layout.fragment_login, container, false);

        editIp = (EditText) fragmentHolder.findViewById(R.id.editIp);
        editLogin = (EditText) fragmentHolder.findViewById(R.id.editLogin);
        editPassword = (EditText) fragmentHolder.findViewById(R.id.editPassword);

        buttonIp = (Button) fragmentHolder.findViewById(R.id.buttonIP);
        buttonLogin = (Button) fragmentHolder.findViewById(R.id.buttonLogin);

        editIp.setVisibility(View.INVISIBLE);
        buttonIp.setVisibility(View.INVISIBLE);

        // Setting ip on SETIP, Kappa
        buttonIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IP = editIp.getText().toString();
                editIp.setVisibility(View.INVISIBLE);
                buttonIp.setVisibility(View.INVISIBLE);
                menuItemIP.setTitle(IP);
            }
        });

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);

        menuItemIP = (MenuItem) menu.findItem(R.id.menuIP);
        menuItemIP.setTitle(IP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_item1) {
            applySample(0);
        } else if (id == R.id.main_item2) {
            applySample(1);
        } else if (id == R.id.main_item3) {
            applySample(2);
        } else if (id == R.id.menuIP){
            editIp.setVisibility(View.VISIBLE);
            buttonIp.setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    private void applySample(int number){
        editLogin.setText(samples[number].username);
        editPassword.setText(samples[number].password);
        PORT = samples[number].port;
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


    class MyLogger extends AsyncTask<Void, String, Void> {

        // to indicate whether connection was successful or what the reason otherwise
        String connectionResult = "";

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
            loggingIn();
            publishProgress(connectionResult);
            log("Thread ended");
            return null;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            if (connectionResult.equals("good")) {
                //switching to messaging fragment
                log("Changing fragment");
                switchFragment = true;
            } else {
                String toast;
                switch (connectionResult) {
                    case "bad": {
                        toast = "Wrong login or password";
                        break;
                    }
                    case "timeExpired": {
                        toast = "Connection time expired";
                        break;
                    }
                    case "missingServer": {
                        toast = "No server connection";
                        break;
                    }
                    default:{
                        // for unexpected cases, which are unexpected
                        toast = "Impossible christmas miracle";
                        break;
                    }
                }
                Toast.makeText(getActivity(),toast,Toast.LENGTH_LONG).show();
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
                socket.connect(new InetSocketAddress(IP, PORT), 3*1000);

                log("opening streams");
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                log("sending login: " + login);
                out.writeUTF("_login_" + login);
                log("sending password: " + password);
                out.writeUTF("_pass_" + password);

                if (in.readBoolean()) {
                    currentUser = login;
                    log("Current user: "+currentUser);
                    connectionResult = "good";
                    return true;
                } else {
                    connectionResult = "bad";
                    closeAll();
                    return false;
                }

            } catch (SocketTimeoutException s) {
                log("Connection time expired");
                connectionResult = "timeExpired";
                closeAll();

                return false;

            } catch (IOException e) {
                closeAll();
                return false;
            }

        }

        private void closeAll() {
            try {
                log("Closing socket and streams");
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {
                log("Closing exception, mystery!!1");
            } catch (NullPointerException e) {
                log("Not initialized, missing server");
                connectionResult = "missingServer";
            }
        }


        //unlocking views
        private void unlockLogging() {
            log("Unlocking buttons");
            buttonLogin.setEnabled(true);
            editLogin.setEnabled(true);
            editPassword.setEnabled(true);
            editPassword.setText("");
        }

    }
}