package ai.ancf.lmos.sdk.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AgentEvent(
    var type: String = "",
    var payload: String = "",
    var conversationId: String? = "",
    var turnId: String? = "",
)