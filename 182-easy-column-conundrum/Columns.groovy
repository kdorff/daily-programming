wordPattern = ~/([0-9a-zA-Z-?!(),.$#;]+)/
def inputFile = "c182e-input.txt" as File
def numColumns, colWidth, spaceWidth
def lines = []
inputFile.eachLine { line ->
    if (numColumns == null) {
        (numColumns, colWidth, spaceWidth) = line.split(" ").collect { it as int }
    } else {
        lines << line
    }
}

def columns = columnize(linesWithWidth(lines, colWidth), numColumns)
outputColumnar(columns, colWidth, spaceWidth)

/**
 * Convert lines of text to a list of strings no longer than colWidth wide.
 * @param lines the lines to convert
 * @param colWidth the maximum column width
 * @return list of string, each line no longer than colWidth
 */
def linesWithWidth(lines, int colWidth) {
    def result = []
    def currentLine = new StringBuilder()
    lines.each { line ->
        line.findAll(wordPattern) { whole, word ->
            // End of word
            if (currentLine.size() == 0 || 
                    (currentLine.size() + word.size() < colWidth)) {
                if (currentLine.size() > 0) {
                    currentLine << ' '
                }
                currentLine << word
            } else {
                result << currentLine.toString()
                currentLine.length = 0
                currentLine << word
            }
        }
    }
    // End of last line
    if (currentLine.size() > 0) {
        result << currentLine.toString()
        currentLine.length = 0
    }
    result
}

/**
 * Convert lines, already at maximum width, into a list (size numColumns)
 * of lists (lines for each column).
 * @param columnLines lines of text already of size colWidth
 * @param numColumns the number of columns to output
 * @return a list of size numColumns, each item being a list of strings for
 * that column.
 */
def columnize(columnLines, int numColumns) {
    def allColumns = []
    (0 ..< numColumns).each {
        allColumns << []
    }
    def maxLinesPerColumn = Math.ceil(columnLines.size() / numColumns) as Integer
    int currentColumn = -1
    columnLines.eachWithIndex { line, index ->
        if (index % maxLinesPerColumn == 0) {
            currentColumn++
        }
        allColumns[currentColumn] << line
    }
    allColumns
}

/**
 * Output result in the format returned by columnize to the screen.
 * @param columnsLists output in the format from columnize
 * @param colWidth the maximum width of a column
 * @param spaceWidth the amount of space between columns
 */
def outputColumnar(columnsLists, int colWidth, int spaceWidth) {
    int numColumns = columnsLists.size()
    int numLines = columnsLists[0].size()
    int lineNo = 0
    while (lineNo < numLines) {
        (0 ..< numColumns).each { columnNo ->
            def line = columnsLists[columnNo][lineNo] ?: ''
            def pad = ' ' * (colWidth - line.size())
            print line
            print pad
            if (columnNo != numColumns - 1) {
                print ' ' * spaceWidth
            }
        }
        println ""
        lineNo++
    }
}