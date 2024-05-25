package com.ml.experimentsandtests
/*
suspend inline fun <reified T> HttpClient.getResults(
    block: HttpRequestBuilder.() -> Unit
): Result<T> = try {
    val response = request(block)
    if (response.status == HttpStatusCode.OK) {
        Result.Success(response.body())
    } else {
        Result.Error(Throwable("${response.status}: ${response.bodyAsText()}"))
    }
} catch (e: Exception) {
    Result.Error(e)
}

sealed interface Result<out T> {
    class Success<out T>(val value: T) : Result<T>
    data object Loading : Result<Nothing>
    class Error(val throwable: Throwable) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(value))
        is Result.Error -> Result.Error(throwable)
        is Result.Loading -> Result.Loading
    }*/