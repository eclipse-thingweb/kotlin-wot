package integration

import dev.langchain4j.model.azure.AzureOpenAiChatModel

class Agent {

    val model: AzureOpenAiChatModel = AzureOpenAiChatModel.builder()
        .apiKey("af12dab9c046453e82dcf4b24af90bca")
        .deploymentName("GPT35T-1106")
        .endpoint("https://gpt4-uk.openai.azure.com/")
        .build();
    fun ask(message: String) : String{
        return model.generate(message)
    }
}