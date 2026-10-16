package io.ampy.stargate;

/** AmpyMath 1.0.0: geometry, easing and watchdog rules without Android dependencies. */
public final class AmpyMath {
 private AmpyMath(){}
 public static float clamp(float x,float lo,float hi){return Math.max(lo,Math.min(hi,x));}
 public static float ease(float x){x=clamp(x,0,1);return 1-(1-x)*(1-x)*(1-x);}
 public static boolean stale(long now,long last,long timeout){return now>=last&&now-last>timeout;}
 public static float[] clip(float l,float t,float r,float b,float width,float height){
  l=clamp(l,0,width);r=clamp(r,0,width);t=clamp(t,0,height);b=clamp(b,0,height);
  return r>l&&b>t?new float[]{l,t,r,b}:null;
 }
}
