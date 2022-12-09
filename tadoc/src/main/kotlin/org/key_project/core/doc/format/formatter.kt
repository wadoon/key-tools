package org.key_project.core.doc.format

import java.util.*

/** The formatter definition.
 * Each formatter value is a pretty-printer instance with all its
 * machinery.  */
abstract class formatter {
    /* The pretty-printer scanning stack. */
    val pp_scan_stack: Stack<pp_scan_elem> = Stack()

    /** The pretty-printer formatting stack.  */
    val pp_format_stack: Stack<pp_format_elem> = Stack()
    val pp_tbox_stack: Stack<tbox> = Stack()

    /* The pretty-printer semantics tag stack. */
    val pp_tag_stack: Stack<String> = Stack()
    val pp_mark_stack: Stack<String> = Stack()

    /* Value of right margin. */
    var pp_margin = 0

    /* Minimal space left before margin, when opening a box. */
    var pp_min_space_left = 0

    /* Maximum value of indentation:
no box can be opened further. */
    var pp_max_indent = 0

    /* Space remaining on the current line. */
    var pp_space_left = 0

    /* Current value of indentation. */
    var pp_current_indent = 0

    /* True when the line has been broken by the pretty-printer. */
    var pp_is_new_line = false

    /* Total width of tokens already printed. */
    var pp_left_total = 0

    /* Total width of tokens ever put in queue. */
    var pp_right_total = 0

    /* Current number of open boxes. */
    var pp_curr_depth = 0

    /* Maximum number of boxes which can be simultaneously open. */
    var pp_max_boxes = 0

    /* Ellipsis string. */
    var pp_ellipsis: String? = null

    /* Output function. */
    abstract fun pp_out_string(s: String?, a: Int, b: Int)

    /* Flushing function. */
    abstract fun pp_out_flush()

    /* Output of new lines. */
    abstract fun pp_out_newline()

    /* Output of break hints spaces. */
    abstract fun pp_out_spaces(a: Int)

    /* Output of indentation of new lines. */
    abstract fun pp_out_indent(a: Int)

    /* Are tags printed ? */
    abstract fun pp_print_tags(): Boolean

    /* Are tags marked ? */
    abstract fun pp_mark_tags(): Boolean

    /* Find opening and closing markers of tags. */
    abstract fun pp_mark_open_tag(stag: String?): String?
    abstract fun pp_mark_close_tag(stag: String?): String?
    abstract fun pp_print_open_tag(stag: String?)
    abstract fun pp_print_close_tag(stag: String?)
    var pp_queue: pp_queue = pp_queue()
}