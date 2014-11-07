/**
 * Reddit daily programming for 2014-11-05
 * http://www.reddit.com/r/dailyprogrammer/comments/2ledaj/11052014_challenge_187_intermediate_finding_time/
 * 
 * Description:
 * I cover the border of my monitor with post it notes with tasks I have to do 
 * during the week. I am very unorganized. Each day I want to find the biggest
 * block of free time to go on to Reddit. But I am not sure when that time is.
 * I am also curious how I spend my days.
 * This challenge you will help me get organized and find that time for
 * me to be on Reddit.
 *
 * Input:
 * I will give you a listing of the post it notes around my monitor. Each line
 * represents a single post it note. Sorry but they are not in any order but I
 * was at least smart enough to date them and put the times of my daily events.
 * 
 * Output:
 * Get me organized. I need to see my schedule for the week. For each day
 * you must find the 1 block of time that is the most time between events
 * on the post its that I can Reddit. Please help maximize my time on Reddit.
 * Assume my start time at work is the beginning of the first event and my 
 * end time at work is the end time of the last event for that day.
 * Then show me my final schedule. And while you are at it show me across the
 * week how many minutes I dedicate to each task with a percentage of time it
 * takes up my time. Hopefully I don't spend most of my time on Reddit.
 */
@Grab(group='joda-time', module='joda-time', version='2.5')

import org.joda.time.*

def schedule = new Schedule()
schedule.exec("schedule.txt")

class Schedule {

    /**
     * Regex to parse the input
     */
    def schedulePattern = ~/(\d{1,2})-(\d{1,2})-(\d{4}): (\d{2}):(\d{2}) ([AP]M) to (\d{2}):(\d{2}) ([AP]M) -- (.*)/

    /**
     * Map to track tasks-per-day (day to interval list map)
     */
    def dayToIntervals = [:]
    /**
     * Given an interval, the name of the task (interval to task name map)
     */
    def intervalToTask = [:]

    /**
     * Primary method.
     */
    def exec(input) {
        // Read the input file
        def lines = (input as File).readLines()
        lines.each { line ->
            // Parse the input to a map of data
            def sched = schedulePartsToMap(line)
            if (sched) {
                // If we got here, parsing worked
                if (!dayToIntervals[sched.mapKey]) {
                    dayToIntervals[sched.mapKey] = []
                }
                // Record the task for the specific interval
                intervalToTask[sched.interval] = sched.event
                // Save the interval to the intervals for a specific day
                dayToIntervals[sched.mapKey] << sched.interval
            }
        }

        // For each day, sort the intervals. This way we can
        // determine the largest gap more easily
        dayToIntervals.each { day, intervals ->
            sortIntervals(intervals)
        }

        // For each day, determine the largest gap. Make an interval
        // for reddit for that gap
        dayToIntervals.each { day, intervals ->
            def maxFree = [freeDuration: -1]
            for (int i = 1; i < intervals.size(); i++) {
                def freeStartDateTime = intervals[i-1].getEnd()
                def freeEndDateTime = intervals[i].getStart()
                def freeDuration = new Duration(freeStartDateTime, freeEndDateTime).standardMinutes
                if (freeDuration > maxFree.freeDuration) {
                    maxFree.freeDuration = freeDuration
                    maxFree.interval = new Interval(freeStartDateTime, freeEndDateTime)
                }
            }
            // We have the largest gap, add reddit to the gap
            // the re-sort the intervals for the day
            intervalToTask[maxFree.interval] = "REDDIT!"
            intervals << maxFree.interval
            sortIntervals(intervals)
        }

        // Obtain the stats for the completed schedule
        def statsMap = obtainStats(dayToIntervals)

        // Determine the sorted days for output
        def sortedDays = dayToIntervals.keySet().sort()

        // Output schedule
        sortedDays.each { day->
            println day
            dayToIntervals[day].each { interval ->
                println "    ${interval} : ${intervalToTask[interval]}"
            }
        }

        // Output statistics
        println ""
        statsMap.each { task, time ->
            println "${task}: Time spent=${time} minutes (${time/statsMap.total*100}%)"
        }
    }

    /**
     * Schedule is complete. Obtain stats.
     */
    def obtainStats(dayToIntervals) {
        def stats = [:]
        stats.total = 0
        dayToIntervals.each { day, intervals ->
            intervals.each { interval ->
                def task = intervalToTask[interval]
                if (stats[task] == null) {
                    stats[task] = 0
                }
                stats[task] += duration(interval)
            }
        }
        stats.each { stat, duration ->
            stats.total += duration
        }
        stats
    }

    /**
     * Determine the duration, in minutes, of an event
     */
    def duration(interval) {
        new Duration(interval.getStart(), interval.getEnd()).standardMinutes
    }

    /**
     * Given a list of intervals, sort it.
     */
    def sortIntervals(intervals) {
        intervals.sort { x, y ->
            x.getStart().compareTo(y.getStart());
        }
    }

    /**
     * Parse an input line to a map of data that is easier to work with.
     * @param line the input line
     * @return a map of data about the schedule (including interval) or
     *         null if the schedule couldn't be parsed.
     */
    def schedulePartsToMap(line) {
        def sched = null
        line.find(schedulePattern) { whole, month, day, year, startHour, startMinute, startAmpm, endHour, endMinute, endAmpm, event ->
            // Sched saves too much information, but that's OK
            sched = [:]
            sched.month = month as Integer
            sched.day = day as Integer
            sched.year = year as Integer
            sched.startHour = (startHour as Integer)
            sched.startHour -= (startAmpm == "AM" && sched.startHour == 12 ? 12 : 0)
            sched.startHour += (startAmpm == "PM" && sched.startHour < 12 ? 12 : 0)
            sched.startMinute = startMinute as Integer
            sched.endHour = (endHour as Integer)
            sched.endHour -= (endAmpm == "AM" && sched.endHour == 12 ? 12 : 0)
            sched.endHour += (endAmpm == "PM" && sched.endHour < 12 ? 12 : 0)
            sched.endMinute = endMinute as Integer
            sched.event = event
            sched.mapKey = "${String.format("%04d", sched.year)}-" + 
                            "${String.format("%02d", sched.month)}-" + 
                            "${String.format("%02d", sched.day)}"
            sched.start = new DateTime(sched.year, sched.month, sched.day, sched.startHour, sched.startMinute)
            sched.end = new DateTime(sched.year, sched.month, sched.day, sched.endHour, sched.endMinute)
            sched.interval = new Interval(sched.start, sched.end)
        }
        sched
    }
}
