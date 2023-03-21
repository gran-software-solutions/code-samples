package de.gransoftware.concurrency

import java.security.MessageDigest

fun sha256(input: String): String {
  val HEX_CHARS = "0123456789ABCDEF"
  val bytes = MessageDigest
    .getInstance("SHA-256")
    .digest(input.toByteArray())
  val result = StringBuilder(bytes.size * 2)

  bytes.forEach {
    val i = it.toInt()
    result.append(HEX_CHARS[i shr 4 and 0x0f])
    result.append(HEX_CHARS[i and 0x0f])
  }

  return result.toString()
}
