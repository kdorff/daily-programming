@Grab(group='com.googlecode.lanterna', module='lanterna', version='2.1.9')

import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.input.Key.Kind
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.ScreenWriter
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.TerminalFacade
import groovy.transform.EqualsAndHashCode
import java.nio.charset.Charset

/**
 * TODO: Fix line of sight. The issue is that when one is close to a wall,
 *       the angle of the line (ray trace) is too oblique because of the
 *       low resolution and therefore line of site returns false when
 *       sometimes it really should be true. Probably not easily fixable.
 * 
 * Done:
 * Place Traps
 * Place Torches
 * Degrade torch, one level per two turns (minimum torch?)
 * Draw random inner walls
 */
class Traveller2 {

    final static int NUMBER_OF_TURNS = 100
    final static int NUMBER_OF_TREASURES = 100
    final static int NUMBER_OF_PITS = 2
    final static int NUMBER_OF_TORCHES = 5
    final static int DEFAULT_TORCH_POWER = 5
    final static int NUMBER_OF_WALLS = 5

    /** The grid. */
    def gridStr = """\
%%%%%%%%%%%%%%%%%%%%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%..................%
%%%%%%%%%%%%%%%%%%%%"""

    /** The terminal, using lanterna, somewhat like curses. */
    Terminal terminal
    /** Buffered interface for Terminal for faster output. */
    Screen screen

    /** The game board. */
    GameBoard gameBoard
    /** Utility class for interacting with the game board. */
    GameBoardUtil gameBoardUtil
    /** Screen position to draw the game board. */
    Pos boardPos

    public Traveller2() {
        // Objects for interacting with the console (like curses)
        terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"));
        screen = new Screen(terminal)

        // Where to draw the board on the screen
        boardPos = new Pos(0, 4)
        gameBoard = new GameBoard(gridStr, new Pos(10, 10) /* Player position */)
        gameBoard.createWalls()
        gameBoard.createItems(NUMBER_OF_TREASURES, GameBoard.TREASURE)
        gameBoard.createItems(NUMBER_OF_TORCHES, GameBoard.TORCH)
        gameBoard.createItems(NUMBER_OF_PITS, GameBoard.PIT)
        gameBoardUtil = new GameBoardUtil(gameBoard, screen, NUMBER_OF_TURNS, boardPos)
        // Add acceptable input commands
        gameBoardUtil.addCommands([
            new Command(new Key(Key.Kind.ArrowDown),  new Pos( 0,  1), 'move'),
            new Command(new Key(Key.Kind.ArrowUp),    new Pos( 0, -1), 'move'),
            new Command(new Key(Key.Kind.ArrowLeft),  new Pos(-1,  0), 'move'),
            new Command(new Key(Key.Kind.ArrowRight), new Pos( 1,  0), 'move'),
            new Command(new Key((char) 'q'), 'quit'),
        ])
    }

    /**
     * Setup lanterna.
     */
    public setup() {
        //terminal.enterPrivateMode()
        screen.startScreen()
    }

    /**
     * Primary execution loop. Draw the board, take a command.
     */
    public boolean loop() {
        def looping = true

        gameBoardUtil.drawBoard()

        boolean endOfGame = false
        if (gameBoardUtil.gameBoard.fellIntoPit) {
            endOfGame = true
        } else if (gameBoardUtil.movementPoints == 0) {
            endOfGame = true
        } else {
            gameBoardUtil.readCommand()
            if (gameBoardUtil.command.command == 'quit') {
                endOfGame = true
            }
        }
        if (endOfGame) {
            gameBoardUtil.drawBoard(true)
            gameBoardUtil.endOfGame()
            looping = false
        }
        looping
    }

    /**
     * Tear down lanterna.
     */
    public tearDown() {
        //terminal.exitPrivateMode()
        screen.stopScreen()
    }

    /**
     * Run the game.
     */ 
    public execute() {
        setup()
        while (loop()) {}
        tearDown()
    }

    /**
     * Kick things off.
     */
    public static void main(String[] args) {
        new Traveller2().execute()
    }
}

/**
 * Utility class to manipulate the game board (input and output).
 */
class GameBoardUtil {
    /** The gameBoard this class is controlling. */
    GameBoard gameBoard
    /** Screen object for buffered input/output. */
    Screen screen
    /** ScreenWriter object for simplified output. */
    ScreenWriter writer
    /** Current score. */
    int score
    /** The last command the user selected. */
    Command command
    /** The key pressed to the user command map. */
    Map<Key, Command> gameKeyToCommand
    /** How many movement points remain in the game. */
    int movementPoints
    /** The board position on the screen */
    Pos boardPos

    /**
     * Constuct this class.
     */
    public GameBoardUtil(
            GameBoard gameBoard, Screen screen, int movementPoints, boardPos) {
        this.gameBoard = gameBoard
        this.screen = screen
        this.writer = new ScreenWriter(screen)
        this.movementPoints = movementPoints
        this.boardPos = boardPos
        score = 0
        gameKeyToCommand = [:]
    }

    /**
     * Add valid input commands.
     */
    def addCommands(List<Command> commands) {
        commands.each { command ->
            gameKeyToCommand[command.key] = command
        }
    }

    /**
     * Draw the board starting at pos. This uses lanterna/screenWriter
     * to do buffered output to the screen for speed.
     */
    def drawBoard(boolean showWholeBoard = false) {
        showWholeBoard = true
        screen.clear()
        // Draw the board
        def playerPos = gameBoard.player.pos
        (0 ..< gameBoard.numY).each { y ->
            (0 ..< gameBoard.numX).each { x ->
                def posToCheck = new Pos(x, y)
                if (posToCheck != playerPos) {
                    // Draw non-player game board spaces
                    def playerDist = playerPos.distanceTo(posToCheck)
                    if (showWholeBoard ||
                            (playerDist <= (gameBoard.player.torchPower + 1) &&
                             playerPos.lineOfSightTo(gameBoard, posToCheck))) {
                        // Draw the item or the space on the board
                        def item = gameBoard.items[posToCheck]
                        if (item) {
                            drawItem(item)
                        } else {
                            drawGridCell(posToCheck)
                        }
                    }
                }
            }
        }

        // Draw the player
        drawItem(gameBoard.player)

        // Draw the score and such
        writer.drawString(0, 0, "Moves Left  : ${movementPoints}")
        writer.drawString(0, 1, "Score       : ${gameBoard.player.score}")
        writer.drawString(0, 2, "Torch Power : ${gameBoard.player.torchPower}")
        screen.refresh()
    }

    def drawGridCell(Pos pos) {
        def cell = gameBoard.grid[pos.y][pos.x]
        if (!GameBoard.FLOORS.contains(cell)) {
            writer.setForegroundColor(GameBoard.WALLS.foregroundColor)
            writer.setBackgroundColor(GameBoard.WALLS.backgroundColor)
            writer.drawString(
                boardPos.x + pos.x, boardPos.y + pos.y, cell)
            writer.setForegroundColor(Terminal.Color.DEFAULT)
            writer.setBackgroundColor(Terminal.Color.DEFAULT)
        }
    }

    /**
     * Draw an item.
     * @param item the item to draw
     */
    def drawItem(Item item) {
        writer.setForegroundColor(item.foregroundColor)
        writer.setBackgroundColor(item.backgroundColor)
        writer.drawString(boardPos.x + item.pos.x, boardPos.y + item.pos.y, 
                item.symbol)
        writer.setForegroundColor(Terminal.Color.DEFAULT)
        writer.setBackgroundColor(Terminal.Color.DEFAULT)
    }

    /**
     * Determine if the player can perform the Command command.
     * @param command the command that might be executed
     * @return true if the command can be executed
     */
    def canMovePlayer(Command command) {
        def result = false
        if (command.command == 'move') {
            def cell = gameBoard.at(new Pos(
                gameBoard.player.pos.x + command.delta.x,
                gameBoard.player.pos.y + command.delta.y))
            if (!GameBoard.WALLS.symbols.contains(cell)) {
                result = true
            }
        }
        result
    }

    /**
     * Attempt to have the player perform the Command command.
     * @param command the command that might be executed
     * @return true if the command was able to be executed
     */
    def movePlayer(Command command) {
        def moved = canMovePlayer(command)
        if (moved) {
            gameBoard.player.decreaseTorch()
            gameBoard.player.pos.x += command.delta.x
            gameBoard.player.pos.y += command.delta.y
            gameBoard.removeItem(gameBoard.player.pos)
        }
        moved
    }

    /**
     * Read the next command from the user. Invalid input is ignored
     * (such as a character that isn't supported or trying to move into
     * a wall). The New Command stored in variable 'command'.
     */
    def readCommand() {
        while (true) {
            def newKey = screen.readInput()
            if (newKey != null) {
                Command check = gameKeyToCommand[newKey]
                if (check != null) {
                    // Valid input
                    if (check.command == 'move') {
                        if  (movePlayer(check)) {
                            movementPoints--
                            command = check
                            break
                        }
                    } else {
                        command = check
                        break
                    }
                }
            } else {
                // No input. Let's wait a short bit.
                Thread.yield()
            }
        }    
    }

    /**
     * Display a message if the user quits or when the game ends.
     */
    def endOfGame() {
        def message = []
        message << '####################'
        message << '#                  #'
        if (command.command == 'quit') {
            message << '#    You Quit!     #'
        } else if (gameBoard.fellIntoPit) {
            message << '# Fell Into a Pit! #'
        } else {
            message << '#    Game Over!    #'
        }
        message << "#    Score ${lpad("${gameBoard.player.score}", 3)}     #"
        message << '#                  #'
        message << '#  Press Any Key   #'
        message << '#                  #'
        message << '####################'
        int messageHeight = message.size()
        int messsageWidth = message[0].size()
        Pos messagePos = new Pos(
                (int) (gameBoard.numX - messsageWidth) / 2,
                (int) (gameBoard.numY - messageHeight) / 2)
        writer.setForegroundColor(Terminal.Color.BLACK)
        writer.setBackgroundColor(Terminal.Color.WHITE)
        (0 ..< messageHeight).each { i ->
            writer.drawString(
                boardPos.x + messagePos.x, 
                boardPos.y + messagePos.y + i, 
                message[i])
        }
        writer.setForegroundColor(Terminal.Color.DEFAULT)
        writer.setBackgroundColor(Terminal.Color.DEFAULT)
        screen.refresh()
        while (screen.readInput() == null) {}
    }

    /**
     * Left pad a string with spaces to a specific width.
     */
    def lpad(String str, int width) {
        str.padLeft(3, ' ')
    }

}

/**
 * Class for the game board, player, and items to pick up.
 */
class GameBoard {
    /** Characters that make up the walls. */
    final static WALLS = [
        symbols: ['%', '-', '#'],
        foregroundColor: Terminal.Color.WHITE,
        backgroundColor: Terminal.Color.DEFAULT,
    ]
    /** Characters that make up the floor. */
    final static FLOORS = ['.', ' ']
    /** The player character. */
    final static INNER_WALL = [
        symbol: WALLS.symbols[0],
        foregroundColor: WALLS.foregroundColor,
        backgroundColor: WALLS.backgroundColor,
        torchPower: 0,
        score: 0,
    ] as Item
    final static PLAYER = [
        symbol: '@',
        foregroundColor: Terminal.Color.BLUE,
        backgroundColor: Terminal.Color.DEFAULT,
        torchPower: Traveller2.DEFAULT_TORCH_POWER,
        score: 0,
    ] as Item
    /** Item character. */
    final static TREASURE = [
        symbol: '$',
        foregroundColor: Terminal.Color.YELLOW,
        backgroundColor: Terminal.Color.DEFAULT,
        torchPower: 0,
        score: 1,
    ] as Item
    final static TORCH = [
        symbol: 'T',
        foregroundColor: Terminal.Color.GREEN,
        backgroundColor: Terminal.Color.DEFAULT,
        torchPower: Traveller2.DEFAULT_TORCH_POWER,
        score: 0,
    ] as Item
    final static PIT = [
        symbol: 'O',
        foregroundColor: Terminal.Color.RED,
        backgroundColor: Terminal.Color.DEFAULT,
        torchPower: 0,
        score: 0,
    ] as Item

    /** Random number generator. */
    Random rand
    /** The grid / game board. */
    List<String> grid
    /** Items that haven't been collected. */
    Map<Pos, Item> items
    /** The player. */
    Item player
    /** How many lines in the grid / game board */
    int numY
    /** How many columns in the grid / game board. */
    int numX
    /** Fell into a pit? */
    boolean fellIntoPit = false

    /**
     * Construct the game board and the player starting position.
     * @param gridStr the grind as a multi-line string
     * @param playerPos the position at which to place the player
     */
    public GameBoard(String gridStr, Pos playerPos) {
        rand = new Random(new Date().time)
        grid = gridStr.split('[\n\r]') as List
        numY = grid.size()
        numX = grid[0].size()
        items = [:]
        player = new Item(GameBoard.PLAYER, playerPos)
    }

    /**
     * Return the symbol (grid, player, item) at the given position.
     * A return value of "!" means pos isn't within the grid
     * @param the position to check
     * @return the symbol at that position
     */
    def at(Pos pos) {
        def symbol = '!'
        if (pos.x >= 0 && pos.y >= 0 && pos.x < numX && pos.y < numY) {
            if (player.pos == pos) {
                symbol = player.symbol
            } else {
                def item = items[pos]
                if (item != null) {
                    symbol = item.symbol
                } else {
                    symbol = grid[pos.y][pos.x]
                }
            }
        }
        symbol
    }

    /**
     * Place numToCreate items onto the grid in positions that are empty
     * (not consumed by grid walls, the player, or other items).
     * @param numToCreate the number of items to place
     */
    def createItems(int numberOfItems, Item itemTemplate) {
        (0 ..< numberOfItems).each { i ->
            while (true) {
                Pos pos = new Pos(rand.nextInt(numX), rand.nextInt(numY))
                def symbol = at(pos)
                if (GameBoard.FLOORS.contains(symbol)) {
                    items[pos] = new Item(itemTemplate, pos)
                    break
                }
            }
        }
    }

    def createWalls() {
        (0 ..< Traveller2.NUMBER_OF_WALLS).each { i ->
            while (true) {
                Pos posStart = new Pos(
                    // for X and Y, skip the edge (border) and first row/col
                    rand.nextInt(numX - 4) + 2, 
                    rand.nextInt(numY - 4) + 2)
                boolean horizontalWall = rand.nextBoolean()
                Pos posEnd
                int wallLength = rand.nextInt(3) + 3
                if (horizontalWall) {
                    posEnd = new Pos(posStart.x + wallLength, posStart.y)
                } else {
                    posEnd = new Pos(posStart.x, posStart.y + wallLength)                    
                }
                def foundColission = false
                ((posStart.x - 1 .. posEnd.x + 1)).each { x ->
                    ((posStart.y - 1) .. (posEnd.y + 1)).each { y ->
                        // Check +/- one square in x and y to
                        // make sure this wall doesn't touch any
                        // others
                        def symbol = at(new Pos(x, y))
                        if (!GameBoard.FLOORS.contains(symbol)) {
                            foundColission = true
                        }
                    }
                }
                if (!foundColission) {
                    // Valid wall, draw it
                    // ELSE we'll try making a new wall
                    (posStart.x .. posEnd.x).each { x ->
                        (posStart.y .. posEnd.y).each { y ->
                            Pos pos = new Pos(x, y)
                                items[pos] = new Item(INNER_WALL, pos)
                        }
                    }
                    // We are done with one wall, break the while(true)                    
                    break
                }
            }
        }
    }

    /**
     * If there is an item at pos, remove it.
     */
    def removeItem(Pos pos) {
        Item item = items[pos]
        if (item != null) {
            items.remove(pos)
            player.score += item.score
            player.torchPower += item.torchPower
            if (PIT.symbol == item.symbol) {
                fellIntoPit = true
            }
        }
    }
}

/**
 * Class to represent keyboard/user input along with how that input
 * changes the position of the player.
 */
class Command {
    /** The lanterna key that triggers the command. */
    Key key
    /** If the command is 'move', the movement delta for the player. */
    Pos delta
    /** The command ('quit', 'move'). */
    String command
    public Command(Key key, Pos delta, String command) {
        this.key = key
        this.delta = delta
        this.command = command
    }

    public Command(Key key, String command) {
        this.key = key
        this.command = command
    }
}

/**
 * Class to represent x,y position.
 */
@EqualsAndHashCode
class Pos {
    /** x of position */
    int x
    /** y of position */
    int y
    /** Construct a position. */
    public Pos(int x, int y) {
        this.x = x
        this.y = y
    }
    /** The absolute distance of this position to the next, always positive. */
    int distanceTo(Pos pos2) {
        def a = Math.abs(x - pos2.x) + 1
        def b = Math.abs(y - pos2.y) + 1
        Math.round(Math.sqrt(a*a + b*b))
    }

    /**
     * Based on Bresenham's algorithm
     * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
     * Algorithm based directly on the "full implementation" algorithm here:
     * http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
     *
     * TODO: This seems to abort too easily such as move near the top left of
     * TODO: the grid walls which should be visible are not.
     *
     * @param gameBoard
     * @param posToCheck
     */
    boolean lineOfSightTo(GameBoard gameBoard, Pos posToCheck) {
        boolean hasLos = true
        Pos playerPos = this
        int x = playerPos.x
        int y = playerPos.y
        int x2 = posToCheck.x
        int y2 = posToCheck.y
        int w = x2 - x 
        int h = y2 - y 
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 
        if (w < 0) { dx1 = -1 } else if (w > 0) { dx1 = 1 }
        if (h < 0) { dy1 = -1 } else if (h > 0) { dy1 = 1 }
        if (w < 0) { dx2 = -1 } else if (w > 0) { dx2 = 1 }
        int longest = Math.abs(w) 
        int shortest = Math.abs(h) 
        def symbol, lastSymbol
        if (!(longest > shortest)) {
            longest = Math.abs(h) 
            shortest = Math.abs(w) 
            if (h < 0) { dy2 = -1  } else if (h > 0)  { dy2 = 1 }
            dx2 = 0             
        }
        int numerator = longest >> 1 
        for (int i=0; i <= longest; i++) {
            // If one hits a wall, the wall is visible,
            // but the next space is marked not visible.
            symbol = gameBoard.at(new Pos(x, y))
            if (GameBoard.WALLS.symbols.contains(lastSymbol)) {
                hasLos = false
                break
            }
            numerator += shortest 
            if (!(numerator < longest)) {
                numerator -= longest 
                x += dx1 
                y += dy1 
            } else {
                x += dx2 
                y += dy2 
            }
            lastSymbol = symbol
        }
        hasLos
    }
}

/**
 * An item (or the player).
 */
class Item {
    // Screen symbol
    String symbol
    // Position of the item
    Pos pos
    // For player Item: remaining torch power
    // For Torch Item: increase players torch by this much
    int torchPower
    // Player: Score for this item
    // Treasure: Score delta for player when picked up
    int score

    Terminal.Color foregroundColor
    Terminal.Color backgroundColor

    boolean decreaseToggle = false

    def decreaseTorch() {
        decreaseToggle = !decreaseToggle
        if (!decreaseToggle) {
            if (torchPower >= 2) {
                // Never go below 1
                torchPower--
            }
        }
    }

    /**
     * Plan constructor. Used to go Map -> Item.f
     */
    public Item() {}

    /**
     * Construct the item.
     * @param symbol the visual symbol for the item.
     * @param pos the position of the item (in the grid)
     */
    public Item(Item itemTemplate, Pos pos) {
        this.symbol = itemTemplate.symbol
        this.foregroundColor = itemTemplate.foregroundColor
        this.backgroundColor = itemTemplate.backgroundColor
        this.torchPower = itemTemplate.torchPower
        this.score = itemTemplate.score
        this.pos = pos
    }
}