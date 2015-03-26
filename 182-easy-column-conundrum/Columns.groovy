println new ColumnMaker().processFile("c182e-input.txt" as File).toString()

class ColumnMaker {
    /** Pattern of word with any following or contained punctuation. */
    def wordPattern = ~/([0-9a-zA-Z-?!(),.$#;]+)/
    /** The number of columns, width-per-column, and spaces between columns
        as defined in the input file's first line. */
    def numColumns, colWidth, spaceWidth
    /** List of processed data. List is numColumns in length, each entry
        contains a list of lines for that column. */
    def columnsLists

    /**
     * Process an input file of the specified input.
     * @param inputFile the input file to columnarize.
     * @return this this object, so .toString(), etc. can be chained.
     */
    ColumnMaker processFile(File inputFile) {
        List<String> widthedLines = readFile(inputFile)
        columnsLists = columnize(widthedLines)
        this
    }

    /**
     * Read the inpput file. The first line should contain 3 integers, the
     * rest of the file are lines to be columnarized
     * @param inputFile the input file to columnarize.
     * @return list of string that contain the contents of the file (without
     * the first line)
     */
    List<String> readFile(File inputFile) {
        List<String> lines = []
        def reader = inputFile.newReader()
        (numColumns, colWidth, spaceWidth) =
            reader.readLine().split(" ").collect { it as int }
        readLinesWithWidth(reader)
    }

    /**
     * Convert lines of text to a list of strings no longer than colWidth wide.
     * @param lines the lines to convert
     * @return list of string, each line no longer than colWidth
     */
    private List<String> readLinesWithWidth(reader) {
        List<String> result = []
        StringBuilder currentLine = new StringBuilder()
        def line = reader.readLine()
        while (line != null) {
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
            line = reader.readLine()
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
     * @return list of text lines, each line no longer than colWidth
     * @return a list of size numColumns, each item being a list of strings for
     * that column.
     */
    private List<List<String>> columnize(columnLines) {
        List<List<String>> allColumns = []
        (0 ..< numColumns).each {
            allColumns << []
        }
        int maxLinesPerColumn = Math.ceil(columnLines.size() / numColumns) as Integer
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
     * Return columnarized text as a String.
     * @return text as a columnarized string with multiple lines
     */
    String toString() {
        StringBuilder sb = new StringBuilder()
        int numColumns = columnsLists.size()
        int numLines = columnsLists[0].size()
        int lineNo = 0
        while (lineNo < numLines) {
            (0 ..< numColumns).each { columnNo ->
                def line = columnsLists[columnNo][lineNo] ?: ''
                def pad = ' ' * (colWidth - line.size())
                sb << line
                sb << pad
                if (columnNo != numColumns - 1) {
                    sb << ' ' * spaceWidth
                }
            }
            sb << "\n"
            lineNo++
        }
        sb.toString()
    }
}