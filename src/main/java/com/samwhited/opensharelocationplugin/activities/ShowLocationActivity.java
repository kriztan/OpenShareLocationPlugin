package com.samwhited.opensharelocationplugin.activities;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.samwhited.opensharelocationplugin.R;
import com.samwhited.opensharelocationplugin.overlays.Marker;
import com.samwhited.opensharelocationplugin.overlays.MyLocation;
import com.samwhited.opensharelocationplugin.util.Config;
import com.samwhited.opensharelocationplugin.util.LocationHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


public class ShowLocationActivity extends LocationActivity implements LocationListener {

	private GeoPoint loc = Config.INITIAL_POS;
	private Location myLoc = null;
	private IMapController mapController;
	private MapView map;

	private Uri createGeoUri() {
		return Uri.parse("geo:" + this.loc.getLatitude() + "," + this.loc.getLongitude());
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
		map = (MapView) findViewById(R.id.map);
		map.setTileSource(Config.TILE_SOURCE_PROVIDER);
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);

		this.mapController = map.getController();
		mapController.setZoom(Config.INITIAL_ZOOM_LEVEL);
		mapController.setCenter(this.loc);

		requestLocationUpdates();
	}

	@Override
	protected void gotoLoc() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void setLoc(final Location location) {
		this.myLoc = location;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_show_location, menu);

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

	private void addOverlays() {
		this.map.getOverlays().clear();
		this.map.getOverlays().add(0, new Marker(this, this.loc));

		if (myLoc != null) {
			this.map.getOverlays().add(1, new MyLocation(this, this.myLoc));
			this.map.invalidate();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		final Intent intent = getIntent();

		if (intent != null) {
			if (intent.hasExtra("longitude") && intent.hasExtra("latitude")) {
				final double longitude = intent.getDoubleExtra("longitude", 0);
				final double latitude = intent.getDoubleExtra("latitude", 0);
				this.loc = new GeoPoint(latitude, longitude);
				if (this.mapController != null) {
					mapController.animateTo(this.loc);
					mapController.setZoom(Config.FINAL_ZOOM_LEVEL);

					addOverlays();
				}
			}
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
				final ClipData clip = ClipData.newPlainText("location", createGeoUri().toString());
				clipboard.setPrimaryClip(clip);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (LocationHelper.isBetterLocation(location, this.myLoc)) {
			this.myLoc = location;
			addOverlays();
		}
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {

	}

	@Override
	public void onProviderEnabled(final String provider) {

	}

	@Override
	public void onProviderDisabled(final String provider) {

	}
}
