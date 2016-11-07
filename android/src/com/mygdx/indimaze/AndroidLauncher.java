package com.mygdx.indimaze;

import android.os.Bundle;
import android.provider.Settings;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.indimaze.IndiscriminateMazeGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {

		String android_id = Settings.Secure.getString(getContext().getContentResolver(),
				Settings.Secure.ANDROID_ID);

		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new IndiscriminateMazeGame(android_id), config);
	}
}
