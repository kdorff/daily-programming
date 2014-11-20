/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2kwfqr/10312014_challenge_186_special_code_or_treat/
 */

new ZombieSim().execute()

/**
 * Zombie Sim!
 */
class ZombieSim {
    // Configuration
    int mapRows = 20
    int mapCols = 20
    int sumVHZ = 300
    int ticksLeft = 500

    def random

    public ZombieSim() {
        random = new Random()
    }

    def execute() {
        // Setup the map
        if (sumVHZ > (mapRows * mapCols)) {
            System.err.println "Population too large for map."
            return
        }
        def map = new Map(random, mapRows, mapCols, sumVHZ)

        // Execute the simulation
        map.show()
        while (ticksLeft--) {
            println "Ticks left = ${ticksLeft}"
            map.moves()
            map.show()
            map.slay()
            map.bite()
            if (map.isComplete()) {
                break
            }
        }
        map.show()
        // Output results
        map.finalOutput(ticksLeft)
    }
}

/**
 * The game map. Contains the map and the players, etc.
 */
class Map {
    def random

    def map
    def rows
    def cols
    def numV
    def numH
    def numZ

    // The population (and bitten/slayed population)
    def population = []
    def removedPolulation = []

    // Stats gathering
    def slayed = 0
    def zombied = 0
    def singleSlay = 0
    def doubleSlay = 0

    // Transient, reset/used during each turn 
    def toSlay = []
    def toZombie = []

    /**
     * Initialize the map
     * @param random the random provided by the caller
     * @param rows the number of rows in the map
     * @param cols the number of cols in the map
     * @param sumVHZ the target sum of victims, hunters, and zombies
     */
    public Map(random, rows, cols, sumVHZ) {
        this.random = random
        map = new I[rows][cols]
        this.rows = rows
        this.cols = cols
        while (true) {
            try {
                println "Making population"
                numZ = random.nextInt((int) (sumVHZ / 3)) + 1
                numV = random.nextInt((int) (sumVHZ / 3)) + 1
                numH = random.nextInt((int) (sumVHZ / 3)) + 1
                if ((numZ + numV + numH) <= rows * cols) {
                    populate()
                    break
                }
            } catch (IllegalArgumentException e) {
                // We cannot do nextInt(0). This will catch that and
                // retry.
            }
        }
    }

    /**
     * Display stats after completion.
     * @param ticksLeft, used to display if we used all the turns or not
     */
    def finalOutput(ticksLeft) {
        def steps = [:]
        def populationCount = [:]
        int huntersBitten = 0
        int victimsBitten = 0
        steps['all'] = 0
        steps['H'] = 0
        steps['V'] = 0
        steps['Z'] = 0
        populationCount['H'] = 0
        populationCount['V'] = 0
        populationCount['Z'] = 0
        population.each { who ->
            def t = who.class.name
            steps['all'] += who.steps
            steps[t] += who.steps
            populationCount[t] += 1
        }
        removedPolulation.each { who ->
            def t = who.class.name
            steps['all'] = steps['all'] ? (steps['all'] + who.steps) : who.steps
            steps[t] = steps[t] ? (steps[t] + who.steps) : who.steps
            if (who instanceof H) {
                huntersBitten++
            } else if (who instanceof V) {
                victimsBitten++
            }
        }

        println("Number of H at start = ${numH}")
        println("Number of V at start = ${numV}")
        println("Number of Z at start = ${numZ}")
        populationCount.each { type, count ->
            println "Number of ${type} at end   = ${count}"
        }
        println "Slayed  zombies: ${slayed}"
        println "Created zombies: ${zombied}"

        steps.each { type, count ->
            println "Steps by ${type} = ${count}"
        }
        println "Slayed  zombies: ${slayed}"
        println "Created zombies: ${zombied}"
        println "singleSlay = ${singleSlay}"
        println "doubleSlay = ${doubleSlay}"
        println "huntersBitten = ${huntersBitten}"
        println "victimsBitten = ${victimsBitten}"
        println "Finished early? = ${ticksLeft >= 0}"
    }

    /**
     * Populate the map
     */
    def populate() {
        int id = 0
        (0..<numV).each {
            placeUnique(new V(id++))
        }
        (0..<numH).each {
            placeUnique(new H(id++))
        }
        (0..<numZ).each {
            placeUnique(new Z(id++))
        }
    }

    /**
     * Determine if a position on the map is populated or not.
     */
    def atPosition(position) {
        if (position.r < 0 || position.r >= rows) {
            null
        } else if (position.c < 0 || position.c >= cols) {
            null
        } else {
            map[position.r][position.c]
        }
    }

    /**
     * Place a player on the board in a non-occupied position.
     */
    def placeUnique(who) {
        population << who
        while (true) {
            def position = [r: random.nextInt(rows), c: random.nextInt(cols)]
            if (!atPosition(position)) {
                map[position.r][position.c] = who
                who.position = position
                break
            }
        }
    }

    /**
     * Perform a move for every existing player.
     */
    def moves() {
        population.each { who ->
            who.move(this)
        }
    }

    /**
     * Attempt to perform a slay (or two) for every Hunter.
     */
    def slay() {
        toSlay.clear()
        population.each { who ->
            if (who instanceof H) {
                who.slay(this)
            }
        }
        toSlay.each { deadZombie ->
            // Perform the actual slaying
            population.remove deadZombie
            removedPolulation << deadZombie
            slayed++
            map[deadZombie.position.r][deadZombie.position.c] = null
        }
    }

    /**
     * Attempt to perform a bite for every Zombie.
     */
    def bite() {
        toZombie.clear()
        population.each { who ->
            if (who instanceof Z) {
                who.bite(this)
            }
        }
        toZombie.each { toZombie ->
            // Perform the actual bite
            def newZombie = new Z(toZombie)
            toZombie.zombified = true            
            map[toZombie.position.r][toZombie.position.c] = newZombie
            population.remove(toZombie)
            population << newZombie
            removedPolulation << toZombie
            zombied++
        }
    }

    /**
     * Draw the current map.
     */
    def show() {
        print " "
        (0..<cols).each { c ->
            print "|"
            print " " + c.toString().padLeft(2, "0") + " "
        }
        println "|"
        (0..<rows).each { r ->
            print r % 10
            (0..<cols).each { c ->
                def cell = map[r][c]
                print "|"
                if (cell) {
                    print cell.toString()
                } else {
                    print "    "
                }
            }
            println "|"
        }
        println ""
    }

    /**
     * Determine if we can quit early (board is all H/V or all Z).
     */
    def isComplete() {
        def sum = [:]
        ['H', 'V', 'Z'].each { t ->
            sum[t] = 0
        }
        population.each { who ->
            def t = who.class.name
            sum[t] += 1
        }
        (sum['H'] + sum['V'] == 0) || (sum['Z'] == 0)
    }
}

/**
 * Superclass for H/V/Z
 */
class I {
    def id
    def position = [r:null, c:null]
    def moves = []
    def steps = 0
    def slays = 0
    def bites = 0
    def type = "?"
    def dead = false
    def zombified = false

    public I() {
    }

    public I(id) {
        this.id = id
    }

    /**
     * Given the current player and a different in row and/or column
     * return a valid new position (stored in the moves List)
     * @param map the map this player is playing on
     * @param rowDelta the difference in row from the current player position
     * @param rowDelta the difference in col from the current player position
     */
    def createMove(map, rowDelta, colDelta) {
        def newRow = position.r + rowDelta
        def newCol = position.c + colDelta
        def move = [r: position.r + rowDelta, c: position.c + colDelta]
        if (move.r < 0 || move.r >= map.rows) {
            // Cannot move off of map, invalid move
            null
        } else if (move.c < 0 || move.c >= map.cols) {
            // Cannot move off of map, invalid move
            null
        } else {
            if (map.atPosition(move)) {
                // Cannot move to populated cell
                null
            } else {
                // This move is acceptable
                moves << move
            }
        }
    }

    /**
     * Generic move for all players types, the import
     * thing is implementing acceptableMoves() for each
     * player type.
     * @param map the map this player is playing on
     */
    def move(map) {
        acceptableMoves(map)
        if (moves) {
            def newPos = moves[map.random.nextInt(moves.size())]
            def oldPos = position
            position = newPos
            map.map[oldPos.r][oldPos.c] = null
            map.map[newPos.r][newPos.c] = this
            steps++
        }
    }

    /**
     * Generic implementation of finding neighbors for the
     * current player.
     * @param map the map this player is playing on
     * @param includeDiagonals if neighbords should be found
     *        on the diagonals, too (otherwise just up 
     *        down left right)
     * @param filters the kinds of neighbors to look for,
     *        ignoring other kinds of players
     */
    def findNeighbors(map, includeDiagonals, List<Class> filters) {
        def neighbors = []
        if (includeDiagonals) {
            // Always find diagonals first, if available. This
            // gives hunters a slight edge over zombies
            neighbors << map.atPosition([r: position.r + -1, c: position.c + -1])
            neighbors << map.atPosition([r: position.r + -1, c: position.c +  1])
            neighbors << map.atPosition([r: position.r +  1, c: position.c + -1])
            neighbors << map.atPosition([r: position.r +  1, c: position.c +  1])
        }
        neighbors << map.atPosition([r: position.r + -1, c: position.c +  0])
        neighbors << map.atPosition([r: position.r +  1, c: position.c +  0])
        neighbors << map.atPosition([r: position.r +  0, c: position.c + -1])
        neighbors << map.atPosition([r: position.r +  0, c: position.c +  1])
        neighbors.grep {
            // Filter the neighbords getting rid of null and
            // keeping only the kinds of players we are looking for
            def keep = false
            if (it) {
                def curClass = it.getClass()
                for (filter in filters) {
                    if (curClass == filter) {
                        keep = true
                        break
                    }
                }
            }
            return keep
        }
    }

    /**
     * Generic implementation. How this player should
     * be displayed on the board
     */
    public String toString() {
        "${this.class.name}${this.id.toString().padLeft(3, '0')}"
    }
}

/**
 * Zombie player.
 */
class Z extends I {
    /**
     * Create a zombie.
     */
    public Z(int id) {
        super(id)
    }

    /**
     * Create a zombie from a non-zombie. Retain position and id,
     * the id is retained to help track an individual (H027 becomes
     * Z027 after a zombie bite turns them into a zombie).
     */
    public Z(I i) {
        this.position = i.position
        this.id = i.id
    }

    /**
     * Find acceptable zombie movies (l/r/d/u).
     * Zombies always try to move (to an unoccpied space).
     * @param map the map this player is playing on
     */
    def acceptableMoves(map) {
        // Z movies l/r/u/d, not diagonal
        moves.clear()
        createMove(map, -1,  0)
        createMove(map,  1,  0)
        createMove(map,  0, -1)
        createMove(map,  0,  1)
    }

    /**
     * Allow the current zombie to attempt to bite 
     * a single non-zombie neighbor (only on l/r/u/d directions)
     * @param map the map this player is playing on
     */
    def bite(map) {
        def nonZombieNeighbors = findNeighbors(map, false, [H, V])
        if (nonZombieNeighbors) {
            // Pick a victim to bite
            while (nonZombieNeighbors) {
                def toZombie = nonZombieNeighbors.remove(map.random.nextInt(nonZombieNeighbors.size()))
                if (!map.toZombie.contains(toZombie)) {
                    println "Zombie ${this} is going to bite ${toZombie}"
                    map.toZombie << toZombie
                    bites += 1
                    break
                }
            }
        }
    }
}

/**
 * Hunter player.
 */
class H extends I {
    /**
     * Create a hunter.
     */
    public H(int id) {
        super(id)
    }

    /**
     * Find acceptable hunter movies (l/r/d/u/diagonal
     * to non-occupied space). Hunters always try to
     * move to an unoccupied space.
     * @param map the map this player is playing on
     */
    def acceptableMoves(map) {
        // H moves l/r/u/d/diag
        moves.clear()
        createMove(map, -1,  0)
        createMove(map,  1,  0)
        createMove(map,  0, -1)
        createMove(map,  0,  1)
        createMove(map, -1, -1)
        createMove(map, -1,  1)
        createMove(map,  1, -1)
        createMove(map,  1,  1)
    }

    /**
     * Allow the current hunter to attempt to slay
     * a up to two zombie neighbors in any direction (l/r/u/d/diagonal)
     * @param map the map this player is playing on
     */
    def slay(map) {
        def zombieNeighbors = findNeighbors(map, true, [Z])
        int removed = 0
        while (zombieNeighbors) {
            // We're pulling which zombie to slay directly
            // from the list of zombies, which will prefer
            // diagonal slays for hunter supremacy
            // (instead of random zombie order).
            def zombieToSlay = zombieNeighbors.remove(0)
            // Don't slay the same zombie twice in a turn
            if (!map.toSlay.contains(zombieToSlay)) {
                println "Hunter ${this} is going to slay ${zombieToSlay}"
                map.toSlay << zombieToSlay
                if (++removed == 2) {
                    break
                }
            }
        }
        // Count the number of single and double slays
        if (removed == 1) {
            map.singleSlay++
        } else if (removed == 2) {
            map.doubleSlay++
        }
    }
}

/**
 * Victim player.
 */
class V extends I {

    /**
     * Create a hunter.
     */
    public V(int id) {
        super(id)
    }

    /**
     * Find acceptable victim movies (l/r/d/u/diagonal
     * to non-occupied space), but ONLY move if adjacent
     * to a Zombie.
     * @param map the map this player is playing on
     */
    def acceptableMoves(map) {
        // H moves l/r/u/d/diag
        moves.clear()
        def zombieNeighbors = findNeighbors(map, true, [Z])
        if (zombieNeighbors) {
            createMove(map, -1,  0)
            createMove(map,  1,  0)
            createMove(map,  0, -1)
            createMove(map,  0,  1)
            createMove(map, -1, -1)
            createMove(map, -1,  1)
            createMove(map,  1, -1)
            createMove(map,  1,  1)
        }
    }
}
