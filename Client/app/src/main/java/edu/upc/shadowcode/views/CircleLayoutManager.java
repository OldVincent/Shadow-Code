package edu.upc.shadowcode.views;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import edu.upc.shadowcode.Controller;

public class CircleLayoutManager extends RecyclerView.LayoutManager {
    private static final double PI = 3.1415926535;

    public int radius;

    public int yOffset;

    public CircleLayoutManager(int circleRadius, int offset){
        radius = circleRadius;
        yOffset = offset;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        int count = getItemCount();

        double baseX = (getWidth() - getPaddingRight() - getPaddingLeft()) / 2.0;
        double baseY = (getHeight() - getPaddingBottom() - getPaddingTop()) / 2.0 + yOffset;

        double offset = - Math.PI / 2;
        double angle = 2 * Math.PI / count;

        for (int index = 0; index < count; ++index) {
            View view = recycler.getViewForPosition(index);
            addView(view);
            measureChild(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);
            double halfWidth = width / 2.0;
            double halfHeight = height / 2.0;
            double centerX = baseX + radius * Math.cos(offset);
            double centerY = baseY + radius * Math.sin(offset);
            layoutDecorated(view, (int)(centerX - halfWidth), (int)(centerY - halfHeight),
                    (int)(centerX + halfWidth), (int)(centerY + halfHeight));
            offset += angle;
        }
    }
}
