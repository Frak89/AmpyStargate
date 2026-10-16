package io.ampy.stargate;
import android.inputmethodservice.InputMethodService;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.text.InputType;
import android.content.Intent;
import android.provider.Settings;

/** AmpyKeyboard 0.1.0. Optional basic IME, precise local key feedback, no text history. */
public final class AmpyKeyboardService extends InputMethodService {
 private AmpyTheme theme;private LinearLayout keyboard;private boolean upper,symbols,numeric;private AmpyGuardClient guard;
 @Override public View onCreateInputView(){theme=new AmpyTheme(this);keyboard=new LinearLayout(this);keyboard.setOrientation(1);build();return keyboard;}
 @Override public void onStartInputView(EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);
  numeric=(info.inputType&InputType.TYPE_MASK_CLASS)==InputType.TYPE_CLASS_NUMBER||(info.inputType&InputType.TYPE_MASK_CLASS)==InputType.TYPE_CLASS_PHONE;
  upper=false;symbols=numeric;if(guard!=null)guard.close();guard=new AmpyGuardClient(this,3);guard.connect(null);if(keyboard!=null)build();
 }
 @Override public void onFinishInputView(boolean finishing){if(guard!=null){guard.close();guard=null;}super.onFinishInputView(finishing);}
 private void build(){keyboard.removeAllViews();keyboard.setBackgroundColor(theme.background);keyboard.setPadding(3,8,3,8);
  if(!AmpyGuardService.recoveryReason(this).isEmpty()){
   TextView label=new TextView(this);label.setText("AmpyKeyboard sospesa dopo recupero");label.setTextColor(theme.text);keyboard.addView(label);
   row(new String[]{"Cambia tastiera","Impostazioni"});return;
  }
  if(symbols){row(chars("1234567890"));row(chars("@#€%&*()-+"));row(chars(".,!?/:;=\"'"));}
  else{row(chars(upper?"QWERTYUIOP":"qwertyuiop"));row(chars(upper?"ASDFGHJKL":"asdfghjkl"));row(chars(upper?"ZXCVBNM":"zxcvbnm"));}
  row(new String[]{"⇧","123 / ABC","⌫"});row(new String[]{"🌐","Spazio","Invio"});
 }
 private String[] chars(String s){return s.codePoints().mapToObj(cp->new String(Character.toChars(cp))).toArray(String[]::new);}
 private void row(String[] labels){LinearLayout row=new LinearLayout(this);row.setOrientation(0);keyboard.addView(row);
  int height=(int)(getResources().getDisplayMetrics().density*47);
  for(String label:labels){Button key=new Button(this);key.setAllCaps(false);key.setText(label);key.setTextColor(theme.text);key.setTextSize(label.length()>3?12:20);key.setPadding(0,0,0,0);key.setMinWidth(0);key.setMinimumWidth(0);
   AmpySurface surface=new AmpySurface(theme);key.setBackground(surface);key.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN)surface.flash();return false;});
   LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,height,label.equals("Spazio")?4:1);p.setMargins(1,1,1,1);row.addView(key,p);key.setOnClickListener(v->press(label));
  }
 }
 private void press(String label){
  InputConnection input=getCurrentInputConnection();
  if(label.equals("🌐")||label.equals("Cambia tastiera")){((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();return;}
  if(label.equals("Impostazioni")){startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return;}
  if(label.equals("⇧")){upper=!upper;build();return;}if(label.equals("123 / ABC")){symbols=!symbols;build();return;}if(input==null)return;
  if(label.equals("⌫")){input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));input.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL));}
  else if(label.equals("Invio")){EditorInfo info=getCurrentInputEditorInfo();int action=info==null?EditorInfo.IME_ACTION_NONE:info.imeOptions&EditorInfo.IME_MASK_ACTION;
   if(action!=EditorInfo.IME_ACTION_NONE&&action!=EditorInfo.IME_ACTION_UNSPECIFIED)input.performEditorAction(action);else input.commitText("\n",1);
  }else input.commitText(label.equals("Spazio")?" ":label,1);
 }
 @Override public void onDestroy(){if(guard!=null)guard.close();super.onDestroy();}
}
