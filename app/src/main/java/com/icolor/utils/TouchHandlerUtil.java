package com.icolor.utils;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

public class TouchHandlerUtil {
    public interface OnGestureListener {
        void onClick(Point p);
        void onTouch(Point p);
        void onDrag(Point origin, Point dragTo, Point lastDragVector);
        void onLeave();
    }

    public TouchHandlerUtil(OnGestureListener l) {
        listener = l;
        downPoint = new Point();
        currentPoint = new Point();
        lastPoint = new Point();
        isDragging = false;
    }

    public boolean handle(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPoint.x = (int) event.getX();
                downPoint.y = (int) event.getY();
                listener.onTouch(new Point(downPoint));
                break;
            case MotionEvent.ACTION_MOVE:
                currentPoint.x = (int) event.getX();
                currentPoint.y = (int) event.getY();

                if (!isDragging && touchPointDistance(currentPoint, downPoint) > draggingJudgmentDistance) {
                    isDragging = true;
                } else if (isDragging) {
                    listener.onDrag(new Point(downPoint), new Point(currentPoint),
                            new Point(currentPoint.x - lastPoint.x,currentPoint.y - lastPoint.y));
                }
                lastPoint.x = currentPoint.x;
                lastPoint.y = currentPoint.y;
                break;
            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    listener.onClick(new Point(downPoint));
                }
                isDragging = false;
                listener.onLeave();
                break;
            default: break;
        }
        return false;
    }

    private static float touchPointDistance(Point a, Point b) {
        return (float) Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2));
    }

    private static final float draggingJudgmentDistance = 5f;

    private final OnGestureListener listener;
    private final Point downPoint;
    private final Point lastPoint;
    private final Point currentPoint;
    private boolean isDragging;
}
