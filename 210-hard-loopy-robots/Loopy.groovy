class Loopy {

    //http://www.reddit.com/r/dailyprogrammer/comments/32vlg8/20150417_challenge_210_hard_loopy_robots/

    final static private MAX_ITERATIONS = 1000000

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        def loopy = new Loopy()
        ["SR", "S", 
         "SRLLRLRLSSS", "SRLLRLRLSSSSSSRRRLRLR",
         "SRLLRLRLSSSSSSRRRLRLRSSLSLS", "LSRS"].each { commands ->
            // Try all the test inputs
            def cycleAfterIterations = loopy.checkForLoops(commands)
            if (cycleAfterIterations) {
                println "Loop detected! ${cycleAfterIterations} cycle(s) to complete loop"
            } else {
                println "No loop detected after ${MAX_ITERATIONS} cycles"
            }
        }
    }

    /**
     * Given an input check for cycles by running the input up to
     * MAX_ITERATIONS times.
     * @param commands the commands (S, L, or R) to run as a String
     * @return int the number of cycles before a loop was found or 0
     * if no cycle was found.
     */
    def checkForLoops(String commands) {
        // dir 0:n, 1:e, 2:s, 3:w
        def startPos = [x:0, y:0, dir:0]
        def pos = startPos.clone()
        def cycleAfterIterations = 0
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            for (command in commands) {
                switch (command) {
                    case 'S':
                        pos = step(pos)
                        break
                    case 'R':
                        pos = right(pos)
                        break
                    case 'L':
                        pos = left(pos)
                        break
                }
            }
            if (pos == startPos) {
                cycleAfterIterations = i + 1
                break
            }
        }
        cycleAfterIterations
    }

    /**
     * Given pos, step in the specified direction.
     * @param pos the current pos
     * @return the new pos
     */
    def step(pos) {
        def result = pos.clone()
        switch (result.dir) {
            case 0: // North
                result.x += 1
                break
            case 1: // East
                result.y += 1
                break
            case 2: // South
                result.x -= 1
                break
            case 3: // West
                result.y -= 1
                break
        }
        result
    }

    /**
     * Given pos, turn left.
     * @param pos the current pos
     * @return the new pos
     */
    def left(pos) {
        def result = pos.clone()
        result.dir = result.dir == 0 ? 3 : result.dir - 1
        result
    }

    /**
     * Given pos, turn right.
     * @param pos the current pos
     * @return the new pos
     */
    def right(pos) {
        def result = pos.clone()
        result.dir = result.dir == 3 ? 0 : result.dir + 1
        result
    }
}



