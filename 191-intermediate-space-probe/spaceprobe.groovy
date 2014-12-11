/**
 * Reddit Daily Programmer, 191 Intermediate
 * @author Kevin Dorff
 * http://www.reddit.com/r/dailyprogrammer/comments/2o5tb7/2014123_challenge_191_intermediate_space_probe/
 *
 * Description:
 * --------------------------
 * NASA has contracted you to program the AI of a new probe. This new 
 * probe must navigate space from a starting location to an end location. 
 * The probe will have to deal with Asteroids and Gravity Wells. Hopefully 
 * it can find the shortest path.
 * 
 * Map and Path:
 * --------------------------
 * This challenge requires you to establish a random map for the challenge. 
 * Then you must navigate a probe from a starting location to an end location.
 * 
 * Map:
 * --------------------------
 * You are given N -- you generate a NxN 2-D map (yes space is 3-D but for 
 * this challenge we are working in 2-D space)
 * 30% of the spots are "A" asteroids
 * 10% of the spots are "G" gravity wells (explained below)
 * 60% of the spots are "." empty space.
 * When you generate the map you must figure out how many of each spaces is 
 * needed to fill the map. The map must then be randomly populated to hold 
 * the amount of Gravity Wells and Asteroids based on N and the above 
 * percentages.
 * 
 * N and Obstacles
 * --------------------------
 * As n changes so does the design of your random space map. Truncate the 
 * amount of obstacles and its always a min size of 1. (So say N is 11 so 
 * 121 spaces. At 10% for wells you need 12.1 or just 12 spots) N can be 
 * between 2 and 1000. To keep it simple you will assume every space is 
 * empty then populate the random Asteroids and Gravity wells (no need 
 * to compute the number of empty spaces - they will just be the ones 
 * not holding a gravity well or asteroid)
 * 
 * Asteroids
 * --------------------------
 * Probes cannot enter the space of an Asteroid. It will just be destroyed.
 * 
 * Empty Spaces
 * --------------------------
 * Probes can safely cross space by the empty spaces of space. Beware 
 * of gravity wells as described below.
 * 
 * Gravity Wells
 * --------------------------
 * Gravity wells are interesting. The Space itself is so dense it cannot 
 * be travelled in. The adjacent spaces of a Gravity well are too strong 
 * and cannot be travelled in. Therefore you might see this.
 * . = empty space, G = gravity well
 *  .....
 *  .....
 *  ..G..
 *  .....
 *  .....
 * But due to the gravity you cannot pass (X = unsafe)
 *  .....
 *  .XXX.
 *  .XGX.
 *  .XXX.
 *  .....
 * You might get Gravity wells next to each other. They do not effect each 
 * other but keep in mind the area around them will not be safe to travel in.
 *  ......
 *  .XXXX.
 *  .XGGX.
 *  .XXXX.
 *  ......
 * Probe Movement:
 * --------------------------
 * Probes can move 8 directions. Up, down, left, right or any of the 4 
 * adjacent corners. However there is no map wrapping. Say you are at 
 * the top of the map you cannot move up to appear on the bottom of 
 * the map. Probes cannot fold space. And for whatever reason we are 
 * contained to only the spots on the map even thou space is infinite
 * in any direction.
 * 
 * Output:
 * --------------------------
 * Must show the final Map and shortest safe route on the map.
 * . = empty space
 * S = start location
 * E = end location
 * G = gravity well
 * A = Asteroid
 * O = Path.
 * If you fail to get to the end because of no valid path you must travel 
 * as far as you can and show the path. Note that the probe path was 
 * terminated early due to "No Complete Path" error.
 * 
 * Challenge Input:
 * --------------------------
 * using (row, col) for coordinates in space.
 * Find solutions for:
 * N = 10, start = (0,0) end = (9,9)
 * N = 10, start = (9, 0) end = (0, 9)
 * N= 50, start = (0,0) end = (49, 49)
 * 
 * Map Obstacle %
 * --------------------------
 * I generated a bunch of maps and due to randomness you will get easy 
 * ones or hard ones. I suggest running your solutions many times to see 
 * your outcomes. If you find the solution is always very straight then 
 * I would increase your asteroid and gravity well percentages. Or if 
 * you never get a good route then decrease the obstacle percentages.
 *
 * TODO
 * --------------------------
 * * Add gravity well spaces and asteroid spaces to skipPositions to
 *   optimize openPaths()
 * * It's possible I'm not always returning optimal. Might be worth
 *   investigating, maybe one of my optimizations is too agressive?
 */

import groovy.transform.EqualsAndHashCode

// Kick things off
new SpaceProbe().exec()

/**
 * Class to implement the SpaceProbe problem.
 */
class SpaceProbe {
    def rand = new Random()
    /** The size of the grid gridSize x gridSize. */
    def gridSize = 50
    /** The grid of space we need to traverse. */
    def space = new char[gridSize][gridSize]
    /** Positions we know cannot be visited, optimization. */
    def skipPositions = []
    /** What percentage of space is asteroids, 10% */
    def asteroidsPercentage = 0.1
    /** What percentage of space is gravity wells, 10% */
    def gravityWellPercentage = 0.05
    /** Start position, top left */
    def startPos = new Pos(0, 0)
    /** End position, bottom right */
    def endPos = new Pos(gridSize - 1, gridSize - 1)
    /** Possible directions that can be moved */
    def adjacentDeltas = [
        // Optimize directions for Start -> Finish
        new Pos( 1,  1, '↘'),
        new Pos( 1,  0, '↓'),
        new Pos( 0,  1, '→'),
        new Pos(-1, -1, '↖'),
        new Pos(-1,  0, '↑'),
        new Pos(-1,  1, '↗'),
        new Pos( 0, -1, '←'),
        new Pos( 1, -1, '↙'),
    ]
    /** Valid next positions in the space grid */
    def validNextPathValues = ['E', '.']
    /** All of the complete paths found. */
    def foundPaths = []
    /** If we get debug output */
    def debugOn = false
    /** If we want the calcuation to be visual */
    def visualCalculation = false
    /** If we get simpler progress output to see that we're working */
    def progressOn = false
    /** How many complete paths we've found */
    def completePaths = 0
    /** How many failed paths we've checked */
    def failedPaths = 0
    /** The shorest path we've found, optimization. */
    def shortestPathLen = Integer.MAX_VALUE

    /**
     * Constructor. Set things up. Create space, place start and end,
     * place asteroids and gravity wells.
     */
    public SpaceProbe() {
        (0 ..< gridSize).each { r ->
            (0 ..< gridSize).each { c ->
                space[r][c] = '.'
            }
        }
        setPosition(startPos, 'S')
        setPosition(endPos, 'E')
        def asteroidCount = numItems(asteroidsPercentage)
        def gravityWellCount = numItems(gravityWellPercentage)
        (0 ..< gravityWellCount).each {
           populate('G')
        }
        (0 ..< asteroidCount).each {
            populate('A')
        }
    }

    /**
     * Main route. Find path from S to E. Report outcome.
     */
    def exec() {
        drawSpace()
        def shortestPath = findShortestPath()
        println ""
        if (shortestPath == null) {
            println "No Complete Path"
        } else {
            drawSpace(shortestPath, true)
            //println "shortestPath=${shortestPath}"
        }
        println "completePaths=${completePaths}"
        println "failedPaths  =${failedPaths}"
    }

    /**
     * Output debug message.
     * @param message the debug message
     */
    def debug(message) {
        if (debugOn) {
            println message
        }
    }

    /**
     * Output progress message.
     * @param message the progress message
     */
    def progress(message) {
        if (progressOn) {
            print message
        }
    }

    /**
     * Clear the script and move cursor to top left.
     */
    def debugClearScreen() {
        final String ANSI_CLS = "\u001b[2J";
        final String ANSI_HOME = "\u001b[H";
        System.out.print(/*ANSI_CLS +*/ ANSI_HOME);
        System.out.flush();        
    }

    /**
     * How many items to places for a given percentage for the given gridSize
     * @param percentage the percentage, 0.0 to 1.0
     * @return the number of items for the percentage
     */
    def numItems(percentage) {
        def count = Math.floor(
            gridSize * gridSize * percentage) as Integer
        if (count == 0) {
            count = 1
        }
        count
    }

    /**
     * Place an item (such as 'S', 'E', 'A', 'G')
     * @param type the type of item to place
     */
    def populate(type) {
        def pos = randomPosition(type)
        setPosition(pos, type)
    }

    /**
     * For a given type, find a random position in space that will
     * accomodate the type.
     * @param type the type of item to place
     * @return the position to place the item
     */
    def randomPosition(type) {
        def pos = null
        while (true) {
            pos = new Pos(rand.nextInt(gridSize), rand.nextInt(gridSize))
            if (safePos(pos, type)) {
                break
            }
        }
        pos
    }

    /**
     * Given a position and a type, determine if that position
     * is a safe / valid place to place that type.
     * @param pos the position to check
     * @param type the type of item to try to place
     */
    def safePos(pos, type) {
        def isSafe = false
        def current = atPosition(pos)
        if ((current == '.') || (type == 'G' && current == 'X')) {
            debug "placing ${type} on ${current} at ${pos}"
            if (type == 'G') {
                // Dead space round 'G' items
                def allSafe = true
                adjacentDeltas.each { deltaPos ->
                    def dsPos = pos + deltaPos
                    def dsCurrent = atPosition(dsPos)
                    if ((dsCurrent == '.') || (dsCurrent == '') || 
                        (type == 'G' && dsCurrent == 'X') ||
                        (type == 'G' && dsCurrent == 'G')) {
                    } else {
                        allSafe = false
                    }
                }
                isSafe = allSafe
            } else {
                isSafe = true
            }
        }
        isSafe
    }

    /**
     * Check if the position in question is within the space grid.
     * @param pos the position to check.
     */
    def checkPos(pos) {
        if (pos && pos.r >= 0 && pos.c >= 0 && 
            pos.r < gridSize && pos.c < gridSize) {
            pos
        } else {
            null
        }
    }

    /**
     * Return the content of the space grid for a given position.
     * If the position is not within the space grid, return ''.
     * @param pos the position to obtain the content of space for
     * @return the content of space at the given position or '' if
     * the position is outside of space.
     */
    String atPosition(pos) {
        if (checkPos(pos)) {
            space[pos.r][pos.c] as String
        } else {
            ''
        }
    }

    /**
     * Set space at a given position to a specific type assuming pos
     * is within the limits of space. If placing a 'G' gravity well,
     * also place 'X' in space around the gravity well.
     * @param pos the position to set the value of the space grid
     * @param type the type of item to place on the space grid
     */
    def setPosition(pos, type) {
        if (checkPos(pos)) {
            space[pos.r][pos.c] = type
            if (type == 'G') {
                // Dead space around 'G'
                adjacentDeltas.each { deltaPos ->
                    def dsPos = pos + deltaPos
                    def dsCurrent = atPosition(dsPos)
                    if (dsCurrent != 'G') {
                        setPosition(dsPos, 'X')
                    }
                }
            }
        }
    }

    /**
     * Draw space. If path is provided, draw the path onto space.
     * If finalPath, draw the path as 'O's. If !finalPath draw
     * the space as arrows.
     * @param path the path to draw (if provided)
     * @param finalPath if this is an intermediate drawing of space
     * or a completed (final) path after shortest path has been found
     */
    def drawSpace(path = [], finalPath = false) {
        println "-" * gridSize
        print "  "
        (0 ..< gridSize).each { c ->
            print c % 10
            print ' '
        }
        println ''
        (0 ..< gridSize).each { r ->
            print r%10
            print ' '
            (0 ..< gridSize).each { c ->
                def pos = new Pos(r, c)
                def current = atPosition(pos)
                def indexInPath = path.indexOf(pos)
                if (indexInPath != -1) {
                    def posInPath = path[indexInPath]
                    //def pathChar = pathChar(path, pos)
                    if (finalPath) {
                        print current == '.' ? "O" : current
                    } else {
                        print current == '.' ? posInPath.arrow : current
                    }
                } else {
                    print current
                }
                print ' '
            }
            println ''
        }
    }

    /**
     * Front-end for the recursive findAllPaths method.
     * Set things up, start the recursive function to find
     * the path, and then handle the results.
     */
    def findShortestPath() {
        // store all of the found paths
        foundPaths.clear()
        // recursive method to find a path
        findAllPaths(startPos, [])
        // Sort the found paths by size of the path,
        // we just want the smallest one.
        foundPaths.sort { a, b ->
            a.size - b.size()
        }
        // Debug output
        foundPaths.each { pathList ->
            debug pathList
        }
        // Return the first smallest path that was found
        foundPaths[0]
    }

    /**
     * Find the path from pos to endNode that is
     * both available and shorest.
     */
    def findAllPaths(pos, pathList) {
        // Find all of the available paths from this
        // node and visit them all
        def openPathList = openPaths(pos, pathList)
        if (!pathList) {
            // First time through, insert the start node
            pathList << pos
        }
        if (openPathList && pathList.size() < shortestPathLen) {
            // Stop if the path size is already equal to
            // the shortest path we've found
            openPathList.each { subPos ->
                // subPos has the arrow we want for the last step in
                // the path, so copy it and set subPos to 'O' denoting
                // (in debug output) the end of the current path
                pathList[-1].arrow = subPos.arrow
                subPos.arrow = 'O'
                pathList << subPos
                def checkValue = atPosition(subPos)
                if (checkValue == 'E') {
                    // We found the end. New COMPLETE path, save it
                    def completeList = []
                    completeList.addAll pathList
                    def completeListLen = completeList.size()
                    pathList.remove(pathList.size() - 1)

                    shortestPathLen = completeListLen
                    foundPaths.add completeList
                    // Pop the end of pathList for the next iteration
                    if (debugOn) {
                        drawSpace(completeList)
                        debug "Complete Paths = length=${completeListLen}, ${foundPaths.size()}"
                    }
                    completePaths++
                    progress "+"
                } else {
                    // Path is not yet complete. Recurse down
                    // the next level.
                    findAllPaths(subPos, pathList)
                    // Pop the end of pathList for the next iteration
                    pathList.remove(pathList.size() - 1)
                }
            }
        } else {
            // Dead end.
            skipPositions << pos
            debug "No (shorter) path from ${pos} pathList=${pathList}"
            progress "-"
            failedPaths++
            //pathList.remove(pathList.size() - 1)
        }
    }

    /**
     * Given a position, find the open next positions we want to
     * search.
     * @param pos the current position
     * @param pathList the existing path, so we don't repeat
     */
    def openPaths(pos, pathList) {
        if (visualCalculation) {
            debugClearScreen()
            drawSpace(pathList)
        }
        debug "openPaths from ${pos} excluding ${pathList}"
        def openPathList = []
        adjacentDeltas.each { posDelta ->
            def checkPos = pos + posDelta
            def checkValue = atPosition(checkPos)
            if (validNextPathValues.contains(checkValue) && 
                    !pathList.contains(checkPos) &&
                    !skipPositions.contains(checkPos)) {
                checkPos.arrow = posDelta.arrow
                openPathList << checkPos
            }
        }
        debug "Found open path from ${pos}: ${openPathList}"
        openPathList
    }
}

/**
 * Position object. We bring in EqualsAndHashCode to simplfiy
 * being able to determine of a list .contains() a Pos but
 * the arrow shouldn't be part of the comparison
 */
@EqualsAndHashCode(excludes=["arrow"])
class Pos {
    /** Row. */
    int r
    /** Column. */
    int c
    /** The arrow. */
    String arrow

    /**
     * Constructor containing arrow.
     * @param r row
     * @param c column
     * @param arrow which arrow to associate with position
     */
    public Pos(r, c, arrow) {
        this.r = r
        this.c = c
        this.arrow = arrow
    }

    /**
     * Constructor with no arrow.
     * @param r row
     * @param c column
     */
    public Pos(r, c) {
        this.r = r
        this.c = c
        this.arrow = null
    }

    /**
     * Function to create a new position of this Pos + the other Pos
     * @param other other position to add to this
     * @param new Pos of this + other
     */
    Pos plus(Pos other) {
        new Pos(this.r + other.r, this.c + other.c)
    }

    /**
     * String representation of this.
     */
    String toString() {
        "[r:${r}, c:${c}, a:${arrow}]"
    }
}