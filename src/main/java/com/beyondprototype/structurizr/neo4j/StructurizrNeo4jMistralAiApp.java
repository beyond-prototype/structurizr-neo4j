package com.beyondprototype.structurizr.neo4j;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.ai.autoconfigure.mistralai.MistralAiAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.neo4j.Neo4jVectorStoreAutoConfiguration;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.MistralAiEmbeddingClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication(exclude = {MistralAiAutoConfiguration.class, Neo4jVectorStoreAutoConfiguration.class})
public class StructurizrNeo4jMistralAiApp {

	@Bean
	@ConditionalOnMissingBean
	public MistralAiApi mistralAiApi(Dotenv dotenv){
		return new MistralAiApi(dotenv.get("spring.ai.mistralai.api-key"));
	}

	@Bean
	@ConditionalOnMissingBean
	public MistralAiEmbeddingClient mistralAiEmbeddingClient(MistralAiApi mistralAiApi){
		return new MistralAiEmbeddingClient(mistralAiApi);
	}

	@Bean
	@ConditionalOnMissingBean
	public MistralAiChatClient mistralAiChatClient(MistralAiApi mistralAiApi){
		//https://docs.spring.io/spring-ai/reference/1.0-SNAPSHOT/api/chat/mistralai-chat.html
		var chatModel = new MistralAiChatClient(mistralAiApi, MistralAiChatOptions.builder()
				.withModel(MistralAiApi.ChatModel.TINY.getValue())
				.withTemperature(0.0f)
				.withMaxToken(200)
//				.withResponseFormat(new MistralAiApi.ChatCompletionRequest.ResponseFormat("json_object"))
				.build());

		return chatModel;
	}

	@Bean
	@Qualifier("MistralAiQAChain")
	@ConditionalOnMissingBean
	public StructurizrNeo4jQAChain mistralAiQAChain(Session session, MistralAiChatClient chatClient, MistralAiEmbeddingClient embeddingClient){
		StructurizrNeo4jQAChain qaChain = new StructurizrNeo4jQAChain(session,chatClient, embeddingClient);
		return qaChain;
	}

	public static void main(String... args) throws Exception {

		SpringApplication.run(StructurizrNeo4jMistralAiApp.class,args);

//		StructurizrNeo4jQAChain qaChain = new StructurizrNeo4jQAChain(session,chatModel, embeddingClient);

//		qaChain.embed("/Users/xwp/github/structurizr/examples/dsl/big-bank-plc/workspace.dsl");
//
//		List<String> questions = List.of(
//		"what software systems are used by customers?"
//		,"what software systems are used by customers to withdraw cash?"
//		,"what software systems are available?"
//		,"what users are available?"
//		,"what software systems are used by staff?"
//		,"what software systems are used by Back Office Staff?"
//		,"what can provide a summary of a customer's bank accounts?"
//		,"what can be used to store customer information?"
//		,"what can be used by customers to view their banking information?"
//		,"what software system can be used to store customer information?");
//
//		questions.subList(8,10).forEach(question -> {
////			log.info("======================================================================================");
//			log.info("\nQuery: %s\n".formatted(question));
//			log.info("\nAnswer: %s\n".formatted(qaChain.invoke(question)));
////			System.out.println("======================================================================================");
//		});
	}

}
