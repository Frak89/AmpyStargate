package io.ampy.stargate;

import android.content.Context;
import android.graphics.*;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

/** AmpyTheme 1.0.0. Local JSON palettes and bounded closed cubic SVG silhouettes. */
public final class AmpyTheme {
 public static final String[] NAMES={"AmpyStargate_Classic","AmpyStargate_Cyberpunk","AmpyStargate_Futuristic","AmpyStargate_Fusion"};
 public int background=0xff06121e,surface=0xff0b2936,accent=0xff35f2df,secondary=0xffffc45d,text=0xffe0fbff;
 public String effect="morph";public int fps=120;public boolean ambient=false,idleSkin=true;
 private String svg="M 80 5 C 230 0 730 0 920 5 C 995 8 999 45 990 120 C 999 220 960 235 900 235 C 700 240 230 240 80 235 C 0 230 0 190 10 120 C 0 40 5 10 80 5 Z";
 private Path base;
 private final Context context;private String selected;
 public AmpyTheme(Context c){context=c;selected=c.getSharedPreferences("AmpyStargate",0).getString("theme",NAMES[3]);install();load();}
 public File root(){return new File(context.getExternalFilesDir(null),"AmpyStargate");}
 private File folder(){return new File(new File(root(),"AmpyThemes"),selected);}
 public String name(){return selected;}
 public void select(int index){selected=NAMES[index];context.getSharedPreferences("AmpyStargate",0).edit().putString("theme",selected).apply();load();}
 private void install(){for(String name:NAMES)for(String f:new String[]{"palette.json","AmpyOrganic.svg","AmpyStargate.svg"}){
  File dest=new File(new File(new File(root(),"AmpyThemes"),name),f);if(dest.exists())continue;dest.getParentFile().mkdirs();
  try(InputStream in=context.getAssets().open("AmpyThemes/"+name+"/"+f)){Files.copy(in,dest.toPath());}catch(IOException e){android.util.Log.w("AmpyTheme",e.toString());}
 }}
 public void load(){try{
  File file=new File(folder(),"palette.json");if(file.length()>8192)throw new IOException("Palette exceeds 8 KiB");
  JSONObject o=new JSONObject(new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8));background=Color.parseColor(o.getString("background"));surface=Color.parseColor(o.getString("surface"));accent=Color.parseColor(o.getString("accent"));secondary=Color.parseColor(o.getString("secondary"));text=Color.parseColor(o.getString("text"));
  JSONObject options=new JSONObject(context.getSharedPreferences("AmpyStargate",0).getString("effects","{}"));
  idleSkin=context.getSharedPreferences("AmpyStargate",0).getBoolean("idle_skin",true);effect=options.optString("effect","morph");fps=options.optInt("fps",120)==60?60:120;ambient=options.optBoolean("ambient",false);
  svg=readPath(new File(folder(),"AmpyOrganic.svg"));
 }catch(Exception e){android.util.Log.w("AmpyTheme","Invalid theme; retaining defaults: "+e.getMessage());}base=parse(svg);}
 public void setIdleSkin(boolean on){context.getSharedPreferences("AmpyStargate",0).edit().putBoolean("idle_skin",on).apply();load();}
 public void saveEffects(String name,boolean idle,int hz){try{JSONObject o=new JSONObject();o.put("effect",name);o.put("ambient",idle);o.put("fps",hz);context.getSharedPreferences("AmpyStargate",0).edit().putString("effects",o.toString()).apply();load();}catch(Exception e){android.util.Log.e("AmpyTheme",e.toString());}}
 public void importFile(String file,InputStream in)throws IOException{
  if(!file.equals("palette.json")&&!file.equals("AmpyOrganic.svg"))throw new IOException("Unsupported theme file");
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1){if(bytes.size()+count>65536)throw new IOException("File exceeds 64 KiB");bytes.write(buffer,0,count);}byte[] data=bytes.toByteArray();if(file.equals("palette.json")&&data.length>8192)throw new IOException("Palette exceeds 8 KiB");
  File temp=new File(folder(),file+".tmp");Files.write(temp.toPath(),data);
  try{if(file.endsWith("svg")){String path=readPath(temp);parse(path);}else{JSONObject o=new JSONObject(new String(data,StandardCharsets.UTF_8));for(String k:new String[]{"background","surface","accent","secondary","text"})Color.parseColor(o.getString(k));}}
  catch(Exception e){temp.delete();throw new IOException("Invalid theme: "+e.getMessage(),e);}
  Files.move(temp.toPath(),new File(folder(),file).toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);load();
 }
 private static String readPath(File f)throws IOException{
  if(f.length()>65536)throw new IOException("SVG exceeds 64 KiB");String xml=new String(Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8);
  if(xml.contains("<!")||xml.contains("<script")||xml.contains("href="))throw new IOException("External or active SVG content is unsupported");
  Matcher m=Pattern.compile("<path\\b[^>]*\\bd=\"([^\"]+)\"").matcher(xml);if(!m.find())throw new IOException("Missing cubic path");String p=m.group(1);parse(p);return p;
 }
 public Path shape(float width,float height,float phase,float strength){
  Path path=new Path(base);Matrix m=new Matrix();m.setScale(width/1000f,height/240f);path.transform(m);
  if(strength!=0){Matrix wave=new Matrix();wave.setSkew((float)Math.sin(phase)*strength*.025f,0,width*.5f,height*.5f);path.transform(wave);}return path;
 }
 private static Path parse(String data){
  Matcher m=Pattern.compile("[MCZ]|[-+]?(?:[0-9]*\\.)?[0-9]+(?:[eE][-+]?[0-9]+)?").matcher(data);ArrayList<String> tokens=new ArrayList<>();int end=0;
  while(m.find()){if(!data.substring(end,m.start()).matches("[\\s,]*"))throw new IllegalArgumentException("Only absolute M/C/Z supported");tokens.add(m.group());end=m.end();}
  if(!data.substring(end).matches("[\\s,]*")||tokens.size()>2048)throw new IllegalArgumentException("Invalid SVG path");
  Path p=new Path();int i=0;boolean started=false,closed=false;
  while(i<tokens.size()){String cmd=tokens.get(i++);if(closed)throw new IllegalArgumentException("One closed contour only");
   if(cmd.equals("M")&&!started){p.moveTo(number(tokens,i++),number(tokens,i++));started=true;}
   else if(cmd.equals("C")&&started){p.cubicTo(number(tokens,i++),number(tokens,i++),number(tokens,i++),number(tokens,i++),number(tokens,i++),number(tokens,i++));}
   else if(cmd.equals("Z")&&started){p.close();closed=true;}else throw new IllegalArgumentException("Invalid M/C/Z sequence");
  }if(!closed)throw new IllegalArgumentException("Unclosed contour");return p;
 }
 private static float number(List<String> t,int i){float v=Float.parseFloat(t.get(i));if(!Float.isFinite(v)||Math.abs(v)>5000)throw new IllegalArgumentException("Coordinate out of range");return v;}
}
