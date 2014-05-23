#!/usr/bin/env groovy
/**
 * Created by dhelleberg on 22/02/14.
 */

//get adb exec
adbExec = getAdbPath();

//check connected devices
def adbDevicesCmd = "$adbExec devices"
def proc = adbDevicesCmd.execute()
proc.waitFor()

def foundDevice = false

proc.in.text.eachLine { //start at line 1 and check for a connected device
        line, number ->
            if(number == 1 && line.contains("device"))  //Todo this could fail because it also finds remote devices
                foundDevice = true
}

if(!foundDevice) {
    println("You need to connect your device via usb first!")
    System.exit(-1)
}


def wlanIP = getWlanIP(true)
if(wlanIP == "0.0.0.0") {
    println("could not determine wlan ip. exit")
    System.exit(-1)
}



println "mobile ip on WLAN: $wlanIP"
def adbConnect = "$adbExec tcpip 5555"
adbConnect.execute()


println("now disconnect your phone and press enter")
System.console().readLine()

//now connect the device
def adbConnectWifi = "$adbExec connect $wlanIP"
proc = adbConnectWifi.execute()
proc.waitFor()

def adbDevices = "$adbExec devices"
proc = adbDevices.execute()
proc.waitFor()
proc.in.text.eachLine { println(it) }

private String getWlanIP(boolean tryTurnItOn) {
    //device is there, check if wifi is running
    def adbshellNetcfgCmd = "$adbExec -d shell netcfg"
    proc = adbshellNetcfgCmd.execute()
    proc.waitFor()

    def ip

    proc.in.text.eachMatch(/wlan.*/) {
        if(it.contains("UP")) {
            //try to get ip address
            def matcher = (it =~ /([a-zA-Z|0]+)?([0-9|.]+)/)
            String wlanIP = matcher[1][0]
            println "WLAN IP $wlanIP"
            if(wlanIP == "0.0.0.0" && tryTurnItOn) { //no wifi yet, try to get it up
                turnWifiOn()
                //on more try
                ip = getWlanIP(false)
            }
            else
                ip = wlanIP
        }
    }

    return ip

}

private void turnWifiOn() {
    //try to turn wifi on
    def startWifiManager = "adb shell am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings"
    p = startWifiManager.execute()
    p.waitFor()
    def switchWifiOn = "adb shell input keyevent 20"
    p = switchWifiOn.execute()
    p.waitFor()

    switchWifiOn = "adb shell input keyevent 23"
    p = switchWifiOn.execute()
    p.waitFor()

    //give it a few seconds to get an ip
    sleep(2000)
}

private String getAdbPath() {
    def adbExec = "adb"   //Todo: check if we need adb.exe on windows
    try {
        def command = "$adbExec"    //try it plain from the path
        command.execute()
        return adbExec
    }
    catch (IOException e) {
        //next try with Android Home
        def env = System.getenv("ANDROID_HOME")
        println("adb not in path trying Android home")
        if (env != null && env.length() > 0) {
            //try it here
            try {
                adbExec = env + "/platform-tools/adb"
                def command = "$adbExec"// is actually a string
                command.execute()
                return adbExec
            }
            catch (IOException ex) {
                println("Could not find $adbExec in path and no ANDROID_HOME is set :(")
                System.exit(-1)
            }
        }
    }
}

