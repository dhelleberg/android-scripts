# adbwifi

Do you use adb-wifi connections? tired of figuring out the ip of your phone and typing it in? 
 
This script tries to solve that.

## install

Just checkout or download adbwifi.groovy to your system. 
Make sure you have groovy installed, and adb-executeable in your PATH

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
