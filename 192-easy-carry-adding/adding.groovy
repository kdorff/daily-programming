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
    def values
    /* The widest size of the input numbers. */
    def maxNumSize
    /* int array of values for the result, one digit per slot. */
    def results
    /* int array of values for the carries, one digit per slot */
    def carries

    /**
     * Setup the grid for calculation given the input equation of 
     * integer additions.
     * @param eqStr the equation of integer additions as a string
     */
    def setup(eqStr) {
        // Convert the equation to a list of ints
        values = (eqStr.split('[+]') as List).collect { it as Integer }
        // Find the maximum length
        maxNumSize = values.inject(0) { maxSize, number -> 
            Math.max(maxSize, numDigits(number) + 1) 
        }
        results = new int[maxNumSize]
        carries = new int[maxNumSize]
    }

    int numDigits(number) {
        int digits = 0
        if (number <= 0) {
            digits = 1
        }
        while (number) {
            number = Math.floor(number / 10) as Integer
            digits++
        }
        digits
    }

    /**
     * Perform the addition calculation.
     */
    String calculate(eqStr) {
        setup(eqStr)
        (maxNumSize .. 0).each { c ->
            def sum = 0
            (0 ..< values.size()).each { r ->
                sum += valueAt(r, c)
            }
            sum += carries[c]
            def sumStr = "${sum}"
            results[c] = sumStr[-1] as Integer
            carries[c - 1] =  sumStr.size() == 2 ? sumStr[0] as Integer : 0
        }
        toString()
    }

    int valueAt(r, c) {
        def value = values[r]
        def numDigits = numDigits(value)
        def diffDigits = maxNumSize - numDigits
        println "value=${value}, maxNumSize=${maxNumSize}, c=${c}, numDigits=${numDigits}, diffDigits=${diffDigits}"
        if (c < diffDigits) {
            0
        } else {
            "${value}"[c - diffDigits] as Integer
        }

    }

    /**
     * Display the grid.
     */
    String toString() {
        def sb = new StringBuilder()
        def leading
        (0 ..< values.size()).each { r ->
            leading = true
            (0 ..< maxNumSize).each { c ->
                def value = valueAt(r, c)
                leading = leading && (value == 0)
                sb << (leading ? ' ' : value)

            }
            sb << '\n'
        }
        sb << "-" * (maxNumSize + 1) << '\n'
        leading = true
        (0 ..< (maxNumSize + 1)).each { c ->
            def value = results[c]
            leading = leading && (value == 0)
            sb << (leading ? ' ' : value)
        }
        sb << '\n'
        sb << "-" * (maxNumSize + 1) << '\n'
        (0 ..< (maxNumSize + 1)).each { c ->
            def value = carries[c]
            sb << (value == 0 ? ' ' : value)
        }
        sb.toString()
    }
}
