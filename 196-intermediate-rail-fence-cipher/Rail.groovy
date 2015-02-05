/**
 * Class to exercise RailCryptor rail encryption/decryption.
 */
class Rail extends GroovyTestCase {

    def word = "I Love kittens."

    /**
     * Test with a rail of -1.
     */
    void testNeg1() {
        shouldFail {
            performTestOnN(-1, word)
        }
    }

    /**
     * Test with a rail of 0.
     */
    void test0() {
        shouldFail {
            performTestOnN(0, word)
        }
    }

    /**
     * Test with a rail of 1.
     */
    void test1() {
        shouldFail {
            performTestOnN(1, word)
        }
    }

    /**
     * Test with a rail of 2.
     */
    void test2() {
        performTestOnN(2, word)
    }

    /**
     * Test with a rail of 3.
     */
    void test3() {
        performTestOnN(3, word)
    }

    /**
     * Test with a rail of 4.
     */
    void test4() {
        performTestOnN(4, word)
    }

    /**
     * Test with a rail of 5.
     */
    void test5() {
        performTestOnN(5, word)
    }

    def performTestOnN(int railHeight, String word) {
        def crypt = new RailCryptor(railHeight, word.size())
        def encrypted = crypt.encrypt(word)
        def decrypted = crypt.decrypt(encrypted)
        assert word == decrypted        
    }
}

/**
 * The Rail encryptor/decryptor class.
 * This could be optimized to note require the index grid and to not need
 * to step into every cell of the index grid when encrypting/decrypting,
 * but this works.
 */
class RailCryptor {
    // Integer 2d grid to store nulls or indexes
    def encRail
    // The height for this rail cryptor
    def railHeight
    // The width of the word RailCryptor will work with
    def railWidth

    /**
     * Contructor a RailCryptor for the rail height and word width.
     */
    public RailCryptor(int height, int width) {
        assert height > 1
        this.railHeight = height
        this.railWidth = width
        encRail = new Integer[railHeight][railWidth]
        int r = 0
        int rInc = 1
        // Build the Integer[][] for this RailCryptor, with indexes
        (0 ..< railWidth).each { i ->
            encRail[r][i] = i
            if (i > 0 && (r == 0 || r == railHeight - 1)) {
                rInc *= -1
            }
            r += rInc
        } 
    }

    /**
     * Encrypt the word using the rail encryptor at a specific rail height.
     */
    def encrypt(String word) {
        def result = ""        
        (0 ..< railHeight).each { r ->
            (0 ..< railWidth).each { c ->
                if (encRail[r][c] != null) {
                    result += word[encRail[r][c]]
                }
            }
        }
        result
    }

    /**
     * Encrypt the word using the rail encryptor at a specific word length.
     */
    def decrypt(String word) {
        def result = new StringBuilder(word)
        int i = 0
        (0 ..< railHeight).each { r ->
            (0 ..< railWidth).each { c ->
                if (encRail[r][c] != null) {
                    def pos = encRail[r][c]
                    //println "r=${r} c=${c} i=${i} pos=${pos}"
                    result[pos..pos] = word[i++]
                }
            }
        }
        result.toString()
    }
}

/*

Some sample rail grids with indexes to oberve how the
system works:

0 2 4 6 8
 1 3 5 7

0   4   8
 1 3 5 7
  2   6

I   V   I   N
 _ O E K T E S
  L   _   T   .

0     6     2
 1   5 7   1
  2 4   8 0
   3     9

0       8       6
 1     7 9     5
  2   6   0   4
   3 5     1 3
    4       2

0 1 2 3 4
 5 6 7 8

0   1   2
 3 4 5 6
  7   8

0     1     2
 3   4 5   6
  7 8   9 0
   1     2

0       1       2
 3     4 5     6
  7   8   9   0
   1 2     3 4
    5       6

*/