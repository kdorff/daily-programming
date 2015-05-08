import java.util.regex.Pattern
import groovy.transform.ToString

// http://www.reddit.com/r/dailyprogrammer/comments/29zut0/772014_challenge_170_easy_blackjack_checker/

/**
 * Test inputs to exercise.
 */
List<String> inputs = [
// Example input #1
"""3
Alice: Ace of Diamonds, Ten of Clubs
Bob: Three of Hearts, Six of Spades, Seven of Spades
Chris: Ten of Hearts, Three of Diamonds, Jack of Clubs""",
// Input with tie score
"""4
Alice: Ace of Diamonds, Ten of Clubs
Bob: Three of Hearts, Six of Spades, Seven of Spades
Chris: Ten of Hearts, Three of Diamonds, Jack of Clubs,
Madge: Ten of Hearts, Three of Diamonds, Eight of Clubs""",
// Example input #2
"""4
Alice: Ace of Diamonds, Ten of Clubs
Bob: Three of Hearts, Six of Spades, Seven of Spades
Chris: Ten of Hearts, Three of Diamonds, Jack of Clubs
David: Two of Hearts, Three of Clubs, Three of Hearts, Five of Hearts, Six of Hearts""",
// Input with tie five card trick
"""5
Alice: Ace of Diamonds, Ten of Clubs
Bob: Three of Hearts, Six of Spades, Seven of Spades
Chris: Ten of Hearts, Three of Diamonds, Jack of Clubs
David: Two of Hearts, Three of Clubs, Three of Hearts, Five of Hearts, Six of Hearts
Madge: Two of Hearts, Three of Clubs, Three of Hearts, Five of Hearts, Four of Hearts""",
]

inputs.each { input ->
    // Try
    BlackjackGame game = BlackjackGameUtil.playGame(input).describe()
}

/**
 * Class to store a blackjack game including hands and the winners
 * and winning score.
 */
class BlackjackGame {
    /**
     * The blackjack hands for a single game.
     */
    List<Hand> hands
    int winnerScore = 0
    List<String> winners = []
    boolean fiveCardTrick

    /**
     * Output results.
     */
    def describe() {
        if (winners.size() > 1)  {
            print "Tie between ${winners.join(", ")}"
        } else {
            print "${winners[0]} has won"
        }
        if (fiveCardTrick) {
            println " with a 5-card trick!"
        } else {
            println " with a score of ${winnerScore}."
        }
    }
}

/**
 * Class to represent a single players hand.
 */
@ToString(includeNames=true)
class Hand {
    String playerName
    List<Card> cards
    int handValue
}

/**
 * Enum for the cards.
 */
enum Card {
    Two(2),     Three(3),   Four(4),
    Five(5),    Six(6),     Seven(7),
    Eight(8),   Nine(9),    Ten(10),
    Jack(10),   Queen(10),  King(10),
    Ace(1)

    int value
    private Card(int value) {
      this.value = value
   }
}

/**
 * Utility class to parse a game from ascii and score the game.
 */
class BlackjackGameUtil {
    /**
     * Create the pattern to find the cards in the input by knowing
     * what cards to expect, namely the keys for CARD_TO_VALUE.
     */
    static CARD_PATTERN 
    static {
        // Build a regex to find cards in the input
        def buildPattern = new StringBuilder('(')
        buildPattern << Card.values()*.toString().join('|')
        buildPattern << ')'
        CARD_PATTERN = Pattern.compile(buildPattern.toString())
    }

    /**
     * Given the ascii description of the current hand (for all players),
     * parse the input and determine the winners.
     * @param input the multi-line String that contains the input for the game
     * @param BlackjackGame the BlackjackGame object fully populated
     */
    static BlackjackGame playGame(String input) {
        def game = new BlackjackGame()
        parseGame(game, input)
        findWinners(game)
        game
    }

    /**
     * Parse the game input into the newly created game object.
     * @param game the game object
     * @param input the multi-line String that contains the input for the game
     */
    static void parseGame(BlackjackGame game, String input) {
        game.with {
            hands = []
            // Parse the hands from the input
            input.split('[\n\r]').eachWithIndex { line, i ->
                if (i) {
                    // Skip the forst line of input, not necessary
                    // For the rest of the lines, parse the input
                    game.hands << parseHand(line)
                }
            }
        }
    }

    /**
     * Find the winner(s) of the game. A five or more card hand <= 21
     * automatically wins. After that, the highest hand(s) <= 21 win.
     * @param game the game object
     */
    static void findWinners(BlackjackGame game) {
        // Score the game
        game.with {
            winnerScore = 0
            winners = []
            hands.each { hand ->
                // Inspect all of the hands
                // println hand
                def handScore = hand.handValue
                if (handScore <= 21) {
                    if (hand.cards.size() >= 5) {
                        if (!fiveCardTrick) {
                            // Previous winner was not a 5-card, replace
                            winners.clear()
                        }
                        fiveCardTrick = true
                        winners << hand.playerName
                        winnerScore = Math.max(handScore, winnerScore)
                    } else if (!fiveCardTrick && handScore >= winnerScore) {
                        if (handScore > winnerScore) {
                            // Remove the previous high scoring player as this
                            // score is higher than the previous
                            winners.clear()
                        }
                        winnerScore = handScore
                        winners << hand.playerName
                    }
                }
            }        
        }
    }

    /**
     * Parse a single player and the player's hand.
     * @param handDesc the players ascii name and hand description
     * example: 'Chris: Ten of Hearts, Three of Diamonds, Jack of Clubs'
     * @return Hand the hand for the player in question
     */
    static Hand parseHand(String handDesc) {
        Hand hand = new Hand()
        def parts = handDesc.split(':', 2)
        hand.playerName = parts[0]
        def cardDesc = parts[1]
        hand.cards = parseCards(cardDesc.trim())
        hand.handValue = calculateSum(hand.cards)
        hand
    }

    /**
     * Given a List[Card], return the highest sum those cards can represent,
     * for example, Aces will be optimally counted as 1 or 11.
     */
    static int calculateSum(List<Card> cards) {
        int sum = 0
        int numAces = 0
        cards.each { Card card ->
            if (card == Card.Ace) {
                numAces++
            }
            sum += card.value
        }
        (0 ..< numAces).each { i ->
            if (sum <= 11) {
                sum += 10
            }
        }
        sum
    }

    /**
     * Parse a single player's list of cards into List[Card].
     * @param cardsDesc the players list of cards.
     * example: 'Ten of Hearts, Three of Diamonds, Jack of Clubs'
     * @return List[Card] the players cards
     */
    static List<Card> parseCards(String cardsDesc) {
        def cards = []
        cardsDesc.findAll(CARD_PATTERN) { whole, cardName ->
            cards << Card.valueOf(cardName)
        }
        cards
    }
}