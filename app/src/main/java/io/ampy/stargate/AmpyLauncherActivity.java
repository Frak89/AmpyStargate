package io.ampy.stargate;
import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.*;
import android.content.res.Configuration;
import android.os.*;
import android.provider.Settings;
import android.graphics.Typeface;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;

/** AmpyLauncher 0.1.0: genuine HOME role and native app drawer. */
public final class AmpyLauncherActivity extends Activity {
 private TextView clock;private final Handler ui=new Handler(Looper.getMainLooper());
 private final Runnable clockTick=new Runnable(){public void run(){if(clock!=null)clock.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));ui.postDelayed(this,60000-System.currentTimeMillis()%60000);}};
 private AmpyTheme theme;private LinearLayout body;private int page,testClicks;private AmpyGuardClient guard;private String pendingImport="AmpyOrganic.svg";
 private final ArrayList<ResolveInfo> apps=new ArrayList<>();
 @Override public void onCreate(Bundle state){super.onCreate(state);theme=new AmpyTheme(this);if(state!=null){page=state.getInt("page",0);pendingImport=state.getString("import","AmpyOrganic.svg");}
  android.view.WindowManager.LayoutParams p=getWindow().getAttributes();p.rotationAnimation=WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT;getWindow().setAttributes(p);
  refreshApps();render();
  if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::back);
 }
 @Override protected void onResume(){super.onResume();theme.load();render();ui.removeCallbacks(clockTick);ui.post(clockTick);
  try{startForegroundService(new Intent(this,AmpyGuardService.class));guard=new AmpyGuardClient(this,1);guard.connect(null);}catch(RuntimeException e){notice("AmpyGuard non avviato: "+e.getMessage());}
 }
 @Override protected void onPause(){ui.removeCallbacks(clockTick);if(guard!=null){guard.close();guard=null;}super.onPause();}
 @Override public void onConfigurationChanged(Configuration c){super.onConfigurationChanged(c);render();}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("page",page);b.putString("import",pendingImport);}
 private void back(){if(page!=0){page=0;render();}else if(!getSystemService(RoleManager.class).isRoleHeld(RoleManager.ROLE_HOME))finish();}
 @Override public boolean onKeyDown(int code,KeyEvent e){if(Build.VERSION.SDK_INT<33&&code==KeyEvent.KEYCODE_BACK)return true;return super.onKeyDown(code,e);}
 @Override public boolean onKeyUp(int code,KeyEvent e){if(Build.VERSION.SDK_INT<33&&code==KeyEvent.KEYCODE_BACK){back();return true;}return super.onKeyUp(code,e);}
 private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);v.setTypeface(Typeface.MONOSPACE);v.setPadding(dp(8),dp(8),dp(8),dp(8));return v;}
 private Button button(LinearLayout host,String title,Runnable action){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(theme.text);b.setPadding(dp(12),dp(8),dp(12),dp(8));AmpySurface surface=new AmpySurface(theme);b.setBackground(surface);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.setMargins(dp(4),dp(4),dp(4),dp(4));host.addView(b,p);b.setOnClickListener(v->{surface.flash();action.run();});return b;
 }
 private void render(){clock=null;
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(theme.background);root.setPadding(dp(12),dp(8),dp(12),dp(8));root.setFitsSystemWindows(true);
  TextView title=text("AMPYSTARGATE  /  0.1.0 α",21,theme.accent);root.addView(title);
  LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);root.addView(nav);
  for(int i=0;i<3;i++){final int tab=i;Button b=button(nav,new String[]{"Home","App","Settings"}[i],()->{page=tab;render();});b.setTextColor(page==i?theme.secondary:theme.accent);LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)b.getLayoutParams();p.width=0;p.weight=1;}
  ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);scroll.addView(body);setContentView(root);
  if(Build.VERSION.SDK_INT>=35)root.setRequestedFrameRate(theme.fps);
  if(page==0)home();else if(page==1)drawer();else settings();
 }
 private void home(){
  clock=text(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),52,theme.text);body.addView(clock);
  body.addView(text("AMPYCORE / VISUAL SHELL\n"+theme.name(),12,theme.secondary));
  String reason=AmpyGuardService.recoveryReason(this);if(!reason.isEmpty())body.addView(text("MODALITÀ SICURA\n"+reason+"\nGli effetti restano spenti fino al tuo prossimo avvio esplicito.",14,theme.secondary));
  button(body,"Apri drawer",()->{page=1;render();});button(body,"Launcher predefinito",this::chooseHome);
  button(body,"Avvia overlay · sperimentale",this::startOverlay);button(body,"Stop effetti / recupero",this::stopEffects);
  TextView test=text("Tocchi nativi: "+testClicks,14,theme.secondary);body.addView(test);
  Button demo=button(body,"Test tocco nativo",()->{testClicks++;test.setText("Tocchi nativi: "+testClicks);try{java.nio.file.Files.write(new File(getFilesDir(),"AmpyNativeClicks.txt").toPath(),Integer.toString(testClicks).getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(IOException e){notice(e.toString());}});
  if(Build.VERSION.SDK_INT>=34)demo.setAccessibilityDataSensitive(View.ACCESSIBILITY_DATA_SENSITIVE_NO);
  body.addView(text("Overlay trasparente ai tocchi, guidato dai soli eventi UI accessibili. Nessuna modifica interna alle altre app. I permessi non si attivano automaticamente.",13,theme.text));
 }
 private void refreshApps(){apps.clear();Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);apps.addAll(getPackageManager().queryIntentActivities(launch,0));apps.sort(new ResolveInfo.DisplayNameComparator(getPackageManager()));}
 private void drawer(){refreshApps();int count=getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE?5:3;LinearLayout row=null;
  for(int i=0;i<apps.size();i++){if(i%count==0){row=new LinearLayout(this);body.addView(row);}ResolveInfo info=apps.get(i);LinearLayout cell=new LinearLayout(this);cell.setOrientation(LinearLayout.VERTICAL);cell.setGravity(Gravity.CENTER);cell.setPadding(dp(6),dp(12),dp(6),dp(8));
   AmpySurface surface=new AmpySurface(theme);cell.setBackground(surface);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(148),1);params.setMargins(dp(3),dp(3),dp(3),dp(3));row.addView(cell,params);
   ImageView icon=new ImageView(this);icon.setImageDrawable(info.loadIcon(getPackageManager()));cell.addView(icon,new LinearLayout.LayoutParams(dp(64),dp(64)));TextView label=text(info.loadLabel(getPackageManager()).toString(),12,theme.text);label.setMaxLines(2);label.setGravity(Gravity.CENTER);cell.addView(label);
   cell.setContentDescription(info.loadLabel(getPackageManager()));cell.setOnClickListener(v->{surface.flash();try{Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(new ComponentName(info.activityInfo.packageName,info.activityInfo.name));startActivity(launch,ActivityOptions.makeScaleUpAnimation(cell,0,0,cell.getWidth(),cell.getHeight()).toBundle());}catch(RuntimeException e){notice("App non avviabile: "+e.getMessage());}});
  }
 }
 private void settings(){
  button(body,"Esporta report AmpyDebug",()->startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain").putExtra(Intent.EXTRA_TITLE,"AmpyStargate-Debug.txt"),13));
  button(body,"Scegli tema SVG",()->new AlertDialog.Builder(this).setTitle("AmpyThemes").setItems(AmpyTheme.NAMES,(d,n)->{theme.select(n);changed();render();}).show());
  button(body,"Effetto pulsanti overlay",()->new AlertDialog.Builder(this).setTitle("AmpyEffects").setItems(new String[]{"Morph organico","Glitch","TV scan lines"},(d,n)->{theme.saveEffects(new String[]{"morph","glitch","scan"}[n],theme.ambient,theme.fps);changed();render();}).show());
  button(body,theme.idleSkin?"Skin dei controlli a riposo: ON":"Skin dei controlli a riposo: OFF",()->{theme.setIdleSkin(!theme.idleSkin);changed();render();});
  button(body,theme.ambient?"Scan ambient: ON":"Scan ambient: OFF",()->{theme.saveEffects(theme.effect,!theme.ambient,theme.fps);changed();render();});
  button(body,"Richiesta display: "+theme.fps+" Hz",()->{theme.saveEffects(theme.effect,theme.ambient,theme.fps==120?60:120);changed();render();});
  button(body,"Importa forma SVG M/C/Z",()->pick("AmpyOrganic.svg","image/svg+xml"));button(body,"Importa palette JSON",()->pick("palette.json","application/json"));
  button(body,"Abilita servizio Accessibilità",this::accessibilityDisclosure);button(body,"Avvia overlay",this::startOverlay);button(body,"Stop effetti / recupero",this::stopEffects);
  button(body,"Autorizza notifiche recupero",()->{if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},11);});
  button(body,"Abilita AmpyKeyboard opzionale",()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
  button(body,"Scegli tastiera",()->getSystemService(InputMethodManager.class).showInputMethodPicker());
  button(body,"Scegli launcher predefinito",this::chooseHome);
  button(body,"Riprendi dopo recupero",()->{if(guard!=null){guard.resumeEffects();notice("Modalità sicura rimossa su richiesta. Overlay ancora spento finché non premi Avvia.");render();}});
  body.addView(text("File locali: "+theme.root().getAbsolutePath()+"\nSVG: un contorno M/C/Z assoluto, viewBox 0 0 1000 240, massimo 64 KiB. Palette: background/surface/accent/secondary/text in HEX. Puoi importare i file dal selettore Android.\n\nAmpyKeyboard è una tastiera propria di base: Gboard non viene modificata. Nessuna lettura dei caratteri digitati da altre tastiere. Nessuna rete, cattura schermo o cronologia input.",12,theme.text));
 }
 private void changed(){sendBroadcast(new Intent(AmpyGuardService.CHANGED).setPackage(getPackageName()));}
 private void pick(String file,String mime){pendingImport=file;startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType(mime).addCategory(Intent.CATEGORY_OPENABLE),12);}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==13&&result==RESULT_OK&&data!=null&&data.getData()!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){String report="AmpyStargate 0.1.0-alpha\nRecovery: "+AmpyGuardService.recoveryReason(this)+"\n";File f=new File(getFilesDir(),"AmpyOverlay.json");if(f.isFile())report+=new String(java.nio.file.Files.readAllBytes(f.toPath()),java.nio.charset.StandardCharsets.UTF_8);if(out!=null)out.write(report.getBytes(java.nio.charset.StandardCharsets.UTF_8));notice("Report esportato");}catch(Exception e){notice(e.getMessage());}}if(request==12&&result==RESULT_OK&&data!=null&&data.getData()!=null){try(InputStream in=getContentResolver().openInputStream(data.getData())){if(in==null)throw new IOException("File vuoto");theme.importFile(pendingImport,in);changed();render();notice("Tema aggiornato");}catch(Exception e){notice("Importazione non riuscita: "+e.getMessage());}}}
 private void chooseHome(){RoleManager roles=getSystemService(RoleManager.class);if(roles.isRoleAvailable(RoleManager.ROLE_HOME))startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME),10);else startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}
 private boolean accessibilityEnabled(){String enabled=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);String name=new ComponentName(this,AmpyOverlayService.class).flattenToString();if(enabled==null)return false;for(String c:enabled.split(":"))if(ComponentName.unflattenFromString(c)!=null&&ComponentName.unflattenFromString(c).equals(ComponentName.unflattenFromString(name)))return true;return false;}
 private void accessibilityDisclosure(){new AlertDialog.Builder(this).setTitle("AmpyOverlay · autorizzazione facoltativa").setMessage(R.string.ampy_accessibility_description).setPositiveButton("Apri impostazioni",(d,w)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).setNegativeButton("Annulla",null).show();}
 private void startOverlay(){if(!accessibilityEnabled()){accessibilityDisclosure();return;}sendBroadcast(new Intent(AmpyGuardService.ARM).setPackage(getPackageName()));notice("Richiesta avvio overlay inviata. Stop disponibile dalla notifica AmpyGuard.");}
 private void stopEffects(){startService(new Intent(this,AmpyGuardService.class).setAction(AmpyGuardService.STOP));sendBroadcast(new Intent(AmpyGuardService.STOP).setPackage(getPackageName()));notice("Effetti fermati");}
 private void notice(String message){Toast.makeText(this,message,Toast.LENGTH_LONG).show();}
}
