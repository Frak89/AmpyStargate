package io.ampy.stargate;
import android.content.*;
import android.os.*;

/** AmpyGuardClient 1.0.0. Heartbeats run on the monitored process's main/UI thread. */
public final class AmpyGuardClient implements ServiceConnection {
 private final Context context;private final int role;private final Handler ui=new Handler(Looper.getMainLooper());
 private Messenger guard;private boolean bound,closed;private Runnable ready;
 public AmpyGuardClient(Context c,int r){context=c;role=r;}
 public void connect(Runnable onReady){ready=onReady;closed=false;bound=context.bindService(new Intent(context,AmpyGuardService.class),this,Context.BIND_AUTO_CREATE);}
 @Override public void onServiceConnected(ComponentName n,IBinder b){if(closed)return;guard=new Messenger(b);send(AmpyGuardService.REGISTER);ui.post(beat);if(ready!=null)ready.run();}
 @Override public void onServiceDisconnected(ComponentName n){guard=null;ui.removeCallbacks(beat);if(role==2)context.sendBroadcast(new Intent(AmpyGuardService.STOP).setPackage(context.getPackageName()));}
 public void resumeEffects(){send(AmpyGuardService.RESUME);}
 private void send(int what){try{if(guard!=null){Message m=Message.obtain(null,what,android.os.Process.myPid(),role);guard.send(m);}}catch(RemoteException e){guard=null;}}
 private final Runnable beat=new Runnable(){public void run(){if(!closed&&guard!=null){send(AmpyGuardService.BEAT);ui.postDelayed(this,1000);}}};
 public void close(){closed=true;ui.removeCallbacks(beat);send(AmpyGuardService.LEAVE);guard=null;if(bound){context.unbindService(this);bound=false;}}
}
