/**
 * Reddit Daily Programmer Challenges (both in this program)
 *
 * http://www.reddit.com/r/dailyprogrammer/comments/2tr6yn/2015126_challenge_199_bank_number_banners_pt_1/
 * http://www.reddit.com/r/dailyprogrammer/comments/2u0fyx/2015126_challenge_199_bank_number_banners_pt_2/
 */

/**
 * Unit test class to test that the codec can round-trip
 * digits -> banner -> digits
 */
class Banner extends GroovyTestCase {
    void testCalc() {
        def bannerCodec = new BannerCodec()
        ['1357924680', '8675309'].each { toConvert ->
            // println "Bannering ${toConvert}"
            def banner = bannerCodec.digitsToBanner(toConvert)
            // println banner
            def decoded = bannerCodec.decodeBanner(banner)
            // println "Debannered to ${decoded}"
            assert decoded == toConvert
            // println "---"
        }
    }
}

/**
 * Digits to banner to digits codec.
 */
class BannerCodec {
    /**
     * Map of the visual representation of the banner digits.
     */
    def digits = [0: [' _ ', '| |', '|_|','   '],
                  1: ['   ', '  |', '  |','   '],
                  2: [' _ ', ' _|', '|_ ','   '],
                  3: [' _ ', ' _|', ' _|','   '],
                  4: ['   ', '|_|', '  |','   '],
                  5: [' _ ', '|_ ', ' _|','   '],
                  6: [' _ ', '|_ ', '|_|','   '],
                  7: [' _ ', '  |', '  |','   '],
                  8: [' _ ', '|_|', '|_|','   '],
                  9: [' _ ', '|_|', ' _|','   ']]

    /**
     * Convert a string of digits, 0-9, to a banner.
     * @param digitsStr string of digits, 0-9, any length.
     * @return String the banner as a string, lines of the banner separated
     * by newlines.
     */
    def digitsToBanner(digitsStr) {
        def result = []
        // Each row of the banner output
        (0 ..< 4).each { digitRow ->
            result << ""
            digitsStr.each { digitStr ->
                // Each digit
                assert digitStr >= '0' && digitStr <= '9'
                def digit = digitStr as Integer
                result[digitRow] += digits[digit][digitRow]
            }
        }
        result.join('\n')
    }

    /**
     * Given a banner string, decode to a string of digits 0-9.
     * @param banner as a string, line of the banner separated by newlines
     * @return String the banner decoded to a string of digits 0-9.
     */
    String decodeBanner(String banner) {
        def bannerLines = splitAndValidateBanner(banner)
        def digitLists = bannerLinesToDigits(bannerLines)
        digitListsToDigits(digitLists)
    }

    /**
     * Given a banner stored in a string, lines separated by newlines,
     * validate the content of the banner and split it into a list of
     * banner lines, one line per row of the banner.
     * @param banner as a string, line of the banner separated by newlines
     * @return List[String] banner as 4 strings in a list (no newlines)
     */
    List<String> splitAndValidateBanner(String banner) {
        // Split
        def bannerLines = banner.split('[\n\r]')

        // Input sanity checks
        assert bannerLines.size() == 4    
        int size = bannerLines[0].size()
        assert (size % 3) == 0
        (1 ..< 4).each { i ->
            assert bannerLines[i].size() == size
        }
        bannerLines
    }

    /**
     * Given banner lines (such as from splitAndValidateBanner) split them
     * into a list. The list contains one entry per digit in the bannered
     * numbers. Each list entry contains a list of 4 strings, which are the
     * four lines for that digit in the banner, similar to what is stored
     * in the value portion of the class level map "digits".
     * @param bannerLines List[String] banner as 4 strings in a list
     * @return List of List[String] of the ascii making up each digit
     */
    def bannerLinesToDigits(bannerLines) {
        // Split into digits
        int size = bannerLines[0].size()
        def numDigits = (size / 3) as Integer
        def digitLists = []
        (0 ..< numDigits).each {
            digitLists << []
        }
        (0 ..< 4).each { r ->
            (0 ..< numDigits).each { i ->
                digitLists[i] << bannerLines[r][(i * 3) .. ((i + 1) * 3) - 1]
            }
        }
        digitLists
    }

    /**
     * Given a List of List[String] (from bannerLinesToDigits()), convert
     * to a single String of digits.
     * @param digitLists List of List[String] of the ascii making up each digit
     * @return the String of digits
     */
    def digitListsToDigits(digitLists) {
        def result = new StringBuilder()
        digitLists.each { digitList ->
            def found = false
            for (mapEntry in digits.entrySet()) {
                def num = mapEntry.key
                def digitListToCheck = mapEntry.value
                if (digitListToCheck == digitList) {
                    found = true
                    result << "${num}"
                    break
                }
            }
            assert found == true
        }
        result.toString()
    }
}