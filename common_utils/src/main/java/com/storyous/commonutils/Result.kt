package com.storyous.commonutils

import androidx.annotation.IntDef
import androidx.annotation.IntRange

data class Result<T>(
    @Type val type: Int,
    val data: T? = null,
    val exception: Throwable? = null,
    @IntRange(from = 0L, to = 100L) val progress: Int = 0) {

    companion object {
        @JvmOverloads
        fun <T> loading(
            @IntRange(from = 0L, to = 100L) progress: Int = 0
        ) = Result<T>(LOADING, progress = progress)

        fun <T> success(
            data: T? = null
        ) = Result<T>(SUCCESS, data, progress = PROGRESS_DONE)

        fun <T> failure(
            exception: Throwable?
        ) = Result<T>(ERROR, exception = exception, progress = PROGRESS_DONE)

        @IntDef(LOADING, SUCCESS, ERROR)
        annotation class Type

        const val LOADING = 0
        const val SUCCESS = 1
        const val ERROR = 2

        const val PROGRESS_DONE = 100
    }

    fun isLoading() = type == LOADING
    fun isError() = type == ERROR
    fun isSuccess() = type == SUCCESS
}

inline fun <reified T> kotlin.Result<T>.toResult(): Result<T> {
    return exceptionOrNull()?.let { Result.failure(it) } ?: Result.success(getOrNull())
}
