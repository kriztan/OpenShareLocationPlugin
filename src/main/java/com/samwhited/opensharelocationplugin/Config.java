package com.samwhited.opensharelocationplugin;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

public final class Config {
	public final static int INITIAL_ZOOM_LEVEL = 4;
	public final static int FINAL_ZOOM_LEVEL = 15;
	public final static GeoPoint INITIAL_POS = new GeoPoint(33.805278, -84.171389);
	public final static int LOCATION_FIX_TIME_DELTA = 0;
	public final static int LOCATION_FIX_SPACE_DELTA = 0;
	public final static int LOCATION_FIX_SIGNIFICANT_TIME_DELTA = 1000 * 60 * 2; // Two minutes
	public final static ITileSource TILE_SOURCE_PROVIDER = TileSourceFactory.MAPNIK;
}
