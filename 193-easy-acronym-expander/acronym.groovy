translate = [:]
translate[acronymToPattern('lol')] = 'laugh out loud'
translate[acronymToPattern('dw')]  = 'don\'t worry'
translate[acronymToPattern('hf')]  = 'have fun'
translate[acronymToPattern('gg')]  = 'good game'
translate[acronymToPattern('brb')] = 'be right back'
translate[acronymToPattern('g2g')] = 'got to go'
translate[acronymToPattern('wtf')] = 'what the fuck'
translate[acronymToPattern('wp')]  = 'well played'
translate[acronymToPattern('gl')]  = 'good luck'
translate[acronymToPattern('imo')] = 'in my opinion'

def inputs = [
    'wtf that was unfair' : 'what the fuck that was unfair',
    'gl all hf' : 'good luck all have fun',
    'imo that was wp. Anyway I\'ve g2g' : 'in my opinion that was well played. Anyway I\'ve got to go',
]

inputs.each { input, expected ->
    def decoded = decode(input)
    println "${input} -> ${decoded} (${decoded == expected})"
}

def acronymToPattern(acronym) {
    ~/\b${acronym}\b/
}

def decode(input) {
    def output = input
    translate.each { pattern, to ->
        output = output.replaceAll(pattern, to)
    }
    output
}