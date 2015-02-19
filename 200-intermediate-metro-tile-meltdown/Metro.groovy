/**
 * Reddit Daily Programmer Challenge
 *
 * http://www.reddit.com/r/dailyprogrammer/comments/2uo3yf/20150204_challenge_200_intermediate_metro_tile/
 *
 * This implementation requires each tile be bordered by
 * ".", which is part of the problem description. It will
 * NOT currently handle tiles that are all the way to the
 * edges, such as the example:
 *
 *    4 4
 *    xx.z
 *    xx..
 *    ..yy
 *    z.yy
 */
class Metro {

    /** Input file */
    def inputFile = "grid3.txt"

    /** Storage for the grid. One row per list entry. */
    List<String> grid
    /** Width of grid. */
    int width
    /** height of grid. */
    int height
    /** The found tiles. */
    List<Tile> tiles
    /** Regex pattern for finding the sub-blocks in a line */
    def blockStartPattern = ~/(?<=\.)([^.]+)/

    /**
     * Program start point.
     */
    public static void main(String[] args) {
        new Metro().execute()
    }

    /**
     * Primary constructor.
     */
    public Metro() {
        grid = []
        tiles = []
    }

    /**
     * Primary method.
     */
    def execute() {
        readGrid(inputFile)
        processGrid()
        output()
    }

    /**
     * Output the final results.
     */
    def output() {
        tiles.each { Tile tile ->
            println "${tile.width}×${tile.height} tile of " + 
                "character '${tile.tileLetter}' " +
                "located at (${tile.c},${tile.r})"
        }
    }

    /**
     * Read the grid from the input file. Lines starting with "#" are comments
     * and are ignored. The first line is two ints, "width height". The
     * rest of the file is the grid. It should be uniform width. Height and
     * width NOT checked.
     * @param inputFile the input file containing the grid
     */
    def readGrid(String inputFile) {
        grid.clear()
        int i = 0  // Line number in file
        new File(inputFile).eachLine { line ->
            if (line.startsWith("#")) {
                // Comment. Ignore.
                println line
            } else if (i == 0) {
                // Width and height of the grid
                (width, height) = line.split(' ').collect { it as Integer }
            } else {
                // Part of the grid, split the string into a list of string
                grid << line
            }
            i++
        }
    }

    /**
     * Process the grid, finding the sub-blocks.
     */
    def processGrid() {
        tiles.clear()
        (0 ..< height).each { r ->
            def matcher = blockStartPattern.matcher(grid[r])
            while(matcher.find()) {
                // Find the start of each block on this row
                int c = matcher.start()
                if ((tiles.find { it.inTile(r, c) }) == null) {
                    int width = matcher.end() - matcher.start()
                    consumeTile(r, c, width)
                }
            }
        }
    }

    /**
     * Consume the sub-block finding width and height. It's then marked
     * as '.' (gutter, background) so it won't be processed again.
     */
    def consumeTile(int r, int c, int width) {
        // Find the height of the sub-block
        def letter = grid[r][c]
        def tileLetter = letter

        def height = 0
        while (letter != '.') {
            height++
            letter = grid[r + height][c]
        }

        tiles << new Tile(
            tileLetter, 
            new IntRange(true, r, r + height - 1),
            new IntRange(true, c, c + width - 1))
    }
}

/**
 * A found tile
 */
class Tile {
    /** The letter for this tile. */
    String tileLetter
    /** Inclusive range for the columns for this tile. */
    IntRange colRange
    /** Inclusive range for the rows for this tile. */
    IntRange rowRange

    /**
     * Construct a new Tile.
     * @param tileLetter the letter of this tile
     * @param colRange the inclusive IntRange for columns covered by this ile
     * @param rowRange the inclusive IntRange for rows covered by this tile
     */
    public Tile(String tileLetter, IntRange rowRange, IntRange colRange) {
        this.rowRange = rowRange
        this.colRange = colRange
        this.tileLetter = tileLetter
    }

    /**
     * Determine if (r,c) is already part of this tile
     * @param r row
     * @param c col
     * @param true if (r,c) is in this tile
     */
    def inTile(int r, int c) {
        colRange.contains(c) && rowRange.contains(r) 
    }

    /** Get the top-left column for this tile */
    def getC() { colRange.getFromInt() }

    /** Get the top-left row for this tile */
    def getR() { rowRange.getFromInt() }

    /** Get the width for this tile */
    def getWidth()  { colRange.getToInt() - colRange.getFromInt() + 1 }
    
    /** Get the height for this tile */
    def getHeight() { rowRange.getToInt() - rowRange.getFromInt() + 1 }
}
