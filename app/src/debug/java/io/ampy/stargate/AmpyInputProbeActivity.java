package io.ampy.stargate;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Debug-only ordinary native editor for synthetic CI input; no storage or logging. */
public final class AmpyInputProbeActivity extends Activity {
 private EditText input;
 @Override public void onCreate(Bundle state){super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  TextView label=new TextView(this);label.setText("AmpyInputProbe · solo test debug");root.addView(label);
  input=new EditText(this);input.setContentDescription("AmpyInputProbe editor");
  boolean numeric=getIntent().getBooleanExtra("numeric",false),done=getIntent().getBooleanExtra("done",false);
  input.setInputType(numeric?InputType.TYPE_CLASS_NUMBER:InputType.TYPE_CLASS_TEXT|(done?0:InputType.TYPE_TEXT_FLAG_MULTI_LINE)|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
  input.setImeOptions(EditorInfo.IME_ACTION_DONE|(!numeric&&!done?EditorInfo.IME_FLAG_NO_ENTER_ACTION:0));
  input.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){label.setText("AmpyInputProbe action done");return true;}return false;});
  input.setMinLines(2);root.addView(input);setContentView(root);input.requestFocus();
 }
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)input.post(()->getSystemService(InputMethodManager.class).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT));}
}
