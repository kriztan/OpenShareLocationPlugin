package com.samwhited.opensharelocationplugin;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class ShareLocationActivity extends Activity implements LocationListener {

	private Location loc;
	private IMapController mapController;
	private Button shareButton;
	private RelativeLayout snackBar;
	private LocationManager locationManager;
	private MapView map;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_share_location);

		// Get map view and configure it.
		map = (MapView) findViewById(R.id.map);
		map.setTileSource(Config.TILE_SOURCE_PROVIDER);
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);

		this.mapController = map.getController();
		mapController.setZoom(Config.INITIAL_ZOOM_LEVEL);
		mapController.setCenter(Config.INITIAL_POS);

		// Setup the cancel button
		final Button cancelButton = (Button) findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		// Setup the share button
		this.shareButton = (Button) findViewById(R.id.share_button);
		this.shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				if (loc != null) {
					final Intent result = new Intent();
					result.putExtra("latitude", loc.getLatitude());
					result.putExtra("longitude", loc.getLongitude());
					result.putExtra("altitude", loc.getAltitude());
					result.putExtra("accuracy", (int) loc.getAccuracy());
					setResult(RESULT_OK, result);
					finish();
				}
			}
		});

		// Setup the snackbar
		this.snackBar = (RelativeLayout) findViewById(R.id.snackbar);
		final TextView snackbarAction = (TextView) findViewById(R.id.snackbar_action);
		snackbarAction.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
		});

		this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Request location updates from the system location manager
		requestLocationUpdates();
	}

	private void requestLocationUpdates() {
		final Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null) {
			this.loc = lastKnownLocation;
			gotoLoc();
		}

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.LOCATION_FIX_TIME_DELTA,
				Config.LOCATION_FIX_SPACE_DELTA, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.LOCATION_FIX_TIME_DELTA,
				Config.LOCATION_FIX_SPACE_DELTA, this);
	}

	private void pauseLocationUpdates() {
		locationManager.removeUpdates(this);
	}

	private void gotoLoc() {
		mapController.animateTo(new GeoPoint(this.loc));
		mapController.setZoom(Config.FINAL_ZOOM_LEVEL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		pauseLocationUpdates();
	}

	private void setShareButtonEnabled(final boolean enabled) {
		if (enabled) {
			this.shareButton.setEnabled(true);
			this.shareButton.setTextColor(0xde000000);
			this.shareButton.setText(R.string.share);
		} else {
			this.shareButton.setEnabled(false);
			this.shareButton.setTextColor(0x8a000000);
			this.shareButton.setText(R.string.locating);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.loc = null;
		if (isLocationEnabled()) {
			this.snackBar.setVisibility(View.GONE);
		} else {
			this.snackBar.setVisibility(View.VISIBLE);
		}
		setShareButtonEnabled(false);

		requestLocationUpdates();
	}

	public boolean isBetterLocation(final Location location) {
		if (loc == null) {
			return true;
		}

		// Check whether the new location fix is newer or older
		final long timeDelta = location.getTime() - loc.getTime();
		final boolean isSignificantlyNewer = timeDelta > Config.LOCATION_FIX_SIGNIFICANT_TIME_DELTA;
		final boolean isSignificantlyOlder = timeDelta < -Config.LOCATION_FIX_SIGNIFICANT_TIME_DELTA;
		final boolean isNewer = timeDelta > 0;

		if (isSignificantlyNewer) {
			return true;
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		final int accuracyDelta = (int) (location.getAccuracy() - loc.getAccuracy());
		final boolean isLessAccurate = accuracyDelta > 0;
		final boolean isMoreAccurate = accuracyDelta < 0;
		final boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		final boolean isFromSameProvider = isSameProvider(location.getProvider(), loc.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	private boolean isSameProvider(final String provider1, final String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	@Override
	public void onLocationChanged(final Location location) {
		if (isBetterLocation(location)) {
			setShareButtonEnabled(true);
			this.loc = location;
			gotoLoc();

			this.map.getOverlays().add(new Marker(this, new GeoPoint(this.loc)));

			// After we get a single good location fix, stop updating.
			// I'm still not sure if this is really the desired behavior.
			pauseLocationUpdates();
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

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private boolean isLocationEnabledKitkat() {
		try {
			final int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
			return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		} catch (final Settings.SettingNotFoundException e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isLocationEnabledLegacy() {
		final String locationProviders = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		return !TextUtils.isEmpty(locationProviders);
	}

	private boolean isLocationEnabled() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			return isLocationEnabledKitkat();
		} else {
			return isLocationEnabledLegacy();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_share_location, menu);
		return true;
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
		}
		return super.onOptionsItemSelected(item);
	}
}
