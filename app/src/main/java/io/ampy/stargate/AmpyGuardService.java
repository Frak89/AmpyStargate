package io.ampy.stargate;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONObject;

/** AmpyGuard 1.0.0. Isolated, user-visible watchdog; never kills foreign processes. */
public final class AmpyGuardService extends Service {
 public static final int REGISTER=1,BEAT=2,LEAVE=3,RESUME=4;
 public static final String STOP="io.ampy.stargate.STOP",CHANGED="io.ampy.stargate.CHANGED",ARM="io.ampy.stargate.ARM";
 private static final String CHANNEL="AmpyGuard";private final HashMap<Integer,Watch> watches=new HashMap<>();
 private final Handler handler=new Handler(Looper.getMainLooper());private Messenger messenger;
 private static final class Watch {int pid,role;long last;Watch(int p,int r){pid=p;role=r;last=SystemClock.elapsedRealtime();}}
 @Override public void onCreate(){super.onCreate();messenger=new Messenger(new Handler(Looper.getMainLooper(),this::message));
  getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(CHANNEL,"AmpyGuard · Stop e recupero",NotificationManager.IMPORTANCE_LOW));handler.post(tick);
 }
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i!=null&&STOP.equals(i.getAction())){recover("Arresto manuale",0);return START_NOT_STICKY;}
  startForeground(410,notification("Protezione Ampy attiva",false));return START_NOT_STICKY;
 }
 @Override public IBinder onBind(Intent i){return messenger.getBinder();}
 private boolean message(Message m){
  if(m.sendingUid!=android.os.Process.myUid())return true;int pid=m.arg1,role=m.arg2;
  if(m.what==LEAVE){watches.remove(pid);return true;}
  if(m.what==RESUME){writeRecovery("");return true;}
  if(m.what==REGISTER&&own(pid,role)){watches.put(pid,new Watch(pid,role));}
  else if(m.what==BEAT){Watch w=watches.get(pid);if(w!=null)w.last=SystemClock.elapsedRealtime();}
  return true;
 }
 private boolean own(int pid,int role){
  if(pid==android.os.Process.myPid())return false;
  String expected=getPackageName()+(role==1?"":role==2?":ampy_overlay":role==3?":ampy_keyboard":":invalid");
  List<ActivityManager.RunningAppProcessInfo> list=getSystemService(ActivityManager.class).getRunningAppProcesses();if(list==null)return false;
  for(ActivityManager.RunningAppProcessInfo p:list)if(p.pid==pid&&p.uid==android.os.Process.myUid()&&expected.equals(p.processName))return true;return false;
 }
 private final Runnable tick=new Runnable(){public void run(){
  long now=SystemClock.elapsedRealtime();for(Watch w:new ArrayList<>(watches.values()))if(AmpyMath.stale(now,w.last,6000)){
   if(!own(w.pid,w.role)){watches.remove(w.pid);continue;}
   recover("Thread UI Ampy non responsivo per almeno 6 secondi",w.pid);break;
  }handler.postDelayed(this,500);
 }};
 private void recover(String reason,int stalledPid){
  writeRecovery(reason);sendBroadcast(new Intent(STOP).setPackage(getPackageName()));
  // Take a stable snapshot before unregistration messages arrive.
  ArrayList<Watch> snapshot=new ArrayList<>(watches.values());watches.clear();
  for(Watch w:snapshot)if((w.role==2||w.pid==stalledPid)&&own(w.pid,w.role))android.os.Process.killProcess(w.pid);
  if(Build.VERSION.SDK_INT<33||checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)getSystemService(NotificationManager.class).notify(411,notification(reason,true));
 }
 private Notification notification(String message,boolean recovery){
  Intent open=new Intent(this,AmpyRecoveryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  PendingIntent show=PendingIntent.getActivity(this,1,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent stop=PendingIntent.getService(this,2,new Intent(this,AmpyGuardService.class).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ampy_stargate).setContentTitle(recovery?"AmpyStargate · modalità sicura":"AmpyGuard")
   .setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setContentIntent(show).setOngoing(!recovery)
   .addAction(new Notification.Action.Builder(null,"Stop effetti",stop).build()).addAction(new Notification.Action.Builder(null,"Launcher Android / recupero",show).build()).build();
 }
 private void writeRecovery(String reason){
  AtomicFile f=new AtomicFile(new File(getFilesDir(),"AmpyGuard.json"));FileOutputStream out=null;
  try{JSONObject o=new JSONObject();o.put("version","1.0.0");o.put("safe_mode",!reason.isEmpty());o.put("reason",reason);o.put("timestamp",System.currentTimeMillis());out=f.startWrite();out.write(o.toString(2).getBytes(StandardCharsets.UTF_8));f.finishWrite(out);}
  catch(Exception e){if(out!=null)f.failWrite(out);android.util.Log.e("AmpyGuard",e.toString());}
 }
 public static String recoveryReason(Context c){try{return new JSONObject(new String(new AtomicFile(new File(c.getFilesDir(),"AmpyGuard.json")).readFully(),StandardCharsets.UTF_8)).optString("reason","");}catch(Exception e){return "";}}
 @Override public void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
}
