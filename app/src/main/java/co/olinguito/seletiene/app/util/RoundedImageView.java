package co.olinguito.seletiene.app.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import co.olinguito.seletiene.app.R;
import com.android.volley.toolbox.NetworkImageView;

public class RoundedImageView extends NetworkImageView {

    private final float defRadius = 3.0f;
    private RectF mRectF;
    float mRadius;
    private Paint mPaint;

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
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            mPaint = getPaint(bm);
            mRectF = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Drawable d = getDrawable();
        Bitmap bitmap = ((BitmapDrawable) d.mutate()).getBitmap();
        if (bitmap != null)
            setImageBitmap(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mPaint == null)
            return;
        mRectF.set(0.0f, 0.0f, getWidth(), getHeight());
        canvas.drawRoundRect(mRectF, getRadius(), getRadius(), mPaint);
    }

    private Paint getPaint(Bitmap bmp) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        BitmapShader shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        return paint;
    }
}
