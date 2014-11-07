def map = [:]
def total = 0
new File("candy.txt").eachLine { line ->
    total++
    map[line] = (map[line] ? (map[line] + 1) : 1)
}
println "Total = ${total}"
map.each { name, subTotal ->
    println "${subTotal} ${name} (${subTotal/total * 100}%)"
}
