package com.beyondprototype.structurizr.neo4j;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.model.*;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Session;
import org.springframework.ai.embedding.EmbeddingClient;

import java.io.File;
import java.util.Map;

@Slf4j
public class StructurizrNeo4jVectorStoreEmbedding {

	private static String CREATE_VECTOR_INDEX_RELATIONSHIP = """
	CREATE VECTOR INDEX Relationship_index
	IF NOT EXISTS
	FOR ()-[r:Uses]-() ON (r.embedding)
	OPTIONS {
		 indexConfig: {
		 `vector.dimensions`: %s,
		 `vector.similarity_function`: '%s'
		}
	}""";

	private static String CREATE_VECTOR_INDEX_ELEMENT = """
	CREATE VECTOR INDEX Element_index
	IF NOT EXISTS
	FOR (e:Element) ON (e.embedding)
	OPTIONS {
		indexConfig: {
	 	`vector.dimensions`: %s,
	 	`vector.similarity_function`: '%s'
		}
	}""";

	private static String CREATE_ELEMENT = "MERGE (:Element {name:$name, description:$description, tags:$tags, type:$type, parent:$parent, source:'%s', embedding:$embedding})";

	private static String CREATE_RELATIONSHIP_CONTAINS = """
   	MATCH (parent:Element {name: $parent.name, type: $parent.type}),(child:Element {name: $child.name, type:$child.type})
   	MERGE (parent)-[:Contains]->(child)
	""";

	private static String CREATE_RELATIONSHIP_USES = """
	MATCH (consumer:Element {name: $consumer.name, type: $consumer.type}), (provider:Element {name: $provider.name, type:$provider.type})
	MERGE (consumer)-[:Uses {technology: $relationship.technology, consumer: $consumer.name, provider: $provider.name,description: $relationship.description, source:$metadata.source, embedding: $embedding}]->(provider)
	""";

	private EmbeddingClient embeddingClient;

	private Session session;

	public StructurizrNeo4jVectorStoreEmbedding(Session session, EmbeddingClient embeddingClient){
		this.session = session;
		this.embeddingClient = embeddingClient;
		setup();
	}

	private void setup() {
		session.run(CREATE_VECTOR_INDEX_ELEMENT.formatted(embeddingClient.dimensions(), "cosine"));
		session.run(CREATE_VECTOR_INDEX_RELATIONSHIP.formatted(embeddingClient.dimensions(), "cosine"));
	}

	public void tearDown(){
		session.run( "MATCH ()-[r:Uses]-() DELETE r" );
		session.run( "MATCH ()-[r:Contains]-() DELETE r" );
		session.run( "MATCH (p) DELETE p" );
		session.run("DROP INDEX Element_index IF EXISTS");
		session.run("DROP INDEX Relationship_index IF EXISTS");
	}

	public void embed(Workspace workspace) {

		String cypher = CREATE_ELEMENT.formatted(workspace.getName());

		//persist person
		workspace.getModel().getPeople().forEach(person -> {
			session.run(cypher,embed(person,embeddingClient));
		});

		workspace.getModel().getSoftwareSystems().forEach(system -> {
			//persist software system
			session.run(cypher,embed(system,embeddingClient));

			system.getContainers().forEach(container -> {
				//persist containers
				session.run(cypher,embed(container,embeddingClient));
				session.run(CREATE_RELATIONSHIP_CONTAINS, Map.of("parent", mapOf(system), "child", mapOf(container)));

				container.getComponents().forEach(component -> {
					//persist components
					session.run(cypher,embed(component, embeddingClient));
					session.run(CREATE_RELATIONSHIP_CONTAINS, Map.of("parent", mapOf(container), "child", mapOf(component)));
				});
			});
		});

		//persist relationship
		workspace.getModel().getRelationships().forEach(relationship -> {
			Element consumer = relationship.getSource();
			Element provider = relationship.getDestination();

			String text = "'%s' %s '%s'".formatted(consumer.getName(),relationship.getDescription(),provider.getName());
			session.run(CREATE_RELATIONSHIP_USES,Map.of(
//					"text", text,
					"embedding", embeddingClient.embed(text),
					"consumer", mapOf(consumer),
					"provider", mapOf(provider),
					"relationship", mapOf(relationship),
					"metadata", Map.of("source",workspace.getName())));
		});
	}

	public void embed(String workspaceDslFile) throws Exception {

		StructurizrDslParser parser = new StructurizrDslParser();

		parser.parse(new File(workspaceDslFile));
		Workspace workspace = parser.getWorkspace();

		embed(workspace);
	}

	private Map<String, Object> mapOf(Element element) {
		String name = element.getName() == null?"":element.getName();
		String description = element.getDescription() == null?"":element.getDescription();
		String type = typeOf(element);
		return Map.of("name", name,
				"description", description,
				"tags",element.getTags(),
				"type", type,
				"parent", element.getParent() == null ? "":element.getParent().getName());
	}

	private Map<String, Object> embed(Element element, EmbeddingClient embeddingClient) {
		String name = element.getName() == null?"":element.getName();
		String description = element.getDescription() == null?"":element.getDescription();
		String type = typeOf(element);
		String text = "name: %s\ntype: %s\ndescription: %s".formatted(name, type, description);
		return Map.of("name", name,
				"description", description,
				"tags",element.getTags(),
				"type", type,
				"parent", element.getParent() == null ? "":element.getParent().getName(),
//				"text",text,
				"embedding", embeddingClient.embed(text));
	}

	private Map<String, Object> mapOf(Relationship relationship) {

		return Map.of("description", relationship.getDescription(),
				"tags",relationship.getTags(),
				"technology",relationship.getTechnology());
	}

	private String typeOf(Element element) {

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
}
