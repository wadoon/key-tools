package org.key_project.core.doc.format

import org.key_project.core.doc.format.BoxType.*
import java.util.*
import kotlin.math.min

/** The pretty-printer queue:
pretty-printing material is not written in the output as soon as emitted;
instead, the material is simply recorded in the pretty-printer queue,
until the enclosing box has a known computed size and proper splitting
decisions can be made.

The pretty-printer queue contains formatting elements to be printed.
Each formatting element is a tuple (size, token, length), where
- length is the declared length of the token,
- size is effective size of the token when it is printed
(size is set when the size of the box is known, so that size of break
hints are definitive). */
data class pp_queue_elem(
    val size: Size = Size.unknown(),
    val token: pp_token? = null,
    val length: Int = 0
)

/** The pretty-printer queue definition. */
class pp_queue(private val impl: LinkedList<pp_queue_elem> = LinkedList<pp_queue_elem>()) :
    Queue<pp_queue_elem> by impl

/** The pretty-printer scanning stack.  The pretty-printer scanning stack: scanning element definition. */
class pp_scan_elem {
    /* Value of pp_left_total when the element was enqueued. */
    val left_total: Int = 0
    val queue_elem: pp_queue_elem = pp_queue_elem()
}

data class tbox(val first: Int, val ref: List<Any>)

/* The pretty-printer formatting stack:
the formatting stack contains the description of all the currently active
boxes; the pretty-printer formatting stack is used to split the lines
while printing tokens. */

/** The pretty-printer formatting stack: formatting stack element definition.
Each stack element describes a pretty-printing box. */
data class pp_format_elem(val box_type: BoxType, val width: Int)


/** The formatter specific tag handling functions. */
interface formatter_stag_functions {
    fun mark_open_stag(stag: String): String
    fun mark_close_stag(stag: String): String
    fun print_open_stag(stag: String)
    fun print_close_stag(stag: String)
}

/* The formatter functions to output material. */
interface formatter_out_functions {
    fun out_string(s: String, a: Int, b: Int)
    fun out_flush()
    fun out_newline(a: Int)
    fun out_spaces(a: Int)
    fun out_indent(a: Int)
}


/** Auxiliaries and basic functions.
Enter a token in the pretty-printer queue. */
fun pp_enqueue(state: formatter, token: pp_queue_elem) {
    state.pp_right_total = state.pp_right_total + token.length
    state.pp_queue.add(token)
}

fun pp_clear_queue(state: formatter) {
    state.pp_left_total = -1
    state.pp_right_total = -1
    state.pp_queue.clear()
}


/** Pp_infinity: large value for default tokens size.

Pp_infinity is documented as being greater than 1e10; to avoid
confusion about the word 'greater', we choose pp_infinity greater
than 1e10 + 1; for correct handling of tests in the algorithm,
pp_infinity must be even one more than 1e10 + 1; let's stand on the
safe side by choosing 1.e10+10.

Pp_infinity could probably be 1073741823 that is 2^30 - 1, that is
the minimal upper bound for integers; now that max_int is defined,
this limit could also be defined as max_int - 1.

However, before setting pp_infinity to something around max_int, we
must carefully double-check all the integer arithmetic operations
that involve pp_infinity, since any overflow would wreck havoc the
pretty-printing algorithm's invariants. Given that this arithmetic
correctness check is difficult and error prone and given that 1e10
+ 1 is in practice large enough, there is no need to attempt to set
pp_infinity to the theoretically maximum limit. It is not worth the
burden ! */
const val pp_infinity = 1000000010


/* Output functions for the formatter. */
fun pp_output_string(state: formatter, s: String) = state.pp_out_string(s, 0, s.length)
fun pp_output_newline(state: formatter) = state.pp_out_newline()
fun pp_output_spaces(state: formatter, n: Int) = state.pp_out_spaces(n)
fun pp_output_indent(state: formatter, n: Int) = state.pp_out_indent(n)


/* Format a textual token */
fun format_pp_text(state: formatter, size: Int, text: String) {
    state.pp_space_left -= size
    pp_output_string(state, text)
    state.pp_is_new_line = false
}

/* Format a string by its length, if not empty */
fun format_string(state: formatter, s: String) {
    if (s != "") format_pp_text(state, s.length, s)
}

/* To format a break, indenting a new line. */
fun break_new_line(state: formatter, before: String, offset: Int, after: String, width: Int) {
    format_string(state, before)
    pp_output_newline(state)
    state.pp_is_new_line = true
    val indent = state.pp_margin - width + offset
    /* Don't indent more than pp_max_indent. */
    val real_indent = min(state.pp_max_indent, indent)
    state.pp_current_indent = real_indent
    state.pp_space_left = state.pp_margin - state.pp_current_indent
    pp_output_indent(state, state.pp_current_indent)
    format_string(state, after)
}

/* To force a line break inside a box: no offset is added. */
fun break_line(state: formatter, width: Int) = break_new_line(state, "", 0, "", width)

/* To format a break that fits on the current line. */
fun break_same_line(state: formatter, before: String, width: Int, after: String) {
    format_string(state, before)
    state.pp_space_left = state.pp_space_left - width
    pp_output_spaces(state, width)
    format_string(state, after)
}


/* To indent no more than pp_max_indent, if one tries to open a box
beyond pp_max_indent, then the box is rejected on the left
by simulating a break. */
fun pp_force_break_line(state: formatter) {
    if (state.pp_format_stack.isEmpty())
        pp_output_newline(state)
    else {
        val (box_type, width) = state.pp_format_stack.peek()
        when {
            width > state.pp_space_left ->
                when (box_type) {
                    Pp_fits, Pp_hbox -> {}
                    Pp_vbox, Pp_hvbox, Pp_hovbox, Pp_box -> break_line(state, width)
                }
        }
    }
}

/* To skip a token, if the previous line has been broken. */
fun pp_skip_token(state: formatter) {
    if (state.pp_queue.isEmpty())
        return
    else {
        val (size, _, length) = state.pp_queue.poll()
        state.pp_left_total = state.pp_left_total - length
        state.pp_space_left = state.pp_space_left + size.to_int()
    }
}


/* Print if token size is known else printing is delayed.
Printing is delayed when the text waiting in the queue requires
more room to format than exists on the current line. */
tailrec fun advance_left(state: formatter) {
    if (state.pp_queue.isNotEmpty()) {
        val (size, token, length) = state.pp_queue.poll()
        val pending_count = state.pp_right_total - state.pp_left_total
        if (Size.is_known(size) || pending_count >= state.pp_space_left) {
            state.pp_queue.poll()  /* Not empty: we peek into it */
        }
        val size = if Size.is_known(size) Size . to_int (size) else pp_infinity
        format_pp_token(state, size, token)
        state.pp_left_total = length + state.pp_left_total
        advance_left(state)
    }
}


/* To enqueue a token : try to advance. */
fun enqueue_advance(state: formatter, tok: pp_queue_elem) {
    pp_enqueue(state, tok)
    advance_left(state)
}


/* To enqueue strings. */
fun enqueue_string_as(state: formatter, size: Int, s.String)
    = enqueue_advance(state, pp_queue_elem(size,pp_token.Pp_text(s), Size.to_int(size)))


fun enqueue_string(state: formatter, s: String) = enqueue_string_as(state, Size.of_int(s.length)), s)

/* region The main pretty printing functions. */
/* Formatting a token with a given size. */
fun format_pp_token(state: formatter, size: Int, ppToken: pp_token) {
    when (ppToken) {
        is pp_token.Pp_text -> format_pp_text(state, size, ppToken.string)
        is pp_token.Pp_begin -> {
            val insertion_point = state.pp_margin - state.pp_space_left
            /* can not open a box right there. */
            if (insertion_point > state.pp_max_indent)
                pp_force_break_line(state)
            val width = state.pp_space_left - ppToken.off
            val box_type = when (ppToken.box_type) {
                Pp_vbox -> Pp_vbox
                else ->
                    if (size > state.pp_space_left) ppToken.box_type else Pp_fits
            }
            state.pp_format_stack.push(pp_format_elem(box_type, width))
        }

        is pp_token.Pp_end -> state.pp_format_stack.pop()
        is pp_token.Pp_tbegin -> state.pp_tbox_stack.push(ppToken.box)
        is pp_token.Pp_tend -> state.pp_tbox_stack.pop()

        is pp_token.Pp_newline -> {
            if (state.pp_format_stack.isEmpty())
                pp_output_newline(state) /* No open box. */
            else break_line(state, state.pp_format_stack.pop().width)
        }

        is pp_token.Pp_if_newline ->
            if (state.pp_current_indent != state.pp_margin - state.pp_space_left)
                pp_skip_token(state)
        /*
        is pp_token.Pp_stab -> {
            if (state.pp_tbox_stack.isEmpty())
                return
            val tabs = state.pp_tbox_stack.pop()
            fun add_tab(n: Int, obj) = when (obj) {
                obj.isEmpty() -> false
                obj.size == 1 -> listOf(obj.first())
                -> x::l as ls
                -> if n < x then n::ls else x::add_tab n l
            }
            tabs = add_tab(state.pp_margin - state.pp_space_left)!tabs
        }

        is pp_token.Pp_tbreak -> {
            let insertion_point = state . pp_margin -state.pp_space_left in
                    begin match Stack.top_opt state . pp_tbox_stack with
            | None -> () /* No open tabulation box. */
            | Some(Pp_tbox tabs) ->
            let tab =
            match !tabs with
            | [] -> insertion_point
            | first::_ ->
            let rec find = function
            | head::tail ->
            if head >= insertion_point then head else find tail
            | [] -> first in
            find !tabs in
            let offset = tab -insertion_point in
                    if offset >= 0
            then break_same_line state("", offset + n, "")
            else break_new_line state ("", tab+off, "") state.pp_margin
        }
        is pp_token.Pp_break ->
            let before

            , off, _ = breaks in
                begin match Stack.top_opt state . pp_format_stack with
            | None

        -> () /* No open box. */
            | Some { box_type; width }
        ->
            begin match box_type with

            | Pp_hovbox
        ->
            if size + String.length before > state . pp_space_left
                    then break_new_line state breaks width
            else break_same_line state fits

        is Pp_box ->
            /* Have the line just been broken here ? */
            if state.pp_is_new_line then break_same_line state fits else
                if size + String.length before > state . pp_space_left
                        then break_new_line state breaks width else
                /* break the line here leads to new indentation ? */
                    if state.pp_current_indent > state.pp_margin - width + off

        then break_new_line state breaks width
        else break_same_line state fits
            |

        Pp_hvbox -> break_new_line state breaks width
            | Pp_fits
        -> break_same_line state fits
            | Pp_vbox
        -> break_new_line state breaks width
            | Pp_hbox
        -> break_same_line state fits
        end
                end

                is Pp_open_tag tag_name ->
            let marker = state . pp_mark_open_tag tag_name in
                    pp_output_string state marker;
        Stack.push tag_name state.pp_mark_stack

            | Pp_close_tag

        ->
            begin match Stack.pop_opt state . pp_mark_stack with

            | None
        -> () /* No more tag to close. */
            | Some tag_name
        ->
            let marker = state . pp_mark_close_tag tag_name in
                    pp_output_string state marker

        end
         */
    }
}
