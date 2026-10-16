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
 call('shell','rm','-f','/sdcard/ampy-ui.xml');call('shell','uiautomator','dump','/sdcard/ampy-ui.xml')
 xml=call('shell','cat','/sdcard/ampy-ui.xml');(out/'hierarchy.xml').write_text(xml);return ET.fromstring(xml)
def tap(label):
 for node in hierarchy().iter('node'):
  if label.casefold() in node.get('text','').casefold() and node.get('clickable')=='true':
   b=list(map(int,re.findall(r'\d+',node.get('bounds',''))))
   if len(b)==4 and b[2]>b[0] and b[3]>b[1]:call('shell','input','tap',str((b[0]+b[2])//2),str((b[1]+b[3])//2));time.sleep(1);return
 raise AssertionError('Missing clickable node '+label)
def screenshot(name):
 with (out/name).open('wb') as f:subprocess.run([adb,'-s',serial,'exec-out','screencap','-p'],stdout=f,check=True)
log=(out/'emulator.log').open('w')
proc=subprocess.Popen([str(sdk/'emulator/emulator'),'-avd','ampy-stargate','-port','5582','-no-window','-no-audio','-no-snapshot','-no-boot-anim','-gpu','swiftshader','-memory','4096','-partition-size','2048','-cores','2'],stdout=log,stderr=log)
try:
 wait_for(lambda:call('shell','getprop','sys.boot_completed',timeout=10).strip()=='1',240)
 call('root');call('wait-for-device');call('shell','wm','size','540x1000');call('shell','wm','density','160')
 def install():
  result=call('install','--no-streaming','-r','apk/app-debug.apk',timeout=90)
  return 'Success' in result
 wait_for(install,90);call('shell','pm','grant',package,'android.permission.POST_NOTIFICATIONS')
 call('shell','am','start','-W','-n',package+'/.AmpyLauncherActivity');time.sleep(2)
 # Provision only this test emulator. The distributed APK never grants itself permissions.
 call('shell','settings','put','secure','enabled_accessibility_services',package+'/.AmpyOverlayService')
 call('shell','settings','put','secure','accessibility_enabled','1')
 wait_for(lambda:state('AmpyOverlay.json')['active'] is False,30)
 tap('Avvia overlay');wait_for(lambda:state('AmpyOverlay.json')['active'])
 for _ in range(3):tap('Test tocco nativo')
 assert int(call('shell','run-as',package,'cat','files/AmpyNativeClicks.txt'))==3,'Overlay blocked native touches'
 wait_for(lambda:state('AmpyOverlay.json')['reactions']>=1)
 screenshot('launcher-overlay.png')
 call('shell','ime','enable',package+'/.AmpyKeyboardService');call('shell','ime','set',package+'/.AmpyKeyboardService')
 # Open an ordinary native text entry surface in system Settings, not a fake text field.
 call('shell','am','start','-W','-a','android.settings.SETTINGS');time.sleep(1)
 # Confirm the IME is installed and selected; key-entry behavior still needs device testing.
 assert 'AmpyKeyboardService' in call('shell','settings','get','secure','default_input_method')
 call('shell','am','start','-W','-n',package+'/.AmpyLauncherActivity');time.sleep(1)
 pid=state('AmpyOverlay.json')['pid'];call('shell','kill','-STOP',str(pid))
 wait_for(lambda:state('AmpyGuard.json').get('safe_mode'),15)
 wait_for(lambda:str(pid) not in [line.split()[0] for line in call('shell','ps','-A','-o','PID,NAME').splitlines()[1:] if line.strip()],30)
 # Kill at most our stopped overlay process; underlying launcher must remain usable.
 tap('Test tocco nativo')
 assert int(call('shell','run-as',package,'cat','files/AmpyNativeClicks.txt'))>=4
 screenshot('recovered.png')
 report={'android_api':call('shell','getprop','ro.build.version.sdk').strip(),'native_touches_pass_through':True,'accessible_click_reactions':True,'optional_ime_registered':True,'stalled_own_overlay_terminated':True,'safe_mode_recorded':True,'launcher_remains_usable':True,'physical_pixel_and_external_keyboard_timing':'not tested'}
 (out/'result.json').write_text(json.dumps(report,indent=2));print(json.dumps(report))
finally:
 try:(out/'logcat.txt').write_text(call('logcat','-d'));screenshot('last-screen.png')
 except Exception:pass
 proc.terminate();proc.wait(timeout=30)
