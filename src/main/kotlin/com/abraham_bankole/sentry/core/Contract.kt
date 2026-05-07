package com.abraham_bankole.sentry.core

enum class Domain {
  IDENTITY, // kyc
  BANKING, // runestone
  HFT, // valkyrie
}

data class Envelope(
  val id: String,
  val domain: Domain,
  val source: String, // RuneStone-Prod, Valkyrie-Edge-01 etc
  val payload: Map<String, Any>,
)
