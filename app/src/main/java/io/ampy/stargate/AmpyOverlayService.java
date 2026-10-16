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
 private AmpyOverlayView overlay;private WindowManager windows;private AmpyGuardClient guard;private boolean armed,arming,registered,connected;private long lastReport;private int reactions;private String lastError="";
 private final Handler ui=new Handler(Looper.getMainLooper());private String currentPackage="";
 private final Runnable armTimeout=()->{if(arming){lastError="AmpyGuard: connessione non pronta entro 4 secondi";stopOverlay();}};
 private final BroadcastReceiver commands=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
  if(AmpyGuardService.STOP.equals(i.getAction()))stopOverlay();
  else if(AmpyGuardService.ARM.equals(i.getAction()))arm();
  else if(AmpyGuardService.CHANGED.equals(i.getAction())&&armed){stopOverlay();arm();}
  else if(AmpyPermissions.QUERY.equals(i.getAction()))status();
 }};
 @Override protected void onServiceConnected(){super.onServiceConnected();windows=getSystemService(WindowManager.class);
  IntentFilter filter=new IntentFilter();filter.addAction(AmpyGuardService.STOP);filter.addAction(AmpyGuardService.ARM);filter.addAction(AmpyGuardService.CHANGED);filter.addAction(AmpyPermissions.QUERY);
  androidx.core.content.ContextCompat.registerReceiver(this,commands,filter,androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);registered=true;connected=true;writeReport(true);status();
  // Accessibility activation never implicitly arms a global overlay.
 }
 private void arm(){if(armed||arming){status();return;}arming=true;lastError="";ui.postDelayed(armTimeout,4000);guard=new AmpyGuardClient(this,2);final AmpyGuardClient client=guard;guard.connect(()->{
  if(!arming||guard!=client)return;
  try{guard.resumeEffects();AmpyTheme theme=new AmpyTheme(this);overlay=new AmpyOverlayView(this,theme);
   WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
   p.gravity=Gravity.TOP|Gravity.LEFT;p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
   windows.addView(overlay,p);armed=true;arming=false;ui.removeCallbacks(armTimeout);writeReport(true);status();ui.post(pollKeyboard);ui.postDelayed(snapshot,180);
  }catch(RuntimeException e){lastError=e.getClass().getSimpleName()+": "+e.getMessage();android.util.Log.e("AmpyOverlay","Overlay unavailable",e);stopOverlay();}
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
  }writeReport(false);ui.postDelayed(this,120);
 }};
 @Override public void onAccessibilityEvent(AccessibilityEvent event){
  if(!armed||overlay==null)return;
  if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){CharSequence pkg=event.getPackageName();boolean ime=false;List<AccessibilityWindowInfo> list=getWindows();if(list!=null)for(AccessibilityWindowInfo w:list)if(w.getId()==event.getWindowId()&&(w.getType()==AccessibilityWindowInfo.TYPE_INPUT_METHOD||w.getType()==AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY))ime=true;if(pkg!=null&&!ime)currentPackage=pkg.toString();}
  if(blocked()){overlay.setVisibility(View.GONE);overlay.clearEffects();return;}
  if(event.isPassword()||(Build.VERSION.SDK_INT>=34&&event.isAccessibilityDataSensitive()))return;
  int kind=event.getEventType();if(kind==AccessibilityEvent.TYPE_VIEW_SCROLLED||kind==AccessibilityEvent.TYPE_WINDOWS_CHANGED||kind==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){overlay.skin(java.util.Collections.emptyList());ui.removeCallbacks(snapshot);ui.postDelayed(snapshot,180);}
  if(kind!=AccessibilityEvent.TYPE_VIEW_CLICKED&&kind!=AccessibilityEvent.TYPE_VIEW_FOCUSED)return;
  AccessibilityNodeInfo node=event.getSource();if(node==null)return;
  try{if(node.isPassword()||(Build.VERSION.SDK_INT>=34&&node.isAccessibilityDataSensitive()))return;Rect bounds=new Rect();node.getBoundsInScreen(bounds);overlay.flash(bounds);reactions++;writeReport(true);}
  finally{node.recycle();}
 }
 private void stopOverlay(){armed=false;arming=false;ui.removeCallbacks(armTimeout);ui.removeCallbacks(pollKeyboard);ui.removeCallbacks(snapshot);
  if(overlay!=null){overlay.close();try{windows.removeViewImmediate(overlay);}catch(RuntimeException ignored){}overlay=null;}
  if(guard!=null){guard.close();guard=null;}writeReport(true);status();
 }
 private void status(){sendBroadcast(new Intent(AmpyPermissions.STATUS).setPackage(getPackageName()).putExtra("connected",connected).putExtra("active",armed).putExtra("arming",arming).putExtra("error",lastError));}
 private final Runnable snapshot=()->{if(armed&&overlay!=null&&!blocked()){int[] p=new int[2];overlay.getLocationOnScreen(p);Rect area=new Rect(p[0],p[1],p[0]+overlay.getWidth(),p[1]+overlay.getHeight());overlay.skin(AmpySkinSnapshot.collect(getRootInActiveWindow(),area));}};
 private void writeReport(boolean force){long now=SystemClock.uptimeMillis();if(!force&&now-lastReport<1000)return;lastReport=now;
  android.util.AtomicFile f=new android.util.AtomicFile(new java.io.File(getFilesDir(),"AmpyOverlay.json"));java.io.FileOutputStream out=null;
  try{org.json.JSONObject o=new org.json.JSONObject();o.put("version","1.1.0");o.put("active",armed);o.put("connected",connected);o.put("arming",arming);o.put("error",lastError);o.put("reactions",reactions);o.put("pid",android.os.Process.myPid());o.put("uptime_ms",now);out=f.startWrite();out.write(o.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));f.finishWrite(out);}catch(Exception e){if(out!=null)f.failWrite(out);android.util.Log.w("AmpyOverlay","Diagnostic write failed",e);}
 }
 @Override public void onInterrupt(){stopOverlay();}
 @Override public boolean onUnbind(Intent i){connected=false;stopOverlay();return super.onUnbind(i);}
 @Override public void onDestroy(){connected=false;stopOverlay();if(registered){unregisterReceiver(commands);registered=false;}super.onDestroy();}
}
