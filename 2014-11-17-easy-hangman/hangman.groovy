/**
 * Reddit daily challenget 189-easy
 * http://www.reddit.com/r/dailyprogrammer/comments/2mlfxp/20141117_challenge_189_easy_hangman/
 */

/* Kick it off */
new Hangman().startGames()

/**
 * Class to play hangman.
 */
class Hangman {
    def rand = new Random()
    // Acceptable levels
    def levels = ['h': [min: 3, max: 5], 
                  'm': [min: 5, max: 7], 
                  'e': [min: 7, max: 99],
                  'q': null]
    // Input from user
    def input = new BufferedReader(new InputStreamReader(System.in))
    // Our dictionary
    def words = []
    // How man times user can fail
    def numTries = 6

    // The word the player is guessing
    def word
    // Correctly guessed letters
    def foundLetters = []
    // Incorrectly guessed letters
    def missedLetters = []
    // All guessed letters
    def guesses = []
    // The level the user is playing on (e, m, h, q)
    def level
    // When we draw the graphic, the number of missed and the char to draw
    def incorrectToSymbol = [1: 'O', 2: '|', 3: '-', 4: '-', 5: '/', 6: '\\']
    // The graphic for gameplay
    def graphic = [
        "---------------",
        "      |       |",
        "      1       |",
        "   3332444    |",
        "      2       |",
        "      2       |",
        "     5 6      |",
        "    5   6     |",
        "              |",
        "             /|",
        "----------------"]

    /**
     * Constructor
     */
    public Hangman() {
        readWords('/usr/share/dict/words')
    }

    /**
     * Read the words from the dictionary
     */
    def readWords(dict) {
        new File(dict).eachLine { startWord ->
            startWord = startWord.trim().toLowerCase()
            // Strip punctuation
            def word = startWord.grep { it.matches('[a-z]') }.join('')
            if (word.size() >= 3) {
                words << word
            }
        }
    }

    /**
     * Primary method to start games of hangman.
     */
    def startGames() {
        while (true) {
            level = getLevel()
            if (level == 'q') {
                break
            }
            word = randomWord()
            playGame()
        }
    }

     /**
      * Read the level from the keyboard.
      * @return the level (e, m, h, q)
      */
     def getLevel() {
        def level
        while (true) {
            def levelsSet = levels.keySet()
            print "Easy, medium, hard, or quit (${levelsSet.join(', ')})? : "
            def input = input.readLine()
            if (input) {
                input = input.substring(0, 1).toLowerCase()
                if (levelsSet.contains(input)) {
                    level = input
                    break
                }
            }
        }
        level
     }

     /**
      * Read the guess from the keyboard.
      * Will only return valid new guesses.
      * @return the single letter guess
      */
     def getGuess() {
        def guess
        while (true) {
            print "Guess a letter : "
            def input = input.readLine()
            if (input) {
                input = input.substring(0, 1).toLowerCase()
                if (input < 'a' || input > 'z') {
                    println "Only a-z are acceptable."
                } else if (guesses.contains(input)) {
                    println "You've already guessed that letter"
                } else {
                    guess = input
                    guesses << guess
                    break
                }
            }
        }
        guess
     }

     /**
      * Start a new game of hangman
      */
     def playGame() {
        guesses.clear()
        foundLetters.clear()
        missedLetters.clear()
        def complete = false
        while (!complete) {
            complete = displayWord()
            if (!complete) {
                // Word hasn't been solved
                if (missedLetters.size() < numTries) {
                    // More tries available, get a guess
                    def guess = getGuess()
                    guesses << guess
                    if (word.indexOf(guess) != -1) {
                        foundLetters << guess
                    } else {
                        missedLetters << guess
                    }
                } else {
                    // Out of tries
                    println "You lost! The word was ${word}"
                    complete = true
                }
            } else {
                println "You won!"
            }
        }
     }

     /**
      * Display the game graphic and guess details.
      * @return if the word has been guessed 100%
      */
     def displayWord() {
        // Output the graphic
        graphic.each { line ->
            line.each { letter ->
                if (letter >= '0' && letter <= '6') {
                    def number = letter as Integer
                    if (number <= missedLetters.size()) {
                        print incorrectToSymbol[number]
                    } else {
                        print ' '
                    }
                } else {
                    print letter
                }
            }
            println ""
        }

        // Report on missed letters and tries remaining
        if (missedLetters.size() > 0) {
            println "Incorrectly guessed: ${missedLetters.join(', ')}"
        } else {
            println "You have no incorrect guesses, yet."
        }
        println "You have ${numTries - missedLetters.size()} " +
                "incorrect guesses left"
        print 'The word: '
        def complete = true
        // Output word with non guessed letters as _
        word.each { letter ->
            if (guesses.contains(letter)) {
                print letter
            } else {
                print '_'
                complete = false
            }
            print ' '
        }
        println ""
        // Report if the word is been 100% guessed
        complete
     }

     /**
      * Select a random word from the dictionary such that it
      * meets our definition of the selected difficulty.
      * @return random word from dictionary for level
      */
     def randomWord() {
        def word
        while (true) {
            // Keep looking until the word meets
            // the difficulty specifiations
            word = words[rand.nextInt(words.size)]
            def wordLen = word.size()
            def constraints = levels[level]
            if (wordLen >= constraints.min && wordLen <= constraints.max) {
                break
            }
        }
        word
     }
}