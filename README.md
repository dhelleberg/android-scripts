# devtools

Do you use the developer options on your android device? Like "show overdraw" or "show layout bounds"?
Wouldn't it be handy to switch those tools on and off via command line?
via devtools you can switch:
* gfx (profile gpu rendering)
* layout (show layout bounds)
* overdraw
* updates (show screen updates)
on and off via command line.
Works on emulators and devices. And, yes: it will control multiple devices at once.

Check the video:
 
[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/GOJaOsJ0BJs/0.jpg)](http://www.youtube.com/watch?v=GOJaOsJ0BJs)

## install

Just checkout or download the scrips to your system. 
Make sure you have groovy installed, and adb-executeable in your PATH

On mac with brew just run:

    brew install https://raw.githubusercontent.com/dhelleberg/android-scripts/master/androidscripts.rb

# adbwifi

Do you use adb-wifi connections? tired of figuring out the ip of your phone and typing it in? 
 
This script tries to solve that.


## use

connect your phone via USB (in best case with WiFi switched on) and run the script.

wait until it tells you to disconnect and press enter.

##sample output


    $> src/adbwifi.groovy
    WLAN IP 192.168.178.44
    mobile ip on WLAN: 192.168.178.44
    now disconnect your phone and press enter

    List of devices attached
    192.168.178.44:5555	unauthorized

    $> adb shell
    shell@hammerhead:/ $
