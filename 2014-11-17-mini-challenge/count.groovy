def input = 'The quick brown fox jumps over the lazy dog and the sleeping cat early in the day.'
def counts = [:]
input.findAll(~/[a-zA-Z]/).collect { it.toLowerCase() }.each {
    counts[it] = counts[it] ? counts[it] + 1 : 1
}
counts.each { letter, number ->
    println "${letter} : ${number}"
}