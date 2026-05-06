package com.abraham_bankole.sentry.core

// Agnostic data model for Sentry
data class RiskContext(val actorId: String, val transactionData: Map<String, Any>)

data class RiskVerdict(val score: Int, val reason: String)
