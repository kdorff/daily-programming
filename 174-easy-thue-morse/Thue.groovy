def INIT_VALUE = '0'
def value = new StringBuilder(INIT_VALUE)
int iteration = 1
println "${iteration++}:${value}"
while (true) {
    thueMorse(value)
    //println "${iteration++}:${value}"
    println iteration++
}

def thueMorse(StringBuilder input) {
    def size = input.size()
    for (int i = 0; i < size; i++) {
        input << (input[i] == '0' ? '1' : '0')
    }
}
