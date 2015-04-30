/**
 * 174 Easy Thue-Morse Sequences
 * http://www.reddit.com/r/dailyprogrammer/comments/2cld8m/8042014_challenge_174_easy_thuemorse_sequences/
 */

/**
 * This version uses a SINGLE StringBuilder and keeps building on
 * the same object for improved performance and reduced garbage collection.
 * Using no special memory configuration, this can generate 30 iterations
 * before running out of memory. The 30th iteration took 33.6 seconds.
 */

def INIT_VALUE = '0'
def value = new StringBuilder(INIT_VALUE)
int iteration = 1
println "${iteration++}:${value}"
while (true) {
    long start = System.currentTimeMillis()
    thueMorse(value)
    println "${iteration++} took ${System.currentTimeMillis() - start}ms"
}

def thueMorse(StringBuilder input) {
    def size = input.size()
    for (int i = 0; i < size; i++) {
        input << (input[i] == '0' ? '1' : '0')
    }
}
