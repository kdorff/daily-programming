/**
 * 174 Easy Thue-Morse Sequences
 * http://www.reddit.com/r/dailyprogrammer/comments/2cld8m/8042014_challenge_174_easy_thuemorse_sequences/
 */

/**
 * This version uses a new String for each iteration, although each new
 * iteration is actually built with a new StringBuilder for performance.
 * Using no special memory configuration, this can generate 27 iterations
 * before running out of memory. The 27th iteration took 28.9 seconds.
 */

def INIT_VALUE = '0'
String value = INIT_VALUE
int iteration = 1
println "${iteration++}:${value}"
while (true) {
    long start = System.currentTimeMillis()
    value = thueMorse(value)
    println "${iteration++} took ${System.currentTimeMillis() - start}ms"
}

def thueMorse(String input) {
    def result = new StringBuilder(input)
    input.each { c ->
        result << (c == '0' ? '1' : '0')
    }
    result.toString()
}
