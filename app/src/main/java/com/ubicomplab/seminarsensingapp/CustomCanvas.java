package com.ubicomplab.seminarsensingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomCanvas extends View {
    int screenHeight;
    int screenWidth;
    int COL_NUM;
    int ROW_NUM;
    static int plotWidth;
    static int plotHeight;

    // Maintain a window of history of accelerometer values to plot.
    float accelXHistory[];
    float accelYHistory[];
    float accelZHistory[];
    Rect rects[][];
    Paint paints[][];

    public CustomCanvas(Context context) {
        super(context);
    }

    public CustomCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float normalizeAccelValuesToNumCols(float value, float minValue, float maxValue) {
        // normalizes the values to 0 to NUM_COL.
        return (COL_NUM - 1) * ((value + Math.abs(minValue)) / (Math.abs(minValue) + maxValue));

    }

    public void updatePlot(float newAccelX, float newAccelY, float newAccelZ) {
        float maxX = newAccelX;
        float maxY = newAccelY;
        float maxZ = newAccelZ;
        float minX = newAccelX;
        float minY = newAccelY;
        float minZ = newAccelZ;

        float rollingMeanX = 0;
        float rollingMeanY = 0;
        float rollingMeanZ = 0;
        // Use a rolling mean window that is 1/3rd the current plot window.
        int rollingMeanWindowSize = 5; // (int) ((1.0/3) * ROW_NUM);
        // Update the window to shift the history buffers left by 1 sample and append the new one.
        for(int i = 0; i < ROW_NUM - 1; i++) {
            // Left shift each signal.
            accelXHistory[i] = accelXHistory[i + 1];
            accelYHistory[i] = accelYHistory[i + 1];
            accelZHistory[i] = accelZHistory[i + 1];
            // Calculate the rolling mean for the least 3rd of the previous points.
            if (i > (ROW_NUM - 1) - rollingMeanWindowSize) {
                rollingMeanX += accelXHistory[i];
                rollingMeanY += accelXHistory[i];
                rollingMeanZ += accelXHistory[i];
            }

            // Set min and max for each signal.
            if (accelXHistory[i] > maxX) {
                maxX = accelXHistory[i];
            }
            if (accelXHistory[i] < minX) {
                minX = accelXHistory[i];
            }
            if (accelYHistory[i] > maxY) {
                maxY = accelYHistory[i];
            }
            if (accelYHistory[i] < minY) {
                minY = accelYHistory[i];
            }
            if (accelZHistory[i] > maxZ) {
                maxZ = accelZHistory[i];
            }
            if (accelZHistory[i] < minZ) {
                minZ = accelZHistory[i];
            }
        }
        rollingMeanX = (newAccelX + rollingMeanX) / ROW_NUM;
        rollingMeanY = (newAccelY + rollingMeanY)  / ROW_NUM;
        rollingMeanZ = (newAccelZ + rollingMeanZ)  / ROW_NUM;
        // Normalize the values and add them to the scrolling buffer representing our time window.
        accelXHistory[accelXHistory.length - 1] = normalizeAccelValuesToNumCols(rollingMeanX, minX, maxX);
        accelYHistory[accelYHistory.length - 1] = normalizeAccelValuesToNumCols(rollingMeanY, minY, maxY);
        accelZHistory[accelZHistory.length - 1] = normalizeAccelValuesToNumCols(rollingMeanZ, minZ, maxZ);
    }

    public void init(int num_cols, int num_rows) {
        // Initialize the measurement and time resolution of the plot.
        COL_NUM = num_cols; // resolution of bins is (max accel value - min accel value) / COL_NUM
        ROW_NUM = num_rows; // COL_NUM is window size of the plot in time.
        // Subdivide the screen into the plot size you specify here.
        plotWidth = screenWidth/COL_NUM;
        plotHeight = screenHeight/2/ROW_NUM;

        // Initialize the history of accelerometer values to be the number or rows.
        accelXHistory = new float[num_rows];
        accelYHistory = new float[num_rows];
        accelZHistory = new float[num_rows];

        // initialize Rectangle grid which represent the plot.
        rects = new Rect[ROW_NUM][COL_NUM];
        for(int i = 0; i<ROW_NUM; i++){
            for(int j = 0; j<COL_NUM; j++){
                int left = j*plotWidth;
                int top = (i-1)*plotHeight;
                int right = left+plotWidth;
                int bottom = top+plotHeight;
                rects[i][j] = new Rect(left,top,right,bottom);
            }
        }

        // initialize Paint objects for each rectangle which can color the rectangles.
        paints = new Paint[ROW_NUM][COL_NUM];
        for(int i = 0; i<ROW_NUM; i++){
            for(int j = 0; j<COL_NUM; j++){
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paints[i][j] = paint;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < ROW_NUM; i++) {
            for (int j = 0; j < COL_NUM; j++) {
                // Drawing exterior of rect i, j.
                paints[i][j].setStyle(Paint.Style.STROKE);
                paints[i][j].setColor(Color.WHITE);
                canvas.drawRect(rects[i][j], paints[i][j]);
                // Drawing fill of rect i, j.
                paints[i][j].setStyle(Paint.Style.FILL);
                paints[i][j].setColor(Color.rgb(0, 0, 0));
                canvas.drawRect(rects[i][j], paints[i][j]);
            }
        }
        for (int i = 0; i < ROW_NUM; i++) {
            // Draw the X accelerometer line.
            paints[i][(int) accelXHistory[i]].setColor(Color.rgb(255, 0, 0));
            canvas.drawRect(rects[i][(int) accelXHistory[i]], paints[i][(int) accelXHistory[i]]);

            // Draw the Y accelerometer line.
            paints[i][(int) accelYHistory[i]].setColor(Color.rgb(0, 255, 0));
            canvas.drawRect(rects[i][(int) accelYHistory[i]], paints[i][(int) accelYHistory[i]]);

            // Draw the Z accelerometer line.
            paints[i][(int) accelZHistory[i]].setColor(Color.rgb(0, 0, 255));
            canvas.drawRect(rects[i][(int) accelZHistory[i]], paints[i][(int) accelZHistory[i]]);

        }
    }
}
