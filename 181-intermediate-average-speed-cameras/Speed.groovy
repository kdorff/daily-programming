@Grab(group='joda-time', module='joda-time', version='2.7')

import java.text.DecimalFormat
import org.joda.time.*

def o = new SpeedLimit("input.txt" as File).exec()

/**
 * Speed camera calculation, see what vehicles broke the speed limit for
 * a specific stretch that has speed cameras.
 */
class SpeedLimit {
    /** Patterns for reading input. */
    def speedLimitPattern = ~/Speed limit is ([1-9][0-9]\.[0-9]{2}) (mph|kmh)\./
    def cameraDistPattern = ~/Speed camera number ([1-9]) is (0|[1-9][0-9]{0,3}) metres down the motorway\./
    /** This pattern is unused, not necessary. */
    def logStartPattern   = ~/Start of log for camera ([1-9])\./
    def logPattern        = ~/Vehicle ([0-9A-Z]{1,4} [0-9A-Z]{1,4}) passed camera ([1-9]) at ([0-9]{2}:[0-9]{2}:[0-9]{2})\./
    /** Format for outputting numbers with 1 decimal place. */
    def numFormat = new DecimalFormat("#.#")

    /** Input file. */
    File file
    /** Speed limit in kph, always used. */
    double speedLimitKph
    /** Speed limit in mph, only used if the local unit is mph. */
    double speedLimitMph
    /** The speed limit unit kmh|mph. */
    String speedUnit
    /** The distance for each camera. */
    def cameraToDistance = [:]
    
    /** Constructor. */
    public SpeedLimit(File file) {
        this.file = file
    }

    /**
     * Process the input.
     */
    def exec() {
        // The last time we saw a specific vehicle
        def lastTime = [:]
        file.eachLine { line ->
            line.find(speedLimitPattern) { whole, limitStr, limitUnits ->
                // Line matches speed limit
                speedLimitKph = limitStr as Double
                speedUnit = limitUnits
                if (limitUnits == 'mph') {
                    // Convert mpg to kph
                    speedLimitMph = speedLimitKph
                    speedLimitKph *= 1.609
                }
            }
            line.find(cameraDistPattern) { whole, cameraStr, distanceStr ->
                // Line matches camera position
                def camera = cameraStr as int
                def distance = distanceStr as int
                cameraToDistance[camera] = distance
            }
            line.find(logPattern) { whole, vehicle, cameraStr, time ->
                // Line is a vehicles speed at a speific time
                def camera = cameraStr as int
                def timeParts = time.split(":").collect { it as int}
                DateTime dt = new DateTime(2000, 1, 1, 
                    timeParts[0], timeParts[1], timeParts[2])
                if (camera > 1) {
                    // This is not the first camera, but a later one
                    // Check the speed for this stretch
                    int distSeconds = Seconds.secondsBetween(
                        lastTime[vehicle], dt).getSeconds()
                    int distMeters = cameraToDistance[camera] - 
                                     cameraToDistance[camera - 1]
                    processVehicle(vehicle, distMeters, distSeconds)
                    lastTime[vehicle] = dt
                }
                lastTime[vehicle] = dt
            }
        }
    }

    /**
     * Given a specific vehicle that has travelled distMeters meters
     * and distSeconds seconds, see if they broke the speed limit and output
     * in the local units if they did.
     * @param vehcile the vehicle registration tag
     * @param distMeters the number of meters the vehicle travelled
     * @param distSeconds the number of seconds that passed to travel distMeters
     */
    def processVehicle(String vehicle, int distMeters, int distSeconds) {
        double kph = (distMeters / distSeconds) * 3.6
        if (kph > speedLimitKph) {
            print "Vehicle ${vehicle} broke the speed limit by "
            if (speedUnit == 'mph') {
                def mph = kph * 0.621371
                print "${numFormat.format(mph - speedLimitMph)} "
            } else {
                print "${numFormat.format(kph - speedLimitKph)} "
            }
            println "${speedUnit}."
        }
    }
}