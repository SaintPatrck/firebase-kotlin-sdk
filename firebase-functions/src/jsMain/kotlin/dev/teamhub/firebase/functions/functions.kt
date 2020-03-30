/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase.functions

import dev.teamhub.firebase.*
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.js.json

actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { dev.teamhub.firebase.functions; FirebaseFunctions(firebase.functions()) }

actual fun Firebase.functions(region: String) =
    rethrow { dev.teamhub.firebase.functions; FirebaseFunctions(firebase.app().functions(region)) }

actual fun Firebase.functions(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.functions; FirebaseFunctions(firebase.functions(app.js)) }

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    rethrow { dev.teamhub.firebase.functions; FirebaseFunctions(app.js.functions(region)) }

actual class FirebaseFunctions internal constructor(val js: firebase.functions.Functions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        rethrow { HttpsCallableReference(js.httpsCallable(name, timeout?.let { json("timeout" to timeout.toDouble()) })) }
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: firebase.functions.HttpsCallable) {

    actual suspend fun call() =
        rethrow { HttpsCallableResult(js().await()) }

    actual suspend fun call(data: Any) =
        rethrow { HttpsCallableResult(js(encode(data)).await()) }

    actual suspend fun <T> call(strategy: SerializationStrategy<T>, data: T) =
        rethrow { HttpsCallableResult(js(encode(strategy, data)).await()) }
}

actual class HttpsCallableResult constructor(val js: firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T> data() =
        rethrow { decode<T>(value = js.data) }

    actual fun <T> data(strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.data) }

}

actual open class FirebaseFunctionsException(code: String?, cause: Throwable): FirebaseException(code, cause)

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.functions.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw FirebaseFunctionsException(e.asDynamic().code as String?, e)
    }
}