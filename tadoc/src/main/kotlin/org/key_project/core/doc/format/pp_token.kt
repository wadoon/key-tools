@file:Suppress("unused", "KDocUnresolvedReference")

package org.key_project.core.doc.format

/** The pretty-printing tokens definition:
 * are either text to print or pretty printing
 * elements that drive indentation and line splitting.  */
abstract class pp_token {
    /** normal text  */
    data class Pp_text(val string: String) : pp_token()

    /** complete break  */
    object Pp_break : pp_token() { //TODO fits: string * int * string;   /* line is not split */
        //TODO breaks: string * int * string; /* line is split */
    }

    /** go to next tabulation  */
    data class Pp_tbreak(var a: Int = 0, var b: Int = 0) : pp_token()

    /** set a tabulation  */
    object Pp_stab : pp_token()

    /** beginning of a box  */
    data class Pp_begin(val off: Int, val box_type: BoxType) : pp_token()

    /** end of a box  */
    object Pp_end : pp_token()

    /* beginning of a tabulation box */
    class Pp_tbegin(var box: tbox? = null) : pp_token()

    /** end of a tabulation box  */
    object Pp_tend : pp_token()

    /** to force a newline inside a box  */
    object Pp_newline : pp_token()

    /** to do something only if this very line has been broken  */
    object Pp_if_newline : pp_token()

    /** opening a tag name  */
    class Pp_open_tag(var stag: String? = null) : pp_token()

    /** closing the most recently open tag */
    object Pp_close_tag : pp_token()
}
