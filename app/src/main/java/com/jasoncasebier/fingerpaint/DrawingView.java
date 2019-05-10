package com.jasoncasebier.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {
    private final float TOUCH_TOLERANCE = 4f;

    private ArrayList<PaintPath> paths;
    private ArrayList<PaintPath> redoPaths;
    private PaintPath paintPath;
    private Path path;
    private float pathX;
    private float pathY;

    private Paint paint;
    private int strokeWidth;
    private int color;

    private ArrayList<Integer> rainbowColors;
    private int colorIndex;
    private boolean isRainbow;

    public DrawingView(Context context) {
        super(context);
        setup(null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(attrs);
    }

    public void setup(AttributeSet attrs) {
        paths = new ArrayList<>();
        redoPaths = new ArrayList<>();
        paint = new Paint();
        strokeWidth = 5;
        color = 0xff000000;
        initRainbowColors();
        isRainbow = false;
        colorIndex = 0;
    }

    public void setRainbow() {
        isRainbow = !isRainbow;
    }

    private void initRainbowColors() {
        rainbowColors = new ArrayList<>();
        for (int r = 0; r < 100; r++) {
            rainbowColors.add(Color.rgb(r * 255 / 100, 255, 0));
        }
        for (int g = 100; g > 0; g--) {
            rainbowColors.add(Color.rgb(255, g * 255 / 100, 0));
        }
        for (int b = 0; b < 100; b++) {
            rainbowColors.add(Color.rgb(255, 0, b * 255 / 100));
        }
        for (int r = 100; r > 0; r--) {
            rainbowColors.add(Color.rgb(r * 255 / 100, 255, 0));
        }
        for (int g = 0; g < 100; g++) {
            rainbowColors.add(Color.rgb(255, g * 255 / 100, 0));
        }
        for (int b = 100; b > 0; b--) {
            rainbowColors.add(Color.rgb(255, 0, b * 255 / 100));
        }
    }

    private int getColorIndex() {
        int size = rainbowColors.size();
        if (colorIndex == size) {
            colorIndex = 0;
        }
        return colorIndex++;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setDither(true);

        for (PaintPath paintPath : paths) {
            paint.setColor(paintPath.color);
            paint.setStrokeWidth(paintPath.strokeWidth);
            canvas.drawPath(paintPath.path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPath(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                movePath(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                endPath();
                invalidate();
                break;
        }

        return true;
    }

    private void startPath(float x, float y) {
        redoPaths.clear();
        path = new Path();
        paintPath = new PaintPath(color, strokeWidth, path);
        paths.add(paintPath);
        path.reset();
        path.moveTo(x, y);
        pathX = x;
        pathY = y;
    }

    private void movePath(float x, float y) {
        float moveX = Math.abs(x - pathX);
        float moveY = Math.abs(y - pathY);
        if (moveX >= TOUCH_TOLERANCE || moveY >= TOUCH_TOLERANCE) {
            if (isRainbow) {
                this.color = rainbowColors.get(getColorIndex());
                path = new Path();
                paintPath = new PaintPath(color, strokeWidth, path);
                paths.add(paintPath);
                path.reset();
                path.moveTo(pathX, pathY);
            }
            path.quadTo(pathX, pathY, (x + pathX)/2, (y + pathY)/2);
            pathX = x;
            pathY = y;
        }
    }

    private void endPath() {
        path.lineTo(pathX, pathY);
    }

    public void undoLast() {
        if (paths.size() > 0) {
            redoPaths.add(paths.get(paths.size() - 1));
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void redoLast() {
        if (redoPaths.size() > 0) {
            paths.add(redoPaths.get(redoPaths.size() - 1));
            redoPaths.remove(redoPaths.size() - 1);
            invalidate();
        }
    }

    public void setCurrentColor(int color) {
        if (isRainbow) {
            isRainbow = !isRainbow;
        }
        this.color = color;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void clearAll() {
        paths.clear();
        redoPaths.clear();
        invalidate();
    }
}
