package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import co.olinguito.seletiene.app.R;
import com.android.volley.toolbox.NetworkImageView;

/**
 * RoundedImageView
 * configure radius from XML
 * // TODO: make borders antialiased
 */
public class RoundedImageView extends NetworkImageView {

    private final float defRadius = 3.0f;
    float mRadius;
    private Path clipPath = new Path();

    public RoundedImageView(Context context) {
        this(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
        setRadius(a.getDimension(R.styleable.RoundedImageView_radius, defRadius));
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public float getRadius() {
        return mRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        clipPath.addRoundRect(rect, getRadius(), getRadius(), Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}
