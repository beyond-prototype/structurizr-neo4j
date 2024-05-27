package com.beyondprototype.structurizr.neo4j;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.mistralai.MistralAiChatClient;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.MistralAiEmbeddingClient;
import org.springframework.ai.mistralai.api.MistralAiApi;

import java.util.List;

@Slf4j
public class StructurizrNeo4jMistralAiApp {

	public static void main(String... args) throws Exception {

		Dotenv dotenv = Dotenv.configure()
				.directory("/Users/xwp/github/beyond-prototype/structurizr-neo4j")
				.filename(".env")
				.load();

		String neo4jUri = dotenv.get("neo4j.uri");
		String username = dotenv.get("neo4j.authentication.username");
		String password = dotenv.get("neo4j.authentication.password");
		String database = dotenv.get("neo4j.database");

		Driver driver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password));

		driver.verifyConnectivity();

		Session session = driver.session(SessionConfig.builder().withDatabase(database == null?"neo4j":database).build());

		MistralAiApi mistralAiApi = new MistralAiApi(dotenv.get("spring.ai.mistralai.api-key"));

		EmbeddingClient embeddingClient = new MistralAiEmbeddingClient(mistralAiApi);

		//https://docs.spring.io/spring-ai/reference/1.0-SNAPSHOT/api/chat/mistralai-chat.html
		var chatModel = new MistralAiChatClient(mistralAiApi, MistralAiChatOptions.builder()
				.withModel(MistralAiApi.ChatModel.TINY.getValue())
				.withTemperature(0.0f)
				.withMaxToken(200)
//				.withResponseFormat(new MistralAiApi.ChatCompletionRequest.ResponseFormat("json_object"))
				.build());

		StructurizrNeo4jQAChain qaChain = new StructurizrNeo4jQAChain(session,chatModel, embeddingClient);

//		qaChain.embed("/Users/xwp/github/structurizr/examples/dsl/big-bank-plc/workspace.dsl");

		List<String> questions = List.of(
		"what software systems are used by customers?"
		,"what software systems are used by customers to withdraw cash?"
		,"what software systems are available?"
		,"what users are available?"
		,"what software systems are used by staff?"
		,"what software systems are used by Back Office Staff?"
		,"what can provide a summary of a customer's bank accounts?"
		,"what can be used to store customer information?"
		,"what can be used by customers to view their banking information?"
		,"what software system can be used to store customer information?");

		questions.subList(8,10).forEach(question -> {
//			log.info("======================================================================================");
			log.info("\nQuery: %s\n".formatted(question));
			log.info("\nAnswer: %s\n".formatted(qaChain.invoke(question)));
//			System.out.println("======================================================================================");
		});
	}
}
