class ISBN extends GroovyTestCase {
    void testCalc() {
        assert true == IsbnValidator.validate("0-7475-3269-9")
    }
}

class IsbnValidator {
    def static digitPattern = ~/([0-9X])/
    def static validate(String isbn) {
        def validates = false
        def finds = isbn.findAll(digitPattern).findAll().collect {
            (it == 'X') ? 10 : (it as Integer)
        }
        int sum = 0
        if (finds.size() == 10) {
            int multiplier = 10
            (0 .. finds.size()).each { v ->
                sum += multiplier-- * v
            }
            validates = sum % 11 == 0
        }
        validates
    }
}