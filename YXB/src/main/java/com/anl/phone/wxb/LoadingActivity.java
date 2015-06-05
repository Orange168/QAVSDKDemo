package com.anl.phone.wxb;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;


public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        
        new Handler().postDelayed(runnable, 2000);
    }

    Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			finish();
			System.out.println("LoadingActivity finish");
		}
	};

}
