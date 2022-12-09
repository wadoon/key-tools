import java.util.LinkedList

class SmallJmlLexer(val content: String) {
    private val modeStack = ArrayDeque<Mode>()

    private var mark = 0;
    private val stream = content.toCharArray()

    init {
        modeStack.add(Mode.JAVA)
    }

    fun lex(): List<Token> {
        val ret = LinkedList<Token>()
        return ret
    }
}

enum class Mode { JAVA, JML, COMMENT, STRING }
enum class Type { JAVA, NEWLINE, }
data class Token(val content: String, val type: Type, val start: Int, val end: Int) {}
