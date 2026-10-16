package io.ampy.stargate;
import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.*;
import java.util.List;

/** AmpyOverlayService 1.0.0. Opt-in event metadata; never reads typed text or screenshots. */
public final class AmpyOverlayService extends AccessibilityService {
 private AmpyOverlayView overlay;private WindowManager windows;private AmpyGuardClient guard;private boolean armed,registered;
 private final Handler ui=new Handler(Looper.getMainLooper());private String currentPackage="";
 private final BroadcastReceiver commands=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
  if(AmpyGuardService.STOP.equals(i.getAction()))stopOverlay();
  else if(AmpyGuardService.ARM.equals(i.getAction()))arm();
  else if(AmpyGuardService.CHANGED.equals(i.getAction())&&armed){stopOverlay();arm();}
 }};
 @Override protected void onServiceConnected(){super.onServiceConnected();windows=getSystemService(WindowManager.class);
  IntentFilter filter=new IntentFilter();filter.addAction(AmpyGuardService.STOP);filter.addAction(AmpyGuardService.ARM);filter.addAction(AmpyGuardService.CHANGED);
  if(Build.VERSION.SDK_INT>=33)registerReceiver(commands,filter,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(commands,filter);registered=true;
  // Accessibility activation never implicitly arms a global overlay.
 }
 private void arm(){if(armed)return;guard=new AmpyGuardClient(this,2);guard.connect(()->{
  try{guard.resumeEffects();AmpyTheme theme=new AmpyTheme(this);overlay=new AmpyOverlayView(this,theme);
   WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
   p.gravity=Gravity.TOP|Gravity.LEFT;p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
   windows.addView(overlay,p);armed=true;ui.post(pollKeyboard);
  }catch(RuntimeException e){android.util.Log.e("AmpyOverlay","Overlay unavailable",e);stopOverlay();}
 });}
 private boolean blocked(){
  if(getSystemService(KeyguardManager.class).isKeyguardLocked())return true;
  return currentPackage.equals("com.android.settings")||currentPackage.equals("com.android.systemui")||currentPackage.contains("permissioncontroller")||currentPackage.contains("packageinstaller");
 }
 private final Runnable pollKeyboard=new Runnable(){public void run(){
  if(!armed||overlay==null)return;
  if(blocked()){overlay.setVisibility(View.GONE);overlay.clearEffects();}
  else{overlay.setVisibility(View.VISIBLE);Rect bounds=new Rect();
   List<AccessibilityWindowInfo> list=getWindows();if(list!=null)for(AccessibilityWindowInfo w:list){if(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD){w.getBoundsInScreen(bounds);break;}}
   overlay.keyboard(bounds);
  }ui.postDelayed(this,120);
 }};
 @Override public void onAccessibilityEvent(AccessibilityEvent event){
  if(!armed||overlay==null)return;
  if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){CharSequence pkg=event.getPackageName();if(pkg!=null)currentPackage=pkg.toString();}
  if(blocked()){overlay.setVisibility(View.GONE);overlay.clearEffects();return;}
  if(event.isPassword()||(Build.VERSION.SDK_INT>=34&&event.isAccessibilityDataSensitive()))return;
  int kind=event.getEventType();if(kind!=AccessibilityEvent.TYPE_VIEW_CLICKED&&kind!=AccessibilityEvent.TYPE_VIEW_FOCUSED)return;
  AccessibilityNodeInfo node=event.getSource();if(node==null)return;
  try{if(node.isPassword())return;Rect bounds=new Rect();node.getBoundsInScreen(bounds);overlay.flash(bounds);}
  finally{node.recycle();}
 }
 private void stopOverlay(){armed=false;ui.removeCallbacks(pollKeyboard);
  if(overlay!=null){overlay.close();try{windows.removeViewImmediate(overlay);}catch(RuntimeException ignored){}overlay=null;}
  if(guard!=null){guard.close();guard=null;}
 }
 @Override public void onInterrupt(){stopOverlay();}
 @Override public boolean onUnbind(Intent i){stopOverlay();return super.onUnbind(i);}
 @Override public void onDestroy(){stopOverlay();if(registered){unregisterReceiver(commands);registered=false;}super.onDestroy();}
}
