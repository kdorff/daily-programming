class Metro {

    /** Input file */
    def inputFile = "grid1.txt"

    /** Storage for the grid. One row per list entry. */
    List<String> grid
    /** Width of grid. */
    int width
    /** height of grid. */
    int height
    /** The found tiles. */
    Tiles tiles

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
        tiles = new Tiles()
    }

    /**
     * Primary method.
     */
    def execute() {
        readGrid(inputFile)
        processGrid()
        output()
    }

    def output() {
        tiles.tiles.each { Tile tile ->
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
        int i = 0  // Line number in file
        new File(inputFile).eachLine { line ->
            if (line.startsWith("#")) {
                // Comment. Ignore.
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
        tiles = new Tiles()
        (0 ..< height).each { r ->
            int c = 0
            grid[r].split('[.]').each { part ->
                if (part && !tiles.inTiles(r, c)) {
                    consume(r, c)
                }
                c += part.size() + 1
            }
        }
    }

    /**
     * Consume the sub-block finding width and height. It's then marked
     * as '.' (gutter, background) so it won't be processed again.
     */
    def consume(int startR, int startC) {
        // Find the width
        int r = startR
        int c = startC
        def width = 0
        def blockLetter
        // Find the width of the sub-block
        while (true) {
            def letter = grid[r][c + width]
            if (letter == '.') {
                break
            }
            if (blockLetter == null) {
                // Define the letter for this grid, the top-left letter
                blockLetter = letter
            }
            width++
            letter = grid[r][c + width]
        }

        // Find the height of the sub-block
        r = startR
        c = startC
        def height = 0
        while (true) {
            def letter = grid[r + height][c]
            if (letter == '.') {
                break
            }
            height++
        }

        tiles.addTile new Tile(blockLetter, r, c, width, height)
    }
}

/**
 * A found tile
 */
class Tile {
    /** Inclusive range for the columns for this tile. */
    IntRange colRange
    /** Inclusive range for the rows for this tile. */
    IntRange rowRange
    /** The top-left row for this tile. */
    int r
    /** The top-left column for this tile. */
    int c
    /** The width for this tile. */
    int width
    /** The height for this tile. */
    int height
    /** The letter for this tile. */
    String tileLetter

    /**
     * Construct a new Tile.
     * @param tileLetter the letter of this tile
     * @param r the top-left row of this tile
     * @param c the top-left column of this tile
     * @param width the width of this tile
     * @param height the height of this tile
     */
    public Tile(String tileLetter, int r, int c, int width, int height) {
        colRange = new IntRange(true, c, c + width - 1)
        rowRange = new IntRange(true, r, r + height - 1)
        this.tileLetter = tileLetter
        this.r = r
        this.c = c
        this.width = width
        this.height = height
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
}

/**
 * Store the tiles found.
 */
class Tiles {
    /** The found files */
    List<Tile> tiles = []
    /**
     * Add a found tile.
     * @param tile the newly found tile
     */
    def addTile(Tile tile) {
        tiles << tile
    }

    /**
     * Determine if (r,c) is already part of a found tile.
     * @param r row
     * @param c col
     * @param true if (r,c) is in an already found tile
     */
    def inTiles(int r, int c) {
        def found = false
        for (Tile tile in tiles) {
            if (tile.inTile(r, c)) {
                found = true
                break
            }
        }
        found
    }
}
