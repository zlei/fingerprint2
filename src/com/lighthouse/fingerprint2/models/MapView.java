package com.lighthouse.fingerprint2.models;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MapView extends ImageViewTouch implements OnTouchListener {

	private List<Point> points = new ArrayList<Point>();
	private Paint paint = new Paint();
	private Bitmap loadedMap;
	private Bitmap paintedMap;
	private boolean paintMode = false;
	private Canvas canvas;
	private Matrix currentMatrix;
	private Point realPoint;
	private Point scaledPoint;

	public MapView(Context context) {
		super(context);
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// to make it focusable so that it will receive touch events
		// properly
		// this.setFocusable(true);
		// this.setFocusableInTouchMode(true);
		// adding touch listener to this view
		// this.setOnTouchListener(this);
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		realPoint = new Point();
		scaledPoint = new Point();
		realPoint.x = event.getX();
		realPoint.y = event.getY();
		scaledPoint = scalePoint(realPoint);
		points.add(scaledPoint);
		Log.d("point", "real x: " + realPoint.x);
		Log.d("point", "real y: " + realPoint.y);
		Log.d("point", "scale x: " + scaledPoint.x);
		Log.d("point", "scale y: " + scaledPoint.y);
		startPaint(paintedMap);
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

	/*
	 * public void onDraw(Canvas canvas) { if (paintMode) { //
	 * this.setFocusable(true); // this.setFocusableInTouchMode(true); // adding
	 * touch listener to this view // this.setOnTouchListener(this);
	 * 
	 * canvas.drawBitmap(loadedMap, mBaseMatrix, paint); for (Point point :
	 * points) { canvas.drawCircle(point.x, point.y, 5, paint); } } else {
	 * if(loadedMap != null) canvas.drawBitmap(loadedMap, mBaseMatrix, paint);
	 * return; } }
	 */
	public void startPaint(Bitmap loadedMap) {
		paintedMap = loadedMap;
		canvas = new Canvas(paintedMap);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		// adding touch listener to this view
		this.setOnTouchListener(this);
		paint.setColor(Color.BLUE);

		for (Point point : points) {
			canvas.drawCircle(point.x, point.y, 2, paint);
		}
		currentMatrix = getDisplayMatrix();
		this.setImageBitmap(paintedMap, currentMatrix, ZOOM_INVALID,
				ZOOM_INVALID);
		this.printMatrix(getDisplayMatrix());
	}

	public void stopPaint(Bitmap loadedMap) {
		currentMatrix = getDisplayMatrix();
		this.setImageBitmap(loadedMap, currentMatrix, ZOOM_INVALID,
				ZOOM_INVALID);
		this.setOnTouchListener(null);
	}

	public Point scalePoint(Point point) {
		Point scaledPoint = new Point();
		float width = this.getWidth();
		float mapWidth = this.getBitmapRect().width();
		float height = this.getHeight();
		float mapHeight = getBitmapRect().height();
		float midWidth = width / 2;
		// float midHeight = height / 2;
		scaledPoint.x = (midWidth + (point.x - midWidth) * (mapWidth / width))/this.getScale();
		scaledPoint.y = point.y;
		return scaledPoint;
	}

}