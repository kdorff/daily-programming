 /**
  * Reddit dailing programming challenge 189 intermediate
  * http://www.reddit.com/r/dailyprogrammer/comments/2ms946/20141119_challenge_189_intermediate_roman_numeral/
  * 
  * Your friend is an anthropology major who is studying roman history. 
  * They have never been able to quite get a handle for roman numerals and 
  * how to read them, so they've asked you to come up with a simple program 
  * that will let them input some numbers and return roman numerals, as well 
  * as the opposite, to input roman numerals and return base-10 numbers. 
  * They are bribing you with Indiana Jones memorabilia, so you are totally up
  * for the challenge!
  *
  * Description
  * -------------------
  * Most people learn about roman numerals at a young age. If you look at
  * many analog clocks, you will find that many of them actually use roman
  * numerals for the numbers. Roman numerals do not just stop at 12 though,
  * they actually can represent numbers as high as 4999 using their most basic
  * form. The challenge, is to create a program that will allow you to convert
  * decimal (base-10) numbers to roman numerals as well as roman numerals to
  * decimal numbers. The history of roman numerals is a bit debated because 
  * of their varied use throughout history and a seeming lack of a standard 
  * definition. Some rules are well accepted and some less-so. Here are the 
  * guidelines for your implementation:
  *
  * I   V   X   L   C   D   M
  * 1   5   10  50  100 500 1000
  *
  * Rules
  * -------------------
  * You cannot repeat the same roman numeral more than three times in a 
  * row, except for M, which can be added up to four times. (Note: Some 
  * descriptions of roman numerals allows for IIII to represent 4 instead of 
  * IV. For the purposes of this exercise, that is not allowed.) When read 
  * from left to right, if successive roman numerals decrease or stay the 
  * same in value, you add them to the total sum. When read from left to 
  * right, if successive roman numerals increase in value, you subtract 
  * the smaller value from the larger one and add the result to the total sum.
  *
  * Restrictions
  * -------------------
  * I can only be subtracted from V or X
  * X can only be subtracted from L or C
  * C can only be subtracted from D or M
  *
  * Only one smaller value can be subtracted from a following larger value. 
  * (e.g. 'IIX' would be an invalid way to represent the number 8)
  *
  * Examples
  * -------------------
  * XII = 10 + 1 + 1 = 12
  * MDCCLXXVI = 1000 + 500 + 100 + 100 + 50 + 10 + 10 + 5 + 1 = 1776
  * IX = "1 from 10" = 10 - 1 = 9
  * XCIV = "10 from 100" + "1 from 5" = (100 - 10) + (5 - 1) = 90 + 4 = 94
  *
  * Inputs and Outputs
  * -------------------
  * Your program should be able to accept numbers in either integer or roman 
  * numeral format to return the other. You may want to add validation checks 
  * on the input. When converting to a roman numeral, the maximum number is 
  * 4999. When converting from a roman numeral, I,V,X,L,C,D,M are the only 
  * valid characters. You should be able to accept one or many numbers or 
  * numerals and convert to the other direction.
  *
  * Challenge
  * -------------------
  * Some historical accounts state that roman numerals could actually go 
  * much higher than 4999. There are incredibly varied explanations and 
  * syntactical requirements for them. Some state that an over-line 
  * (vinculum) would be used over a number to multiply it by 1000, some say 
  * that you would put a curved line on either side of a number to multiply 
  * it by 1000. For the challenge, see if you can add support to your code 
  * to allow parenthesis to encapsulate parts of a number that can be 
  * multiplied by one thousand. You can nest parenthesis as well to allow 
  * for numbers that are incredibly large.
  *
  * Restriction
  * -------------------
  * The last roman numeral digit inside a set of parenthesis can not be an "I".
  * There are two reasons for this (1) because historical accounts claimed that
  * confusion would happen with the curved lines that encapsulate a number to 
  * be multiplied by one thousand and (2) because the easiest way to validate 
  * your numbers is with Wolfram Alpha and they do not allow it either.
  *
  * Examples
  * -------------------
  * (V)M = 5*1000 + 1000 = 6000
  * (X)MMCCCXLV = 10*1000 + 1000 + 1000 + 100 + 100 + 100 + (50 - 10) + 5 = 10000 + 2000 + 300 + 40 + 5 = 12345
  * ((XV)M)DCC = ((10 + 5) * 1000 + 1000) * 1000 + 500 + 100 + 100 = (15000 + 1000) * 1000 + 1700 = 16000000 + 1700 = 16001700
  */

// Roman numeral digits to their values. We'll include the valid subtractive
// values in the list to assist with parsing and creation of roman numerals
value = [
    'I':  1,    
    'IV': 4,    'V': 5,    
    'IX': 9,    'X': 10,
    'XL': 40,   'L': 50,   
    'XC': 90,   'C': 100,
    'CD': 400,  'D': 500,
    'CM': 900,  'M': 1000,
]
// Just the roman digits but in reverse order
romanDigitsReverse = (value.keySet() as List).reverse()

// Format for roman numerals including the () notation where values are
// multiplied * 1000 for each nesting depth.
romanMatcher = ~/^(\([()IVXLCDM]+\))?([IVXLCDM]*)$/

// Count passes and fails
int passes = 0
int fails = 0
// Input data with expected value. This includes the test cases from
// reddit as well as some of my own test cases. A value of -1 means
// I expect the parsing to fail.
['IIX': -1,   // Cannot have two smaller before larger
 'IIB': -1,   // Invalid numerals
 '()V()V': -1,  // This will initial pass regex, but is invalid
 '(XI)V': -1,   // Multiplier sub-number cannot end with I
 '(VVVV)V': -1,   // Only 3 consecutive values allowed
 '(VVV)IIII': -1,   // Only 3 consecutive values allowed
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
 ].each { romanValue, intValue ->
    // Convert the romanValue to intValue
    boolean passed
    try {
        int calcValue = romanNumeralsToInt(romanValue, 1)
        passed = calcValue == intValue
        print "${romanValue} = ${calcValue} expect ${intValue} ${passed}"
        println "()"
    } catch (IllegalArgumentException e) {        
        passed = intValue == -1
        print "${roman} ${e.message} "
        println "(${passed})"
    }
    if (passed) {
        passes++
    } else {
        fails++
    }

    // Convert the intValue to romanValue
    if (intValue != -1) {
        if (!romanValue.contains("(")) {
            // Only convert roman values that we know are valid
            String calcValue = intToRomanNumerals(intValue)
            passed = calcValue == romanValue
            if (passed) {
                passes++
            } else {
                fails++
            }
            print "${intValue} = ${calcValue} expect ${romanValue} ${passed}"
        }
    }
}
// Final eport
println "Number of passes = ${passes}"
println "Number of fails =  ${fails}"

/**
 * Convert a roman numeral string with a multiplier to int. This will
 * check for formatting mistakes within the roman numeral and throw
 * exceptions if they ar found.
 * @param roman the roman numeral string
 * @param multiplier the multiplier (for parens support)
 */
def romanNumeralsToInt(roman, multiplier) throws IllegalArgumentException {
    boolean matched = false
    int total = 0
    int subTotal = 0
    roman.find(romanMatcher) { whole, subMatch, toParse ->
        // We've matched valid looking input. Try to parse it.
        matched = true
        if (subMatch) {
            // We have a sub roman number (in parens)
            def subParse = subMatch[1..-2]
            if (subParse.endsWith("I")) {
                // Invalid roman numeral
                throw new IllegalArgumentException(
                    "Invalid roman numeral ${subParse}, multiplied values " +
                    "cannot end in I")                
            }
            // Parse the sub roman numeral
            subTotal = romanNumeralsToInt(
                subParse, multiplier * 1000)            
        }
        romanDigitsReverse.each { romanToCheck ->
            def digitValue = value[romanToCheck]
            int repeatRomanCount = 0
            while (toParse.startsWith(romanToCheck)) {
                repeatRomanCount++
                if ((romanToCheck == "M" && repeatRomanCount > 4) || 
                    (romanToCheck != "M" && repeatRomanCount > 3)) {
                    throw new IllegalArgumentException(
                        "Invalid roman numeral ${roman} only 3 same " +
                        "consecutive values allowed " + 
                        "(except M which may have 4)")
                }
                // Add to the running sum then consume the parsed digits
                // from the front of the roman numeral
                total += digitValue
                toParse = romanToCheck.size() < toParse.size() ? 
                    toParse[romanToCheck.size() .. -1] : ""
            }
        }
        if (toParse) {
            // We still have unparsed roman numerals. This means a larger
            // value follows a smaller value
            throw new IllegalArgumentException(
                "Invalid roman numeral ${roman}, large value " +
                "found after smaller value")
        }
    }
    if (!matched) {
        // Regex didn't match. Bad roman numeral string
        throw new IllegalArgumentException(
            "Invalid roman numeral ${roman}, invalid digits?")
    }
    (total * multiplier) + subTotal
}

/**
 * Convert an int to a roman numeral. This does not yet handle
 * values > 4999. Really no error handling is necessary since
 * intValues is always well formed.
 * @param intValue the number to convert to roman numerals
 * @return number as roman numerals after conversion
 */
def intToRomanNumerals(intValue) {
    def parts = [:]
    StringBuilder result = new StringBuilder()
    for (romanDigit in romanDigitsReverse) {
        // Starting wiht M
        def scale = value[romanDigit]
        if (intValue >= scale) {
            parts[romanDigit] = Math.floor(intValue / scale) as Integer
            intValue -= parts[romanDigit] * scale
        }
    }
    /*
    if (parts.M && parts.M > 4) {
        // We don't yet handle values > 4999. I believe this is where
        // we would handle that, perhaps?
        parts.M = 4
    }
    */
    // Convert the parts into Roman Numeral string
    parts.collect { k, v -> k*v }.join("")
}
