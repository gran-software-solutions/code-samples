package de.gransoftware.concurrency

import java.time.Instant

object DataStore {
  data class Document(val id: String, val content: String, val lastUpdatedAt: Instant)

  private val data = mutableMapOf<String, Document>()

  fun get(id: String): Document? = data[id]

  fun put(document: Document) {
    data[document.id] = document
  }
}
