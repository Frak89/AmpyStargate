"""Disposable-emulator proof: native touches through overlay, IME and own-process recovery.
Never run these provisioning commands against a personal phone.
"""
import json,os,re,subprocess,time
from pathlib import Path
import xml.etree.ElementTree as ET
sdk=Path(os.environ['ANDROID_HOME']);serial='emulator-5582';adb=str(sdk/'platform-tools/adb')
out=Path('AmpyRuntimeEvidence');out.mkdir(exist_ok=True)
package='io.ampy.stargate'
def call(*args,timeout=30):
 p=subprocess.run([adb,'-s',serial,*args],capture_output=True,text=True,timeout=timeout)
 if p.returncode:raise RuntimeError(p.stdout+p.stderr)
 return p.stdout
def state(name):return json.loads(call('shell','run-as',package,'cat','files/'+name))
def wait_for(fn,seconds=30):
 end=time.monotonic()+seconds;last=None
 while time.monotonic()<end:
  try:
   last=fn()
   if last:return last
  except (RuntimeError,ValueError,KeyError):pass
  time.sleep(.5)
 raise AssertionError('Condition timed out: '+repr(last))
def hierarchy():
 call('shell','rm','-f','/sdcard/ampy-ui.xml');call('shell','uiautomator','dump','--windows','/sdcard/ampy-ui.xml')
 xml=call('shell','cat','/sdcard/ampy-ui.xml');(out/'hierarchy.xml').write_text(xml);return ET.fromstring(xml)
def tap(label,geometry=None,exact=False):
 # Shell uiautomator suppresses accessibility services. Read geometry once before
 # enabling the service; never invoke UI automation while testing the overlay.
 for node in (initial_geometry if geometry is None else geometry).iter('node'):
  match=node.get('text','')==label if exact else label.casefold() in node.get('text','').casefold()
  if match and node.get('clickable')=='true':
   b=list(map(int,re.findall(r'\d+',node.get('bounds',''))))
   if len(b)==4 and b[2]>b[0] and b[3]>b[1]:call('shell','input','tap',str((b[0]+b[2])//2),str((b[1]+b[3])//2));time.sleep(1);return
 raise AssertionError('Missing clickable node '+label)
def screenshot(name):
 with (out/name).open('wb') as f:subprocess.run([adb,'-s',serial,'exec-out','screencap','-p'],stdout=f,check=True)
log=(out/'emulator.log').open('w')
proc=subprocess.Popen([str(sdk/'emulator/emulator'),'-avd','ampy-stargate','-port','5582','-no-window','-no-audio','-no-snapshot','-no-boot-anim','-gpu','swiftshader','-memory','4096','-partition-size','2048','-cores','2'],stdout=log,stderr=log)
try:
 wait_for(lambda:call('shell','getprop','sys.boot_completed',timeout=10).strip()=='1',240)
 assert call('shell','getprop','ro.kernel.qemu').strip()=='1','Refusing non-emulator provisioning'
 call('root');call('wait-for-device');call('shell','wm','size','540x1000');call('shell','wm','density','160')
 def install():
  result=call('install','--no-streaming','-r','apk/app-debug.apk',timeout=90)
  return 'Success' in result
 wait_for(install,90);call('shell','pm','grant',package,'android.permission.POST_NOTIFICATIONS')
 call('shell','am','start','-W','-n',package+'/.AmpyLauncherActivity');time.sleep(2)
 # Complete actual key entry BEFORE enabling accessibility: shell UI dumps suppress it.
 call('shell','ime','enable',package+'/.AmpyKeyboardService');call('shell','ime','set',package+'/.AmpyKeyboardService')
 call('shell','settings','put','secure','show_ime_with_hard_keyboard','1')
 def probe(numeric=False,done=False):
  call('shell','am','start','-W','-f','0x10008000','-n',package+'/.AmpyInputProbeActivity','--ez','numeric',str(numeric).lower(),'--ez','done',str(done).lower());time.sleep(2)
  def visible():
   tree=hierarchy()
   return tree if any(n.get('content-desc','').startswith('AmpyKeyboard') for n in tree.iter('node')) else None
  return wait_for(visible,45)
 def typed():
  geometry=hierarchy()
  for node in geometry.iter('node'):
   if node.get('content-desc')=='AmpyInputProbe editor':return node.get('text','')
  raise AssertionError('Missing native input probe')
 def key(label):tap(label,hierarchy(),exact=True)
 def write_private(name,value):
  p=subprocess.run([adb,'-s',serial,'shell','run-as',package,'tee','files/'+name],input=json.dumps(value),capture_output=True,text=True,timeout=20)
  assert p.returncode==0,p.stderr
 def has_theme(geometry,name):return any(node.get('content-desc')=='AmpyKeyboard · '+name for node in geometry.iter('node'))
 keyboard_geometry=probe()
 assert has_theme(keyboard_geometry,'AmpyStargate_Fusion')
 key('q');key('⇧');key('W');key('Spazio');key('123 / ABC');key('7');key('⌫')
 assert typed()=='qW ','Text/shift/space/symbol/delete input failed'
 key('Invio');assert typed()=='qW \n','Multiline enter ignored IME_FLAG_NO_ENTER_ACTION'
 screenshot('keyboard-text.png')
 probe(True);key('4');key('2');assert typed()=='42','Numeric editor failed'
 probe(done=True);key('q');key('Invio');assert typed()=='q','Done action inserted unwanted newline'
 assert any(n.get('text')=='AmpyInputProbe action done' for n in hierarchy().iter('node')),'Done action not delivered to editor'
 ime_pid=call('shell','pidof',package+':ampy_keyboard').strip()
 # Hide the IME, change the shared theme from a separate process, then reopen it.
 call('shell','input','keyevent','BACK');write_private('AmpyThemeConfig.json',{'theme':'AmpyStargate_Classic'})
 assert has_theme(probe(),'AmpyStargate_Classic'),'Living keyboard did not reload theme'
 assert call('shell','pidof',package+':ampy_keyboard').strip()==ime_pid,'Theme check restarted IME instead of reloading'
 # Synthetic safe state exercises native escape buttons without custom SVG rendering.
 call('shell','input','keyevent','BACK');write_private('AmpyGuard.json',{'safe_mode':True,'reason':'CI synthetic keyboard recovery','timestamp':0})
 safe=probe();assert any(n.get('text')=='AmpyKeyboard sospesa dopo recupero' for n in safe.iter('node'))
 settings_component=call('shell','cmd','package','resolve-activity','--brief','-a','android.settings.INPUT_METHOD_SETTINGS').strip().splitlines()[-1]
 assert '/' in settings_component,'Missing native keyboard settings activity'
 tap('Impostazioni',safe,exact=True)
 wait_for(lambda:settings_component in call('shell','dumpsys','activity','top'),20)
 call('shell','input','keyevent','BACK');write_private('AmpyGuard.json',{'safe_mode':False,'reason':'','timestamp':0})
 write_private('AmpyThemeConfig.json',{'theme':'AmpyStargate_Fusion'})
 call('shell','am','start','-W','-f','0x30000000','-n',package+'/.AmpyLauncherActivity');time.sleep(1)
 initial_geometry=hierarchy()
 # Provision only this test emulator. The distributed APK never grants itself permissions.
 call('shell','settings','put','secure','enabled_accessibility_services',package+'/.AmpyOverlayService')
 call('shell','settings','put','secure','accessibility_enabled','1')
 wait_for(lambda:state('AmpyOverlay.json')['active'] is False,30)
 tap('Avvia overlay');wait_for(lambda:state('AmpyOverlay.json')['active'])
 for _ in range(3):
  tap('Test tocco nativo');assert state('AmpyOverlay.json')['active'],'Test instrumentation disconnected overlay'
 assert int(call('shell','run-as',package,'cat','files/AmpyNativeClicks.txt'))==3,'Overlay blocked native touches'
 wait_for(lambda:state('AmpyOverlay.json')['reactions']>=1)
 screenshot('launcher-overlay.png')
 pid=state('AmpyOverlay.json')['pid'];call('shell','kill','-STOP',str(pid))
 wait_for(lambda:state('AmpyGuard.json').get('safe_mode'),15)
 wait_for(lambda:str(pid) not in [line.split()[0] for line in call('shell','ps','-A','-o','PID,NAME').splitlines()[1:] if line.strip()],30)
 # Kill at most our stopped overlay process; underlying launcher must remain usable.
 tap('Test tocco nativo')
 assert int(call('shell','run-as',package,'cat','files/AmpyNativeClicks.txt'))>=4
 screenshot('recovered.png')
 # Recovery of the overlay must not disarm monitoring of the healthy launcher.
 main_pid=int(call('shell','pidof',package).strip());first_recovery=state('AmpyGuard.json')['timestamp']
 call('shell','kill','-STOP',str(main_pid))
 wait_for(lambda:state('AmpyGuard.json')['timestamp']>first_recovery,15)
 wait_for(lambda:str(main_pid) not in [line.split()[0] for line in call('shell','ps','-A','-o','PID,NAME').splitlines()[1:] if line.strip()],30)
 report={'android_api':call('shell','getprop','ro.build.version.sdk').strip(),'native_touches_pass_through':True,'accessible_click_reactions':True,'optional_ime_registered':True,'ime_text_shift_space_symbol_delete_newline':True,'ime_numeric_entry':True,'ime_theme_reload_same_process':True,'ime_safe_settings_escape':True,'stalled_own_overlay_terminated':True,'safe_mode_recorded':True,'launcher_remains_usable':True,'second_launcher_stall_recovered':True,'physical_pixel_and_external_keyboard_timing':'not tested'}
 (out/'result.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
finally:
 try:
  (out/'logcat.txt').write_text(call('logcat','-d'));screenshot('last-screen.png')
  for name in ('AmpyGuard.json','AmpyOverlay.json'):
   try:(out/name).write_text(call('shell','run-as',package,'cat','files/'+name))
   except RuntimeError:pass
 except Exception:pass
 proc.terminate();proc.wait(timeout=30)
