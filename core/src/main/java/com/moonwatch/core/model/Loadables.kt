package com.moonwatch.core.model

sealed class Loadable<out T> {
    open val copyWithLoadingInProgress: Loadable<T>
        get() = LoadingFirst

    open val copyWithClearedError: Loadable<T>
        get() = Empty

    open fun copyWithError(error: Throwable?): Loadable<T> = FailedFirst(error)

    abstract fun <R> map(block: (T) -> R): Loadable<R>

    inline fun <reified E> isFailedWith(): Boolean = (this as? Failed)?.error is E
}

sealed class WithValue<T> : Loadable<T>() {
    abstract val value: T
}

sealed class WithoutValue : Loadable<Nothing>()

object Empty : WithoutValue() {
    override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

interface LoadingInProgress

object LoadingFirst : WithoutValue(), LoadingInProgress {
    override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

data class LoadingNext<T>(
    override val value: T,
) : WithValue<T>(), LoadingInProgress {
    override val copyWithLoadingInProgress: Loadable<T>
        get() = this

    override val copyWithClearedError: Loadable<T>
        get() = this

    override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

    override fun <R> map(block: (T) -> R): LoadingNext<R> = LoadingNext(block(value))
}

interface Failed {
    val error: Throwable?
}

data class FailedFirst(override val error: Throwable?) : WithoutValue(), Failed {
    override val copyWithLoadingInProgress: LoadingFirst
        get() = LoadingFirst

    override fun <R> map(block: (Nothing) -> R): Loadable<R> = this
}

data class FailedNext<T>(
    override val value: T,
    override val error: Throwable?,
) : WithValue<T>(), Failed {
    override val copyWithClearedError: Ready<T>
        get() = Ready(value)

    override val copyWithLoadingInProgress: Loadable<T>
        get() = LoadingNext(value)

    override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

    override fun <R> map(block: (T) -> R): FailedNext<R> = FailedNext(block(value), error)
}

data class Ready<T>(override val value: T) : WithValue<T>() {
    override val copyWithLoadingInProgress: LoadingNext<T>
        get() = LoadingNext(value)

    override val copyWithClearedError: Loadable<T>
        get() = this

    override fun copyWithError(error: Throwable?): FailedNext<T> = FailedNext(value, error)

    override fun <R> map(block: (T) -> R): WithValue<R> = Ready(block(value))
}
