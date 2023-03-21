package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.DataStore
import de.gransoftware.concurrency.DataStore.Document
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant
import java.time.OffsetDateTime

suspend fun saveDocument(ctx: RoutingContext) {
  val newContent = ctx.body().asString()

  if (newContent.isNullOrEmpty()) {
    ctx.response().setStatusCode(400).end(jsonObjectOf("message" to "You need to pass some content!").encode())
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
  if (ctx.request().getHeader("If-Match").isNullOrBlank()) {
    ctx.response().setStatusCode(428).end(jsonObjectOf("message" to "If-Match header is required").encode())
    return
  }
  val lastUpdatedAt = OffsetDateTime.parse(ctx.request().getHeader("If-Match"))
  if (lastUpdatedAt.toInstant() != document.lastUpdatedAt) {
    ctx.response().setStatusCode(412)
      .end(jsonObjectOf("message" to "Document has been updated in the meantime").encode())
    return
  } else {
    DataStore.put(
      document.copy(content = newContent, lastUpdatedAt = Instant.now())
    )
    ctx.response().putHeader("Location", ctx.location("/documents/${document.id}")).setStatusCode(204).end()
  }
}

private fun insertDocument(documentId: String, newContent: String, ctx: RoutingContext) {
  DataStore.put(Document(documentId, newContent, Instant.now()))
  ctx.response().putHeader("Location", ctx.location("/documents/$documentId")).setStatusCode(204).end()
}

private fun RoutingContext.location(path: String): String {
  return "${request().absoluteURI().removeSuffix(request().path())}$path"
}
