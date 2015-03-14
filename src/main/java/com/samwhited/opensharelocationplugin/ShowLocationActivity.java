package com.samwhited.opensharelocationplugin;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;


public class ShowLocationActivity extends Activity {

	protected Location loc;

	private Uri createGeoUri() {
		return Uri.parse("geo:" + loc.getLatitude() + "," + loc.getLongitude());
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidGraphicFactory.createInstance(this.getApplication());

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.activity_show_location);
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_share_location, menu);

		final MenuItem item = menu.findItem(R.id.action_share_location);
		if (item.getActionProvider() != null && loc != null) {
			final ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();
			final Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, loc.toString());
			shareIntent.setType("text/plain");
			mShareActionProvider.setShareIntent(shareIntent);
		} else {
			// This isn't really necessary, but while I was testing it was useful. Possibly remove it?
			item.setVisible(false);
		}


		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_about:
				// TODO: Launch about dialog here.
				return true;
			case R.id.action_copy_location:
				final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				final ClipData clip = ClipData.newPlainText("location", this.createGeoUri().toString());
				clipboard.setPrimaryClip(clip);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
