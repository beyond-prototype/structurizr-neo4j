package com.beyondprototype.structurizr.neo4j;

//import com.azure.ai.openai.OpenAIClient;
//import com.azure.ai.openai.OpenAIClientBuilder;
//import com.azure.core.credential.AzureKeyCredential;
//import io.github.cdimascio.dotenv.Dotenv;
//import lombok.extern.slf4j.Slf4j;
//import org.neo4j.driver.Session;
//import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration;
//import org.springframework.ai.autoconfigure.vectorstore.neo4j.Neo4jVectorStoreAutoConfiguration;
//import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
//import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
//import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingClient;
//import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
//import org.springframework.ai.document.MetadataMode;
//import org.springframework.ai.embedding.EmbeddingClient;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;

//@Slf4j
//@SpringBootApplication(exclude = {AzureOpenAiAutoConfiguration.class, Neo4jVectorStoreAutoConfiguration.class})
public class StructurizrNeo4jAzureOpenAiApp {

//	@Bean
//	@Qualifier("AzureOpenAiClient")
//	@ConditionalOnMissingBean
//	public OpenAIClient azureOpenAiClient(Dotenv dotenv){
//
//		return new OpenAIClientBuilder()
//				.credential(new AzureKeyCredential(dotenv.get("spring.ai.azure.openai.api-key")))
//				.endpoint(dotenv.get("spring.ai.azure.openai.endpoint"))
//				.buildClient();
//	}
//
//	@Bean
//	@ConditionalOnMissingBean
//	public AzureOpenAiEmbeddingClient azureOpenAiEmbeddingClient(@Qualifier("AzureOpenAiClient")OpenAIClient client, Dotenv dotenv){
//
//		AzureOpenAiEmbeddingOptions options = new AzureOpenAiEmbeddingOptions();
//		options.setDeploymentName(dotenv.get("spring.ai.azure.openai.embedding.options.deployment-name"));
//
//		return new AzureOpenAiEmbeddingClient(client, MetadataMode.EMBED, options);
//	}
//
//	@Bean
//	@ConditionalOnMissingBean
//	public AzureOpenAiChatClient azureOpenAiChatClient(@Qualifier("AzureOpenAiClient")OpenAIClient client, Dotenv dotenv) {
//		var chatModel = new AzureOpenAiChatClient(client, AzureOpenAiChatOptions.builder()
//				.withDeploymentName(dotenv.get("spring.ai.azure.openai.chat.options.deployment-name"))
//				.withTemperature(0.0f)
//				.withMaxTokens(200)
//				.build());
//
//		return chatModel;
//	}
//
//	@Bean
//	@Qualifier("AzureOpenAiQAChain")
//	@ConditionalOnMissingBean
//	public StructurizrNeo4jQAChain azureOpenAiQAChain(Session session, AzureOpenAiChatClient chatClient, AzureOpenAiEmbeddingClient embeddingClient){
//		StructurizrNeo4jQAChain qaChain = new StructurizrNeo4jQAChain(session,chatClient, embeddingClient);
//		return qaChain;
//	}
//
//	public static void main(String... args) throws Exception {
//		SpringApplication.run(StructurizrNeo4jAzureOpenAiApp.class,args);
//	}
}
