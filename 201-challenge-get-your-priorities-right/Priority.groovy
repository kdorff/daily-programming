import groovy.transform.*

def q = new DoublePriorityQueue()

q.enqueue("a", 2.0, 4.0)
q.enqueue("b", 5.0, 1.0)
q.enqueue("c", 3.0, 3.0)
q.enqueue("d", 1.0, 5.0)
q.enqueue("e", 4.0, 2.0)
q.dump()

println q.dequeue('x')
println q.dequeue('n')
println q.dequeue('a')
println q.dequeue('b')
println q.dequeue('a')
println q.dequeue('n')

/**
 * Implements a double priority queue. This should be pretty
 * easy to extend to be a n-priority queue.
 * This is NOT thread safe!!
 */
class DoublePriorityQueue {
    /** The list of the queues. */
    static QUEUES = ['n', 'a', 'b']

    /** Map of queue name (n, a, b) to the head of that priority queue. */
    Map<String, Item> heads = [:]
    /** Map of queue name (n, a, b) to the tail of that priority queue. */
    Map<String, Item> tails = [:]

    /**
     * Enqueue a value with priorityA and priorityB.
     * @param value the value to enqueue
     * @param priorityA the a-priority value
     * @param priorityA the b-priority value
     */
    def enqueue(String value, double priorityA, double priorityB) {
        def item = new Item()
        item.value = value
        item.priorities.a = priorityA
        item.priorities.b = priorityB
        if (!heads.n) {
            // Add to head, this is the only item
            heads.n = item
            tails.n = item
            heads.a = item
            tails.a = item
            heads.b = item
            tails.b = item
        } else {    
            // Queue of at least one item exists, add item to the queues
            enqueueNatural(item)
            QUEUES.minus('n').each { q ->
                enqueuePrioriy(item, q)
            }
        }
    }

    /**
     * Enqueue in natural order (FIFO).
     * @param item the item to enqueue
     */
    def enqueueNatural(item) {
        tails.n.nexts.n = item
        tails.n = item
    }

    /**
     * Enqueue based on the items's priority queue 'q' (n, a, b).
     * @param item the item to enqueue
     * @param q the queue to insert into (n, a, b)
     */
    def enqueuePrioriy(Item item, String q) {
        Item current = heads[q]
        Item previous = null
        def found = false
        while (current) {
            if (item.priorities[q] > current.priorities[q]) {
                // item goes BEFORE current
                if (previous == null) {
                    // Goes to head of list
                    item.nexts[q] = heads[q]
                    heads[q] = item
                } else {
                    // after previous, before current
                    previous.nexts[q] = item
                    item.nexts[q] = current
                }
                found = true
                break
            } else {
                // Add after current
                previous = current
                current = current.nexts[q]
            }
        }
        if (!found) {
            // Add to tail
            tails[q].nexts[q] = item
            tails[q] = item
        }
    }

    /**
     * Dequeue from insertion order.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueNatural() {
        dequeue('n')
    }

    /**
     * Dequeue from priority A.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueA() {
        dequeue('a')
    }

    /**
     * Dequeue from priority B.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueB() {
        dequeue('b')
    }

    /**
     * Dequeue from the specified queue.
     * @param q the queue to remove from
     */
    String dequeue(String q) {
        Item item
        if (QUEUES.contains(q)) {
            if (heads[q]) { 
                // Remove from the named queue
                item = heads[q]
                heads[q] = heads[q].nexts[q]
                QUEUES.minus(q).each { removeQ ->
                    // Remove from the other queues
                    remove item, removeQ
                }
            }
        }
        item?.value
    }

    /**
     * Remove specified item from the specified queue (n, a, b).
     * @param item the item to remove
     * @param q the queue to remove the item from
     */
    private void remove(Item item, String q) {
        if (item == heads[q]) {
            if (item == tails[q]) {
                // Head AND tail, removing the single element
                heads[q] = null
                tails[q] = null
            } else {
                // Just head
                heads[q] = heads[q].nexts[q]
            }
        } else {
            // Found somewhere after the head, at least two elements
            Item previous = heads[q]
            Item current = heads[q].nexts[q]
            while (current) {
                if (current == item) {
                    previous.nexts[q] = current.nexts[q]
                    // Found the item to delete
                    if (current == tails[q]) {
                        // Remove the tail
                        tails[q] = previous
                    }
                }
                previous = current
                current = current.nexts[q]
            }
        }
    }

    /**
     * Display the various queues.
     */
    def dump() {
        QUEUES.each { q ->
            Item current = heads[q]
            print "${q}="
            while (current) {
                print current
                current = current.nexts[q]
            }
            println ""
        }
        println "-------"
    }
}

@EqualsAndHashCode
class Item {
    String value
    Map<String, Double> priorities = [:]
    Map<String, Item> nexts = [:]

    String toString() {
        "[${value}:a(${priorities.a}) b(${priorities.b})]"
    }
}