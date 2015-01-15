
def a = new MySet<Double>(-3.0, 5.01, 10.0, -3.0, -1.0, 15.0)
def b = new MySet<Double>(10.0, 15.0, -3.0, 25.0, 30.0)
println "a=" + a
println "b=" + b
println "u=" + MySet.union(a,b)
println "i=" + MySet.intersection(a,b)

def ac = MySet.complement(a)
println ac
println ac.contains(-3)
println ac.contains(-4)

class MySet<T> {
    List<T> data = new ArrayList<T>()
    boolean complement = false
    public MySet(T... values) {
        values.each { value ->
            add value
        }
    }

    public MySet(MySet<T> otherSet) {
        otherSet.data.each { value ->
            add value
        }
    }

    public void add(T value) {
        if (!data.contains(value)) {
            data << value
        }
    }

    public boolean contains(T value) {
        if (complement) {
            !data.contains(value)
        } else {
            data.contains(value)
        }
    }

    public boolean size() {
        data.size()
    }

    public static MySet<T> union(MySet<T> set1, MySet<T> set2) {
        MySet<T> result = new MySet<T>(set1)
        set2.data.each { value ->
            result.add value
        }
        result
    }

    public static MySet<T> intersection(MySet<T> set1, MySet<T> set2) {
        MySet<T> result = new MySet<T>()
        set1.data.each { value ->
            if (set2.contains(value)) {
                result.add value
            }
        }
        result
    }

    public static MySet<T> complement(MySet<T> set1) {
        MySet<T> result = new MySet<T>(set1)
        result.complement = true
        result
    }

    public String toString() {
        "{${data.sort().join(", ")}}${complement ? "'" : ""}"
    }

    public static boolean equals(MySet<T> set1, MySet<T> set2) {
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