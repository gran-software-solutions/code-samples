package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.WikiStore
import de.gransoftware.concurrency.WikiStore.Wiki
import de.gransoftware.concurrency.asErrorMsg
import de.gransoftware.concurrency.etag
import de.gransoftware.concurrency.handlers.HttpStatus.BAD_REQUEST
import de.gransoftware.concurrency.handlers.HttpStatus.NO_CONTENT
import de.gransoftware.concurrency.handlers.HttpStatus.PRECONDITION_FAILED
import de.gransoftware.concurrency.handlers.HttpStatus.PRECONDITION_REQUIRED
import de.gransoftware.concurrency.location
import io.vertx.ext.web.RoutingContext
import java.time.Instant

fun saveWiki(ctx: RoutingContext) {
  val newContent = ctx.body().asString()

  if (newContent.isNullOrEmpty()) {
    ctx.response().setStatusCode(BAD_REQUEST).end("You need to pass some content!".asErrorMsg())
    return
  }

  val wikiId = ctx.pathParam("id")
  val wiki = WikiStore.get(wikiId)

  val response = ctx.response().putHeader("Location", ctx.location("/wikis/$wikiId")).setStatusCode(NO_CONTENT)
  if (wiki == null) {
    insertWiki(wikiId, newContent)
    response.end()
  } else {
    val requestETag = ctx.request().getHeader("If-Match")
    if (requestETag.isNullOrBlank()) {
      ctx.response().setStatusCode(PRECONDITION_REQUIRED)
        .end("If-Match header is required".asErrorMsg())
      return
    }
    if (requestETag != wiki.etag) {
      ctx.response().setStatusCode(PRECONDITION_FAILED)
        .end("Wiki has been updated in the meantime".asErrorMsg())
    } else {
      WikiStore.put(wiki.copy(content = newContent, lastUpdatedAt = Instant.now()))
      response.end()
    }
  }
}

private fun insertWiki(wikiId: String, content: String) {
  WikiStore.put(Wiki(wikiId, content, Instant.now()))
}
