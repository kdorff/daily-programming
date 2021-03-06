@Grab(group='com.googlecode.lanterna', module='lanterna', version='2.1.9')
@Grab(group='org.apache.commons', module='commons-lang3', version='3.3.2')

import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.ScreenWriter
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.TerminalFacade
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.input.Key.Kind
import java.nio.charset.Charset
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import java.util.Random
import org.apache.commons.lang3.StringUtils

/**
 * This is a copy of Traveller2 with a functioning line drawing algorithm
 * named Pos.lineOfSightTo() to verify that the line drawing method works
 * in all octants from the player to the extends of the box. Now that
 * the line drawing algorithm is verified, we can convert it to a lineOfSight
 * algorithm.
 */
class LOS {

    final static int numberOfTurns = 100

    /** The grid. */
    def gridStr = """\
%%%%%%%%%%%%%%%%%%%%
%..................%
%..................%
%..................%
%..%%%%%%%%%%%%%...%
%..................%
%..%...........%...%
%..%...........%...%
%..%...........%...%
%..%...........%...%
%..%...........%...%
%..%...........%...%
%..%...........%...%
%..................%
%..%%%%%%%%%%%%%...%
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

    public LOS() {
        // Objects for interacting with the console (like curses)
        terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"));
        screen = new Screen(terminal)

        // Where to draw the board on the screen
        boardPos = new Pos(0, 4)
        gameBoard = new GameBoard(gridStr, new Pos(10, 10) /* Player position */)
        gameBoard.createItems(200)
        gameBoardUtil = new GameBoardUtil(gameBoard, screen, numberOfTurns, boardPos)
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

    def traceLogic(int x, int y) {
        gameBoardUtil.drawBoard()
        println gameBoard.player.pos.lineOfSightTo(gameBoardUtil, new Pos(x, y))
        println "Waiting for input"
        while (screen.readInput() == null) {}
    }

    def rayTrace() {    
        //rayTraceTop()
        //rayTraceRight()
        //rayTraceBottom()
        //rayTraceLeft()
        //Right
        traceLogic(19, 8)
        traceLogic(19, 10)
        traceLogic(19, 12)

        // Left
        traceLogic(0, 8)
        traceLogic(0, 10)
        traceLogic(0, 12)

        // Top
        traceLogic(8, 0)
        traceLogic(10, 0)
        traceLogic(12, 0)

        // Bottom
        traceLogic(8, 19)
        traceLogic(10, 19)
        traceLogic(12, 19)

    }

    def rayTraceTop() {
        def y = 0
        (0 .. (gameBoard.numX - 1)).each { x ->
            traceLogic(x, y)
        }
    }

    def rayTraceRight() {
        def x = gameBoard.numX - 1
        (0 .. (gameBoard.numY - 1)).each { y ->
            traceLogic(x, y)
        }
    }

    def rayTraceBottom() {
        def y = gameBoard.numY - 1
        ((gameBoard.numX - 1) .. 0).each { x ->
            traceLogic(x, y)
        }
    }

    def rayTraceLeft() {
        def x = 0
        ((gameBoard.numY - 1) .. 0).each { y ->
            traceLogic(x, y)
        }
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
        rayTrace()
        tearDown()
    }

    /**
     * Kick things off.
     */
    public static void main(String[] args) {
        new LOS().execute()
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
    def drawBoard() {
        screen.clear()
        // Draw the board
        (0 ..< gameBoard.numY).each { y ->
            (0 ..< gameBoard.numX).each { x ->
                def playerDist = new Pos(x, y).distanceTo(gameBoard.player.pos)
                if (playerDist <= gameBoard.player.torchPower) {
                    writer.drawString(
                        boardPos.x + x, boardPos.y + y,
                        gameBoard.grid[y][x])
                }
            }
        }
        // Draw the items
        gameBoard.items.each { itemPos, item ->
            def playerDist = itemPos.distanceTo(gameBoard.player.pos)
            def cell
            if (playerDist <= gameBoard.player.torchPower) {
                writer.drawString(
                    boardPos.x + item.pos.x, 
                    boardPos.y + item.pos.y, 
                    item.symbol)
            }
        }
        // Draw the player
        writer.drawString(
                boardPos.x + gameBoard.player.pos.x, 
                boardPos.y + gameBoard.player.pos.y, 
                gameBoard.player.symbol)
        // Draw the score and such
        writer.drawString(0, 0, "Moves Left: ${movementPoints}")
        writer.drawString(0, 1, "Score     : ${score}")
        screen.refresh()
    }

    def clear() {
        screen.clear()
    }

    def plot(int x, int y, String symbol, color) {
        println "${x},${y} = ${gameBoard.grid[y][x]}"
        writer.setForegroundColor(Terminal.Color.YELLOW)
        writer.drawString(boardPos.x + x, boardPos.y + y, symbol)
        writer.setForegroundColor(Terminal.Color.DEFAULT)
        screen.refresh()
        Thread.sleep(25)
    }

    /**
     * Determine if the player can perform the Command command.
     * @param command the command that might be executed
     * @return true if the command can be executed
     */
    def canMovePlayer(Command command) {
        def result = false
        if (command.command == 'move') {
            def pos = gameBoard.at(new Pos(
                gameBoard.player.pos.x + command.delta.x,
                gameBoard.player.pos.y + command.delta.y))
            if (!GameBoard.WALLS.contains(pos)) {
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
            gameBoard.player.pos.x += command.delta.x
            gameBoard.player.pos.y += command.delta.y
            if (gameBoard.removeItem(gameBoard.player.pos)) {
                score++
            }
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
        def message
        if (command.command == 'quit') {
            message = ['###################',
                       '#                 #',
                       '#    You Quit!    #',
                       "#    Score ${lpad("${score}", 3)}    #",
                       '#                 #',
                       '###################']
        } else {
            message = ['###################',
                       '#                 #',
                       '#    Game Over!   #',
                       "#    Score ${lpad("${score}", 3)}    #",
                       '#                 #',
                       '###################']
        }
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
        Thread.sleep(3000)

    }

    /**
     * Left pad a string with spaces to a specific width.
     */
    def lpad(String str, int width) {
        StringUtils.leftPad(str, width, ' ')
    }

}

/**
 * Class for the game board, player, and items to pick up.
 */
class GameBoard {
    /** Characters that make up the walls. */
    final static WALLS = ['%', '-', '#']
    /** Characters that make up the floor. */
    final static FLOORS = ['.', ' ']
    /** The player character. */
    final static PLAYER = '@'
    /** Item character. */
    final static ITEMS = '$'
    final static TORCH = 'T'
    final static PIT = 'O'

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
        player = new Item(GameBoard.PLAYER, playerPos, 500)
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
    def createItems(int numToCreate) {
        (0 ..< numToCreate).each { i ->
            while (true) {
                Pos pos = new Pos(rand.nextInt(numX), rand.nextInt(numY))
                def symbol = at(pos)
                if (GameBoard.FLOORS.contains(symbol)) {
                    items[pos] = new Item(GameBoard.ITEMS, pos)
                    break
                }
            }
        }
    }

    /**
     * If there is an item at pos, remove it.
     * @return true if an item was removed
     */
    def removeItem(Pos pos) {
        Item item = items[pos]
        if (item != null) {
            items.remove(pos)
        }
        item != null
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
@ToString(includeNames=true)
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
     * Based on the final algorithm found
     * http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html
     */
    boolean lineOfSightTo(GameBoardUtil gameBoardUtil, Pos posToCheck) {
        boolean hasLos = true
        Pos playerPos = this
        int x0 = playerPos.x
        int y0 = playerPos.y
        int x1 = posToCheck.x
        int y1 = posToCheck.y
        println "Calculating LOS ${playerPos} to ${posToCheck}"

        int dx = Math.abs(x1 - x0)
        int dy = Math.abs(y1 - y0)
        int x = x0
        int y = y0
        int n = 1 + dx + dy
        int x_inc = (x1 > x0) ? 1 : -1
        int y_inc = (y1 > y0) ? 1 : -1
        int error = dx - dy
        dx *= 2
        dy *= 2

        def symbol, lastSymbol
        for (; n > 0; --n) {
            symbol = gameBoardUtil.gameBoard.at(new Pos(x, y))
            if (GameBoard.WALLS.contains(lastSymbol)) {
                hasLos = false
                break
            }
            if (error > 0) {
                x += x_inc
                error -= dy
            } else {
                y += y_inc
                error += dx
            }
            lastSymbol = symbol
        }
        hasLos
    }

    /**
     * Based on Bresenham's algorithm
     * http://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
     * Algorithm based directly on the "full implementation" algorithm here:
     * http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
     */
    boolean lineOfSightToOld(GameBoardUtil gameBoardUtil, Pos posToCheck) {
        boolean hasLos = true
        Pos playerPos = this
        int x = playerPos.x
        int y = playerPos.y
        int x2 = posToCheck.x
        int y2 = posToCheck.y
        int w = x2 - x 
        int h = y2 - y 
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 
        if (w < 0) {
            dx1 = -1  
        } else if (w > 0) {
            dx1 = 1 
        }
        if (h < 0) {
            dy1 = -1  
        } else if (h > 0) {
            dy1 = 1 
        }
        if (w < 0) {
            dx2 = -1  
        } else if (w > 0) {
            dx2 = 1 
        }
        int longest = Math.abs(w) 
        int shortest = Math.abs(h) 
        def symbol, lastSymbol
        if (!(longest > shortest)) {
            longest = Math.abs(h) 
            shortest = Math.abs(w) 
            if (h < 0) {
                dy2 = -1  
            } else if (h > 0)  {
                dy2 = 1 
            }
            dx2 = 0             
        }
        int numerator = longest >> 1 
        for (int i=0; i <= longest; i++) {
            symbol = gameBoardUtil.gameBoard.at(new Pos(x, y))
            if (GameBoard.WALLS.contains(lastSymbol)) {
                hasLos = false
                break
            }
            gameBoardUtil.plot(x, y, "*", Terminal.Color.YELLOW)
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
    // Remaining torch, currently only valid for the player (not other items)
    int torchPower

    /**
     * Construct the item.
     * @param symbol the visual symbol for the item.
     * @param pos the position of the item (in the grid)
     */
    public Item(String symbol, Pos pos, int torchPower = 0) {
        this.symbol = symbol
        this.pos = pos
        this.torchPower = torchPower
    }
}
