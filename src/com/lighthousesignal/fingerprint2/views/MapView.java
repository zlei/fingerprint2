package com.lighthousesignal.fingerprint2.views;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
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
	// used for change point
	private Bitmap tempMap;
	private Bitmap showLogsMap;
	private Canvas canvas;
	private Matrix currentMatrix;
	private Point realPoint;
	private Point scaledPoint;
	private int pointCounter;
	protected Bitmap startPoint, stopPoint;
	private HashMap<Integer, Point> points;
	private ArrayList<Point> serverPoints;
	private Boolean isPointChangable;

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
	@SuppressLint("UseSparseArrays")
	protected void init() {
		Resources res = getContext().getResources();
		startPoint = ((BitmapDrawable) res.getDrawable(R.drawable.arrow_blue))
				.getBitmap();
		stopPoint = ((BitmapDrawable) res.getDrawable(R.drawable.arrow_red))
				.getBitmap();
		points = new HashMap<Integer, Point>();
		// 1 for start point, 2 for end point
		pointCounter = 1;
		isPointChangable = true;
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

	class Point {
		float x, y;
	}

	/**
	 * setBitmap with current map
	 * 
	 * @param loadedMap
	 */
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
	public boolean startPaint(Bitmap loadedMap) {
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
				/**
				 * if (points.containsValue(scaledPoint)) { dragPoint(); } else
				 * {
				 */
				if (isPointChangable) {
					switch (points.size()) {
					// initiate start point
					case 0:
						points.put(pointCounter, scaledPoint);
						canvas = new Canvas(paintedMap);
						drawPoint();
						break;
					// replace start point
					case 1:
						replacePoint(pointCounter);
						points.put(pointCounter, scaledPoint);
						paintedMap = tempMap.copy(tempMap.getConfig(), true);
						canvas = new Canvas(paintedMap);
						drawPoint();
						break;
					// replace end point
					case 2:
						replacePoint(pointCounter);
						points.put(pointCounter, scaledPoint);
						paintedMap = tempMap.copy(tempMap.getConfig(), true);
						canvas = new Canvas(paintedMap);
						drawPoint();
						drawLine();
						break;
					}
				} else {
					// for now, only draw two points, one line. Might be
					// multiple lines in the future
					if (points.size() == 1) {
						pointCounter++;
						points.put(pointCounter, scaledPoint);
						drawPoint();
						drawLine();
					}
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
		tempMap = paintedMap.copy(paintedMap.getConfig(), true);
		// For now, only two points one line
		// if (points.size() == 2) {
		// this.setOnTouchListener(null);
		// }
		return true;
	}

	/**
	 * draw point on painted map
	 */
	public boolean drawPoint() {
		paint.setColor(Color.RED);
		// canvas.drawCircle(scaledPoint.x, scaledPoint.y, 4, paint);
		// canvas.drawCircle(realPoint.x, realPoint.y, 4, paint);
		canvas.drawBitmap(startPoint, scaledPoint.x, scaledPoint.y, paint);
		// points.put(pointCounter, realPoint);
		updatePaint(paintedMap);
		// this.setOnTouchListener(null);
		return true;
	}

	/**
	 * change listeners when stopping painting
	 * 
	 * @return
	 */
	public boolean stopPaint() {
		updatePaint(paintedMap);
		this.setOnTouchListener(null);
		this.setOnLongClickListener(null);
		return true;
	}

	/**
	 * update canvas with current scaled map
	 * 
	 * @param loadedMap
	 */
	public void updatePaint(Bitmap loadedMap) {
		currentMatrix = getDisplayMatrix();
		this.setImageBitmap(loadedMap, currentMatrix, ZOOM_INVALID,
				ZOOM_INVALID);
	}

	/**
	 * show logs on server
	 * 
	 * @param loadedMap
	 */
	public boolean showLogs(Bitmap loadedMap) {
		showLogsMap = loadedMap;
		canvas = new Canvas(showLogsMap);

		drawServerPoints();
		updatePaint(showLogsMap);
		return true;
	}

	/**
	 * draw server logs on map. No real data now, made up several points
	 * 
	 * @return
	 */
	private boolean drawServerPoints() {
		// load points here
		serverPoints = new ArrayList<Point>();
		Point p1 = new Point();
		p1.x = 500;
		p1.y = 250;
		Point p2 = new Point();
		p2.x = 500;
		p2.y = 500;
		Point p3 = new Point();
		p3.x = 250;
		p3.y = 500;

		serverPoints.add(p1);
		serverPoints.add(p2);
		serverPoints.add(p3);

		Paint linePaint = new Paint();
		linePaint.setStyle(Style.STROKE);
		linePaint.setStrokeWidth(2);
		linePaint.setColor(Color.BLUE);

		Point prePoint = null;
		for (Point tempPoint : serverPoints) {
			canvas.drawBitmap(startPoint, tempPoint.x, tempPoint.y, paint);
			if (prePoint != null) {
				float x1 = prePoint.x + 2;
				float y1 = prePoint.y + 2;
				float x2 = tempPoint.x + 2;
				float y2 = tempPoint.y + 2;
				canvas.drawLine(x1, y1, x2, y2, linePaint);
			}
			prePoint = tempPoint;
			updatePaint(showLogsMap);
		}

		return true;
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

	/**
	 * draw lines between points
	 * 
	 * @return
	 */
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

	/**
	 * on click clear button, clear all stored data. possibly check the data is
	 * stored or not, if not, back up data
	 * 
	 * @return
	 */
	public boolean clearData() {
		points.clear();
		pointCounter = 1;
		return true;
	}

	public boolean replacePoint(int point) {
		// start point
		if (point == 1) {
			points.remove(1);
		}
		// end point
		if (point == 2) {
			points.remove(2);
		}
		return true;
	}

	/**
	 * get start and stop points
	 */
	public HashMap<Integer, Point> getPoints() {
		return points;
	}

	/**
	 * set change point or draw lines between points
	 */
	public boolean setPointChangable(boolean bool) {
		isPointChangable = bool;
		return true;
	}

	/**
	 * deal with drag points to modify log, TO DO
	 */
	@Override
	public boolean onDrag(View view, DragEvent event) {

		return true;
	}

	private boolean dragPoint() {
		this.setOnDragListener(this);
		return true;
	}

}