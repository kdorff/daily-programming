def m = new MarkovChain()
m.train(['../189-easy-hangman/5000-common-english-words.txt'])
["examqle", "superczmputer", "horqqar", "axumilog"].each { word ->
    println "${word} spelling probably corrrect? ${m.checkWord(word)}"
}

class MarkovChain {
    def matrix = [:]
    def train(inputs) {
        inputs.each { input ->
            (new File(input)).eachLine { word ->
                if (word && !word.startsWith('#')) trainWord(word.trim().toLowerCase())
            }
        }
    }

    def trainWord(word) {
        if (word.size() > 1) {
            (0 .. word.size() - 2).each { i ->
                def fromLetter = word[i]
                def toLetter = word[i + 1]
                def key = "${fromLetter}:${toLetter}"
                matrix[key] = matrix[key] == null ? 1 : matrix[key] + 1
            }
        }
    }

    def checkWord(wordToCheck) {
        def result = true
        if (wordToCheck.size() > 1) {
            def word = wordToCheck.toLowerCase()
            (0 .. word.size() - 2).each { i ->
                def fromLetter = word[i]
                def toLetter = word[i + 1]
                def key = "${fromLetter}:${toLetter}"
                int markov = matrix[key] ?: 0
                println "${key} is ${markov}"
                if (markov == 0) {
                    result = false
                }
            }
        }
        result
    }
}