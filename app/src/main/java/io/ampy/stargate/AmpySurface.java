package io.ampy.stargate;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.animation.*;
import android.animation.ValueAnimator;

/** AmpySurface 1.0.0: vector contour and press pulse; no touch interception. */
public final class AmpySurface extends Drawable {
 private final AmpyTheme theme;private final Paint paint=new Paint(3);private float pulse;private ValueAnimator animation;private boolean selected;
 public AmpySurface(AmpyTheme t){theme=t;}
 public void selected(boolean on){selected=on;invalidateSelf();}
 public void flash(){if(animation!=null)animation.cancel();animation=ValueAnimator.ofFloat(1,0);animation.setDuration(420);animation.addUpdateListener(a->{pulse=(float)a.getAnimatedValue();invalidateSelf();});animation.start();}
 @Override public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left+3,b.top+3);Path p=theme.shape(Math.max(1,b.width()-6),Math.max(1,b.height()-6),pulse*6,pulse);
  paint.setStyle(Paint.Style.FILL);paint.setColor(theme.surface);c.drawPath(p,paint);paint.setStyle(Paint.Style.STROKE);
  if(selected){paint.setStrokeWidth(10);paint.setColor((theme.selection&0xffffff)|0x18000000);c.drawPath(p,paint);paint.setStrokeWidth(5);paint.setColor((theme.selection&0xffffff)|0x44000000);c.drawPath(p,paint);}
  paint.setStrokeWidth(1.5f+pulse*3);paint.setColor(selected?theme.selection:pulse>.1?theme.secondary:theme.accent);c.drawPath(p,paint);c.restore();}
 @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}
 @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
 @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
