package com.beyondprototype.structurizr.neo4j;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.mistralai.MistralAiEmbeddingClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.vectorstore.SearchRequest;

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


		EmbeddingClient embeddingClient = new MistralAiEmbeddingClient(new MistralAiApi(dotenv.get("spring.ai.mistralai.api-key")));

		StructurizrNeo4jVectorStoreEmbedding embedding = new StructurizrNeo4jVectorStoreEmbedding(session, embeddingClient);
		embedding.embed("/Users/xwp/github/structurizr/examples/dsl/big-bank-plc/workspace.dsl");

		List<String> questions = List.of(
		"what software systems are used by customers?"
		,"what software systems are used by customers to withdraw cash?"
		,"what software systems are available?"
		,"what users are available?"
		,"what software systems are used by staff?"
		,"What software systems are used by Back Office Staff?"
		,"what can provide a summary of a customer's bank accounts?"
		,"what can be used to store customer information?"
		,"what can be used by customers to view their banking information?"
		,"what software system can be used to store customer information?");

		StructurizrNeo4jVectorStoreRetriever retriever = new StructurizrNeo4jVectorStoreRetriever(session, embeddingClient);

		questions.forEach(query -> {
			System.out.println("Query: %s".formatted(query));
			List<Document> results = retriever.similaritySearch(SearchRequest.query(query).withSimilarityThreshold(0.8).withTopK(2));
			results.forEach( doc -> {
				System.out.println(doc.toString());
			});
			System.out.println();
		});
	}
}
