/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2er1v0/8272014_challenge_177_intermediate/
 * Code to convert a string to morse code on the screen AND output a .wav
 * file of the morse code.
 *
 * Author: Kevin Dorff
 */

def output = MorseUtil.stringToMorse("I like cats")
MorseUtil.write(new File("morse-output.wav"), output)

/**
 * Utility class to convert strings to morse code and output audio file.
 */
class MorseUtil {
	/**
	 * Map of upper case letters to morse code.
	 */
	static charToMorseCharsMap = [
      'A' : '.-',    'B' : '-...',  'C' : '-.-.',
      'D' : '-..',   'E' : '.',     'F' : '..-.',
      'G' : '--.',   'H' : '....',  'I' : '..',
      'J' : '.---',  'K' : '-.-',   'L' : '.-..',
      'M' : '--',    'N' : '-.',    'O' : '---',
      'P' : '--.-',  'Q' : '--.-',  'R' : '.-.',
      'S' : '...',   'T' : '-',     'U' : '..-',
      'V' : '...-',  'W' : '.--',   'X' : '-..-',
      'Y' : '-.--',  'Z' : '--..',  '1' : '.----',
      '2' : '..---', '3' : '...--', '4' : '....-',
      '5' : '.....', '6' : '-....', '7' : '--...',
      '8' : '---..', '9' : '----.', '0' : '-----',
      ' ' : ' ',
	]

	/**
	 * Map of morse code ./- characters to their object.
	 */
	static morseCharToObjsMap = [
		'.' : new MorseDot(),
		'-' : new MorseDash(),
		' ' : new MorseWordEnd(),
	]
	// Morse code end-of-char object.
	static morseCharEnd = new MorseCharEnd()

	/**
	 * Convert a string into a list of MorseCharacter objects.
	 * @param str the string to covnert to morse code
	 * @return list of MorseCharacter
	 */
	static List<MorseCharacter> stringToMorse(String str) {
		def result = []
		print "${str} -> "
		str.toUpperCase().each { c ->
			def morseChars = charToMorseCharsMap[c]
			if (morseChars) {
				if (morseChars == ' ') {
					print "/ "
				} else {
					print "${morseChars} "
				}
				morseChars.each { m ->
					def morseChar = morseCharToObjsMap[m]
					if (morseChar) {
						result << morseChar
					}
				}
				result << morseCharEnd
			}
		}
		println ""
		result
	}

	/**
	 * Write a List of MorseCharacter to a .wav file. This uses the class
	 * WavFile from A.Greensted, see:
	 * http://www.labbookpages.co.uk/audio/javaWavFiles.html
	 * @param outputFile the output .wav file to write to
	 * @param morse the List of MorseCharacter to write to the .wav file
	 */
	static void write(File outputFile, List<MorseCharacter> morse) {
		long totalFrameCount = morse*.totalDuration.sum()
		WavFile wavFile = WavFile.newWavFile(
			outputFile, 2, totalFrameCount, 16, MorseCharacter.sampleRate)
		morse.each { morseSymbol ->
			morseSymbol.write(wavFile)
		}
		wavFile.close()
	}
}

// From: http://en.wikipedia.org/wiki/Morse_code
// The duration of a dash is three times the duration of a dot. 
// Each dot or dash is followed by a short silence, equal to the dot duration. 
// The letters of a word are separated by a space equal to three dots (one dash), 
// and the words are separated by a space equal to seven dots

/**
 * A morse character and methods to deal with it. (MorseCharacter
 * represents "." timing, so MorseDot is an empty extension of MorseCharacter.)
 */
class MorseCharacter {
	static int sampleRate = 44100
	static long fileFrameCounter = 0
	int duration = 5000
	int postDuration = duration // Between morse ./-, duration of 1 . silence
	int hertzTone = 400
	int hertzSilence = 0
	double[][] buffer = null

	/**
	 * Get total duration, including tone plus post tone silence.
	 * @return total duration for this morse character.
	 */
	public int getTotalDuration() {
		duration + postDuration
	}

	/**
	 * Write the current morse character to the wav file.
	 * @param wavFile the WavFile object we are writing to
	 */
	public void write(wavFile) {
		def outputFrames = totalDuration
		if (buffer == null) {
			buffer = new double[2][outputFrames]
		}
		def bufferIndex = 0
		// Output the tone to buffer
		(0 ..< duration).each {
			double tone = Math.sin(2.0 * Math.PI * hertzTone * 
				fileFrameCounter / sampleRate)
			buffer[0][bufferIndex] = tone
			buffer[1][bufferIndex] = tone
			bufferIndex++
			fileFrameCounter++
		}
		// Output the silence to buffer
		(0 ..< postDuration).each {
			double tone = Math.sin(2.0 * Math.PI * hertzSilence * 
				fileFrameCounter / sampleRate)
			buffer[0][bufferIndex] = tone
			buffer[1][bufferIndex] = tone
			bufferIndex++
			fileFrameCounter++
		}
		// Write the buffer to the .wave file
		wavFile.writeFrames(buffer, outputFrames)
	}
}

/**
 * A '.' morse character. No timing change from MorseCharacter.
 */
class MorseDot extends MorseCharacter {
}

/**
 * A '-' morse character. Tone is 3x the .-duration.
 */
class MorseDash extends MorseCharacter {
	public MorseDash() {
		duration *= 3
	}
}

/**
 * The end of a morse character (such as after writing all of ".-" for an A).
 */
class MorseCharEnd extends MorseCharacter {
	public MorseCharEnd() {
		// We've already paused for .-duration, we need a total of 3
		// .-duration after an entire character is output. So we need two more.
		postDuration = (duration * 2)
		duration = 0
	}
}

/**
 * The end of a morse word.
 */
class MorseWordEnd extends MorseCharacter {
	public MorseWordEnd() {
		// We've already paused for end of char duration, 3 * .-duration.
		// At the end of a word, we need 7 * .-duration, so we need four more.
		postDuration = (duration * 4)
		duration = 0
	}
}
