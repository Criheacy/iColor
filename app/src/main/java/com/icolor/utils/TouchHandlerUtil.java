package com.icolor.utils;

import android.graphics.Point;
import android.view.MotionEvent;

public class TouchHandlerUtil {
    public interface OnTouchEvent {
        boolean onClick(Point p);
        boolean onTouch(Point p);
        boolean onDrag(Point origin, Point dragTo, Point lastDragVector);
        boolean onLeave();
    }

    public TouchHandlerUtil(OnTouchEvent t) {
        listener = t;
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
                return listener.onTouch(new Point(downPoint));

            case MotionEvent.ACTION_MOVE:
                currentPoint.x = (int) event.getX();
                currentPoint.y = (int) event.getY();

                boolean result = false;
                if (!isDragging && touchPointDistance(currentPoint, downPoint) > draggingJudgmentDistance) {
                    isDragging = true;
                } else if (isDragging) {
                    result = listener.onDrag(new Point(downPoint), new Point(currentPoint),
                            new Point(currentPoint.x - lastPoint.x,currentPoint.y - lastPoint.y));
                }
                lastPoint.x = currentPoint.x;
                lastPoint.y = currentPoint.y;
                return result;
            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    return listener.onClick(new Point(downPoint));
                }
                isDragging = false;
                return listener.onLeave();
            default: break;
        }
        return false;
    }

    private static float touchPointDistance(Point a, Point b) {
        return (float) Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2));
    }

    private static final float draggingJudgmentDistance = 5f;

    private final OnTouchEvent listener;
    private final Point downPoint;
    private final Point lastPoint;
    private final Point currentPoint;
    private boolean isDragging;
}
