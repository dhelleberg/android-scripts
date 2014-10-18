#!/usr/bin/env groovy
/**
 * Created by dhelleberg on 24/09/14.
 * Improve command line parsing
 */


gfx_command_map = ['on' : 'visual_bars', 'off' : 'false', 'lines' : 'visual_lines']
layout_command_map = ['on' : 'true', 'off' : 'false']
overdraw_command_map = ['on' : 'show',  'off' : 'false', 'deut' : 'show_deuteranomaly']


command_map = ['gfx' : gfx_command_map,
               'layout' : layout_command_map,
                'overdraw' : overdraw_command_map]

verbose = true


//check args
if(args.length != 2) {
    printHelp()
    System.exit(-1)
}

//get args
String command = args[0]
String option = args[1]

//get adb exec
adbExec = getAdbPath();

//check connected devices
def adbDevicesCmd = "$adbExec devices"
def proc = adbDevicesCmd.execute()
proc.waitFor()

def foundDevice = false
deviceIds = []

proc.in.text.eachLine { //start at line 1 and check for a connected device
        line, number ->
            if(number > 0 && line.contains("device")) {
                foundDevice = true
                //grep out device ids
                def values = line.split('\\t')
                if(verbose)
                    println("found id: "+values[0])
                deviceIds.add(values[0])
            }
}

if(!foundDevice) {
    println("No usb devices")
    System.exit(-1)
}


def adbcmd = ""
switch ( command ) {
    case "gfx" :
        adbcmd = "shell setprop debug.hwui.profile "+gfx_command_map[option]
        break
    case "layout" :
        adbcmd = "shell setprop debug.layout "+layout_command_map[option]
        break
    case "overdraw" :
        adbcmd = "shell setprop debug.hwui.overdraw "+overdraw_command_map[option]
        break
    default:
        printHelp()
        System.exit(-1)

}

executeADBCommand(adbcmd)

kickSystemService(adbExec)

System.exit(0)






void kickSystemService(String adbExec) {
    def proc
    int SYSPROPS_TRANSACTION = 1599295570 // ('_'<<24)|('S'<<16)|('P'<<8)|'R'

    def pingService = "shell service call activity $SYSPROPS_TRANSACTION"
    executeADBCommand(pingService)
}

void executeADBCommand(String adbcmd) {
    deviceIds.each { deviceId ->
        def proc
        def adbConnect = "$adbExec -s $deviceId $adbcmd"
        if(verbose)
            println("Executing $adbConnect")
        proc = adbConnect.execute()
        proc.waitFor()
    }
}

String getAdbPath() {
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

void printHelp() {
    println("usage: devtools command option")
    print("command: ")
    command_map.each { command, options ->
        print("\n  $command -> ")
        options.each {
            option, internal_cmd -> print("$option ")
        }
    }
    println()
    println()
}