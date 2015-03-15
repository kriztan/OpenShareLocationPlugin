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

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;


public class ShowLocationActivity extends Activity {

	protected String locName;
	protected GeoPoint loc = Config.INITIAL_POS;
	protected MapView map;
	protected IMapController mapController;

	private Uri createGeoUri() {
		return Uri.parse("geo:" + loc.getLatitude() + "," + loc.getLongitude());
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.activity_show_location);

		// Get map view and configure it.
		this.map = (MapView) findViewById(R.id.map);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);

		this.mapController = map.getController();
		mapController.setZoom(Config.INITIAL_ZOOM_LEVEL);
		mapController.setCenter(this.loc);
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
			shareIntent.putExtra(Intent.EXTRA_TEXT, createGeoUri().toString());
			shareIntent.setType("text/plain");
			mShareActionProvider.setShareIntent(shareIntent);
		} else {
			// This isn't really necessary, but while I was testing it was useful. Possibly remove it?
			item.setVisible(false);
		}


		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		final Intent intent = getIntent();

		if (intent != null) {
			this.locName = intent.getStringExtra("name");

			if (intent.hasExtra("longitude") && intent.hasExtra("latitude")) {
				final double longitude = intent.getDoubleExtra("longitude", 0);
				final double latitude = intent.getDoubleExtra("latitude", 0);
				this.loc = new GeoPoint(latitude, longitude);
				if (this.mapController != null) {
					mapController.animateTo(this.loc);
					mapController.setZoom(Config.FINAL_ZOOM_LEVEL);
				}
			}
		} else {
			this.locName = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));
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
