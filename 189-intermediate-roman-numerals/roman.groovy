// Roman numeral digits to their values
value = [
    'I': 1,    'V': 5,    'X': 10,
    'L': 50,   'C': 100,  'D': 500,
    'M': 1000,
]

// Format for roman numerals including the
// () notation where values are multiplied * 1000 for
// each nesting depth.
romanMatcher = ~/^(\([()IVXLCDM]+\))?([IVXLCDM]*)$/

// Input data with expected value
['IIX': -1, 'IIB': -1, "()V()V": -1,
 'IV': 4, 'VI': 6, 
 'XII': 12, 'MDCCLXXVI': 1776, 'IX': 9, 'XCIV':94,
 'IV' : 4, 'XXXIV' : 34, 'CCLXVII' : 267, 'DCCLXIV' : 764, 
 'CMLXXXVII' : 987, 'MCMLXXXIII' : 1983, 'MMXIV' : 2014, 
 'MMMM' : 4000, 'MMMMCMXCIX' : 4999,
 '(V)' : 5000, '(V)CDLXXVIII' : 5478, '(V)M' : 6000, 
 '(IX)' : 9000, '(X)M' : 11000, '(X)MM' : 12000, 
 '(X)MMCCCXLV' : 12345, '(CCCX)MMMMCLIX' : 314159, 
 '(DLXXV)MMMCCLXVII' : 578267, '(MMMCCXV)CDLXVIII' : 3215468, 
 '(MMMMCCX)MMMMCDLXVIII' : 4214468, '(MMMMCCXV)CDLXVIII' : 4215468, 
 '(MMMMCCXV)MMMCDLXVIII' : 4218468, '(MMMMCCXIX)CDLXVIII' : 4219468, 
 '((XV)MDCCLXXV)MMCCXVI' : 16777216, '((CCCX)MMMMCLIX)CCLXV' : 314159265, 
 '((MLXX)MMMDCCXL)MDCCCXXIV' : 1073741824, 
 ].each { roman, expected ->
    // Convert the roman number 
    try {
        int value = parseRomanNumerals(roman, 1)
        print "${roman} = ${value} expecting ${expected} "
        println "(${expected == value ? true : false})"
    } catch (IllegalArgumentException e) {        
        print "${roman} ${e.message} "
        println "(${expected == -1 ? true : false})"
    }
}

/**
 * Parse a roman numeral string to an int
 * @param the roman number digits
 * @param multiplier the multiplier 
 */
def parseRomanNumerals(roman, multiplier) throws IllegalArgumentException {
    int total = 0
    int numSmaller
    int largestValue = 0
    int subTotal = 0
    boolean matched = false
    roman.find(romanMatcher) { whole, subMatch, toParse ->
        // Regex match for roman numeral. Doesn't mean it's valid
        // but it contains the rights characters
        matched = true
        if (subMatch) {
            // We have an embedded roman number (within ()'s)
            // Recurse to parse the new value at a 1000x multiplier
            subTotal = parseRomanNumerals(
                // Sub-parse without enclosing parens
                subMatch[1..-2], multiplier * 1000)
        }
        // Iterate in reverse order
        toParse.reverse().each { letter ->
            int currentValue = value[letter]
            if (largestValue && largestValue > currentValue) {
                // Subtract a smaller value from the total
                // Such as the I in IX subtracts 1 from the total
                total -= currentValue
                numSmaller++
                if (numSmaller > 1) {
                    // But we can only do this once.
                    throw new IllegalArgumentException(
                        "Invalid roman numeral ${roman}")
                }
            } else {
                // We have a larger or equal value than before. Add to total
                total += currentValue
                numSmaller = 0
            }
            if (largestValue < currentValue) {
                // New largest value
                largestValue = currentValue
            }
        }
    } 
    if (!matched) {
        // Regex didn't match. Bad roman numeral string
        throw new IllegalArgumentException("Invalid roman numeral ${roman}")
    }
    (total * multiplier) + subTotal
}