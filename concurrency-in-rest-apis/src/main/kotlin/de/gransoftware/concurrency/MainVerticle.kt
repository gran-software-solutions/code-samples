package de.gransoftware.concurrency

import de.gransoftware.concurrency.handlers.getDocument
import de.gransoftware.concurrency.handlers.saveDocument
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class MainVerticle : CoroutineVerticle() {

  override suspend fun start() {
    val routerBuilder = RouterBuilder.create(vertx, "src/main/resources/openapi.yaml").await().apply {
      operation("getDocument").coHandler(::getDocument)
      operation("saveDocument").coHandler(::saveDocument)
    }
    vertx
      .createHttpServer()
      .requestHandler(routerBuilder.createRouter())
      .listen(8888)
      .await()
  }
}
