package io.ampy.stargate;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import java.util.ArrayList;

/** AmpyOverlay 1.0.0. Transparent hardware canvas; bounded transient effects. */
public final class AmpyOverlayView extends View {
 private final AmpyTheme theme;private final Paint paint=new Paint(3);private final ArrayList<Flash> flashes=new ArrayList<>();
 private final ArrayList<RectF> skin=new ArrayList<>();
 private final RectF keyboard=new RectF(),from=new RectF(),target=new RectF();private long keyboardAt;private boolean active=true,keyboardVisible;
 private static final class Flash {RectF rect;long time;Flash(RectF r){rect=new RectF(r);time=SystemClock.uptimeMillis();}}
 public AmpyOverlayView(Context c,AmpyTheme t){super(c);theme=t;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);setFocusable(false);setClickable(false);setBackgroundColor(Color.TRANSPARENT);if(android.os.Build.VERSION.SDK_INT>=35)setRequestedFrameRate(t.fps);}
 public void skin(java.util.List<Rect> bounds){skin.clear();int[] pos=new int[2];getLocationOnScreen(pos);for(Rect r:bounds){RectF local=new RectF(r);local.offset(-pos[0],-pos[1]);skin.add(local);}invalidate();}
 public void flash(Rect bounds){
  if(bounds.isEmpty())return;int[] pos=new int[2];getLocationOnScreen(pos);RectF r=new RectF(bounds);r.offset(-pos[0],-pos[1]);
  float[] clipped=AmpyMath.clip(r.left,r.top,r.right,r.bottom,getWidth(),getHeight());if(clipped==null)return;
  if(flashes.size()>=24)flashes.remove(0);flashes.add(new Flash(new RectF(clipped[0],clipped[1],clipped[2],clipped[3])));invalidate();
 }
 public void keyboard(Rect bounds){
  boolean visible=!bounds.isEmpty();int[] pos=new int[2];getLocationOnScreen(pos);RectF next=new RectF(bounds);if(visible)next.offset(-pos[0],-pos[1]);
  if(!visible&&!keyboardVisible)return;
  if(visible&&keyboardVisible&&next.equals(target))return;
  if(visible&&!keyboardVisible){from.set(next.left,getHeight()+1,next.right,getHeight()+1+next.height());}
  else from.set(keyboard);
  if(!visible)next.set(keyboard.left,getHeight()+1,keyboard.right,getHeight()+1+keyboard.height());
  target.set(next);keyboardVisible=visible;keyboardAt=SystemClock.uptimeMillis();invalidate();
 }
 public void clearEffects(){flashes.clear();skin.clear();keyboardVisible=false;keyboardAt=0;keyboard.setEmpty();target.setEmpty();from.setEmpty();invalidate();}
 public void close(){active=false;clearEffects();}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(!active)return;long now=SystemClock.uptimeMillis();
  if(theme.ambient){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);paint.setColor((theme.accent&0xffffff)|0x10000000);float offset=(now%2400)/2400f*30;for(float y=offset;y<getHeight();y+=30)c.drawLine(0,y,getWidth(),y,paint);}
  float k=AmpyMath.ease((now-keyboardAt)/150f);keyboard.set(lerp(from.left,target.left,k),lerp(from.top,target.top,k),lerp(from.right,target.right,k),lerp(from.bottom,target.bottom,k));
  if(!keyboardVisible&&k>=1)keyboard.setEmpty();
  if(theme.idleSkin){c.save();if(!keyboard.isEmpty())c.clipOutRect(keyboard);for(RectF r:skin){c.save();c.translate(r.left,r.top);Path p=theme.shape(r.width(),r.height(),0,0);paint.setStyle(Paint.Style.FILL);paint.setColor((theme.surface&0xffffff)|0x18000000);c.drawPath(p,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.5f);paint.setColor((theme.accent&0xffffff)|0x66000000);c.drawPath(p,paint);c.restore();}c.restore();}
  if(!keyboard.isEmpty()){
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);paint.setColor((theme.accent&0xffffff)|0x66000000);c.drawRoundRect(keyboard,28,28,paint);
   paint.setColor((theme.secondary&0xffffff)|0x44000000);c.drawLine(keyboard.left+28,keyboard.top+5,keyboard.right-28,keyboard.top+5,paint);
  }
  for(int i=flashes.size()-1;i>=0;i--){Flash f=flashes.get(i);float age=(now-f.time)/650f;if(age>=1){flashes.remove(i);continue;}float fade=1-age;RectF r=f.rect;
   c.save();c.translate(r.left,r.top);Path p=theme.shape(r.width(),r.height(),age*6,theme.effect.equals("morph")?fade:0);
   paint.setStyle(Paint.Style.FILL);paint.setColor((theme.accent&0xffffff)|((int)(fade*30)<<24));c.drawPath(p,paint);
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2+fade*3);paint.setColor((theme.accent&0xffffff)|((int)(fade*220)<<24));c.drawPath(p,paint);
   if(theme.effect.equals("glitch")){paint.setColor((theme.secondary&0xffffff)|((int)(fade*180)<<24));c.translate((float)Math.sin(age*95)*fade*8,0);c.drawPath(p,paint);}
   if(theme.effect.equals("scan")){c.clipPath(p);paint.setStrokeWidth(1);for(float y=0;y<r.height();y+=6)c.drawLine(0,y,r.width(),y,paint);}
   c.restore();
  }
  if(theme.ambient||!flashes.isEmpty()||k<1)postInvalidateOnAnimation();
 }
 private float lerp(float a,float b,float x){return a+(b-a)*x;}
}
