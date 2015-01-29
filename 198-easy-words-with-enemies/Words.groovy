/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2syz7y/20150119_challenge_198_easy_words_with_enemies/
 *
 * Code to play words-with-enemies. Not a full game, but just compare two words
 * and determine the winner.
 *
 * Author: Kevin Dorff
 */

/**
 * Test for the WordsWithEnemiesEngine class.
 */
class Words extends GroovyTestCase {
    void testExamples() {
        def engine = new WordsWithEnemiesEngine()
        // Negatives the left wins, positives the right wins, 0 means tie.
        assert engine.compare('because', 'cause') == -2
        assert engine.compare('hello', 'below') == 0
        assert engine.compare('hit', 'miss') == 1
        assert engine.compare('rekt', 'pwn') == -1
        assert engine.compare('combo', 'jumbo') == 0
        assert engine.compare('critical', 'optical') == -1
        assert engine.compare('isoenzyme', 'apoenzyme') == 0
        assert engine.compare('tribesman', 'brainstem') == 0
        assert engine.compare('blames', 'nimble') == 0
        assert engine.compare('yakuza', 'wizard') == 0
        assert engine.compare('longbow', 'blowup') == -1
    }
}

/**
 * Comparison engine for Words with Enemies, to compare two words
 * and determine the winner.
 */
class WordsWithEnemiesEngine {
    /**
     * Compare a left word and a right word. Any letters that they have 1:1 in
     * common are ignored/eliminated and the side with more remaining letters
     * wins.
     * Returns <0 if leftWordStr wins, 0 for a tie, >0 if rightWordStr wins.
     */
    def compare(String leftWordStr, String rightWordStr) {
        // Convert leftWordStr and rightWordStr from strings to a list of letters
        def leftWordList = leftWordStr.toLowerCase().each { it }.collect()
        def rightWordList = rightWordStr.toLowerCase().each { it }.collect()
        // Remove letters in leftWordList from rightWordList and mark them
        // for deletion from leftWordList. I'm using leftWordList.collect() to
        // make a new list so leftWordList.remove(letter) won't throw
        // a concurrent modification exception.
        leftWordList.collect().each { letter ->
            if (rightWordList.remove(letter)) {
                // The letter was in rightWordList so
                // remove it from leftWordList, too.
                leftWordList.remove(letter)
            }
        }
        // Return the difference
        rightWordList.size() - leftWordList.size()
    }
}