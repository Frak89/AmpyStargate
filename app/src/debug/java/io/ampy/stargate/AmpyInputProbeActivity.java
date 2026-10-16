package io.ampy.stargate;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Debug-only ordinary native editor for synthetic CI input; no storage or logging. */
public final class AmpyInputProbeActivity extends Activity {
 @Override public void onCreate(Bundle state){super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
  TextView label=new TextView(this);label.setText("AmpyInputProbe · solo test debug");root.addView(label);
  EditText input=new EditText(this);input.setContentDescription("AmpyInputProbe editor");
  input.setInputType(getIntent().getBooleanExtra("numeric",false)?InputType.TYPE_CLASS_NUMBER:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
  input.setMinLines(2);root.addView(input);setContentView(root);input.requestFocus();
  input.postDelayed(()->getSystemService(InputMethodManager.class).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT),300);
 }
}
