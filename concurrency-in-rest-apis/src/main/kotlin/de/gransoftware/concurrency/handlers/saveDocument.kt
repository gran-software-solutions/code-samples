package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.DataStore
import de.gransoftware.concurrency.DataStore.Document
import de.gransoftware.concurrency.asErrorMsg
import de.gransoftware.concurrency.etag
import de.gransoftware.concurrency.handlers.HttpStatus.BAD_REQUEST
import de.gransoftware.concurrency.handlers.HttpStatus.NO_CONTENT
import de.gransoftware.concurrency.handlers.HttpStatus.PRECONDITION_FAILED
import de.gransoftware.concurrency.handlers.HttpStatus.PRECONDITION_REQUIRED
import de.gransoftware.concurrency.location
import io.vertx.ext.web.RoutingContext
import java.time.Instant

suspend fun saveDocument(ctx: RoutingContext) {
  val newContent = ctx.body().asString()

  if (newContent.isNullOrEmpty()) {
    ctx.response().setStatusCode(BAD_REQUEST).end("You need to pass some content!".asErrorMsg())
    return
  }

  val documentId = ctx.pathParam("id")
  val document = DataStore.get(documentId)

  val response = ctx.response().putHeader("Location", ctx.location("/documents/$documentId")).setStatusCode(NO_CONTENT)
  if (document == null) {
    insertDocument(documentId, newContent)
    response.end()
  } else {
    val requestETag = ctx.request().getHeader("If-Match")
    if (requestETag.isNullOrBlank()) {
      ctx.response().setStatusCode(PRECONDITION_REQUIRED)
        .end("If-Match header is required".asErrorMsg())
      return
    }
    if (requestETag != document.etag) {
      ctx.response().setStatusCode(PRECONDITION_FAILED)
        .end("Document has been updated in the meantime".asErrorMsg())
    } else {
      DataStore.put(document.copy(content = newContent, lastUpdatedAt = Instant.now()))
      response.end()
    }
  }
}

private fun insertDocument(documentId: String, content: String) {
  DataStore.put(Document(documentId, content, Instant.now()))
}
