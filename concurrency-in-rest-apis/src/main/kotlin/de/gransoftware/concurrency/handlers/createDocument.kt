package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.DataStore
import de.gransoftware.concurrency.etag
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

suspend fun createDocument(ctx: RoutingContext) {
  val content = ctx.body().asString()
  if (content.isNullOrEmpty()) {
    ctx.response().setStatusCode(400).end(jsonObjectOf("message" to "You need to pass some content!").encode())
    return
  }
  val documentId = ctx.pathParam("id")
  val document = DataStore.Document(documentId, content, Instant.now())
  DataStore.put(document)

  ctx.response().setStatusCode(201)
    .putHeader("Location", "/documents/$documentId")
    .putHeader("ETag", document.etag)
    .end()
}
