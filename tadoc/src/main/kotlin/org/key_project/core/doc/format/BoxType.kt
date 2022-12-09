@file:Suppress("unused", "KDocUnresolvedReference")
package org.key_project.core.doc.format

/**
 * The pretty-printing boxes definition:
 * a pretty-printing box is either
 * - hbox: horizontal box (no line splitting)
 * - vbox: vertical box (every break hint splits the line)
 * - hvbox: horizontal/vertical box
 * (the box behaves as an horizontal box if it fits on
 * the current line, otherwise the box behaves as a vertical box)
 * - hovbox: horizontal or vertical compacting box
 * (the box is compacting material, printing as much material as possible
 * on every lines)
 * - box: horizontal or vertical compacting box with enhanced box structure
 * (the box behaves as an horizontal or vertical box but break hints split
 * the line if splitting would move to the left)
 */
enum class BoxType {
    Pp_hbox, Pp_vbox, Pp_hvbox, Pp_hovbox, Pp_box, Pp_fits
}