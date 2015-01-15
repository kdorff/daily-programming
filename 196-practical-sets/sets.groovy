
def a = new MySet(-3, 5, 10, -3, -1, 15)
def b = new MySet(10, 15, -3, 25, 30)
println "a=" + a
println "b=" + b
println "u=" + MySet.union(a,b)
println "i=" + MySet.intersection(a,b)

def ac = MySet.complement(a)
println ac
println ac.contains(-3)
println ac.contains(-4)

class MySet {
    def data = []
    def complement = false
    public MySet(int... values) {
        values.each { value ->
            add value
        }
    }

    public MySet(MySet otherSet) {
        otherSet.data.each { value ->
            add value
        }
    }

    public void add(int value) {
        if (!data.contains(value)) {
            data << value
        }
    }

    public boolean contains(int value) {
        if (complement) {
            !data.contains(value)
        } else {
            data.contains(value)
        }
    }

    public boolean size() {
        data.size()
    }

    public static MySet union(MySet set1, MySet set2) {
        MySet result = new MySet(set1)
        set2.data.each { value ->
            result.add value
        }
        result
    }

    public static MySet intersection(MySet set1, MySet set2) {
        MySet result = new MySet()
        set1.data.each { value ->
            if (set2.contains(value)) {
                result.add value
            }
        }
        result
    }

    public static MySet complement(MySet set1) {
        MySet result = new MySet(set1)
        result.complement = true
        result
    }

    public String toString() {
        "{${data.sort().join(", ")}}${complement ? "'" : ""}"
    }

    public static boolean equals(MySet set1, MySet set2) {
        def result = true
        if (set1 == null && set2 == null) {
            result = true
        } else if (set1 == null || set2 == null) {
            result = false
        } else if (set1.size() != set2.size()) {
            result = false
        } else {
            // Same size, no nulls. Verify content.
            for (value in set1.data) {
                if (!set2.contains(value)) {
                    result = false
                    break
                }
            }
        }
        result
    }
}