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
    SortType currentSort = SortType.NONE;

    /**
     * Enqueue a value with priorityA and priorityB. This will force a re-sort
     * upon next dequeue as this will just place the item to enqueue to the
     * tail of the existing list.
     * @param value the value to enqueue
     * @param priorityA the a-priority value
     * @param priorityA the b-priority value
     */
    def enqueue(String value, double priorityA, double priorityB) {
        currentSort = SortType.NONE
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
    String dequeueNatural() {
        sort(SortType.INDEX)
        list.poll()?.value
    }

    /**
     * Dequeue from priority A.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueA() {
        sort(SortType.PRI_A)
        list.poll()?.value
    }

    /**
     * Dequeue from priority B.
     * @return null if queue is empty, otherwise the value
     */
    String dequeueB() {
        sort(SortType.PRI_B)
        list.poll()?.value
    }

    /**
     * Re-sort the backing list based on the desired sort. If the list is
     * already sorted in the desired order, this will NOT resort the list.
     */
    private void sort(SortType desiredSort) {
        int result
        if (currentSort != desiredSort) {
            // Dequeue order has changed OR there has been an insertion
            // Resort
            currentSort = desiredSort
            List<SortType> sortDescList = currentSort.sortDescList
            list = list.sort { a, b ->
                if (sortDescList) {
                    SortDesc priSort = sortDescList[0]
                    double aValue = a.priorities[priSort.sortField]
                    double bValue = b.priorities[priSort.sortField]
                    if (aValue == bValue) {
                        // Secondary sort? We supprort 2 levels of sort at most
                        if (sortDescList.size() > 1) {
                            SortDesc secSort = sortDescList[1]
                            aValue = a.priorities[secSort.sortField]
                            bValue = b.priorities[secSort.sortField]
                            if (secSort.sortOrder == SortDesc.SortOrder.ASC) {
                                result = aValue <=> bValue
                            } else {
                                result = bValue <=> aValue
                            }
                        } else {
                            // Only a single sort type. They are equal.
                            0
                        }
                    } else {
                        if (priSort.sortOrder == SortDesc.SortOrder.ASC) {
                            result = aValue <=> bValue
                        } else {
                            result = bValue <=> aValue
                        }
                    }
                }
            }
            result
        }
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

/**
 * A sort description (key and order).
 */
class SortDesc {
    /** Possible sort directions. */
    enum SortOrder {
        ASC,
        DESC,
    }

    /** The key to sort (key from PQItem.priorities, one of SortField). */
    PQItem.SortField sortField
    /** The order to sort. */
    SortOrder sortOrder

    /**
     * Construct a SortDesc.
     * @param sortField field to sort by
     * @param sortOrder the order (asc, desc) to sort this field by
     */
    public SortDesc(PQItem.SortField sortField, SortOrder sortOrder) {
        this.sortField = sortField
        this.sortOrder = sortOrder
    }
}

/**
 * The sort type for a given priority queue.
 */
enum SortType {
    // No sorting
    NONE(null),
    // Sorting on insertion order (ascending)
    INDEX([new SortDesc(PQItem.SortField.INDEX, SortDesc.SortOrder.ASC)]),
    // Sorting on priorityA (descending), index (ascending)
    PRI_A([new SortDesc(PQItem.SortField.PRI_A, SortDesc.SortOrder.DESC),
           new SortDesc(PQItem.SortField.INDEX, SortDesc.SortOrder.ASC)]),
    // Sorting on priorityB (descending), index (ascending)
    PRI_B([new SortDesc(PQItem.SortField.PRI_B, SortDesc.SortOrder.DESC),
           new SortDesc(PQItem.SortField.INDEX, SortDesc.SortOrder.ASC)])

    /** Storage for the per-enum sort description. */
    private final List<SortDesc> sortDescList

    /**
     * Enum initializer, one per entry.
     * @param sortDescList the list of SortDesc sort descriptions.
     */
    public SortType(List<SortDesc> sortDescList) {
        this.sortDescList = sortDescList
    }
}
