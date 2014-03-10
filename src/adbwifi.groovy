#!/usr/bin/env groovy
/**
 * Created by dhelleberg on 22/02/14.
 */

//get adb exec
def adbExec = getAdbPath();

//check connected devices
def adbDevicesCmd = "$adbExec devices"
def proc = adbDevicesCmd.execute()

proc.waitFor()

def foundDevice = false

proc.in.text.eachLine {
        line, number ->
            if(number == 1 && line.contains("device"))
                foundDevice = true
}

if(!foundDevice) {
    println("You need to connect your device via usb first!")
    System.exit(-1)
}

//device is there, check if wifi is running
def adbshellNetcfgCmd = "$adbExec -d shell netcfg"
proc = adbshellNetcfgCmd.execute()

proc.waitFor()

def wifiup = true
def wlanIP = ""
proc.in.text.eachMatch(/wlan.*/) {
    if(it.contains("UP")) {
        //try to get ip address
        def matcher = (it =~ /([a-zA-Z|0]+)?([0-9|.]+)/)
        wlanIP = matcher[1][0]
        if(wlanIP == "0.0.0.0")
            wifiup = false
        else
            println "mobile ip on WLAN: $wlanIP"
    }
}

def adbConnect = "$adbExec tcpip 5555"
adbConnect.execute()


println("now disconnect your phone and press enter")
System.console().readLine()

//now connect the device
def adbConnectWifi = "$adbExec connect $wlanIP"
adbConnectWifi.execute()



if(!wifiup) {
    turnWifiOn()
}


//println "return code: ${ proc.exitValue()}"


private void turnWifiOn() {
    //try to turn wifi on
    def startWifiManager = "adb shell am start -a android.intent.action.MAIN -n com.android.settings/.wifi.WifiSettings"
    startWifiManager.execute()
    def switchWifiOn = "adb shell input keyevent 20 & adb shell input keyevent 23"
    switchWifiOn.execute()
    //give it a few seconds to get an ip
    sleep(2000)
}

private String getAdbPath() {
    def adbExec = "adb"
    try {
        def command = "$adbExec"// is actually a string
        command.execute()
        return adbExec
    }
    catch (IOException e) {
        //try with to get Android Home
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
                println("Could not find adb in path and no ANDROID_HOME is set :(")
                System.exit(0)
            }
        }
    }
}


def command = """adbs shell netcfg"""// Create the String
/*proc.waitFor()                               // Wait for the command to finish

// Obtain status and output
println "return code: ${ proc.exitValue()}"
println "stderr: ${proc.err.text}"
println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
//proc.in.text.eachLine {"each:"+println(it)}*/