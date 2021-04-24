package com.icolor.utils;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureUtil {
    public interface OnGestureListener {
        void click();
        void touch();

        void dragStart(GestureOrientation orientation);
        void dragging(GestureOrientation orientation, int draggingDistance);
        void dragEnd();
    }

    public enum GestureOrientation {
        NONE, HORIZONTAL, VERTICAL
    }

    public GestureUtil(View view, OnGestureListener gestureListener) {
        this.view = view;
        isDragging = false;
        this.touchHandler = new TouchHandlerUtil(new TouchHandlerUtil.OnTouchEvent() {
            @Override
            public void onClick(Point p) {
                if (WindowUtil.pointInView(p, view)) {
                    gestureListener.click();
                }
            }

            @Override
            public void onTouch(Point p) {
                if (WindowUtil.pointInView(p, view)) {
                    gestureListener.touch();
                }
            }

            @Override
            public void onDrag(Point origin, Point dragTo, Point lastDragVector) {
                if (!WindowUtil.pointInView(origin, view)) {
                    return;
                }
                if (!isDragging) {
                    isDragging = true;
                    currentOrientation = getOrientation
                            (new Point(dragTo.x - origin.x, dragTo.y - origin.y));
                    gestureListener.dragStart(currentOrientation);
                }
                if (currentOrientation.equals(GestureOrientation.HORIZONTAL)) {
                    gestureListener.dragging(currentOrientation, lastDragVector.x);
                } else if (currentOrientation.equals(GestureOrientation.VERTICAL)) {
                    gestureListener.dragging(currentOrientation, lastDragVector.y);
                }
            }

            @Override
            public void onLeave() {
                isDragging = false;
                currentOrientation = GestureOrientation.NONE;
                gestureListener.dragEnd();
            }
        });
    }

    public boolean handle(MotionEvent event) {
        return touchHandler.handle(event);
    }

    private GestureOrientation getOrientation(Point vector) {
        float horizontalAngle = getAngle(vector.x, vector.y, 1f, 0f);
        float verticalAngle = getAngle(vector.x, vector.y, 0f, 1f);

        horizontalAngle = (float) Math.min(Math.abs(horizontalAngle), Math.abs(horizontalAngle - Math.PI));
        verticalAngle = (float) Math.min(Math.abs(verticalAngle), Math.abs(verticalAngle - Math.PI));

        if (horizontalAngle < orientationAcceptableRange * Math.PI / 180f) {
            return GestureOrientation.HORIZONTAL;
        } else if (verticalAngle < orientationAcceptableRange * Math.PI / 180f) {
            return GestureOrientation.VERTICAL;
        } else {
            return GestureOrientation.NONE;
        }
    }

    private float getAngle(float x1, float y1, float x2, float y2) {
        double dotProduct = x1 * x2 + y1 * y2;
        double length1 = Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2));
        double length2 = Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));
        // Log.d("Angle", "(" + x1 + "," + y1 + ") (" + x2 + "," + y2 + ") -> " + dotProduct / (length1 * length2));
        return (float) Math.acos(dotProduct / (length1 * length2));
    }

    private float orientationAcceptableRange = 30f;

    private boolean isDragging;
    private GestureOrientation currentOrientation;

    private View view;
    private TouchHandlerUtil touchHandler;
}
