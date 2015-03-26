import groovy.transform.*

def q = new DoublePriorityQueue()

assert q.size() == 0
q.enqueue("a", 2.0, 4.0)
assert q.size() == 1
q.enqueue("b", 5.0, 1.0)
assert q.size() == 2
q.enqueue("c", 3.0, 3.0)
assert q.size() == 3
q.enqueue("d", 1.0, 5.0)
assert q.size() == 4
q.enqueue("e", 4.0, 2.0)
assert q.size() == 5
q.enqueue("f", 4.0, 2.0)
assert q.size() == 6
println q

assert 'a' == q.dequeueNatural()
assert q.size() == 5
assert 'b' == q.dequeueA()
assert q.size() == 4
assert 'd' == q.dequeueB()
assert q.size() == 3
assert 'e' == q.dequeueA()
assert q.size() == 2
assert 'f' == q.dequeueA()
assert q.size() == 1
assert 'c' == q.dequeueNatural()
assert q.size() == 0

/**
 * Implements a double priority queue. This should be pretty
 * easy to extend to be a n-priority queue.
 * This is NOT thread safe!!
 */
class DoublePriorityQueue {
    /** The list of the queues (natural, a, b). */
    static QUEUES = ['n', 'a', 'b']

    /** Map of queue name (n, a, b) to the head of that priority queue. */
    Map<String, Item> heads = [:]
    /** Map of queue name (n, a, b) to the tail of that priority queue. */
    Map<String, Item> tails = [:]

    /** The size. */
    int size = 0

    /**
     * Obtain the current size of the queue.
     * @return the size of the queue
     */
    def size() {
        size
    }

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
        size++
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
    private String dequeue(String q) {
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
        if (item != null) {
            size--
        }
        item?.value
    }

    /**
     * Remove specified item from the specified queue (n, a, b).
     * @param item the item to remove
     * @param q the queue to remove the item from
     */
    private void remove(Item item, String q) {
        def removed = false
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
                    break
                }
                previous = current
                current = current.nexts[q]
            }
        }
    }

    /**
     * Display the various queues.
     */
    String toString() {
        def sb = new StringBuilder()
        QUEUES.each { q ->
            Item current = heads[q]
            sb << "${q}="
            while (current) {
                sb << current
                current = current.nexts[q]
            }
            sb << "\n"
        }
        sb << "-------" << "\n"
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