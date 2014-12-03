/**
 * Reddit Daily Programmer Challenge 191, Easy, Word Counting
 * http://www.reddit.com/r/dailyprogrammer/comments/2nynip/2014121_challenge_191_easy_word_counting/
 * 
 * You've recently taken an internship at an up and coming lingustic and natural
 * language centre. Unfortunately, as with real life, the professors have
 * allocated you the mundane task of counting every single word in a book and 
 * finding out how many occurences of each word there are.
 * 
 * To them, this task would take hours but they are unaware of your programming 
 * background (They really didn't assess the candidates much). Impress them with 
 * that word count by the end of the day and you're surely in for more smooth 
 * sailing.
 * 
 * Description
 * ------------------
 * Given a text file, count how many occurences of each word are present in that 
 * text file. To make it more interesting we'll be analyzing the free books 
 * offered by Project Gutenberg
 * The book I'm giving to you in this challenge is an illustrated monthly on 
 * birds. You're free to choose other books if you wish.
 * 
 * Input
 * ------------------
 * Pass your book through for processing
 * 
 * Output
 * ------------------
 * Output should consist of a key-value pair of the word and its word count.
 * 
 * Example Output
 * ------------------
 * {'the' : 56,
 * 'example' : 16,
 * 'blue-tit' : 4,
 * 'wings' : 75}
 * 
 * Clarifications
 * ------------------
 * For the sake of ease, you don't have to begin the word count when the book 
 * starts, you can just count all the words in that text file (including the 
 * boilerplate legal stuff put in by Gutenberg).
 * 
 * Bonus
 * ------------------
 * As a bonus, only extract the book's contents and nothing else.
 * 
 */

// Text to parse from
// http://www.gutenberg.org/cache/epub/47498/pg47498.txt
def inputs = ['pg47498.txt']

// Patterns for content of book start and end (ignore before and after)
def bookStart = "*** START"
def bookEnd = "*** END"

// Count the words within each input file (count for a word is for a single
// file not across files)
inputs.each { input ->
    println "Counting the words in the file ${input}:"
    def inBook = false

    // Map to store the counts
    def wordToCountMap = [:]

    // Process the file
    new File(input).eachLine { line ->
        if (line.startsWith(bookStart)) {
            inBook = true
        } else if (line.startsWith(bookEnd)) {
            inBook = false
        } else if (inBook) {
            line.findAll(~/[a-zA-Z]+/).each { word ->
                // Find all the words on the line and then add to count
                // ignoring the case of the word when making the counts
                def key = word.toLowerCase()
                wordToCountMap[key] = wordToCountMap[key] == null ? 1 :
                    wordToCountMap[key] + 1
            }
        }
    }
    outputResult(wordToCountMap)
}

/**
 * Output the result of the counting, sorted by freuqency.
 * @param wordToCountMap a map of word (lowercase) to the number of times
 * the word was found. No order implied.
 */
def outputResult(wordToCountMap) {
    // Sort to put most used words on the top of the list
    def sortedMap = wordToCountMap.sort { a, b -> b.value <=> a.value }

    // Output
    int num = 0
    print "{"
    sortedMap.each { word, count ->
        if (num++ > 0) {
            println ","
        }
        print "'${word}' : ${count}"
    }
    println "}"
}
