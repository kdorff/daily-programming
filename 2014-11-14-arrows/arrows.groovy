/**
 * Wednesday's challenge was released later than I wanted it to be 
 * (my fault entirely), so I'll make it up to you by posting this one 
 * early. I fear some previous hard challenges have appeared unapproachable 
 * to some people due to their logical or mathematical complexity. I aim to 
 * make a Hard challenge today which is innately simple, but will still 
 * require a Hard degree of thought (assuming you come up with the algorithm 
 * yourself.)
 *
 * Take this grid of characters:
 *
 * v<^><>>v><>^<>vvv^^>
 * >^<>^<<v<>>^v^v><^<<
 * v^^>>>>>><v^^<^vvv>v
 * ^^><v<^^<^<^^>>>v>v>
 * ^<>vv^><>^<^^<<^^><v
 * ^vv<<<><>>>>^<>^^^v^
 * ^<^^<^>v<v^<>vv<^v<>
 * v<>^vv<^>vv>v><v^>^^
 * >v<v><^><<v>^^>>^<>^
 * ^v<>^<>^>^^^vv^v>>^<
 * v>v^^<>><<<^^><^vvv^
 *
 * Let's imagine they all represent arrows, pointing to a cell next to them. 
 * For example, v points downward, and < points left. Let's also imagine the
 * grid is infinite - ie. a > arrow at the right-hand side will 'wrap around' 
 * and point to the leftmost character on the same row, meaning the board has 
 * no limits. Now, we're going to follow the direction of the arrows. Look at 
 * the top-left cell. It's a v, so it points down to the cell below it, which 
 * is a >. That points to the cell to its right, which is a ^. This points up 
 * to the cell above it, which is a <. This points to the cell to its left... 
 * which is exactly where we started. See how this has formed a 'loop'? You 
 * could go round and round and round forever. Remember, the board wraps 
 * around, so this grid is also a loop:
 *
 * >>>>>>>>>>>>
 *
 * And so is this, if you follow the arrows:
 *
 * ^^>
 * >^^
 * ^>^
 *
 * This looping structure is called a cycle. The discrete mathematicians 
 * in this sub should have all collectively just said 'aha!', as they should 
 * know already be thinking of how to approach the challenge from that last 
 * sentence. If you're not a discrete mathematician, read on. Your challenge 
 * today is simply described: given a grid such as the one above, find the 
 * largest cycle in it.
 *
 * One important point: the 'length' of the cycle is just the part of the
 * cycle that repeats. For example, the cycle is not made longer by adding 
 *
 * an 'intro' to it:
 *
 *     >>v
 *     ^<<
 *      ^
 *      ^
 *      ^
 *      ^
 *
 * The length of this cycle is 6 regardless of where you start from, as
 * that is the length of the 'cycle'.
 *
 * Formal Inputs and Outputs
 *
 * Input Description
 *
 * You will input 2 numbers first - these are the width and height of the
 * grid you'll be working with. Then you will input a grid in the same format 
 * as described above.
 *
 * Output Description
 *
 * You are to output the length of the longest cycle on the grid, possibly 
 * along with some representation of where that cycle is on the board (eg. 
 * print the cycle in another color.)
 */

// Kick the whole thing off
new Arrows().exec([
    """
    5 5
    >>>>v
    ^v<<v
    ^vv^v
    ^>>v<
    ^<<<^
    """,
    """
    45 20
    ^^v>>v^>>v<<<v>v<>>>>>>>>^vvv^^vvvv<v^^><^^v>
    >><<>vv<><<<^><^<^v^^<vv>>^v<v^vv^^v<><^>><v<
    vv<^v<v<v<vvv>v<v<vv<^<v<<<<<<<<^<><>^><^v>>>
    <v<v^^<v<>v<>v<v<^v^>^<^<<v>^v><^v^>>^^^<><^v
    ^>>>^v^v^<>>vvv>v^^<^<<<><>v>>^v<^^<>v>>v<v>^
    ^^^<<^<^>>^v>>>>><>>^v<^^^<^^v^v<^<v^><<^<<<>
    v<>v^vv^v<><^>v^vv>^^v^<>v^^^>^>vv<^<<v^<<>^v
    <<<<<^<vv<^><>^^>>>^^^^<^<^v^><^v^v>^vvv>^v^^
    <<v^<v<<^^v<>v>v^<<<<<>^^v<v^>>>v^><v^v<v^^^<
    ^^>>^<vv<vv<>v^<^<^^><><^vvvv<<v<^<<^>^>vv^<v
    ^^v^>>^>^<vv^^<>>^^v>v>>v>>v^vv<vv^>><>>v<<>>
    ^v<^v<v>^^<>>^>^>^^v>v<<<<<>><><^v<^^v><v>^<<
    v>v<><^v<<^^<^>v>^><^><v^><v^^^>><^^<^vv^^^>^
    v><>^><vv^v^^>><>^<^v<^><v>^v^<^<>>^<^vv<v>^v
    ><^<v>>v>^<<^>^<^^>v^^v<>>v><<>v<<^><<>^>^v<v
    >vv>^>^v><^^<v^>^>v<^v><>vv>v<^><<<<v^<^vv<>v
    <><<^^>>^<>vv><^^<vv<<^v^v^<^^^^vv<<>^<vvv^vv
    >v<<v^><v<^^><^v^<<<>^<<vvvv^^^v<<v>vv>^>>^<>
    ^^^^<^<>^^vvv>v^<<>><^<<v>^<<v>>><>>><<^^>vv>
    <^<^<>vvv^v><<<vvv<>>>>^<<<^vvv>^<<<^vv>v^><^
    """

])

/**
 * Class to perform challenge
 */
class Arrows {

    // How arrows manipulate movement
    static moveDeltas = [
        '^': [r: -1, c:  0],
        'v': [r: +1, c:  0],
        '<': [r:  0, c: -1],
        '>': [r:  0, c: +1]
    ]
    /**
     * Main method
     * @param gridStrs list of strings, which are the datasets (one per string).
     */
    def exec(gridStrs) {
        // Process all datasets
        gridStrs.each { gridStr ->
            // Process one dataset
            execOne(gridStr)
        }
    }

    /**
     * Process one dataset stored in a string.
     * @param gridStr the dataset in the form of a string
     */
    def execOne(gridStr) {
        def grid = parseGridStr(gridStr)
        showGrid(grid)
        def maxPath = [len: 0, path: []]
        for (r in 0..<(grid.h)) {
            for (c in 0..<(grid.w)) {
                def pathPos = [r: r, c: c]
                if (!maxPath.path.contains(pathPos)) {
                    // If the largest path didn't contain this position
                    // check the path
                    def lenMap = pathLength(grid, pathPos)
                    if (lenMap.len > maxPath.len) {
                        maxPath = lenMap
                    }
                }
            }
        }
        println "Size of path is ${maxPath.len}"
        showGrid(grid, maxPath)
    }

    /**
     * Given a starting position, search until we end up back on a
     * cell that is on the same line. If we end up on the starting
     * cell we have a path, otherwise we quit and ignore this path.
     * @param grid the grid
     * @param startPos the starting position to look for a circular path
     * @return a map of the path length and positions that comprise the path
     */
    def pathLength(grid, startPos) {
        def lastPos = startPos
        def len = 1
        def visited = []
        visited << lastPos
        while (true) {
            def pos = nextPosition(grid, lastPos)
            if (visited.contains(pos)) {
                // We're done, we ended up back on the
                // same path
                lastPos = pos
                break
            } else {
                // Keep searching
                visited << pos
                len++
                lastPos = pos
            }
        }
        if (lastPos == startPos) {
            // We connected back to the start of the path,
            // keep this path.
            [len: len, path: visited]
        } else {
            // We connected back to the middle of the path,
            // disregard this path.
            [len: -1, path: null]
        }
    }

    /**
     * Given a current position, return the next position.
     * This will wrap left/right/top/down.
     * @param grid the grid
     * @param pos the current position
     * @return the next position
     */
    def nextPosition(grid, pos) {
        def arrow = cellValue(grid, pos)
        def deltas = moveDeltas[arrow]

        def newPos = [r: pos.r + deltas.r, c: pos.c + deltas.c]
        // Wrap
        if (newPos.r < 0) {
            newPos.r = grid.h - 1
        }
        if (newPos.r == grid.h) {
            newPos.r = 0
        }
        if (newPos.c < 0) {
            newPos.c = grid.w - 1
        }
        if (newPos.c == grid.w) {
            newPos.c = 0
        }
        newPos
    }

    /**
     * Convert the grid from string format to our grid format.
     * @param gridStr the grid in string format
     * @return the grid in our format
     */
    def parseGridStr(gridStr) {
        def grid = [:]
        def gridList = gridStr.trim().split("[\n\r]").collect { it.trim() }
        def (w, h) = gridList.remove(0).split(' ')
        grid.w = w as Integer
        grid.h = h as Integer
        grid.rows = gridList
        grid
    }

    /**
     * Given a grid and position, return the character on the
     * grid at that position.
     * @param grid the grid
     * @param pos the position
     */
    def cellValue(grid, pos) {
        grid.rows[pos.r][pos.c]
    }

    /**
     * Display the grid. If maxPath is provided ONLY show the path
     * on the grid (non-path will just be blank space).
     * @param grid the grid
     * @param maxPath (optional) the map of max path (len and path)
     */
    def showGrid(grid, maxPath = null) {
        for (r in 0..<(grid.h)) {
            for (c in 0..<(grid.w)) {
                def pos = [r:r, c:c]
                def outputVal
                if ((maxPath == null) || (maxPath.path?.contains(pos))) {
                    outputVal = cellValue(grid, pos)
                } else {
                    outputVal = ' '
                }
                print outputVal
            }
            println ""
        }
    }
}