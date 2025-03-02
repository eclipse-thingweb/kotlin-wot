package ai.ancf.lmos.sdk.model


data class AgentRequest(
    val messages: List<Message>,
    val conversationContext: ConversationContext? = null,
    val systemContext: List<SystemContextEntry>? = null,
    val userContext: UserContext? = null,
)

data class UserContext(
    val userId: String? = null,
    val userToken: String? = null,
    val profile: List<ProfileEntry>,
)

data class ConversationContext(
    val conversationId: String,
    val turnId: String? = null,
    val anonymizationEntities: List<AnonymizationEntity>? = null,
)

data class ProfileEntry(
    val key: String,
    val value: String,
)

data class SystemContextEntry(
    val key: String,
    val value: String,
)