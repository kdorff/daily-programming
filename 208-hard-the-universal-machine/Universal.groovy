def inputs = [
    [file: 'input.txt' as File,
     pre:  '01100110' + '\n' + 
           '↑       ',
     post: '_10011010_' + '\n' + 
           ' ↑        '
    ],
    [file: 'input-morse.txt' as File,
     pre:  '/././../.../..../k' + '\n' + 
           '↑                 ',
     post: '_________________k/././../.../..../' + '\n' + 
           '|                 ^                '
    ],
    [file: 'input-binary.txt' as File,
     pre:  '0110100#' + '\n' +
           '↑       ',
     post: '0110100#0110100' + '\n' +
           '|       ^      '
    ]
]

inputs.each { input ->
    def m = Machine.create(input.file as File)
    assert input.pre == m.toString()
    m.execute()
    assert input.post == m.toString()
    println m.toString()
}

/**
 * A state machine class.
 */
class Machine {
    /** The valid alphabet. */
    Set<String> alphabet
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
    Map<String, Transition> transitions

    /**
     * Create a state machine based on the input file.
     */
    static Machine create(File inputFile) {
        Machine m = new Machine()
        m.transitions = [:]
        m.position = 0
        m.tape = [:]

        def line
        inputFile.withReader { reader ->
            m.alphabet = nextLine(reader).collect { it } as Set
            m.alphabet << '_'
            m.states = nextLine(reader).split(" ") as Set
            m.state = nextLine(reader)
            m.endState = nextLine(reader)
            nextLine(reader).eachWithIndex { String letter, int index ->
                m.tape[index] = letter
            }

            line = nextLine(reader)
            while (line != null) {
                // Read the transitions for the rest of the file
                def transition = Transition.create(line)
                if (transition) {
                    m.transitions[transition.mapKey] = transition
                } else {
                    System.err.println "Error parsing transition line '${line}'"
                }
                line = nextLine(reader)
            }
        }
        m.validate() ? m : null
    }

    /**
     * Read the next line from the reader, skipping "#" comment lines.
     */
    static String nextLine(reader) {
        String result
        while (true) {
            result = reader.readLine()
            if (result == null || !result.startsWith('#')) {
                // Found a returnable line
                break
            }
        }
        result
    }

    /**
     * Validate the initial state of the machine.
     */
    boolean validate() {
        def result = true
        result &= states.contains(state)
        result &= states.contains(endState)
        result &= position == 0
        tape.values().each { letter ->
            result &= alphabet.contains(letter)
        }
        transitions.values().each { transition ->
            result &= transition.validate(this)
        }
        result
    }

    /**
     * Execute the state machine. Runs in a loop until the endState is found.
     */
    def execute() {
        // Display start state
        while (state != endState) {
            String tapeAtPosition = readTape()
            Transition transition = transitions[
                Transition.mapKeyFor(state, tapeAtPosition)]
            if (!transition) {
                System.err.print "ERROR: Could not find a transition from state=${state} "
                System.err.println "char=${tapeAtPosition}"
                break
            }
            state = transition.endState
            writeTape(transition.endValue)
            position += transition.direction
        }
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
    /** Transition start value. */
    String startState
    /** Transition at a specific tape value. */
    String initialValue
    /** New state after transition. */
    String endState
    /** New tape value after transition. */
    String endValue
    /** Direction to move read head after transition, '<' or '>'. */
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

    /**
     * Validate the values of the Transition based on the possible
     * values defined in the machine the transition belongs to.
     */
    boolean validate(Machine m) {
        def result = true
        result &= m.states.contains(startState)
        result &= m.alphabet.contains(initialValue)
        result &= m.states.contains(endState)
        result &= m.alphabet.contains(endValue)
        result
    }

    /**
     * Transitions are stored using a map. Key for a given state/tapeValue.
     */
    static String mapKeyFor(String state, String tapeValue) {
        "${state}:${tapeValue}"
    }

    /**
     * Transitions are stored using a map. Key for the current Transition.
     */
    String getMapKey() {
        mapKeyFor(startState, initialValue)
    }
}