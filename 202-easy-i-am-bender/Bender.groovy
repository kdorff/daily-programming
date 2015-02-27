def inputs = [
["011100000110110001100101011000",
 "010111001101100101001000000111",
 "010001100001011011000110101100",
 "100000011101000110111100100000",
 "0110110101100101"],
["011011000110100101100110011001",
 "010010000001110010011010010110",
 "011101101000011101000010000001",
 "101110011011110111011100100000",
 "011010010111001100100000011011",
 "000110111101101110011001010110",
 "110001111001"]
 ]

inputs.each { inputLines ->
    def input = inputLines.join("")
    println "${input} ->"
    println "   '${decode(input)}'"
}

String decode(String binaryStr) {
    def result = new StringBuilder()
    def size = binaryStr.size()
    assert size % 8 == 0
    int numChars = size / 8
    (0 ..< numChars).each { i ->
        def charBinaryStr = binaryStr[(i*8) ..< (i*8+8)]
        def charInt = Integer.parseInt(charBinaryStr, 2);
        result << (char) charInt
    }
    result
}