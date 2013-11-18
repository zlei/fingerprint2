package com.lighthouse.fingerprint2.models;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.lighthouse.fingerprint2.R;

public class MapView extends ImageViewTouch implements OnTouchListener {

	private Paint paint = new Paint();
	private Bitmap loadedMap;
	private Bitmap paintedMap;
	private boolean paintMode = false;
	private Canvas canvas;
	private Matrix currentMatrix;
	private Point realPoint;
	private Point scaledPoint;
	protected Bitmap startPoint, stopPoint;

	public MapView(Context context) {
		super(context);
		init();
	}

	/**
	 * main constructor
	 * @param context
	 * @param attrs
	 */
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/** 
	 * initiate points with drawable image 
	 * 
	 */
	protected void init() {
		Resources res = getContext().getResources();
		startPoint = ((BitmapDrawable) res.getDrawable(R.drawable.arrow_blue))
				.getBitmap();
		stopPoint = ((BitmapDrawable) res.getDrawable(R.drawable.arrow_red))
				.getBitmap();
	}

	/**
	 * To change listener when in paint mode
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		realPoint = new Point();
		scaledPoint = new Point();
		realPoint.x = event.getX();
		realPoint.y = event.getY();
		scaledPoint = scalePoint(realPoint);
		// points.add(scaledPoint);
		Log.d("point", "real x: " + realPoint.x);
		Log.d("point", "real y: " + realPoint.y);
		Log.d("point", "scale x: " + scaledPoint.x);
		Log.d("point", "scale y: " + scaledPoint.y);
		// startPaint(paintedMap);
		paintPoint();
		return true;
	}

	class Point {
		float x, y;
	}
	
	public void setBitmap(Bitmap loadedMap) {
		this.loadedMap = loadedMap;
		this.setImageBitmap(loadedMap);
	}

	public Bitmap getBitmap() {
		return loadedMap;
	}
	
	/**
	 * Start paint
	 * @param loadedMap
	 */
	public void startPaint(Bitmap loadedMap) {
		paintedMap = loadedMap;
		canvas = new Canvas(paintedMap);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		// adding touch listener to this view
		this.setOnTouchListener(this);
		// paint.setColor(Color.BLUE);

		// for (Point point : points) {
		// canvas.drawBitmap(startPoint, point.x, point.y, paint);
		// canvas.drawCircle(scaledPoint.x, scaledPoint.y, 2, paint);
		// }
		updatePaint(paintedMap);
	}

	public void paintPoint() {
		paint.setColor(Color.BLUE);
		canvas.drawCircle(scaledPoint.x, scaledPoint.y, 2, paint);
		updatePaint(paintedMap);
	}

	public void stopPaint() {
		updatePaint(paintedMap);
		this.setOnTouchListener(null);
	}

	public void updatePaint(Bitmap loadedMap) {
		currentMatrix = getDisplayMatrix();
		this.setImageBitmap(loadedMap, currentMatrix, ZOOM_INVALID,
				ZOOM_INVALID);
	}

	/**
	 * TODO FIND THE CORRECT SCALE
	 * @param point
	 * @return
	 */
	public Point scalePoint(Point point) {
		Point scaledPoint = new Point();
		float width = this.getWidth();
		float mapWidth = this.getBitmapRect().width();
		float height = this.getHeight();
		float mapHeight = getBitmapRect().height();
		float midWidth = width / 2;
		// float midHeight = height / 2;
		scaledPoint.x = (midWidth + (point.x - midWidth) * (mapWidth / width))
				/ this.getScale();
		scaledPoint.y = point.y;
		return scaledPoint;
	}

}