package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.DataStore
import de.gransoftware.concurrency.etag
import io.vertx.ext.web.RoutingContext

suspend fun getDocument(ctx: RoutingContext) {
  val documentId = ctx.pathParam("id")
  val document = DataStore.get(documentId)
  if (document == null) {
    ctx.response().setStatusCode(HttpStatus.NOT_FOUND).end()
  } else {
    ctx.response()
      .putHeader("Content-Type", "text/plain")
      .putHeader("ETag", document.etag)
      .end(document.content)
  }
}
