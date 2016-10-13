#!/usr/bin/env groovy
@Grab('joda-time:joda-time:2.9.4')
import org.joda.time.DateTime
import org.joda.time.format.*

/**
 * Created by dhelleberg on 24/09/14.
 * Improve command line parsing
 */

gfx_command_map = ['on': 'visual_bars', 'off': 'false', 'lines': 'visual_lines']
layout_command_map = ['on': 'true', 'off': 'false']
overdraw_command_map = ['on': 'show', 'off': 'false', 'deut': 'show_deuteranomaly']
overdraw_command_map_preKitKat = ['on': 'true', 'off': 'false']
show_updates_map = ['on': '0', 'off': '1']
date_single_option_possibilites = ['reset']
date_format_supported = ['d', 'h', 'm', 's']
date_opration_supported = ['+', '-']
devtools_options = ['-d', '-e', '-s']

command_map = ['gfx'     : gfx_command_map,
               'layout'  : layout_command_map,
               'overdraw': overdraw_command_map,
               'updates' : show_updates_map]

verbose = false
def serialNumber

/**
 * Devtools options
 */

def cli = new CliBuilder(usage: 'devtools.groovy command option')
cli.with {
    v longOpt: 'verbose', 'prints additional output'
}
def opts = cli.parse(args)

if (!opts) {
    printDevtoolsOptionsUsageHelp("Not provided correct option")
}

if (opts.v) {
    verbose = true
}

//get adb exec
adbExec = getAdbPath()

//check connected devices
def adbDevicesCmd = "$adbExec devices"
def proc = adbDevicesCmd.execute()
proc.waitFor()

def foundDevice = false
deviceIds = []

proc.in.text.eachLine {
        //start at line 1 and check for a connected device
    line, number ->
        if (number > 0 && line.contains("device")) {
            foundDevice = true
            //grep out device ids
            def values = line.split('\\t')
            if (verbose)
                println("found id: " + values[0])
            deviceIds.add(values[0])
        }
}

if (!foundDevice) {
    println("No usb devices")
    System.exit(-1)
}

/**
 * Command & Command options
 */
//get args
String command = opts.arguments().get(0)
String option
options = new String[opts.arguments().size() - 1]

switch (command) {
    case "gfx":
    case "layout":
    case "overdraw":
    case "updates":
        if (opts.arguments().size() != 2) {
            printHelpForSpecificCommand(command, false, null)
        }
        option = opts.arguments().get(1)
        break

    case "date":
        for (int i = 0; i < options.length; i++) {
            options[i] = opts.arguments().get(i + 1)
        }

        if (options.size() == 0)
            printHelpForSpecificCommand(command, false, null)

        if (options.size() == 1) {
            if (!isAValidDateSingleOption(options[0]) && !isAValidDateOption(options[0])) {
                printHelpForSpecificCommand(command, false, null)
            }
        }
}

def adbCmd = ""
switch (command) {
    case "gfx":
        adbCmd = "shell setprop debug.hwui.profile " + gfx_command_map[option]
        executeADBCommand(adbCmd)
        break

    case "layout":
        adbCmd = "shell setprop debug.layout " + layout_command_map[option]
        executeADBCommand(adbCmd)
        break

    case "overdraw":
        //tricky, properties have changed over time
        adbCmd = "shell setprop debug.hwui.overdraw " + overdraw_command_map[option]
        executeADBCommand(adbCmd)
        adbCmd = "shell setprop debug.hwui.show_overdraw " + overdraw_command_map_preKitKat[option]
        executeADBCommand(adbCmd)
        break

    case "updates":
        adbCmd = "shell service call SurfaceFlinger 1002 android.ui.ISurfaceComposer" + show_updates_map[option]
        executeADBCommand(adbCmd)
        break

    case "date":
        adbCmd = buildDateCommand()
        executeADBCommand(adbCmd)
        break

    default:
        printHelpForSpecificCommand(command, false, null)

}

kickSystemService()
System.exit(0)

/* CMD METHODS */

String fixFormat(String val) {
    if (val.length() == 1)
        return "0" + val
    return val
}

String buildResetCommand() {
    Calendar calendar = Calendar.getInstance()
    //println("Setting device date to : " + DateGroovyMethods.format(calendar.getTime(), "dd/MMM/yyyy HH:mm:ss"))

    String monthOfYear = fixFormat(String.valueOf((calendar.get(Calendar.MONTH) + 1)))
    String dayOfMonth = fixFormat(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)))
    String minutesOfHour = fixFormat(String.valueOf(calendar.get(Calendar.MINUTE)))
    String secondsOfMinutes = fixFormat(String.valueOf(calendar.get(Calendar.SECOND)))

    String adbCmd
    if (isNOrLater()) {
        adbCmd = "shell date " +
                monthOfYear +
                dayOfMonth +
                calendar.get(Calendar.HOUR_OF_DAY) +
                minutesOfHour +
                calendar.get(Calendar.YEAR) +
                "." +
                secondsOfMinutes

    } else {
        adbCmd = "shell date -s " +
                calendar.get(Calendar.YEAR) +
                monthOfYear +
                dayOfMonth +
                "." +
                calendar.get(Calendar.HOUR_OF_DAY) +
                minutesOfHour +
                secondsOfMinutes
    }
    return adbCmd
}

String buildDateCommand() {
    if (options.size() == 1 && isAValidDateSingleOption(options[0])) {
        buildResetCommand()

    } else {
        DateTime deviceDateTime = getDeviceDateTime()
        String resultMessage = "Date changed from " + deviceDateTime + " to "

        options.each { option ->
            if (option.length() > 4 || option.length() < 3) {
                printHelpForSpecificCommand("date", true, option)
            }

            def operation = option.take(1)
            def rangeType = option.reverse().take(1).reverse()

            if (!(operation in date_opration_supported)) {
                printHelpForSpecificCommand("date", true, option)
            }

            if (!(rangeType in date_format_supported)) {
                printHelpForSpecificCommand("date", true, option)
            }

            def range = option.substring(1, option.length() - 1)
            if (!range.isNumber()) {
                printHelpForSpecificCommand("date", true, option)
            }

            deviceDateTime = applyRangeToDate(deviceDateTime, operation, Integer.valueOf(range), rangeType)
        }

        resultMessage += deviceDateTime
        println(resultMessage)

        String formattedDate = formatDateForAdbCommand(deviceDateTime)

        if (isNOrLater()) {
            adbCmd = "shell date " + formattedDate

        } else {
            adbCmd = "shell date -s " + formattedDate
        }
        println(adbCmd)

        return adbCmd

    }
}

private boolean isAValidDateOption(String option) {
    def operation = option.take(1)
    def rangeType = option.reverse().take(1).reverse()

    if (!(operation in date_opration_supported)) {
        return false
    }

    if (!(rangeType in date_format_supported)) {
        return false
    }

    def range = option.substring(1, option.length() - 1)
    if (!range.isNumber()) {
        return false
    }

    return true
}

private boolean isAValidDateSingleOption(String option) {
    if (option in date_single_option_possibilites)
        return true

    return false
}

private DateTime applyRangeToDate(DateTime dateTime, def operation, int range, def rangeType) {
    if (operation.equals("+")) {
        return addRange(dateTime, rangeType, range)
    } else {
        return minusRange(dateTime, rangeType, range)
    }
}

private DateTime addRange(DateTime fromDate, def rangeType, int range) {
    switch (rangeType) {
        case "d":
            return fromDate.plusDays(range)

        case "h":
            return fromDate.plusHours(range)

        case "m":
            return fromDate.plusMinutes(range)

        case "s":
            return fromDate.plusSeconds(range)
    }
}

private DateTime minusRange(DateTime fromDate, def rangeType, int range) {
    switch (rangeType) {
        case "d":
            return fromDate.minusDays(range)
            break

        case "h":
            return fromDate.minusHours(range)
            break

        case "m":
            return fromDate.minusMinutes(range)
            break

        case "s":
            return fromDate.minusSeconds(range)
    }
}

private String getDeviceDate() {
    if (isNOrLater()) {
        adbCmd = "shell date +%Y%m%d.%H%M%S"
    } else {
        adbCmd = "shell date +%Y%m%d.%H%M%S"
    }
    return executeADBCommand(adbCmd)
}

private DateTime getDeviceDateTime() {
    deviceDate = getDeviceDate()
    if (verbose)
        println("Device current Date: " + deviceDate)

    int year = Integer.valueOf(deviceDate.take(4))
    int month = Integer.valueOf(deviceDate[4..5])
    int day = Integer.valueOf(deviceDate[6..7])
    int hours = Integer.valueOf(deviceDate[9..10])
    int minutes = Integer.valueOf(deviceDate[11..12])
    int seconds = Integer.valueOf(deviceDate[13..14])

    return new DateTime(year, month, day, hours, minutes, seconds)
}

private String formatDateForAdbCommand(DateTime dateTime) {
    def dateFormat
    if (isNOrLater()) {
        dateFormat = "MMddHHmmYYYY.ss"
    } else {
        dateFormat = "YYYYMMd.HHmmss"
    }

    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat)
    return dateTime.toString(dateTimeFormatter)
}

/* print help */

void printDevtoolsUsageHelp(String additionalMessage) {
    if (additionalMessage) {
        println("Error $additionalMessage")
        println()
    }

    println("Usage: devtools.groovy [-v] command option")
    print("command: ")
    command_map.each { command, options ->
        print("\n  $command -> ")
        options.each {
            option, internal_cmd -> print("$option ")
        }
    }
    println()
    System.exit(-1)
}

void printDevtoolsOptionsUsageHelp(String additionalMessage) {
    println(additionalMessage)
    println()
    // TODO: print devtools command options: -d, -e, -s
    println()

    println("Run devtools --help for more details")
    println()

    System.exit(-1)
}

void printHelpForSpecificCommand(String command, boolean isOptionError, String option) {
    switch(command) {
        case "gfx":
        case "layout":
        case "overdraw":
        case "updates":
            println("You need to provide two arguments: command and option")
            break
        case "date":
            if (isOptionError) {
                println("Not valid command option: " + option + " for: " + command)
            } else {
                println("Not valid command: " + command)
                // TODO: printDateCommandHelp
            }
            break
        case "devtools":
            printDevtoolsUsageHelp(option)
            break
        default:
            println("Could not find the command $command you provided")
            printDevtoolsUsageHelp(null)
    }
    println()
    System.exit(-1)
}

/* ADB UTILS */

String executeADBCommand(String adbCmd) {
    if (deviceIds.size == 0) {
        println("no devices connected")
        System.exit(-1)
    }
    def proc
    deviceIds.each { deviceId ->
        def adbConnect = "$adbExec -s $deviceId $adbCmd"
        if (verbose)
            println("Executing $adbConnect")
        proc = adbConnect.execute()
        proc.waitFor()
    }
    return proc.text
}

String getAdbPath() {
    def adbExec = "adb"
    if (isWindows())
        adbExec = adbExec + ".exe"
    try {
        def command = "$adbExec"    //try it plain from the path
        command.execute()
        if (verbose)
            println("using adb in " + adbExec)
        return adbExec
    }
    catch (IOException e) {
        //next try with Android Home
        def env = System.getenv("ANDROID_HOME")
        if (verbose)
            println("adb not in path trying Android home")
        if (env != null && env.length() > 0) {
            //try it here
            try {
                adbExec = env + File.separator + "platform-tools" + File.separator + "adb"
                if (isWindows())
                    adbExec = adbExec + ".exe"

                def command = "$adbExec"// is actually a string
                command.execute()
                if (verbose)
                    println("using adb in " + adbExec)

                return adbExec
            }
            catch (IOException ex) {
                println("Could not find $adbExec in path and no ANDROID_HOME is set :(")
                System.exit(-1)
            }
        }
        println("Could not find $adbExec in path and no ANDROID_HOME is set :(")
        System.exit(-1)
    }
}

void kickSystemService() {
    def proc
    int SYSPROPS_TRANSACTION = 1599295570 // ('_'<<24)|('S'<<16)|('P'<<8)|'R'

    def pingService = "shell service call activity $SYSPROPS_TRANSACTION"
    executeADBCommand(pingService)
}

boolean isWindows() {
    return (System.properties['os.name'].toLowerCase().contains('windows'))
}

private boolean isNOrLater() {
    GString apiLevelCmd = "$adbExec shell getprop ro.build.version.sdk";
    proc = apiLevelCmd.execute()
    proc.waitFor()

    Integer apiLevel = 0
    proc.in.text.eachLine { apiLevel = it.toInteger() }
    if (apiLevel == 0) {
        println("Could not retrieve API Level")
        System.exit(-1)
    } else {
        if (apiLevel >= 24) {
            return true
        } else {
            return false
        }
    }
}