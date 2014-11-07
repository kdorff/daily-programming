
import groovy.transform.ToString

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
    Map<String, Node> nameToNode = [:]
    def exec(paths) {
        def nodeNames = paths*.name as Set
        nodeNames.addAll paths*.destName
        nodeNames.sort().each { name ->
            nameToNode[name] = new Node(name)
        }
        paths.each { path ->
            nameToNode[path.name].addPath(nameToNode[path.destName], path.paths)   
        }
        nameToNode.each { name, node ->
            println node.name
            node.pathToPathCount.each { subNode, numPaths ->
                println "   ${subNode.name} ${numPaths}"
            }
        }
    }
}

@ToString(includeNames=true)
class Node {
    String name
    Map<Node, Integer> pathToPathCount = [:]
    public Node(name) {
        this.name = name
    }
    public addPath(Node dest, int numPaths) {
        pathToPathCount[dest] = [numPaths: numPaths, usedPaths: 0]
    }
}