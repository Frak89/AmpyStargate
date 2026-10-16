package io.ampy.stargate;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.*;

/** AmpySkin 1.0.0. Bounded idle geometry snapshot. Consumes and recycles root. No text access. */
public final class AmpySkinSnapshot {
 private AmpySkinSnapshot(){}
 public static ArrayList<Rect> collect(AccessibilityNodeInfo root,Rect display){
  ArrayList<Rect> result=new ArrayList<>();if(root==null)return result;
  ArrayDeque<AccessibilityNodeInfo> pending=new ArrayDeque<>();pending.add(root);HashSet<String> seen=new HashSet<>();int visited=0;
  try{while(!pending.isEmpty()&&visited++<240&&result.size()<80){AccessibilityNodeInfo node=pending.removeFirst();
   try{if(!node.isVisibleToUser()||node.isPassword()||(Build.VERSION.SDK_INT>=34&&node.isAccessibilityDataSensitive()))continue;
    if(node.isClickable()&&!node.isEditable()){
     Rect r=new Rect();node.getBoundsInScreen(r);if(r.intersect(display)&&r.width()>=12&&r.height()>=12&&(long)r.width()*r.height()<(long)display.width()*display.height()/3&&seen.add(r.flattenToString()))result.add(r);
    }
    for(int i=0;i<Math.min(node.getChildCount(),24)&&pending.size()<240;i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)pending.add(child);}
   }finally{node.recycle();}
  }}catch(RuntimeException e){android.util.Log.w("AmpySkin","UI geometry unavailable: "+e.getClass().getSimpleName());}
  finally{while(!pending.isEmpty())pending.removeFirst().recycle();}return result;
 }
}
