package org.key_project.core.doc.format

@JvmInline
value class Size(private val obj: Int) {
    fun to_int(): Int = obj
    fun is_known(sz: Size?): Boolean {
        return obj >= 0
    }

    companion object {
        fun of_int(i: Int): Size {
            return Size(i)
        }

        fun zero(): Size {
            return of_int(0)
        }

        fun unknown(): Size {
            return of_int(-1)
        }
    }
}