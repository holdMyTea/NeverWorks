package com.forsenboyz.rise42.neverworks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_item1) {
            Toast.makeText(this,"Nothing 1",Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.main_item2) {
            Toast.makeText(this,"Nothing 2",Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.main_item3) {
            Toast.makeText(this,"Nothing 3",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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