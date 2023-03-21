package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.DataStore
import de.gransoftware.concurrency.DataStore.Document
import de.gransoftware.concurrency.etag
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

suspend fun saveDocument(ctx: RoutingContext) {
  val newContent = ctx.body().asString()

  if (newContent.isNullOrEmpty()) {
    ctx.response().setStatusCode(HttpStatus.BAD_REQUEST)
      .end(jsonObjectOf("message" to "You need to pass some content!").encode())
    return
  }

  val documentId = ctx.pathParam("id")
  val document = DataStore.get(documentId)

  if (document == null) {
    insertDocument(documentId, newContent, ctx)
  } else {
    updateDocument(ctx, document, newContent)
  }
}

private fun updateDocument(ctx: RoutingContext, document: Document, newContent: String) {
  val requestETag = ctx.request().getHeader("If-Match")
  if (requestETag.isNullOrBlank()) {
    ctx.response().setStatusCode(HttpStatus.PRECONDITION_REQUIRED)
      .end(jsonObjectOf("message" to "If-Match header is required").encode())
    return
  }
  val currentEtag = document.etag
  if (requestETag != currentEtag) {
    ctx.response().setStatusCode(HttpStatus.PRECONDITION_FAILED)
      .end(jsonObjectOf("message" to "Document has been updated in the meantime").encode())
    return
  } else {
    DataStore.put(
      document.copy(content = newContent, lastUpdatedAt = Instant.now())
    )
    ctx.response().putHeader("Location", ctx.location("/documents/${document.id}")).setStatusCode(HttpStatus.NO_CONTENT)
      .end()
  }
}

private fun insertDocument(documentId: String, content: String, ctx: RoutingContext) {
  DataStore.put(Document(documentId, content, Instant.now()))
  ctx.response().putHeader("Location", ctx.location("/documents/$documentId")).setStatusCode(HttpStatus.NO_CONTENT)
    .end()
}

private fun RoutingContext.location(path: String): String {
  return "${request().absoluteURI().removeSuffix(request().path())}$path"
}
