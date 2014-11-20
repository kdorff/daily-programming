/**
 * Reddit dailyprogrammer challenge from 2014-11-10
 * http://www.reddit.com/r/dailyprogrammer/comments/2lvgz6/20141110_challenge_188_easy_yyyymmdd/
 *
 * iso 8601 standard for dates tells us the proper way to do an extended day 
 * is yyyy-mm-dd
 * yyyy = year
 * mm = month
 * dd = day
 *
 * A company's database has become polluted with mixed date formats. They 
 * could be one of 6 different formats
 *
 * yyyy-mm-dd
 * mm/dd/yy
 * mm#yy#dd
 * dd*mm*yyyy
 * (month word) dd, yy
 * (month word) dd, yyyy
 *
 * (month word) can be: Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec
 * Note if is yyyy it is a full 4 digit year. If it is yy then it is only the
 * last 2 digits of the year. Years only go between 1950-2049.
 *
 * Input:
 * You will be given 1000 dates to correct.
 *
 * Output:
 * You must output the dates to the proper iso 8601 standard of yyyy-mm-dd
 */

// Kick it off
new Dates().exec("input.txt")

/**
 * Convert date in known formats to ISO8601.
 */
class Dates {

    /**
     * Number shorthand (3 letter) to integer month.
     */
    def months = ['Jan' : 1, 'Feb' : 2,  'Mar' : 3,  'Apr' : 4,  
                  'May' : 5, 'Jun' : 6,  'Jul' : 7,  'Aug' : 8,
                  'Sep' : 9, 'Oct' : 10, 'Nov' : 11, 'Dec' : 12,]

    /**
     * Data about known formats and how to split
     * and read them.
     */
    def formats = [
        '-' : [findExpr: '-'  ,    order: ['y', 'm', 'd']],
        '/' : [findExpr: '/'  ,    order: ['m', 'd', 'y']],
        '#' : [findExpr: '#'  ,    order: ['m', 'y', 'd']],
        '*' : [findExpr: '\\*',    order: ['d', 'm', 'y']],
        ' ' : [findExpr: '( |, )', order: ['M', 'd', 'y']],
    ]

    /**
     * Primary execution starts here.
     */
    def exec(inputFile) {
        new File(inputFile).eachLine { date ->
            // Determine the format of the string
            formats.each { toFind, patternMap ->
                if (date.contains(toFind)) {
                    // Obtain the ISO8601 date from the string
                    def newDateStr = parseDateString(date, toFind, patternMap)
                    if (newDateStr == null) {
                        // Report error of input date we couldn't parse
                        println "ERROR PARSING ${date} !"
                    } else {
                        // Normal output
                        println "${date} -> ${newDateStr}"
                    }
                }
            }
        }
    }

    /**
     * Parse a date in a known format to ISO8601. Return
     * null if we couldn't parse as we thought we could.
     * @param dateStr the date in a known format
     * @param toFind the parameter to split the date
     * @param patternMap the map the describes the known date format
     */
    def parseDateString(dateStr, toFind, patternMap) {
        def newDate
        // Split the date string, we should get 3 parts back
        def dateParts = dateStr.split(patternMap.findExpr)
        if (dateParts.size() == 3) {
            // Take the 3 parts and convert them to an ISO8601 date
            newDate = parseDateFromSplit(dateParts, patternMap.order)
        }
        newDate
    }

    /**
     * Parse a date in a known format to ISO8601.
     * @param dateParts 3 parts that make up the date in the order
     * specified in orderList.
     * @param orderList the order of the parts in dateParts (ymMd,
     * M==3 character month name)
     */
    def parseDateFromSplit(dateParts, orderList) {
        def year
        def month
        def day 
        int i = 0
        for (item in orderList) {
            switch (item) {
                case 'y':
                    year = dateParts[i++] as Integer
                    if (year < 100) {
                        if (year >= 50) {
                            year += 1900 
                        } else {
                            year += 2000
                        }
                    }
                    break
                case 'm':
                    month = dateParts[i++] as Integer
                    break
                case 'd':
                    day = dateParts[i++] as Integer
                    break
                case 'M':
                    month = months[dateParts[i++]]
                    break
            }
        }
        // Construct ISO8601.
        "${year}-${String.format("%02d", month)}-${String.format("%02d", day)}"
    }
}