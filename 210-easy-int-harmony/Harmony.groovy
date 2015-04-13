/**
 * Reddit Daily Programmer, 210 (easy) intHarmoy.
 * http://www.reddit.com/r/dailyprogrammer/comments/32goj8/20150413_challenge_210_easy_intharmonycom/
 */

["20 65515",
 "32000 101",
 "42000 42",
 "13 12345",
 "9999 9999",
 "8008 37331",
 "54311 2",
 "3120 34335"].each { String pair ->
    // Process all provided inputs
    harmonize(pair.split(' ').collect {it as int})
}

/**
 * Obtain the intHarmony score and avoids for the two provided ints.
 * @param ints List[Integer] contains two ints
 */
def harmonize(List ints) {
    assert ints.size() == 2
    // Obatin the binary for the two provided ints, left padded to 32 bits.
    def (int1, int2) = ints
    def (bint1, bint2) = [Long.toBinaryString(int1).padLeft(32, '0'), 
                          Long.toBinaryString(int2).padLeft(32, '0')]
    // Compute the inverse for the provided numbers
    def (inv1, inv2) = [inverse(bint1), inverse(bint2)]
    // Calculate the score for the provided numbers
    def score = score(bint1, bint2)
    // Output
    println "${score}% Compatibility"
    println "${int1} should avoid ${inv1}"
    println "${int2} should avoid ${inv2}"
    println ""
}

/**
 * Given an arbitrar binary number stored in a string,
 * retrn the inverse number as a long (all bits flipped).
 * @param bin the binary number stored in a string
 * @return the long number which is the inverse of bin
 */
long inverse(String bin) {
    def result = new StringBuilder()
    bin.each { c ->
        if (c == '0') {
            result << '1'
        } else {
            result << '0'
        }
    }
    Long.parseLong(result.toString(), 2)
}

/**
 * Given two binary numbers stored in Strings, each of the same number
 * of binary digits, score them (count of positions in bin1 that are the same
 * in bin2). Both bin1 and bin2 must be of the same length.
 * @param bin1 the first binary number
 * @param bin2 the second binary number
 * @return the int score (0 to 100).
 */
int score(String bin1, String bin2) {
    int score = 0
    assert bin1.size() == bin2.size()
    bin1.eachWithIndex { bin1char, index ->
        bin2char = bin2[index]
        if (bin1char == bin2char) {
            score++
        }
    }
    (int) Math.round((score / bin1.size()) * 100)
}