package com.aws.amazonlocation.data.common

/**
 * sealed class for different type of runtime exception
 */
sealed class DataSourceException(val messageResource: Any?) : RuntimeException() {
    class Error(messageResource: String) : DataSourceException(messageResource)
}
