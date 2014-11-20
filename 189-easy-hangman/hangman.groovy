/**
 * Reddit daily challenget 189-easy
 * http://www.reddit.com/r/dailyprogrammer/comments/2mlfxp/20141117_challenge_189_easy_hangman/
 *
 * We all know the classic game hangman, today we'll be making it. With 
 * the wonderful bonus that we are programmers and we can make it as 
 * hard or as easy as we want. here is a wordlist to use if you don't 
 * already have one. That wordlist comprises of words spanning 3 - 15+ 
 * letter words in length so there is plenty of scope to make this 
 * interesting!
 *
 * Rules
 * ---------------------------
 * For those that don't know the rules of hangman, it's quite simple.
 * There is 1 player and another person (in this case a computer) that 
 * randomly chooses a word and marks correct/incorrect guesses.
 * The steps of a game go as follows:
 * * Computer chooses a word from a predefined list of words
 * * The word is then populated with underscores in place of where the 
 *   letters should. ('hello' would be '_ _ _ _ _')
 * * Player then guesses if a word from the alphabet [a-z] is in that word
 * * If that letter is in the word, the computer replaces all occurences 
 *   of '_' with the correct letter
 * * If that letter is NOT in the word, the computer draws part of the 
 *   gallow and eventually all of the hangman until he is hung (see here 
 *   for additional clarification)
 *
 * This carries on until either
 * * The player has correctly guessed the word without getting hung
 * * The player has been hung
 *
 * ---------------------------
 * Formal inputs and outputs:
 * input description
 * ---------------------------
 * Apart from providing a wordlist, we should be able to choose a 
 * difficulty to filter our words down further. For example, hard 
 * could provide 3-5 letter words, medium 5-7, and easy could be 
 * anything above and beyond!
 * On input, you should enter a difficulty you wish to play in.
 * ---------------------------
 * output description
 * ---------------------------
 * The output will occur in steps as it is a turn based game. The 
 * final condition is either win, or lose.
 */

/* Kick it off */
new Hangman().startGames()

/**
 * Difficulties enum.
 */
enum Difficulty {
    e(7, 99),
    m(5, 7),
    h(3, 5),
    q(0, 0)
    private int minLetters
    private int maxLetters
    List<String> words

    /**
     * Construct the enum.
     * @param minLetters the minimum number of word letters for this level
     * @param minLetters the maximum number of word letters for this level
     */
    Difficulty(int minLetters, int maxLetters) {
        this.minLetters = minLetters
        this.maxLetters = maxLetters
        words = []
    }

    /**
     * Add a word for the specified difficulty only if it meets
     * requirements.
     * @param word the word to try to add to the difficulty
     * It won't be added if it doesn't meet the level's requirements
     * for word length.
     */
    void addWord(String word) {
        int wordLen = word.size()
        if (wordLen >= minLetters && wordLen <= maxLetters) {
            words << word
        }
    }

    /**
     * Obtain the next word for this level. Won't repeat words.
     * @param rand a random number generator
     * @param removeFromList if true, the word will be removed from the list
     * so it won't be offered again until the program is restarted.
     * @return the next word or null if a new word couldn't be found
     */
     String nextWord(Random rand, boolean removeFromList) {
        def word
        if (words) {
            word = words[rand.nextInt(words.size())]
            if (removeFromList) {
                words.remove(word)
            }
        }
        word
     }
}

/**
 * Class to play hangman.
 */
class Hangman {
    Random rand = new Random()
    // Input from user
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in))
    // Our dictionary
    List<String> words = []
    // List of played words so we don't repeat a word
    List<String> playedWords = []
    // How man times user can fail
    int numTries = 6

    // When we draw the graphic, the number of missed and the char to draw
    Map<Integer, String> incorrectToSymbol = [
        1: 'O', 2: '|', 3: '-', 4: '-', 5: '/', 6: '\\'
    ]
    // The graphic for gameplay
    List<String> graphic = [
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
     * Constructor.
     */
    public Hangman() {
        // Found this alternative diction as the Mac dictionary has 
        // lots of bizarre words in it that nobody will ever guess.
        words = readWords('5000-common-english-words.txt')
    }

    /**
     * Read the words from the dictionary.
     * @param dict the dictionary file to read from, one word per line
     */
    List<String> readWords(dict) {
        List<String> words = []
        new File(dict).eachLine { startWord ->
            if (!startWord.startsWith("#")) {
                // Not a comment
                startWord = startWord.trim().toLowerCase()
                if (startWord) {
                    // Non-empty word, strip punctuation
                    String word = startWord.grep { it.matches('[a-z]') }.join('')
                    Difficulty.e.addWord(word)
                    Difficulty.m.addWord(word)
                    Difficulty.h.addWord(word)
                }
            }
        }
        words
    }

    /**
     * Primary method to start games of hangman.
     */
    void startGames() {
        boolean play = true
        while (play) {
            Difficulty level = getLevel()
            if (level == Difficulty.q) {
                play = false
            } else {
                String word = level.nextWord(rand, true)
                if (word)  {
                    playGame(word)
                } else {
                    // No more words to play with
                    println "No words at selected level. Try a different level."
                }
            }
        }
    }

     /**
      * Read the level from the keyboard.
      * @return the level (e, m, h, q)
      */
     Difficulty getLevel() {
        Difficulty level
        while (!level) {
            List<String> levelsSet = Difficulty.values()*.toString()
            print "Easy, medium, hard, or quit (${levelsSet.join(', ')})? : "
            String input = input.readLine()
            if (input) {
                input = input.substring(0, 1).toLowerCase()
                if (levelsSet.contains(input)) {
                    level = Difficulty.valueOf(input)
                    break
                }
            }
        }
        level
     }

     /**
      * Read the guess from the keyboard. Will only return valid new guesses.
      * @param guesses already made (good or bad) guesses
      * @return the single letter guess
      */
     String getGuess(List<String> guesses) {
        String guess
        while (!guess) {
            print "Guess a letter : "
            String input = input.readLine()
            if (input) {
                input = input.substring(0, 1).toLowerCase()
                if (input < 'a' || input > 'z') {
                    println "Only a-z are acceptable."
                } else if (guesses.contains(input)) {
                    println "You've already guessed that letter"
                } else {
                    guess = input
                    break
                }
            }
        }
        guess
     }

     /**
      * Start a new game of hangman
      * @param word the word to play the game with
      */
     void playGame(String word) {
        // Incorrectly guessed letters
        List<String> missedLetters = []
        // All guessed letters
        List<String> guesses = []

        boolean complete = false
        while (!complete) {
            complete = displayGameBoard(word, guesses, missedLetters)
            if (!complete) {
                // Word hasn't been solved
                if (missedLetters.size() < numTries) {
                    // More tries available, get a guess
                    String guess = getGuess(guesses)
                    guesses << guess
                    if (word.indexOf(guess) == -1) {
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
      * @param word the word the user is guessing
      * @param guesses all (good and bad) guesses made so far
      * @param missedLetters list of incorrect guesses
      * @return true if the word has been guessed 100% (game complete)
      */
     boolean displayGameBoard(
            String word, List<String> guesses, List<String>missedLetters) {
        // Output the graphic
        graphic.each { line ->
            line.each { letter ->
                if (letter >= '0' && letter <= '6') {
                    int number = letter as Integer
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
        int triesLeft = numTries - missedLetters.size()
        println "You have ${triesLeft} incorrect guesses left"
        print 'The word: '
        // Output word with non guessed letters as _
        boolean complete = true
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
}