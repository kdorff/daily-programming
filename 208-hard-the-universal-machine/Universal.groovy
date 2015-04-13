def m = new Machine('input-binary.txt' as File)
m.execute()

/**
 * A state machine class.
 */
class Machine {
    /** The valid alphabet. */
    List<String> alphabet
    /** The valid states. */
    Set<String> states
    /** The current state. */
    String state
    /** The ending state. */
    String endState
    /** The current position. */
    int position
    /** The current tape, key is tape index, value is single character. */
    Map<Integer, String> tape
    /** The defined transitions. */
    List<Transition> transitions

    /**
     * Create a state machine based on the input file.
     */
    public Machine(File inputFile) {
        transitions = []
        position = 0
        int index = 0
        tape = [:]
        inputFile.eachLine { line ->
            if (line.startsWith('#') || line.trim() == "") {
                // Skip comments and blank lines
                return
            }
            switch (index++) {
                // Configure the state machine
                case 0:
                    alphabet = line.collect { it }
                    break
                case 1:
                    states = line.split(" ") as Set
                    break
                case 2:
                    state = line
                    break
                case 3:
                    endState = line
                    break
                case 4:
                    readInitialTape(line)
                    position = 0
                    break
                default:
                    // Read the transitions for the rest of the
                    // file
                    def transition = Transition.create(line)
                    if (transition) {
                        transitions << transition
                    } else {
                        println "Error parsing transition line '${line}'"
                    }
                    break
            }
        }
    }

    /**
     * Read the initial value of the tape, populate the tape map.
     */
    def readInitialTape(String line) {
        line.eachWithIndex { String letter, int index ->
            tape[index] = letter
        }
    }

    /**
     * Execute the state machine. Runs in a loop until the endState is found.
     */
    def execute() {
        // Display start state
        println this
        println "... processing"
        while (state != endState) {
            //print "."
            String tapeAtPosition = readTape()
            Transition t = findTransition(state, tapeAtPosition)
            if (!t) {
                print "ERROR: Could not find a transition from state=${state} "
                println "char=${tapeAtPosition}"
                break
            }
            state = t.endState
            writeTape(t.endValue)
            position += t.direction
        }
        // Display end state
        println this
    }

    /**
     * Obtain the value of the tape at the current position
     * @return String the char value of the tape at the current position
     */
    def readTape() {
        tape[position] ?: '_'
    }

    /**
     * Set the value of the tape at the current position to the
     * specified value.
     * @param setChar the character to set the tape to at the current position
     */
    def writeTape(String setChar) {
        tape[position] = setChar
    }

    /**
     * Given the current state and value of the tape at the current
     * position, find the transition that machines the current.
     * @param currentState the current state
     * @param currentValue the current value of the tape at the current position
     * @return Transition the next transition (or null if no matching 
     * transition was found.)
     */
    Transition findTransition(currentState, currentValue) {
        Transition result = null
        for (Transition t in transitions) {
            if (currentState == t.startState) {
                if (t.initialValue == currentValue) {
                    result = t
                    break
                }
            }
        }
        result
    }

    /**
     * Output the machine as a String. Display the start of tape mark (|),
     * the current position mark (^). If these coincide, it will display ↑.
     * @return String the state of the tape
     */
    String toString() {
        def resultChars = new StringBuilder()
        def resultMarks = new StringBuilder()
        def indexes = tape.keySet().sort()
        def minIndex = indexes[0]
        def maxIndex = indexes[-1]
        (minIndex .. maxIndex).each { index ->
            resultChars << tape[index] ?: '_'
            if (index == position) {
                if (index == 0) {
                    resultMarks << '↑'
                } else {
                    resultMarks << '^'
                }
            } else if (index == 0) {
                resultMarks << '|'
            } else {
                resultMarks << ' '
            }
        }
        "${resultChars.toString()}\n${resultMarks.toString()}"
    }
}

/**
 * A transition object. Describes the before state and the after state
 * including how to move the pointer.
 */
class Transition {
    /** Regex pattern for parsing transitions from the input file. */
    static transitionPattern = /^(.+) (.+) = (.+) (.+) ([<>])$/
    String startState
    String initialValue
    String endState
    String endValue
    int direction

    /**
     * Read the description of the transition and create a
     * Transition object from it (or null of the format was incorrect).
     * @param transitionStr the transition as a string
     * @return Transition the Transitino object or null
     */
    static Transition create(String transitionStr) {
        def result = null
        transitionStr.find(transitionPattern) {  
                whole, startStateM, initialValueM,
                endStateM, endValueM, directionM ->
            result = new Transition()
            result.startState = startStateM
            result.initialValue = initialValueM
            result.endState = endStateM
            result.endValue = endValueM
            result.direction = directionM == "<" ? -1 : 1
        }
        result
    }
}