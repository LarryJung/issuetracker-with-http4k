package com.larry.issuetracker.utils


fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C = { a ->
    f(this(a))
}