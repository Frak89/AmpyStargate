package io.ampy.stargate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;

/** AmpyRecovery 1.0.0. Deliberately simple UI in the guard process. */
public final class AmpyRecoveryActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);
  sendBroadcast(new Intent(AmpyGuardService.STOP).setPackage(getPackageName()));
  LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(32,48,32,32);
  TextView message=new TextView(this);message.setText("AmpyStargate · recupero\n\nGli effetti sono stati fermati.\n"+AmpyGuardService.recoveryReason(this)+"\n\nPer tornare alla Home Android scegli Pixel Launcher nelle impostazioni. Ampy non può cambiare questa scelta senza di te.");box.addView(message);
  add(box,"Scegli launcher Android",()->startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)));
  add(box,"Disattiva servizio overlay",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  add(box,"Impostazioni tastiera",()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
  add(box,"Apri AmpyStargate",()->startActivity(new Intent(this,AmpyLauncherActivity.class)));
  setContentView(box);
 }
 private void add(LinearLayout box,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setOnClickListener(v->action.run());box.addView(b);}
}
