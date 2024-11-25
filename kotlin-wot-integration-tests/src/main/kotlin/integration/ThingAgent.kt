package ai.ancf.lmos.wot.reflection.annotations

import dev.langchain4j.model.azure.AzureOpenAiChatModel


@Thing(id= "agent", title="Agent",
    description= "A simple agent.")
class ThingAgent() {

    private val model: AzureOpenAiChatModel = AzureOpenAiChatModel.builder()
        .apiKey("af12dab9c046453e82dcf4b24af90bca")
        .deploymentName("GPT35T-1106")
        .endpoint("https://gpt4-uk.openai.azure.com/")
        .build();

    @Property(name = "modelTemperature", readOnly = true)
    var modelTemperature: ModelConfiguration = ModelConfiguration(0.5, 50)

    @Action(name = "ask")
    fun ask(message : String) : String {
        return model.generate(message)
    }
}

data class ModelConfiguration(val modelTemperature: Double, val maxTokens: Int)

