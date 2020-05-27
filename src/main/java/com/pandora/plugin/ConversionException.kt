package com.pandora.plugin

internal class ConversionException(
    message: String,
    internal val isError: Boolean = false,
    exception: Throwable? = null
) : RuntimeException(message, exception)
