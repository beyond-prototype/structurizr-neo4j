package com.beyondprototype.structurizr.neo4j;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.model.*;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.mistralai.MistralAiEmbeddingClient;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class StructurizrNeo4jEmbeddingApplication {
	public static <Neo4jVector> void main(String... args) throws Exception {

		StructurizrDslParser parser = new StructurizrDslParser();

		String[] workspaces = new String[]{
				"/Users/xwp/github/structurizr/examples/dsl/big-bank-plc/workspace.dsl"
		};

		Dotenv dotenv = Dotenv.configure()
				.directory("/Users/xwp/github/beyond-prototype/structurizr-neo4j")
				.filename(".env")
				.load();

		System.out.println("spring.neo4j.uri = " + dotenv.get("spring.neo4j.uri"));

		try (var driver = GraphDatabase.driver(dotenv.get("spring.neo4j.uri"),
				AuthTokens.basic(dotenv.get("spring.neo4j.authentication.username"), dotenv.get("spring.neo4j.authentication.password")))) {
			driver.verifyConnectivity();

			Neo4jVectorStore neo4jVectorStore = createVectorStore(dotenv, driver);

			//https://neo4j.com/docs/java-manual/current/transactions/
			try (Session session = driver.session(SessionConfig.builder().withDatabase("neo4j").build())) {
				//clean up
				session.run( "MATCH ()-[r]-() DELETE r" );
				session.run( "MATCH (p) DELETE p" );

				for(String ws: workspaces) {
					parser.parse(new File(ws));
					Workspace workspace = parser.getWorkspace();

					//persist person
					workspace.getModel().getPeople().forEach(person -> {
						session.run("MERGE (:Person {name: $person.name, description: $person.description, tags: $person.tags})", Map.of("person", mapOf(person)));
					});

					//persist software system
					workspace.getModel().getSoftwareSystems().forEach(system -> {
						session.run("MERGE (:SoftwareSystem {name: $system.name, description: $system.description, tags: $system.tags})", Map.of("system", mapOf(system)));

						//persist containers
						system.getContainers().forEach(container -> {
							session.run("MERGE (:Container {name: $container.name, description: $container.description, tags: $container.tags})", Map.of("container", mapOf(system)));
//							session.run("MATCH (system:SoftwareSystem {name: $system.name}), (container:Container {name: $container.name}) " +
//									" CREATE (system)-[:Contains]->(container)",
//									Map.of("system", mapOf(system), "container", mapOf(container)));

							session.run("MATCH (system:SoftwareSystem {name: $system.name}) " +
											" MERGE (system)-[:Contains]->(container:Container {name: $container.name})",
									Map.of("system", mapOf(system), "container", mapOf(container)));

							container.getComponents().forEach(component -> {
								session.run(" MERGE (:Component {name: $component.name, description: $component.description, tags: $component.tags})", Map.of("component", mapOf(component)));
//								session.run(" MATCH (container:Container {name: $container.name}), (component:Component {name: $component.name}) " +
//												" CREATE (container)-[:Contains]->(component)",
//										Map.of("container", mapOf(container), "component", mapOf(component)));

								session.run(" MATCH (container:Container {name: $container.name})" +
												" MERGE (container)-[:Contains]->(component:Component {name: $component.name}) ",
										Map.of("container", mapOf(container), "component", mapOf(component)));
							});
						});
					});

					workspace.getModel().getRelationships().forEach(relationship -> {
							Element destination = relationship.getDestination();
							Element source = relationship.getSource();
							StringBuffer sql = new StringBuffer();
							sql.append(" MATCH  (source:%s {name: $source.name}), (destination:%s {name: $destination.name})".formatted(typeOf(source),typeOf(destination)));
//							sql.append(" CREATE (source)-[:`%s`]->(destination)".formatted(relationship.getDescription()));
//							if (source instanceof Person && destination instanceof Person){
//								sql.append(" CREATE (source)-[:Interacts {description: $relationship.description}]->(destination)");
//							}
							sql.append(" MERGE (source)-[:Uses {description: $relationship.description}]->(destination)");
							session.run(sql.toString(),Map.of("source", mapOf(source),
									"destination", mapOf(destination),
									"relationship", mapOf(relationship)));
					});

					//Create embedding for selected types of elements
					Set<String> types = Set.of("Person", "SoftwareSystem", "Container", "Component");
					List<Document> documents = new ArrayList<>();

					workspace.getModel().getElements().forEach(element -> {
						if (types.contains(typeOf(element))) {
							documents.add(new Document(element.getName(),
									"Name:%s\nDescription:%s".formatted(element.getName(), element.getDescription()),
									Map.of("tags", element.getTags(),
											"name", element.getName(),
											"type", typeOf(element),
											"parent", element.getParent() == null?"":element.getParent().getName(),
											"source", workspace.getName())));
						}
					});
					neo4jVectorStore.add(documents);
				}

				//get all objects
//				System.out.println("******* get all SoftwareSystem objects ********");
//				Result result = session.run( "MATCH (s:SoftwareSystem) RETURN s.name, s.description" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});
//
//				System.out.println("******* get all Person objects ********");
//				result = session.run( "MATCH (p:Person) RETURN p.name, p.description" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});
//
//				System.out.println("******* get all Person Relationships ********");
//				//MATCH p=()-[r:Uses]->() RETURN p
//				result = session.run( "MATCH (p:Person)-[:Uses]-(s:SoftwareSystem) return p.name as Person, s.name as SoftwareSystem" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});
//
//				System.out.println("******* get SoftwareSystem Contains Relationships ********");
//				Result result = session.run( "MATCH (p:SoftwareSystem)-[:Contains]-(s) return p.name as SoftwareSystem, s.name as Container" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});
//
//				System.out.println("******* get Container Contains Relationships ********");
//				result = session.run( "MATCH (p:Container)-[:Contains]-(s) return p.name as Container, s.name as Component" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});

				//Clean Up
//				session.run( "MATCH (:Person)-[r:Uses]-(:SoftwareSystem) DELETE r" );
//				session.run( "MATCH (:SoftwareSystem)-[r:Uses]-(:SoftwareSystem) DELETE r" );
//				session.run( "MATCH ()-[r:Contains]-() DELETE r" );
//				session.run( "MATCH (p:Person) DELETE p" );
//				session.run( "MATCH (s:SoftwareSystem) DELETE s" );

				//Similarity Search
//				List<Document> results = neo4jVectorStore.similaritySearch(SearchRequest.query("what software systems can be used to store customer information?").withTopK(1));
//				results.forEach( doc -> {
//					System.out.println(doc.toString());
//				});

//				Result result = session.run("MATCH (n:Element) where n.`metadata.type`='SoftwareSystem' RETURN n");
//				Result result = session.run( "MATCH (s:SoftwareSystem) RETURN s.name, s.description" );
//				result.forEachRemaining( record -> {
//					System.out.println(record);
//				});
			}
		}
	}

	public static Map<String, Object> mapOf(Element element) {
		return Map.of("name", element.getName() == null?"":element.getName(),
				"description", element.getDescription() == null?"":element.getDescription(),
				"tags",element.getTags());
	}

	public static Map<String, Object> mapOf(Relationship relationship) {
		return Map.of("description", relationship.getDescription(),
				"tags",relationship.getTags());
	}

	public static String typeOf(Element element) {

		if(element instanceof Person) {
			return "Person";
		} else if(element instanceof SoftwareSystem) {
			return "SoftwareSystem";
		} else if(element instanceof Container) {
			return "Container";
		} else if(element instanceof Component) {
			return "Component";
		} else if(element instanceof ContainerInstance) {
			return "ContainerInstance";
		} else if(element instanceof DeploymentNode) {
			return "DeploymentNode";
		} else if(element instanceof SoftwareSystemInstance) {
			return "SoftwareSystemInstance";
		}

		return element.getCanonicalName();
	}

	public static Neo4jVectorStore createVectorStore(Dotenv dotenv, Driver driver){
		MistralAiApi mistralAiApi = new MistralAiApi(dotenv.get("spring.ai.mistralai.api-key"));
		EmbeddingClient embeddingClient = new MistralAiEmbeddingClient(mistralAiApi);
		Neo4jVectorStore.Neo4jVectorStoreConfig config =  Neo4jVectorStore.Neo4jVectorStoreConfig
				.builder()
				.withLabel("Element")
				.withIdProperty("name")
				.withEmbeddingProperty("embedding")
				.withIndexName("Element_index")
				.withConstraintName("Element_unique_idx")
				.build();

		Neo4jVectorStore neo4jVector = new Neo4jVectorStore(driver,embeddingClient, config);

		return neo4jVector;
	}
}
