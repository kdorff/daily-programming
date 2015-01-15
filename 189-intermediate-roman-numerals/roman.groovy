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

// Roman numeral digits to their values
value = [
    'I':  1,    
    'IV': 4,    'V': 5,    
    'IX': 9,    'X': 10,
    'XL': 40,   'L': 50,   
    'XC': 90,   'C': 100,
    'CD': 400,  'D': 500,
    'CM': 900,  'M': 1000,
]
romanDigitsReverse = (value.keySet() as List).reverse()

// Format for roman numerals including the
// () notation where values are multiplied * 1000 for
// each nesting depth.
romanMatcher = ~/^(\([()IVXLCDM]+\))?([IVXLCDM]*)$/

// Input data with expected value
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
    // Convert the roman romanValue 
    try {
        int calculatedValue = romanNumeralsToInt(romanValue, 1)
        print "${romanValue} = ${calculatedValue} expecting ${intValue} "
        println "(${calculatedValue == intValue ? true : false})"
    } catch (IllegalArgumentException e) {        
        print "${roman} ${e.message} "
        println "(${intValue == -1 ? true : false})"
    }
    /*
    if (intValue != -1) {
        String calculatedValue = intToRomanNumerals(intValue)
        print "${intValue} = ${calculatedValue} expecting ${romanValue} "
        println "(${calculatedValue == romanValue ? true : false})" 
    }
    */
}

/**
 * Convert an int to a roman numeral. This does not yet handle
 * values > 4999.
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
        } else {
            parts[romanDigit] = 0
        }
    }
    println parts
    if (parts['M'] > 3) {
        parts['M'] = 3
    }
    splitToRoman(parts)
}

def splitToRoman(parts) {
    def roman = new StringBuilder()
    parts.each { digit, count ->
      (0..<count).each {
          roman << digit
      }
    }
    roman.toString()
}


def romanNumeralsToInt(roman, multiplier) throws IllegalArgumentException {
    def matched = false
    int total = 0
    int subTotal = 0
    roman.find(romanMatcher) { whole, subMatch, toParse ->
        matched = true
        if (subMatch) {
            def subParse = subMatch[1..-2]
            if (subParse.endsWith("I")) {
                throw new IllegalArgumentException(
                    "Invalid roman numeral ${subParse}, multiplied values " +
                    "cannot end in I")                
            }
            subTotal = romanNumeralsToInt(
                // Sub-parse without enclosing parens
                subParse, multiplier * 1000)            
        }
        romanDigitsReverse.each { romanToCheck ->
            def digitValue = value[romanToCheck]
            if (toParse.startsWith(romanToCheck)) {
                total += digitValue
                toParse = toParse.substring(romanToCheck.size())
            }
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
 * Parse a roman numeral string to an int.
 * @param the roman numerals
 * @param multiplier the multiplier 
 * @return roman numerals (roman) as int
 */
def xromanNumeralsToInt(roman, multiplier) throws IllegalArgumentException {
    int total = 0
    int numSmaller
    int largestValue = 0
    int subTotal = 0
    boolean matched = false
    int consecutiveValues = 0
    String lastValue = null
    roman.find(romanMatcher) { whole, subMatch, toParse ->
        // Regex match for roman numeral. Doesn't mean it's valid
        // but it contains the rights characters
        matched = true
        if (subMatch) {
            // We have an embedded roman number (within ()'s)
            // Recurse to parse the new value at a 1000x multiplier
            def subParse = subMatch[1..-2]
            if (subParse.endsWith("I")) {
                throw new IllegalArgumentException(
                    "Invalid roman numeral ${subParse}, multiplied values " +
                    "cannot end in I")                
            }
            subTotal = romanNumeralsToInt(
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
                        "Invalid roman numeral ${roman}, too many smaller " +
                        "values after a larger value (only one allowed).")
                }
            } else {
                // We have a larger or equal value than before. Add to total
                total += currentValue
                numSmaller = 0
            }
            if (lastValue && lastValue == letter) {
                consecutiveValues++
                if ((letter == "M" && consecutiveValues > 3) || 
                    (letter != "M" && consecutiveValues > 2)) {
                    throw new IllegalArgumentException(
                        "Invalid roman numeral ${roman} only 3 same " +
                        "consecutive values allowed " + 
                        "(except M which may have 4)")
                }
            } else {
                consecutiveValues = 0
            }
            if (largestValue < currentValue) {
                // New largest value
                largestValue = currentValue
            }
            lastValue = letter
        }
    } 
    if (!matched) {
        // Regex didn't match. Bad roman numeral string
        throw new IllegalArgumentException(
            "Invalid roman numeral ${roman}, invalid digits?")
    }
    (total * multiplier) + subTotal
}
