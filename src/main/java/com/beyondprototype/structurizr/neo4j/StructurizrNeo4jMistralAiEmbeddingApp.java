package com.beyondprototype.structurizr.neo4j;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.mistralai.MistralAiEmbeddingClient;
import org.springframework.ai.mistralai.api.MistralAiApi;

@Slf4j
public class StructurizrNeo4jMistralAiEmbeddingApp {

	public static void main(String... args) throws Exception {

		Dotenv dotenv = Dotenv.configure()
				.directory("/Users/xwp/github/beyond-prototype/structurizr-neo4j")
				.filename(".env")
				.load();

		EmbeddingClient embeddingClient = new MistralAiEmbeddingClient(new MistralAiApi(dotenv.get("spring.ai.mistralai.api-key")));

		StructurizrNeo4jEmbedding embedding = new StructurizrNeo4jEmbedding(dotenv, embeddingClient);

		embedding.embed("/Users/xwp/github/structurizr/examples/dsl/big-bank-plc/workspace.dsl");

//		embedding.tearDown();
		embedding.close();
	}
}
