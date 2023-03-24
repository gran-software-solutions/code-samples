package de.gransoftware.concurrency.handlers

import de.gransoftware.concurrency.WikiStore
import de.gransoftware.concurrency.etag
import io.vertx.ext.web.RoutingContext

fun getWiki(ctx: RoutingContext) {
  val wikiId = ctx.pathParam("id")
  val wiki = WikiStore.get(wikiId)
  if (wiki == null) {
    ctx.response().setStatusCode(HttpStatus.NOT_FOUND).end()
  } else {
    ctx.response()
      .putHeader("Content-Type", "text/plain")
      .putHeader("ETag", wiki.etag)
      .end(wiki.content)
  }
}
