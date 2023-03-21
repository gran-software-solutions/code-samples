package de.gransoftware.concurrency

import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle(), testContext.succeeding<String> { _ -> testContext.completeNow() })
  }

  @Test
  fun `getDocument returns 404 for no document found`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val response = vertx.createHttpClient()
      .request(HttpMethod.GET, 8888, "localhost", "/documents/${UUID.randomUUID()}")
      .await().send().await()
    assert(response.statusCode() == 404)
  }

  @Test
  fun `createDocument should create a doc`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val httpClient = vertx.createHttpClient()

    val docId = UUID.randomUUID().toString()
    val response = httpClient.createDocument(docId)
    val location = response.getHeader("Location")
    assert(location == "http://localhost:8888/documents/$docId")
  }

  @Test
  fun `getDocument should get a created doc`(vertx: Vertx) = runBlocking(vertx.dispatcher()) {
    val httpClient = vertx.createHttpClient()
    val docId = UUID.randomUUID().toString()
    val response = httpClient.createDocument(docId)
    val location = response.getHeader("Location")
    val documentResponse = httpClient.getDocument(docId)
    documentResponse.body().await().toString().let { assert(it == "test") }
  }

  private suspend fun HttpClient.createDocument(docId: String): HttpClientResponse {
    val response = this
      .request(HttpMethod.PUT, 8888, "localhost", "/documents/$docId").await()
      .putHeader("Content-Type", "text/plain")
      .send("test")
      .await()
    assert(response.statusCode() == 204)
    return response
  }

  private suspend fun HttpClient.getDocument(docId: String): HttpClientResponse {
    val response = this
      .request(HttpMethod.GET, 8888, "localhost", "/documents/$docId").await()
      .putHeader("Accept", "text/plain")
      .send()
      .await()
    assert(response.statusCode() == 200)
    return response
  }
}
