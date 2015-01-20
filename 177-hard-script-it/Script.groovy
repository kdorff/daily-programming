/**
 * Reddit Daily Programmer Challenge 177, Hard, Script It.
 * By Kevin Dorff
 * http://www.reddit.com/r/dailyprogrammer/comments/2exnal/8292014_challenge_177_hard_script_it_language/
 */

import java.util.regex.Pattern
import java.text.DecimalFormat

def script = new ScriptStats()
script.processScript(new File("holy.grail.txt"))
script.showStats()

/**
 * Class to parse a script and obtain statistics.
 */
class ScriptStats {
    /** The current line index. */
    int i
    /** All of the lines in the script (List of String) */
    List<String> lines
    /** The total number of lines in the script. */
    int numLines
    /** Pattern for the start of a scene. */
    Pattern sceneChangePattern = ~/^(Scene \d+|Narrative Interlude)$/

    /** All of the scenes. */
    List<Scene> scenes
    /** The current scene. */
    Scene currentScene
    /** All of the scenes. */
    Scene allScenes

    /**
     * Process a script.
     * @param script the File containing the script (text file).
     */
    def processScript(File script) {
        i = 0
        lines = script.readLines()
        numLines = lines.size()
        scenes = []
        currentScene = null
        while (processNextLine()) {}
        collateStats()
    }

    def collateStats() {
        allScenes = SceneUtil.collateFrom(new Scene("whole script"), scenes)
    }

    /**
     * Display the stats, scene by scene.
     */
    def showStats() {
        println "Number of scenes: ${scenes.size()}"
        scenes.each { scene ->
            scene.showStats()
        }
        allScenes.showStats()
    }

    /**
     * Process the next line of the script.
     * @return true if there are more lines to process
     */
    def processNextLine() {
        boolean moreToProcess = true
        def currentLine
        if (i <= numLines - 1) {
            currentLine = lines[i].trim()
            if (currentLine.matches(sceneChangePattern)) {
                // New scene. Finalize the previous scene then add the new
                // scene to the list.
                currentScene?.finalizeScene()
                currentScene = new Scene(currentLine)
                scenes << currentScene
            } else {
                if (currentScene) {
                    // We have a line in the script of the current scene.
                    // Processes that line.
                    currentScene.observeLine(currentLine)
                }
            }
        } else {
            // End of input. current=null marks end
            // of input. Finalize the current scene.
            moreToProcess = false
            currentScene?.finalizeScene()
        }
        i++
        // Are we done?
        moreToProcess
    }
}

/**
 * Class that stores stats for a single scene.
 */
class Scene {
    /** The name of the scene. */
    String name
    /**
     * Map of a word to the number of times that word 
     * has been seen per scene.
     */
    Map<String, Integer> wordToCount
    /** Map of number of lines a specific character has spoken. */
    Map<String, Integer> characterToLineCount
    /** Map of number of words a specific character has spoken. */
    Map<String, Integer> characterToWordCount
    /** Number of stage directions present in the scene. */
    int stageDirectionCount
    /** The current character. */
    String currentCharacter

    /**
     * Unprocessed text for the current character until characters change or
     * the scene ends.
     */
    StringBuilder unprocessedText

    /** Regex to match scene directions. */
    Pattern directionPattern = ~/^\[.*\]$/
    /** Regex to match spoken text including a character name. */
    Pattern spokenLinePattern = ~/^([A-Z0-9#? ]+):\s+(.*)$/

    /**
     * Contructor to process a new scene.
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name    
        wordToCount = [:]
        characterToLineCount = [:]
        characterToWordCount = [:]
        stageDirectionCount = 0
        currentCharacter = null
        unprocessedText = new StringBuilder()
    }

    /**
     * Display the stats for a scene.
     */
    def showStats() {
        println "Scene: ${name}"
        int numberOfSpokenLines = characterToLineCount.values().sum()
        println "Number of lines: ${numberOfSpokenLines}"
        println "Number of directions: ${stageDirectionCount}"
        int numberOfSpokenWords = wordToCount.values().sum()
        println "How many words are spoken: ${numberOfSpokenWords}"
        def topThreeWords = (wordToCount.keySet() as List)[0..3]
        print "Top 3 words:"
        (0..3).each { i ->
            def countPerWord = wordToCount[topThreeWords[i]]
            def percentagePerWord = (countPerWord / numberOfSpokenWords) * 100
            print "  ${topThreeWords[i]}: ${countPerWord} "
            println "(${new DecimalFormat("#.##").format(percentagePerWord)}%)"
        }
        println "Characters: (count: ${characterToWordCount.keySet().size()})"
        characterToWordCount.each { character, numWords ->
            print "  Character: ${character}, "
            print "lines: ${characterToLineCount[character]} "
            def percentageOfLines = 
                (characterToLineCount[character] / numberOfSpokenLines) * 100
            print "(${new DecimalFormat("#.##").format(percentageOfLines)}%), "
            print "words: ${numWords} "
            def percentageOfWords = (numWords / numberOfSpokenWords) * 100
            println "(${new DecimalFormat("#.##").format(percentageOfWords)}%)"
        }
        println ""
    }

    /**
     * Observe a line for the scene. This is NOT called with stage directions,
     * scene changes. This will detect the character name if prefixed with ":".
     * @param scriptLine a line from the screen for the current scene.
     */
    def observeLine(scriptLine) {
        def line = scriptLine
        if (line) {
            if (line.matches(directionPattern)) {
                // This line is not dialog, it's stage direction
                stageDirectionCount++
            } else {
                def matcher =  line =~ spokenLinePattern
                if (matcher.matches()) {
                    // This line of dialog contains the current speakers name
                    changeCharacter matcher[0][1]
                    observeSpokenLine matcher[0][2]
                } else {
                    // This line of dialog is a still the previous speaker
                    observeSpokenLine line   
                }
            }
        }
    }

    /**
     * Process text for the current character, we've either changed characters
     * or scenes. Process the line(s) of text for the current character.
     * This will split the text from a character into lines (ending in .?!)
     * and then split those lines into words for counting.
     */
    def processText() {
        if (unprocessedText.size() > 0) {
            def lines = unprocessedText.toString().split('[.?!]')
            lines.each { untrimmedLine ->
                def line = untrimmedLine.trim()
                if (line) {
                    // Increment line count for current character
                    SceneUtil.countMapAdd(
                        characterToLineCount, currentCharacter, 1)
                    line.findAll(~/[a-zA-Z-']+/).each { word ->
                        // Increment word count for current character
                        // and globally.
                        def lcWord = word.toLowerCase()
                        SceneUtil.countMapAdd(
                            characterToWordCount, currentCharacter, 1)
                        SceneUtil.countMapAdd(
                            wordToCount, lcWord, 1)
                    }
                }
            }
            unprocessedText.length = 0
        }
    }

    /**
     * The scene changed. Process the final lines in the scene
     * then Sort the maps, etc.
     */
    def finalizeScene() {
        processText()

        // Sort the count maps by count (value), decreasing
        wordToCount = wordToCount.sort { a, b ->
            b.value <=> a.value
        }
        characterToLineCount = characterToLineCount.sort { a, b ->
            b.value <=> a.value
        }
        characterToWordCount = characterToWordCount.sort { a, b ->
            b.value <=> a.value
        }
    }

    /**
     * A character has changed. Process the lines of dialog for the
     * previous character then set to the new character.
     */
    public void changeCharacter(String currentCharacter) {
        // Changing to a new character. Process all the text for
        // the previous character
        processText()
        this.currentCharacter = currentCharacter
    }

    /**
     * Collect the spoken line for the current character, appending
     * to previous lines in the script the character has spoken so
     * we can process all of that characters continuous lines all
     * at once.
     * @param line the line of dialog
     */
    def observeSpokenLine(line) {
        // Spoken text for a character. Append it to unprocessedText
        // until we change scene or character
        if (unprocessedText.size() > 0) {
            unprocessedText << " "
        }
        unprocessedText << line
    }
}

/**
 * Utility methods to assist with scenes.
 */
class SceneUtil {
    /**
     * Merge the list of Scene in froms into the object to.
     * @param to the Scene to collate the froms into
     * @param from the list of Scenes to collect into to
     */
    static Scene collateFrom(Scene to, List<Scene> froms) {
        froms.each { Scene from ->
            to.stageDirectionCount += from.stageDirectionCount
            from.wordToCount.each { k, v ->
                countMapAdd(to.wordToCount, k, v)
            }
            from.characterToLineCount.each { k, v ->
                countMapAdd(to.characterToLineCount, k, v)
            }
            from.characterToWordCount.each { k, v ->
                countMapAdd(to.characterToWordCount, k, v)
            }
        }
        to.finalizeScene()
        to
    }

    /**
     * We use a number of maps which contain a key to a counter. This will
     * take the map "map" at index "key" and increase by the value add.
     * If the map doesn't contain "key", a new entry will be added to the map
     * with the value add.
     */
    static void countMapAdd(Map<String, Integer> map, String key, int add) {
        map[key] = (map[key] == null) ? (add) : (map[key] + add)
    }
}
