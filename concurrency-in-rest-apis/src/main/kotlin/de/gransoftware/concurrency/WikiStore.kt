package de.gransoftware.concurrency

import java.time.Instant

object WikiStore {
  data class Wiki(val id: String, val content: String, val lastUpdatedAt: Instant)

  private val data = mutableMapOf<String, Wiki>()

  fun get(id: String): Wiki? = data[id]

  fun put(wiki: Wiki) {
    data[wiki.id] = wiki
  }
}
