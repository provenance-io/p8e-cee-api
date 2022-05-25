package io.provenance.api.util.hamkrest

import com.natpryce.hamkrest.equalTo

val isTrue = equalTo(true)
val isFalse = !isTrue
