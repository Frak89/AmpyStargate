package io.ampy.stargate;
import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.text.InputType;
import android.content.Intent;
import android.provider.Settings;

/** AmpyKeyboard 0.1.1. Optional basic IME, precise local key feedback, no text history. */
public final class AmpyKeyboardService extends InputMethodService {
 private AmpyTheme theme;private LinearLayout keyboard;private boolean upper,symbols,numeric;private AmpyGuardClient guard;private int navigationBottom;
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private void padding(){keyboard.setPadding(dp(3),dp(8),dp(3),dp(8)+navigationBottom);}
 @Override public View onCreateInputView(){keyboard=new LinearLayout(this);keyboard.setOrientation(LinearLayout.VERTICAL);
  keyboard.setOnApplyWindowInsetsListener((v,insets)->{navigationBottom=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;padding();return insets;});
  refreshTheme();return keyboard;}
 @Override public void onStartInputView(EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);
  int inputClass=info==null?InputType.TYPE_CLASS_TEXT:info.inputType&InputType.TYPE_MASK_CLASS;
  numeric=inputClass==InputType.TYPE_CLASS_NUMBER||inputClass==InputType.TYPE_CLASS_PHONE;
  upper=false;symbols=numeric;if(guard!=null)guard.close();guard=new AmpyGuardClient(this,3);guard.connect(null);if(keyboard!=null)refreshTheme();
 }
 @Override public void onFinishInputView(boolean finishing){if(guard!=null){guard.close();guard=null;}super.onFinishInputView(finishing);}
 private void refreshTheme(){
  // Read the shared atomic config on every show, including a living IME process.
  // Recovery must not parse or render a possibly faulty imported theme.
  if(AmpyGuardService.recoveryReason(this).isEmpty()){if(theme==null)theme=new AmpyTheme(this);else theme.load();}
  build();
 }
 private void build(){keyboard.removeAllViews();padding();
  if(!AmpyGuardService.recoveryReason(this).isEmpty()){
   keyboard.setBackgroundColor(android.graphics.Color.BLACK);keyboard.setContentDescription("AmpyKeyboard recupero");
   TextView label=new TextView(this);label.setText("AmpyKeyboard sospesa dopo recupero");label.setTextColor(android.graphics.Color.WHITE);keyboard.addView(label);
   for(String title:new String[]{"Cambia tastiera","Impostazioni"}){Button key=new Button(this);key.setAllCaps(false);key.setText(title);keyboard.addView(key);key.setOnClickListener(v->press(title));}return;
  }
  if(theme==null)theme=new AmpyTheme(this);
  keyboard.setBackgroundColor(theme.background);keyboard.setContentDescription("AmpyKeyboard · "+theme.name());
  if(symbols){row(chars("1234567890"));row(chars("@#€%&*()-+"));row(chars(".,!?/:;=\"'"));}
  else{row(chars(upper?"QWERTYUIOP":"qwertyuiop"));row(chars(upper?"ASDFGHJKL":"asdfghjkl"));row(chars(upper?"ZXCVBNM":"zxcvbnm"));}
  row(new String[]{"⇧","123 / ABC","⌫"});row(new String[]{"🌐","Spazio","Invio"});
 }
 private String[] chars(String s){return s.codePoints().mapToObj(cp->new String(Character.toChars(cp))).toArray(String[]::new);}
 private void row(String[] labels){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);keyboard.addView(row);
  int height=(int)(getResources().getDisplayMetrics().density*47);
  for(String label:labels){Button key=new Button(this);key.setAllCaps(false);key.setText(label);key.setTextColor(theme.text);key.setTextSize(label.length()>3?12:20);key.setPadding(0,0,0,0);key.setMinWidth(0);key.setMinimumWidth(0);
   AmpySurface surface=new AmpySurface(theme);key.setBackground(surface);key.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN)surface.flash();return false;});
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,height,label.equals("Spazio")?4:1);p.setMargins(1,1,1,1);row.addView(key,p);key.setOnClickListener(v->press(label));
  }
 }
 private void press(String label){
  if(!AmpyGuardService.recoveryReason(this).isEmpty()&&!label.equals("Cambia tastiera")&&!label.equals("Impostazioni")&&!label.equals("🌐")){refreshTheme();return;}
  InputConnection input=getCurrentInputConnection();
  if(label.equals("🌐")||label.equals("Cambia tastiera")){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();return;}
  if(label.equals("Impostazioni")){startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return;}
  if(label.equals("⇧")){upper=!upper;build();return;}if(label.equals("123 / ABC")){symbols=!symbols;build();return;}if(input==null)return;
  if(label.equals("⌫")){input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL));}
  else if(label.equals("Invio")){EditorInfo info=getCurrentInputEditorInfo();int action=info==null?EditorInfo.IME_ACTION_NONE:info.imeOptions&EditorInfo.IME_MASK_ACTION;
   boolean newline=info==null||(info.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION)!=0||action==EditorInfo.IME_ACTION_NONE||action==EditorInfo.IME_ACTION_UNSPECIFIED;
   if(newline)input.commitText("\n",1);else input.performEditorAction(action);
  }else input.commitText(label.equals("Spazio")?" ":label,1);
 }
 @Override public void onDestroy(){if(guard!=null)guard.close();super.onDestroy();}
}
