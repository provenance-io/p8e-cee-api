package io.provenance.api.util

fun Int.zeroPaddedString(pad: Int) = toString().padStart(pad, '0')
