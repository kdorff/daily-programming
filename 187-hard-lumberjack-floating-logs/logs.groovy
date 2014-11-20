
/**
 * Reddit Daily Programmer for 2014-11-05 (hard).
 * http://www.reddit.com/r/dailyprogrammer/comments/2lljyq/11052014_challenge_187_hard_lumberjack_floating/
 * 
 * Path finding problem. Fill a lake with logs.
 * Or I link to think of it as paths between two cities
 * in ticket to ride.
 */
(new Logs()).exec([
    [name: "A", destName: "B", paths: 6],
    [name: "A", destName: "C", paths: 2],
    [name: "B", destName: "E", paths: 3],
    [name: "B", destName: "D", paths: 3],
    [name: "D", destName: "C", paths: 2],
    [name: "D", destName: "F", paths: 1],
    [name: "C", destName: "G", paths: 5],
    [name: "E", destName: "H", paths: 1],
    [name: "E", destName: "I", paths: 2],
    [name: "F", destName: "H", paths: 1],
    [name: "G", destName: "H", paths: 2],
    [name: "G", destName: "I", paths: 2],
    [name: "H", destName: "I", paths: 4],
])

class Logs {
    /**
     * Map to quickly look up a Node from a node name
     */
    Map<String, Node> nameToNode = [:]

    /**
     * All paths found during findShortestPath() to
     * select the shortest path.
     */
    def foundPaths = []
    def allShorestPaths = [:]
    def debugOn = false

    def debug(message) {
        if (debugOn) {
            println message
        }
    }

    def exec(paths) {
        // Obtain the string names of the graph nodes
        def nodeNames = paths*.name as Set
        nodeNames.addAll paths*.destName
        // Create Node objects for each node in the graph
        nodeNames.sort().each { name ->
            nameToNode[name] = new Node(name)
        }
        paths.each { path ->
            nameToNode[path.name].addEdge(nameToNode[path.destName], path.paths)   
        }
        displayGraph()

        // Primary loop, keep finding shortest path (log paths)
        // until the river is full.
        int sent = 0
        while (true) {
            // Find the current shorest path
            def shortestPath = findShortestPath()
            if (shortestPath) {
                // Found a path, mark it as used
                consumePath(shortestPath)
                // Save the path for final output
                allShorestPaths[++sent] = shortestPath.clone()
                displayGraph()
            } else {
                break
            }
        }
        // Final output
        def numLogs = 0
        allShorestPaths.each { log, path ->
            println "Log #${log} takes ${path*.name.join("->")} - path of ${path.size()}"
            numLogs += path.size()
        }
        println "River is now full. Can send ${numLogs} logs."
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
        findAllPaths(nameToNode["A"], [])
        // Sort the found paths by size of the path,
        // we just want the smallest one.
        foundPaths.sort { a, b ->
            a.size - b.size()
        }
        // Debug output
        foundPaths.each { pathList ->
            debug pathList*.name
        }
        // Return the first smallest path that was found
        foundPaths[0]
    }

    /**
     * Find the path from startNode to endNode that is
     * both available and shorest.
     */
    def findAllPaths(startNode, nodeList) {
        if (!nodeList) {
            // First time through, insert the start node
            nodeList << startNode
        }
        // Find all of the available paths from this
        // node and visit them all
        startNode.openPaths().each { subNode ->
            nodeList << subNode
            if (!subNode.edgeToNode) {
                // New COMPLETE path, save it
                def completeList = []
                completeList.addAll nodeList
                foundPaths.add completeList
                // Pop the end of nodeList for the next iteration
                nodeList.remove(nodeList.size() - 1)
                debug "Found a complete path"
                debug completeList*.name
            } else {
                // Path is not yet complete. Recurse down
                // the next level.
                debug "Finding path from ${startNode.name} to ${subNode.name}"
                // Search down the next sub path
                findAllPaths(subNode, nodeList)
                // Pop the end of nodeList for the next iteration
                nodeList.remove(nodeList.size() - 1)
            }
        }
    }

    /**
     * Mark a found shortest path from A->I as consumed.
     */
    def consumePath(path) {
        debug "Consuming Path ${path*.name}"
        for (int i = 0; i < path.size() - 1; i++) {
            def node = path[i]
            def nextNode = path[i + 1]
            node.edgeToNode[nextNode].usedPaths++
        }
    }

    /**
     * Output the state of the graph (if debug is on).
     */
    def displayGraph() {
        nameToNode.each { name, node ->
            debug node.name
            node.edgeToNode.each { subNode, numPaths ->
                debug "   ${subNode.name} ${numPaths}"
            }
        }
    }
}

/**
 * Class to represent the node in a graph
 */
class Node {
    // The name of the node (such as A, B or I)
    String name
    // This map represents edges from this node to 
    // other nodes (key) and details about the number
    // of times the edge can be / has been used (value).
    Map<Node, Map> edgeToNode = [:]

    /** Initialize. */
    public Node(name) {
        this.name = name
    }

    /** Initialize. */
    def addEdge(Node dest, int numPaths) {
        edgeToNode[dest] = [numPaths: numPaths, usedPaths: 0]
    }

    /**
     * For this node, return a list of Nodes that
     * can still be accessed from this node (the edge
     * to that node hasn't been used up).
     */
    def openPaths() {
        def paths = []
        edgeToNode.each { dest, detailsMap ->
            if (detailsMap.usedPaths < detailsMap.numPaths) {
                paths << dest
            }
        }
        paths
    }
}