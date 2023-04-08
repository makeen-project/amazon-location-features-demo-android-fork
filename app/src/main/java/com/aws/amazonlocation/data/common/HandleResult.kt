package com.aws.amazonlocation.data.common

/**
 * sealed class for handle different network results like
 * [onSuccess] [onError] [onLoading]
 */
sealed class HandleResult<out T> {
    data class Success<out T>(val response: T) : HandleResult<T>()
    data class Error(val exception: DataSourceException) : HandleResult<Nothing>()
    object Loading : HandleResult<Nothing>()
}

inline fun <T : Any> HandleResult<T>.onSuccess(action: (T) -> Unit): HandleResult<T> {
    if (this is HandleResult.Success) action(response)
    return this
}

inline fun <T : Any> HandleResult<T>.onError(action: (DataSourceException) -> Unit): HandleResult<T> {
    if (this is HandleResult.Error) action(exception)
    return this
}

inline fun <T : Any> HandleResult<T>.onLoading(action: () -> Unit): HandleResult<T> {
    if (this is HandleResult.Loading) action()
    return this
}
