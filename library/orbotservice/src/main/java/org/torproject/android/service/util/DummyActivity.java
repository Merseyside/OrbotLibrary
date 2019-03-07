package org.torproject.android.service.util;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
 * To combat background service being stopped/swiped
 */
public class DummyActivity extends AppCompatActivity {
	@Override
	public void onCreate( Bundle icicle ) {
		super.onCreate( icicle );
		finish();
	}
}