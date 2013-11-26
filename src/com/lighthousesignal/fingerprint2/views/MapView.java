package com.lighthousesignal.fingerprint2.views;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;

import com.lighthousesignal.fingerprint2.R;

public class MapView extends ImageViewTouch implements OnTouchListener,
		OnDragListener {

	private Paint paint = new Paint();
	private Bitmap loadedMap;
	private Bitmap paintedMap;
	private boolean paintMode = false;
	private Canvas canvas;
	private Matrix currentMatrix;
	private Point realPoint;
	private Point scaledPoint;
	private int pointCounter = 0;
	protected Bitmap startPoint, stopPoint;
	private HashMap<Integer, Point> points = new HashMap<Integer, Point>();

	public MapView(Context context) {
		super(context);
		init();
	}

	/**
	 * main constructor
	 * 
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
		super.onTouchEvent(event);
		// TODO Auto-generated method stub
		realPoint = new Point();
		scaledPoint = new Point();
		realPoint.x = event.getX();
		realPoint.y = event.getY();
		Log.d("x:  ", Float.toString(realPoint.x));
		Log.d("y:  ", Float.toString(realPoint.y));
		scaledPoint = scalePoint(realPoint);
		return true;
	}

	public boolean onDrag(View view, DragEvent event) {

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

	private Bitmap getPaintedBitmap() {
		return paintedMap;
	}

	/**
	 * Start paint
	 * 
	 * @param loadedMap
	 */
	public void startPaint(Bitmap loadedMap) {
		paintedMap = loadedMap;
		canvas = new Canvas(paintedMap);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		// adding touch listener to this view
		this.setOnTouchListener(this);

		this.setLongClickable(true);
		this.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				if (points.containsValue(scaledPoint)) {
					dragPoint();
				} else {
					drawPoint();
					drawLine();
				}
				return true;
			}
		});
		// paint.setColor(Color.BLUE);

		// for (Point point : points) {
		// canvas.drawBitmap(startPoint, point.x, point.y, paint);
		// canvas.drawCircle(scaledPoint.x, scaledPoint.y, 2, paint);
		// }
		updatePaint(paintedMap);
	}

	public void drawPoint() {
		paint.setColor(Color.RED);
		// canvas.drawCircle(scaledPoint.x, scaledPoint.y, 4, paint);
		// canvas.drawCircle(realPoint.x, realPoint.y, 4, paint);
		canvas.drawBitmap(startPoint, scaledPoint.x, scaledPoint.y, paint);
		pointCounter++;
		points.put(pointCounter, scaledPoint);
		// points.put(pointCounter, realPoint);
		updatePaint(paintedMap);
		this.setOnTouchListener(null);
	}

	public void stopPaint() {
		updatePaint(paintedMap);
		this.setOnTouchListener(null);
		this.setOnLongClickListener(null);
	}

	public void updatePaint(Bitmap loadedMap) {
		currentMatrix = getDisplayMatrix();
		this.setImageBitmap(loadedMap, currentMatrix, ZOOM_INVALID,
				ZOOM_INVALID);
	}

	/**
	 * TODO FIND THE CORRECT SCALE
	 * 
	 * @param point
	 * @return
	 */
	public Point scalePoint(Point point) {
		Point scaledPoint = new Point();

		float scalex = getValue(this.mDisplayMatrix, Matrix.MSCALE_X);
		float scaley = getValue(this.mDisplayMatrix, Matrix.MSCALE_Y);
		float tx = getValue(this.mDisplayMatrix, Matrix.MTRANS_X);
		float ty = getValue(this.mDisplayMatrix, Matrix.MTRANS_Y);

		scaledPoint.x = (point.x - tx) / scalex;
		scaledPoint.y = (point.y - ty) / scaley;

		return scaledPoint;
	}

	private boolean drawLine() {
		if (pointCounter > 1) {
			Paint linePaint = new Paint();
			linePaint.setStyle(Style.STROKE);
			linePaint.setStrokeWidth(2);
			linePaint.setColor(Color.BLUE);
			float x1 = points.get(pointCounter - 1).x + 2;
			float y1 = points.get(pointCounter - 1).y + 2;
			float x2 = points.get(pointCounter).x + 2;
			float y2 = points.get(pointCounter).y + 2;
			canvas.drawLine(x1, y1, x2, y2, linePaint);
		}
		updatePaint(paintedMap);
		this.setOnTouchListener(this);
		return true;
	}

	private boolean dragPoint() {
		this.setOnDragListener(this);
		return true;
	}

}