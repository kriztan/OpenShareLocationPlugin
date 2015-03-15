package com.samwhited.opensharelocationplugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

public class Marker extends SimpleLocationOverlay {
	private final GeoPoint position;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Context ctx;

	public Marker(final Context ctx, final GeoPoint position) {
		super(ctx);
		this.paint.setAntiAlias(true);
		this.position = position;
		this.ctx = ctx;
	}

	@Override
	public void draw(final Canvas c, final MapView view, final boolean shadow) {
		super.draw(c, view, shadow);
		final Point mapCenterPoint = new Point();

		view.getProjection().toPixels(position, mapCenterPoint);

		final Bitmap icon = BitmapFactory.decodeResource(view.getResources(), R.drawable.ic_location_on_grey600_48dp);

		c.drawBitmap(icon,
				mapCenterPoint.x - icon.getWidth() / 2,
				mapCenterPoint.y - icon.getHeight(),
				null);

	}
}
