#!/usr/bin/env groovy
/**
 * Created by chrjsorg parts from dhelleberg on 10/04/16.
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
            if(number > 0 && line.contains("device"))
            {
                if(!line.contains("emulator"))
                    foundDevice = true
            }
}

if(!foundDevice) {
    println("You need to connect your device via usb first! Emulators don't support screenrecord")
    System.exit(-1)
}

//Min Sdk Level 19 is required
checkForSdkLevel();

//If filename is provided use it, otherwise use screenrecord.mp4
if(args.length == 0) {
    executeScreenRecord("screenrecord.mp4");
}
else {
    executeScreenRecord(args[0]);
}

private void executeScreenRecord(String fileName) {
    def console = System.console()
    def adbShellScreenrecord = "$adbExec shell screenrecord /sdcard/$fileName"
    def p = adbShellScreenrecord.execute();
    console.readLine("Press enter to finish screenrecording");
    //Kill process
    p.destroy();
    //Take some time so the file can be saved completely, if the file is corrupt after pulling, increase sleep time
    sleep(2000);
    //Download file to pwd
    getFile(fileName);
}

private void getFile(String fileName) {
    def process = new ProcessBuilder().inheritIO().command(adbExec, "pull", "/sdcard/$fileName")
                                    .redirectErrorStream(true).start();
    process.waitFor();    
}

private void checkForSdkLevel() {
    //screenrecord is only available on API level 19 and higher
    def apilevelCmd = "$adbExec -d shell getprop ro.build.version.sdk"
    proc = apilevelCmd.execute()
    proc.waitFor()
    def apilevel
    proc.in.text.eachLine { apilevel = it.toInteger() }    
    if(apilevel < 19) {
        println("Screenrecord is not available below API Level 19")
        System.exit(-1)
    }
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