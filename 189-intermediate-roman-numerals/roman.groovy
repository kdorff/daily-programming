// Roman numeral digits to their values
value = [
    'I': 1,    'V': 5,    'X': 10,
    'L': 50,   'C': 100,  'D': 500,
    'M': 1000,
]

def romanMatcher = ~/^(\([()IVXLCDM]+\))?([IVXLCDM]+)/

// Input data with expected value
['IIX': -1, 'IV': 4, 'VI': 6, 
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
        print "${e.message} "
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
    int largestValue
    StringBuilder subValue = new StringBuilder()
    int nestDepth = 0
    int subTotal = 0

    // Iterate over the roman number value in reverse
    roman.reverse().each { letter ->
        if (nestDepth > 0 || letter == ')') {
            // Collect the sub-ruman number that is 1000x greater
            if (letter == ')') { 
                nestDepth++ 
            } else if (letter == '(') { 
                nestDepth-- 
            }
            subValue << letter
            if (nestDepth == 0 && subValue.size() > 0) {
                String subParse = subValue.reverse()[1..-2]
                subTotal = parseRomanNumerals(
                    subParse, multiplier * 1000)
                subValue.length = 0
            }
        } else {
            Integer curValue = value[letter]
            if (curValue == null) {
                throw new IllegalArgumentException(
                    "Invalid roman numeral digit ${letter} in ${roman}")
            }
            if (largestValue && largestValue > curValue) {
                total -= curValue
                numSmaller++
                if (numSmaller > 1) {
                    throw new IllegalArgumentException(
                        "Invalid roman numeral ${roman}")
                }
            } else {
                total += curValue
                numSmaller = 0
            }
            if (largestValue < curValue) {
                largestValue = curValue
            }
        }
    }
    (total * multiplier) + subTotal
}