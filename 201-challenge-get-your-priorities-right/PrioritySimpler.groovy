def q = new DoublePriorityQueueSimple()

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

assert 'a' == q.dequeueByInsertionOrder()
assert q.size() == 5
assert 'b' == q.dequeuePriorityA()
assert q.size() == 4
assert 'd' == q.dequeuePriorityB()
assert q.size() == 3
assert 'e' == q.dequeuePriorityA()
assert q.size() == 2
assert 'f' == q.dequeuePriorityA()
assert q.size() == 1
assert 'c' == q.dequeueByInsertionOrder()
assert q.size() == 0

/**
 * Implements a double priority queue.
 * This is NOT thread safe!!
 * This is an implementation of the double priority queue that uses a single
 * LinkedList to store the data and remembers the sort order of that data.
 * The sort is lost upon enqueue, but subsequent retrievals requiring the
 * same order will not need to resort (after the first resort) unless you
 * enqueue or decide to change the retrieval order (index, priorityA,
 * or priorityB).
 *
 * Benefits over PriorityOrig:
 * * Much simpler conceptually.
 * * Enqueue is always faster
 * * Repeated dequeue of the same priority is faster (after the first sort)
 * Benefits of PriorityOrig
 * * If retrieval priority changes frequently, retrieval is likely faster
 */
class DoublePriorityQueueSimple {
    /** The backing list. */
    LinkedList<PQItem> list = []

    /** The number of inserts, for making the index of the PQItems */
    int numInserts = 0

    /** The current sort. */
    PQItem.SortField currentSort = PQItem.SortField.NONE;

    /**
     * Enqueue a value with priorityA and priorityB. This will force a re-sort
     * upon next dequeue as this will just place the item to enqueue to the
     * tail of the existing list.
     * @param value the value to enqueue
     * @param priorityA the a-priority value
     * @param priorityA the b-priority value
     */
    def enqueue(String value, double priorityA, double priorityB) {
        currentSort = PQItem.SortField.NONE
        def item = new PQItem()
        item.value = value
        item.priorities[PQItem.SortField.PRI_A] = priorityA
        item.priorities[PQItem.SortField.PRI_B] = priorityB
        item.priorities[PQItem.SortField.INDEX] = numInserts++
        list << item
    }

    /**
     * The number of items in the priority queue.
     * @return the number of items currently queued
     */
    def size() {
        list.size()
    }

    /**
     * Dequeue from insertion order.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueByInsertionOrder() {
        sort(PQItem.SortField.INDEX)
        list.poll()?.value
    }

    /**
     * Dequeue from priority A.
     * @return null if queue is empty, otherwise the value
     */
    String dequeuePriorityA() {
        sort(PQItem.SortField.PRI_A)
        list.poll()?.value
    }

    /**
     * Dequeue from priority B.
     * @return null if queue is empty, otherwise the value
     */
    String dequeuePriorityB() {
        sort(PQItem.SortField.PRI_B)
        list.poll()?.value
    }

    /**
     * Re-sort the backing list based on the desired sort. If the list is
     * already sorted in the desired order, this will NOT resort the list.
     * @param desiredSort the sort / retrievalgit order that is desired
     */
    private void sort(PQItem.SortField desiredSort) {
        int result
        if (currentSort != desiredSort) {
            // Dequeue order has changed OR there has been an insertion
            // Resort
            currentSort = desiredSort
            list = list.sort { a, b ->
                if (currentSort == PQItem.SortField.INDEX) {
                    indexSort(a, b)
                } else {
                    prioritySort([currentSort, PQItem.SortField.INDEX], a, b)
                }
            }
        }
    }

    /**
     * Compare two items with insertion index ordering.
     * @param a left item to compare
     * @param b right item to compare
     */
    int indexSort(PQItem a, PQItem b) {
        // Sort in ascending order
        def field = PQItem.SortField.INDEX
        a.priorities[field] <=> b.priorities[field]
    }

    /**
     * Compare two items with priority a or b ordering.
     * @param a left item to compare
     * @param b right item to compare
     */
    int prioritySort(List<PQItem.SortField> sortFields, PQItem a, PQItem b) {
        int result = 0
        for (field in sortFields) {
            double aVal = a.priorities[field]
            double bVal = b.priorities[field]
            if (field != PQItem.SortField.INDEX) {
                // Index uses ascending sort, but priority a and b 
                // use descending sort, so swap a and b
                (aVal, bVal) = [bVal, aVal]
            }
            result = aVal <=> bVal
            if (result) {
                // We found a non-equal (0) value, we're done
                break
            }
        }
        result
    }

    /**
     * Display the various queues.
     */
    String toString() {
        def sb = new StringBuilder()
        sb << "currentSort=${currentSort} ["
        list.each { current ->
            sb << current
        }
        sb << "]\n"
    }
}

/**
 * The item being stored in the priority queue.
 */
class PQItem {
    /** The field to sort on. The keys of PQItem.priorities. */
    enum SortField {
        NONE, // Only used for current sort
        INDEX,
        PRI_A,
        PRI_B
    }

    /** The value of the item stored in the priority queue. */
    String value

    /** The priorities, keys such as a, b, and i (insertion index). */
    Map<SortField, Double> priorities = [:]

    /**
     * Convert this item to a String representation.
     * @return the string representation
     */
    String toString() {
        "[${value}[${priorities[SortField.INDEX]}]:a(${priorities[SortField.PRI_A]}) b(${priorities[SortField.PRI_B]})]"
    }
}
