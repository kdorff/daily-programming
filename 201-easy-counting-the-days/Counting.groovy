@Grab(group='joda-time', module='joda-time', version='2.7')

/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2vc5xq/20150209_challenge_201_easy_counting_the_days/
 */

import org.joda.time.*
import org.joda.time.format.*

def inputs = ["2015 7 4", "2015 10 31", "2015 12 24",
              "2016 1 1", "2016 2 9",   "2020 1 1",
              "2020 2 9", "2020 3 1",   "3015 2 9"]

def today = new LocalDate()
inputs.each { when ->
    def format =  DateTimeFormat.forPattern("yyyy MM dd")
    def targetDate = LocalDate.parse(when, format)
    def diff = Days.daysBetween(today, targetDate) 
    println "${diff.days} days from ${today.toString(format)} " + 
        "until ${targetDate.toString(format)}"
}