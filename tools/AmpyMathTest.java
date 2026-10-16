import io.ampy.stargate.AmpyMath;
public final class AmpyMathTest {
 static void require(boolean condition){if(!condition)throw new AssertionError();}
 public static void main(String[] args){
  float[] r=AmpyMath.clip(-50,20,300,1000,200,400);require(r[0]==0&&r[2]==200&&r[3]==400);
  require(AmpyMath.clip(250,0,300,10,200,400)==null);
  require(AmpyMath.clip(20,0,10,10,200,400)==null);
  require(!AmpyMath.stale(100,200,10));require(!AmpyMath.stale(1000,0,6000));require(AmpyMath.stale(7001,1000,6000));
  require(AmpyMath.ease(-1)==0&&AmpyMath.ease(2)==1);require(AmpyMath.ease(.5f)>.5f);
  System.out.println("PASS: clipped overlay geometry, monotonic watchdog timeout and easing bounds");
 }
}
