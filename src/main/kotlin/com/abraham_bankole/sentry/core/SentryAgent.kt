package com.abraham_bankole.sentry.core

import ai.koog.agents.core.agent.entity.AIAgentNodeBase
import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.message.Message
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase

data class RiskContext(val actorId: String, val transactionData: Map<String, Any>)

data class RiskVerdict(val score: Int, val reason: String)

val graphDbDriver =
  GraphDatabase.driver(
    System.getenv("NEO4J_URI") ?: "bolt://localhost:7687",
    AuthTokens.basic(
      System.getenv("NEO4J_USER") ?: "neo4j",
      System.getenv("NEO4J_PASSWORD") ?: "password"
    )
  )

class MemgraphTool {
  suspend fun call(accountId: String): String {
    // Simulated graph lookup
    return "History for $accountId: [No fraud detected in recent cycles]"
  }
}

val memgraphTool = MemgraphTool()

val sentryStrategy =
  strategy<Envelope, RiskVerdict>("sentry-intelligence") {
    val nodeStart: AIAgentNodeBase<Envelope, Envelope> by node("ingest") { it }

    val nodeRouter: AIAgentNodeBase<Envelope, Envelope> by node("domain-router") { it }

    val nodeBankingEnrich: AIAgentNodeBase<Envelope, Envelope> by
      node("banking-enrich") { env ->
        val history = memgraphTool.call(env.payload["accountId"]?.toString() ?: "unknown")
        env.copy(payload = env.payload + ("graph_context" to history))
      }

    val nodeHftEnrich: AIAgentNodeBase<Envelope, Envelope> by
      node("hft-enrich") { env ->
        // TODO: fetch market liquidity/tick data
        env
      }

    val nodePreparePrompt: AIAgentNodeBase<Envelope, String> by
      node("prepare-prompt") { env ->
        when (env.domain) {
          Domain.BANKING -> "Analyze this Banking Transaction Data: ${env.payload}"
          Domain.IDENTITY -> "Analyze this User KYC and Identity Data: ${env.payload}"
          Domain.HFT -> "Analyze this HFT Order Pattern: ${env.payload}"
        }
      }

    val nodeAnalyze: AIAgentNodeBase<String, Message.Response> by nodeLLMRequest("ai_judgement")

    // execution flow
    nodeStart then nodeRouter

    // routing based on domain
    edge(nodeRouter forwardTo nodeBankingEnrich onCondition { it.domain == Domain.BANKING })
    edge(nodeRouter forwardTo nodeHftEnrich onCondition { it.domain == Domain.HFT })
    edge(nodeRouter forwardTo nodePreparePrompt onCondition { it.domain == Domain.IDENTITY })

    // merge enrichment back into prompt prep
    nodeBankingEnrich then nodePreparePrompt
    nodeHftEnrich then nodePreparePrompt

    // prompt to LLM
    nodePreparePrompt then nodeAnalyze

    // LLM to finish
    edge(
      nodeAnalyze forwardTo
        nodeFinish onAssistantMessage
        { true } transformed
        { msg ->
          RiskVerdict(score = 50, reason = msg)
        }
    )
  }

// val sentryAgent = AIAgent.builder().graphStrategy(sentryStrategy).build()
