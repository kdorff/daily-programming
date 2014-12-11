def eq = new Equation()
[   '12+34+56+78+90',
    '1+34+56+78+90+325',
    '999999+1',
].each { eqStr ->
    println eq.calculate(eqStr)
    println ""
}

/**
 * Class to calculate addition using elementary method.
 * This class is NOT threadsafe.
 */
class Equation {
    /** 2d array of int's to hold input and output data. */
    def valuesGrid
    /* The widest size of the input numbers. */
    def maxNumSize
    /* The number of input numbers. */
    def numValues
    /* The index of the result row. */
    def resultRow
    /* The index of the carries row. */
    def carriesRow

    /**
     * Setup the grid for calculation given the input equation of 
     * integer additions.
     * @param eqStr the equation of integer additions as a string
     */
    def setupGrid(eqStr) {
        // Convert numbers to ints and back to strings, values
        // will contain a list of strings that are int values
        // If an incoming int value was '095', just '95' will
        // be stored in the list.
        def strValues = (eqStr.trim().split('[+]') as List).collect { "${it.trim() as Integer}" }
        numValues = strValues.size()
        // Find the maximum length
        maxNumSize = strValues.inject(0) { maxSize, number -> 
            Math.max(maxSize, number.size()) 
        }
        resultRow = numValues
        carriesRow = numValues + 1
        valuesGrid = new int[numValues + 2][maxNumSize + 1]
        populateInputs(strValues)
    }

    /**
     * Populate the incoming values onto the grid.
     * @param strValues list of string integers.
     */
    def populateInputs(strValues) {
        strValues.eachWithIndex { numberStr, r ->
            int numWidth = numberStr.size()
            int c = maxNumSize + 1 - numWidth
            numberStr.each { digit ->
                valuesGrid[r][c++] = digit as Integer
            }
        }
    }
    /**
     * Perform the addition calculation.
     */
    String calculate(eqStr) {
        setupGrid(eqStr)
        (maxNumSize .. 0).each { c ->
            def sum = 0
            (0 ..< numValues).each { r ->
                sum += valuesGrid[r][c]
            }
            sum += valuesGrid[carriesRow][c]
            def sumStr = "${sum}"
            valuesGrid[resultRow][c] = sumStr[-1] as Integer
            valuesGrid[carriesRow][c - 1] = 
                sumStr.size() == 2 ? sumStr[0] as Integer : 0
        }
        toString()
    }

    /**
     * Display the grid.
     */
    String toString() {
        def sb = new StringBuilder()
        def leading
        (0 ..< numValues).each { r ->
            leading = true
            (0 ..< (maxNumSize + 1)).each { c ->
                def value = valuesGrid[r][c]
                leading = leading && (value == 0)
                sb << (leading ? ' ' : value)

            }
            sb << '\n'
        }
        sb << "-" * (maxNumSize + 1) << '\n'
        leading = true
        (0 ..< (maxNumSize + 1)).each { c ->
            def value = valuesGrid[resultRow][c]
            leading = leading && (value == 0)
            sb << (leading ? ' ' : value)
        }
        sb << '\n'
        sb << "-" * (maxNumSize + 1) << '\n'
        (0 ..< (maxNumSize + 1)).each { c ->
            def value = valuesGrid[carriesRow][c]
            sb << (value == 0 ? ' ' : value)
        }
        sb.toString()
    }
}
