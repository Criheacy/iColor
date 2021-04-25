package com.icolor.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GestureUtil {
    public interface OnGestureListener {
        void click();
        void touch();
        void longTouch();

        void dragStart(GestureOrientation orientation);
        void dragging(GestureOrientation orientation, int draggingDistance);
        void dragEnd();
    }

    public enum GestureOrientation {
        NONE, HORIZONTAL, VERTICAL
    }

    public GestureUtil(View view, OnGestureListener gestureListener) {
        this.view = view;

        longTouchTimer = new ValueAnimator();
        longTouchTimer.setDuration(longTouchDuration);
        longTouchTimer.setIntValues(0, 1);
        longTouchTimer.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) {
                longTouching = false;
            }
            @Override public void onAnimationRepeat(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (longTouching) {
                    longTouching = false;
                    completeLongTouch = true;
                    gestureListener.longTouch();
                    Log.d("Long Click", "Long Touch!");
                }
            }
        });

        isDragging = false;
        longTouching = false;
        completeLongTouch = false;
        this.touchHandler = new TouchHandlerUtil(new TouchHandlerUtil.OnTouchEvent() {
            @Override
            public boolean onClick(Point p) {
                if (WindowUtil.pointInView(p, view)) {
                    // long touch cancels the click callback, but handles the event
                    if (!completeLongTouch) {
                        gestureListener.click();
                    }
                    // click cancels long touch
                    longTouchTimer.cancel();
                    completeLongTouch = false;
                    return true;
                }
                return false;
            }

            @Override
            public boolean onTouch(Point p) {
                if (WindowUtil.pointInView(p, view)) {
                    gestureListener.touch();
                    longTouchTimer.start();
                    longTouching = true;
                    return false;
                }
                return false;
            }

            @Override
            public boolean onDrag(Point origin, Point dragTo, Point lastDragVector) {
                if (!WindowUtil.pointInView(origin, view)) {
                    return false;
                }
                if (!isDragging) {
                    isDragging = true;

                    // drag cancels long touch
                    longTouchTimer.cancel();
                    currentOrientation = getOrientation
                            (new Point(dragTo.x - origin.x, dragTo.y - origin.y));
                    gestureListener.dragStart(currentOrientation);
                }
                if (currentOrientation.equals(GestureOrientation.HORIZONTAL)) {
                    gestureListener.dragging(currentOrientation, lastDragVector.x);
                } else if (currentOrientation.equals(GestureOrientation.VERTICAL)) {
                    gestureListener.dragging(currentOrientation, lastDragVector.y);
                }
                return true;
            }

            @Override
            public boolean onLeave() {
                Log.d("Gesture", "Leave");
                isDragging = false;
                completeLongTouch = false;
                longTouchTimer.cancel();
                currentOrientation = GestureOrientation.NONE;
                gestureListener.dragEnd();
                return false;
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

    private final float orientationAcceptableRange = 40f;

    // Tracks if it's in the process of long touching; Set to false when animator is canceled
    private boolean longTouching;

    // Tracks if a long touch event has completed
    private boolean completeLongTouch;
    private final long longTouchDuration = 500;
    private final ValueAnimator longTouchTimer;

    private boolean isDragging;
    private GestureOrientation currentOrientation;

    private final View view;
    private final TouchHandlerUtil touchHandler;
}
