/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2s7ezp/20150112_challenge_197_easy_isbn_validator/
 * Code to validate ISBN numbers.
 *
 * Author: Kevin Dorff
 */

/**
 * This is the default class for this script. Test validating
 * an ISBN.
 */
class ISBN extends GroovyTestCase {
    void testCalc() {
        assert true == ISBNValidator.validate("0-7475-3269-9")
    }

    void testCalcFailWrongCheck() {
        assert false == ISBNValidator.validate("0-7475-3269-X")
    }

    void testCalcWrongLength() {
        assert false == ISBNValidator.validate("0-7475-3269")
    }

    void testCalcBadDigit() {
        assert false == ISBNValidator.validate("0-7475-326X-9")
    }
}

/**
 * Class to validate 10 digit ISBN numbers such as
 * "0-7475-3269-9".
 */
class ISBNValidator {
    // ISBN must match this pattern
    def static isbnPattern = 
        ~/^(\d)-(\d)(\d)(\d)(\d)-(\d)(\d)(\d)(\d)-(\d|X)$/

    /**
     * Validate a ten digit ISBN value.
     * @param isbn the isbn to validate
     * @return true if the ISBN validates, otherwise false
     */
    def static validate(String isbn) {
        def validates = false
        def matcher =  isbn =~ isbnPattern
        if (matcher.matches()) {
            // matcher[0][0] is the whole match, the
            // rest of the elements of matcher[0][n]
            // are the capture groups, so we will
            // skip matcher[0][0] in the following
            def digits = matcher[0][1..-1].collect {
                it == 'X' ? 10 : it as Integer
            }
            int sum = 0
            int multiplier = 10
            digits.eachWithIndex { v, i ->
                sum += (10 - i) * v
            }
            validates = sum % 11 == 0
        }
        validates
    }
}