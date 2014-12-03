// Text to parse from
// http://www.gutenberg.org/cache/epub/47498/pg47498.txt
def bookLines = new File('pg47498.txt').readLines()

// Patterns for content of book start and end (ignore before and after)
def bookStart = "*** START"
def bookEnd = "*** END"

// If we are within the content of the book (we've found bookStart 
// but not yet found bookEnd)
def inBook = false

// Map to store the counts
def wordToCountMap = [:]

// Process the file
bookLines.each { line ->
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

// Sort to put most used words on the top of the list
wordToCountMap = wordToCountMap.sort { a, b -> b.value <=> a.value }

// Output
int num = 0
print "{"
wordToCountMap.each { word, count ->
    if (num++ > 0) {
        println ","
    }
    print "'${word}' : ${count}"
}
println "}"