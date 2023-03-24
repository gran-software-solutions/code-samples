package de.gransoftware.concurrency

import de.gransoftware.concurrency.handlers.HttpStatus
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
    vertx.deployVerticle(MainVerticle(), testContext.succeeding { _ ->
      webClient = WebClient.create(vertx)
      testContext.completeNow()
    })
  }

  @Test
  fun `getWiki returns 404 for no wiki found`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    webClient.get(8888, "localhost", "/wikis/${UUID.randomUUID()}")
      .send()
      .await()
      .let { assert(it.statusCode() == HttpStatus.NOT_FOUND) }
  }

  @Test
  fun `saveWiki should create a new wiki`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val wikiId = UUID.randomUUID().toString()
    val response = webClient.createWiki(wikiId)
    val location = response.getHeader("Location")
    assert(location == "http://localhost:8888/wikis/$wikiId")
  }

  @Test
  fun `getWiki should get a created wiki`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val wikiId = UUID.randomUUID().toString()
    webClient.createWiki(wikiId)
    val wikiResponse = webClient.getWiki(wikiId)
    wikiResponse.body().toString().let { assert(it == "test") }
  }

  @Test
  fun `updating a wiki with a correct Etag is possible, with wrong one - not`(vertx: Vertx) =
    runBlocking(vertx.dispatcher()) {
      val wikiId = UUID.randomUUID().toString()
      webClient.createWiki(wikiId)
      val wikiResponse = webClient.getWiki(wikiId)
      wikiResponse.body().toString().let { assert(it == "test") }
      val etag = wikiResponse.getHeader("ETag")
      var updateResponse = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/wikis/$wikiId")
        .putHeader("Content-Type", "text/plain")
        .putHeader("If-Match", etag)
        .sendBuffer(Buffer.buffer("test2"))
        .await()

      assert(updateResponse.statusCode() == 204)

      updateResponse = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/wikis/$wikiId")
        .putHeader("Content-Type", "text/plain")
        .putHeader("If-Match", etag)
        .sendBuffer(Buffer.buffer("test3"))
        .await()

      assert(updateResponse.statusCode() == HttpStatus.PRECONDITION_FAILED)
    }

  @Test
  fun `One can not update a wiki without an Etag header`(vertx: Vertx) =
    runBlocking(vertx.dispatcher()) {
      val wikiId = UUID.randomUUID().toString()
      webClient.createWiki(wikiId)
      val wikiResponse = webClient.getWiki(wikiId)
      wikiResponse.body().toString().let { assert(it == "test") }
      val statusCode = webClient
        .request(HttpMethod.PUT, 8888, "localhost", "/wikis/$wikiId")
        .putHeader("Content-Type", "text/plain")
        .sendBuffer(Buffer.buffer("test2"))
        .await().statusCode()
      assert(statusCode == HttpStatus.PRECONDITION_REQUIRED)
    }


  private suspend fun WebClient.createWiki(wikiId: String): HttpResponse<Buffer> {
    val response = this
      .request(HttpMethod.PUT, 8888, "localhost", "/wikis/$wikiId")
      .putHeader("Content-Type", "text/plain")
      .sendBuffer(Buffer.buffer("test"))
      .await()
    assert(response.statusCode() == HttpStatus.NO_CONTENT)
    return response
  }

  private suspend fun WebClient.getWiki(docId: String): HttpResponse<Buffer> {
    val response = this
      .request(HttpMethod.GET, 8888, "localhost", "/wikis/$docId")
      .putHeader("Accept", "text/plain")
      .send()
      .await()
    assert(response.statusCode() == HttpStatus.OK)
    assert(!response.getHeader("ETag").isNullOrBlank())
    return response
  }
}
