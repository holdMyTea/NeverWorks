package com.forsenboyz.rise42.neverworks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    LoginFragment loginFragment;
    MessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginFragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,loginFragment).commit();

        new fragmentSwitcher().execute();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu are used in fragments
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/


    class fragmentSwitcher extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("MY_TAG","Switcher started");
            while(!loginFragment.isSwitch()){}
            Log.d("MY_TAG","Time to switch");
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.d("MY_TAG","Actually switching fragments");
            messageFragment = new MessageFragment();

            Log.d("MY_TAG","Passing socket between fragments");
            messageFragment.setSocket(loginFragment.getSocket());
            messageFragment.setIn(loginFragment.getIn());
            messageFragment.setOut(loginFragment.getOut());
            messageFragment.setCurrentUser(loginFragment.getCurrentUser());

            Log.d("MY_TAG", "Starting new fragment");
            getSupportFragmentManager().beginTransaction().remove(loginFragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer,messageFragment).commit();
        }
    }
}