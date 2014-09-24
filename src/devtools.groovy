#!/usr/bin/env groovy
/**
 * Created by dhelleberg on 24/09/14.
 */

//get args
String command = args[0]
String option = args[1]

def gfx_command_map = ['bars' : 'visual_bars', 'off' : 'false', 'lines' : 'visual_lines']

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
    println("No usb devices")
    System.exit(-1)
}

int SYSPROPS_TRANSACTION = 1599295570 // ('_'<<24)|('S'<<16)|('P'<<8)|'R'

def adbcmd = ""
switch ( command ) {
    case "gfx" :
        adbcmd = "shell setprop debug.hwui.profile "+gfx_command_map[option]
}

def adbConnect = "$adbExec $adbcmd"
println(adbConnect)
proc = adbConnect.execute()
proc.waitFor()

def pingService = "$adbExec shell service call activity $SYSPROPS_TRANSACTION"
proc = pingService.execute()
proc.waitFor()


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

