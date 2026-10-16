package io.ampy.stargate;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/** AmpyPermissions 1.0.0. Public settings only; never grants or bypasses permissions. */
public final class AmpyPermissions {
 public static final String QUERY="io.ampy.stargate.ACCESS_QUERY",STATUS="io.ampy.stargate.ACCESS_STATUS";
 public static final String RESTRICTED_GUIDE="Se Android mostra ‘Controlled by Restricted Setting’ o ‘Impostazione con limitazioni’:\n\n1. Apri Informazioni app di AmpyStargate.\n2. Tocca ⋮ in alto → Allow restricted settings / Consenti impostazioni con limitazioni.\n3. Conferma la richiesta di Android, poi torna qui.\n4. Apri Accessibilità, seleziona AmpyStargate e abilitalo.\n5. Torna qui e premi Avvia overlay.\n\nLa voce può comparire dopo il primo tentativo di attivazione bloccato. Se non compare, controlla le indicazioni della tua versione Android o eventuali criteri del dispositivo. Ampy non può autorizzarsi da sola.";
 private AmpyPermissions(){}
 public static boolean accessibilityEnabled(Context c){
  String enabled=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
  ComponentName ours=new ComponentName(c,AmpyOverlayService.class);if(enabled==null)return false;
  for(String item:enabled.split(":"))if(ours.equals(ComponentName.unflattenFromString(item)))return true;return false;
 }
 public static boolean notificationsEnabled(Context c){return c.getSystemService(NotificationManager.class).areNotificationsEnabled();}
 public static boolean keyboardSelected(Context c){String name=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);return new ComponentName(c,AmpyKeyboardService.class).equals(ComponentName.unflattenFromString(name==null?"":name));}
 public static boolean homeSelected(Context c){return c.getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_HOME);}
 public static void appInfo(Activity a){a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+a.getPackageName())));}
 public static void accessibility(Activity a){a.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
}
