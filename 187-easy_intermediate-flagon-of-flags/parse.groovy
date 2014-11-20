/**
 * My solution to
 * http://www.reddit.com/r/dailyprogrammer/comments/2l6dll/11032014_challenge_187_easy_a_flagon_of_flags/
 */

def parser = new ArgParser()
println "test1:"
parser.exec("""4
a:all
f:force
n:networking
N:numerical-list
-aN 12 --verbose 192.168.0.44""".split("[\n\r]") as List)
parser.output()

println ""
println "test2:"
parser.exec("""6
a:all
*A:address
f:force
n:networking
N:numerical-list
*o:output
-aNo output-dir/file.txt 12 --address 1234 --verbose --address=192.168.0.44
-aNo output-dir/file.txt 12 --verbose --address 192.168.0.44""".split("[\n\r]") as List)
parser.output()

/**
 *
 */
class ArgParser {
    def config = [:]
    def results = [:]
    def fails = false

    /**
     * Parse given configuration and input.
     */
    def exec(lines) {
        // Read the configuration
        fails = false
        config.clear()
        results.clear()
        def configCount = lines.remove(0) as Integer
        (0..<configCount).each {
            // Create the arg from the line if configuration input
            def arg = new Arg(lines.remove(0))
            // Add an entry in the config Map to look up
            // by either flag or shortFlag
            config[arg.shortFlag] = arg
            config[arg.flag] = arg
        }

        // After the configuration are the lines of input
        while (lines) {
            def toParse = lines.remove(0)
            results[toParse] = parse(toParse)
        }
    }

    /**
     * After exec() has been called, call this to display output.
     */
    def output() {
        if (!fails) {
            results.each { toParse, result ->
                println "${toParse} parsed as:"
                result.each {
                    println it
                }
            }
        }
    }

    /**
     * Parse a single input line.
     * @param input the line of input to parse
     * @return An array of Arg and/or Parameter
     */
    def parse(input) {
        def result = []
        def parts = input.split(" ") as List
        while (parts) {
            def part = parts.remove(0)
            if (part.startsWith("--")) {
                // Long parameter
                def value
                if (part.contains("=")) {
                    (part, value) = part.split("=", 2)
                }
                // Strip off the --
                def flag = part.substring(2)
                def arg = config[flag]
                if (arg) {
                    // Duplicate the arg in case an arg
                    // is used twice, the value in config
                    // is just used as a template.
                    arg = new Arg(arg)
                    if (value) {
                        arg.value = value
                    } else if (arg.hasValue) {
                        // flag with parameter, retrieve the parameter
                        // This should be improved to handle the
                        // error when there isn't a next value to grab
                        arg.value = parts.remove(0)
                    }
                } else {
                    // We didn't find an arg for flag so create
                    // a new Arg for this flag.
                    arg = new Arg(value != null, "", flag)
                    arg.value = value
                }
                // Add the arg to the parsed result
                result << arg
            } else if (part.startsWith("-")) {
                // Short flag (or parameters)
                // Strip off the "-"
                part = part.substring(1)
                part.chars.each { shortFlag ->
                    // From the short flag, find the arg
                    def arg = config[shortFlag.toString()]
                    if (arg) {
                        // Duplicate the arg in case an arg
                        // is used twice, the value in config
                        // is just used as a template.                        
                        arg = new Arg(arg)
                        def value
                        if (arg.hasValue) {
                            // Short flag with parameter, retrieve the parameter
                            // This should be improved to handle the
                            // error when there isn't a next value to grab
                            arg.value = parts.remove(0)
                        }
                        result << arg
                    } else {
                        // Error, short flag not found in config
                        System.err.println "Flag: unrecognized short flag ${shortFlag}"
                        fails = true
                    }
                }
            } else {
                // Parameter (non flag)
                result << new Parameter(part)
            }
        }
        result
    }

    /**
     * A parameter (non-flag).
     */
    class Parameter {
        String value

        public Parameter(String value) {
            this.value = value
        }

        /**
         * How to display in output.
         */
        public String toString() {
            "parameter: ${value}"
        }
    }

    /**
     * An argument (flag).
     */
    class Arg {
        // Configuration of the arg
        String shortFlag
        String flag
        boolean hasValue = false

        // Actual value. Will not exist in configuration but
        // will exist in results, if this flag has a value.
        def value

        // Create an arg from a line of configuration
        public Arg(String input) {
            def parts = input.split(":", 2)
            def hasValue = false
            if (parts[0].startsWith("*")) {
                parts[0] = parts[0].substring(1)
                hasValue = true
            }
            this.hasValue = hasValue
            this.shortFlag = parts[0]
            this.flag = parts[1]
        }

        // Create an arg from configuration
        public Arg(boolean hasValue, String shortFlag, String flag) {
            this.hasValue = hasValue
            this.shortFlag = shortFlag
            this.flag = flag
        }

        // Create an Arg from an arg, but value will not be transferred
        public Arg(Arg toCopy) {
            this.hasValue = toCopy.hasValue
            this.shortFlag = toCopy.shortFlag
            this.flag = toCopy.flag
        }

        /**
         * How to display in output.
         */
        public String toString() {
            def result = "flag: ${flag}"
            if (hasValue) {
                result += " (value: ${value})"
            }
            result
        }
    }
}
