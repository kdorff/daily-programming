/**
 * Rövarspråket is not very complicated: you take an ordinary word and 
 * * replace the consonants with the consonant doubled and with an "o" in between. 
 * * Vowels are left intact. 
 */

def vowels = ['A', 'E', 'I', 'O', 'U', 'Y', 'Å', 'Ä', 'Ö']
def inputs = [
    /Jag talar Rövarspråket!/ : /Jojagog totalolaror Rorövovarorsospoproråkoketot!/,
    /I'm speaking Robber's language!/ : /I'mom sospopeakokinongog Rorobobboberor'sos lolanongoguagoge!/,
    /Tre Kronor är världens bästa ishockeylag./ : /Totrore Kokrorononoror äror vovärorloldodenonsos bobäsostota isoshohocockokeylolagog./,
    /Vår kung är coolare än er kung./ : /Vovåror kokunongog äror cocoololarore änon eror kokunongog./,
]

inputs.each { input, expected ->
    def result = new StringBuilder()
    input.each { c ->
        if (vowels.contains(c.toUpperCase())) {
            // Vowels unchanged
            result << c
        } else if (Character.isAlphabetic((int) c)) {
            // Letters but non-vowels, letter-o-letter
            result << c << 'o' << c.toLowerCase()
        } else {
            // All other characters
            result << c
        }
    }
    println result
    assert result.toString() == expected
}