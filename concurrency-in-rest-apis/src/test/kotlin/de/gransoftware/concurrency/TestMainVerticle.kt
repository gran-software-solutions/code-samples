package de.gransoftware.concurrency

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
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

  private lateinit var webClient: WebClient

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle(), testContext.succeeding<String> { _ ->
      webClient = WebClient.create(vertx)
      testContext.completeNow()
    })
  }

  @Test
  fun `getDocument returns 404 for no document found`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    webClient.get(8888, "localhost", "/documents/${UUID.randomUUID()}")
      .send()
      .await()
      .let { assert(it.statusCode() == 404) }
  }

  @Test
  fun `createDocument should create a doc`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val docId = UUID.randomUUID().toString()
    val response = webClient.createDocument(docId)
    val location = response.getHeader("Location")
    assert(location == "http://localhost:8888/documents/$docId")
  }

  @Test
  fun `getDocument should get a created doc`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val docId = UUID.randomUUID().toString()
    val response = webClient.createDocument(docId)
    val documentResponse = webClient.getDocument(docId)
    documentResponse.body().toString().let { assert(it == "test") }
  }

  @Test
  fun `updating a doc with a correct Etag is possible, with wrong one - not`(vertx: Vertx) =
    runBlocking(vertx.dispatcher()) {
      val docId = UUID.randomUUID().toString()
      webClient.createDocument(docId)
      val documentResponse = webClient.getDocument(docId)
      documentResponse.body().toString().let { assert(it == "test") }
      val etag = documentResponse.getHeader("ETag")
      var updateResponse = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/documents/$docId")
        .putHeader("Content-Type", "text/plain")
        .putHeader("If-Match", etag)
        .sendBuffer(Buffer.buffer("test2"))
        .await()

      assert(updateResponse.statusCode() == 204)

      updateResponse = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/documents/$docId")
        .putHeader("Content-Type", "text/plain")
        .putHeader("If-Match", etag)
        .sendBuffer(Buffer.buffer("test3"))
        .await()

      assert(updateResponse.statusCode() == 412)
    }

  @Test
  fun `One can not update a doc without an Etag header`(vertx: Vertx) =
    runBlocking(vertx.dispatcher()) {
      val docId = UUID.randomUUID().toString()
      webClient.createDocument(docId)
      val documentResponse = webClient.getDocument(docId)
      documentResponse.body().toString().let { assert(it == "test") }
      val statusCode = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/documents/$docId")
        .putHeader("Content-Type", "text/plain")
        .sendBuffer(Buffer.buffer("test2"))
        .await().statusCode()
      assert(statusCode == 428)
    }


  private suspend fun WebClient.createDocument(docId: String): HttpResponse<Buffer> {
    val response = this
      .request(HttpMethod.PUT, 8888, "localhost", "/documents/$docId")
      .putHeader("Content-Type", "text/plain")
      .sendBuffer(Buffer.buffer("test"))
      .await()
    assert(response.statusCode() == 204)
    return response
  }

  private suspend fun WebClient.getDocument(docId: String): HttpResponse<Buffer> {
    val response = this
      .request(HttpMethod.GET, 8888, "localhost", "/documents/$docId")
      .putHeader("Accept", "text/plain")
      .send()
      .await()
    assert(response.statusCode() == 200)
    assert(!response.getHeader("ETag").isNullOrBlank())
    return response
  }
}
