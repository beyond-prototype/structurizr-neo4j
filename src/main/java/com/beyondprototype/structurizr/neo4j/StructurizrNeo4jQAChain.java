package com.beyondprototype.structurizr.neo4j;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Session;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingClient;

import java.util.List;
import java.util.Map;

@Slf4j
public class StructurizrNeo4jQAChain {

    private ChatClient chatClient;

    private Session session;

    private String schema;

    private EmbeddingClient embeddingClient;

    private StringBuilder examples = new StringBuilder();

    private StructurizrNeo4jVectorStoreRetriever retriever;

    public StructurizrNeo4jQAChain(Session session, ChatClient chatClient, EmbeddingClient embeddingClient) {
        this.session = session;
        this.chatClient = chatClient;
        this.schema = getSchema();
        this.embeddingClient = embeddingClient;
        this.retriever = new StructurizrNeo4jVectorStoreRetriever(session, embeddingClient);

        if (!CYPHER_EXAMPLES.isEmpty()) {
            CYPHER_EXAMPLES.forEach(example -> {
                examples.append("\n#").append(example.question);
                examples.append("\n").append(example.cypher);
            });
        }
    }

    private PromptTemplate createCypherGenerationPromptTemplate(){

        StringBuilder template = new StringBuilder(MY_CYPHER_GENERATION_TEMPLATE_PREFIX);
//        if (!CYPHER_EXAMPLES.isEmpty()) {
//            template.append("Here are a few examples of generated Cypher statements for particular questions:");
//            CYPHER_EXAMPLES.forEach(example -> {
//                template.append("\n#").append(example.question);
//                template.append("\n").append(example.cypher);
//            });
//        }
        template.append(MY_CYPHER_GENERATION_TEMPLATE_SUFFIX);

        PromptTemplate promptTemplate = new PromptTemplate(template.toString());

        return promptTemplate;
        //return promptTemplate.create(Map.of("schema", this.schema, "question","sample"));
    }

    private PromptTemplate createQAGenerationPromptTemplate(){

        StringBuilder template = new StringBuilder(MY_CYPHER_QA_TEMPLATE);

        PromptTemplate promptTemplate = new PromptTemplate(template.toString());

        return promptTemplate;
        //return promptTemplate.create(Map.of("schema", this.schema, "question","sample"));
    }
//    public String invoke(Prompt prompt) {
//       return chatClient.call(prompt);
//    }

    public String invoke(String query) {

        PromptTemplate promptTemplate = createCypherGenerationPromptTemplate();
        Prompt cyperGenerationPrompt = promptTemplate.create(Map.of("schema", this.schema, "question",query, "examples", examples.toString()));

        StringBuilder context = new StringBuilder();
        chatClient.call(cyperGenerationPrompt).getResults().forEach( generation -> {

            AssistantMessage message = generation.getOutput();

            String cypher = message.getContent();

            try {
                session.run(cypher).forEachRemaining(record -> {
                    record.keys().forEach(key -> {
                        context.append(key).append(":\"").append(record.get(key).asString()).append("\",");
                    });
                });
            }catch (Exception e) {
               // e.printStackTrace();
            }

            System.out.println("Generated Cypher:\n%s\n\nCypher Query Result: %s\n".formatted(cypher, context.toString()));

            retriever.similaritySearch(query).forEach(document -> {

                System.out.println("Similarity Search Results: %s\n".formatted(document.getContent()));

                context.append("context:\"").append(document.getContent()).append("\",");
            });
        });

        StringBuilder answer = new StringBuilder();
        Prompt qaPrompt = createQAGenerationPromptTemplate().create(Map.of("context", context.toString(), "question", query));
        chatClient.call(qaPrompt).getResults().forEach(generation -> {
            AssistantMessage message = generation.getOutput();
            answer.append(message.getContent());
        });

        return answer.toString();
    }


    private String getSchema(){
//        Node properties:
//        Element {tags: STRING, name: STRING, type: STRING, embedding: LIST, source: STRING, parent: STRING, description: STRING}
//        Relationship properties:
//        Uses {consumer: STRING, provider: STRING, embedding: LIST, source: STRING, technology: STRING, description: STRING}
//        The relationships:
//        (:Element)-[:Uses]->(:Element)
//        (:Element)-[:Contains]->(:Element)
        StringBuilder schema = new StringBuilder();
        schema.append("Node properties:\n");
        session.run(node_properties_query).list().forEach(record -> {
            record.values().forEach(value -> {
                schema.append(value.get("label").asString()).append(" {");
                value.get("properties").asList().forEach(map ->{
                    schema.append(((Map)map).get("property")).append(":").append(((Map)map).get("type")).append(", ");
                });
                schema.deleteCharAt(schema.lastIndexOf(","));
                schema.append("}\n");
            });
        });

        schema.append("Relationship properties:\n");
        session.run(rel_properties_query).list().forEach(record -> {
            record.values().forEach(value -> {
                schema.append(value.get("label").asString()).append(" {");
                value.get("properties").asList().forEach(map ->{
                    schema.append(((Map)map).get("property")).append(":").append(((Map)map).get("type")).append(", ");
                });
                schema.deleteCharAt(schema.lastIndexOf(","));
                schema.append("}\n");
            });
        });

        schema.append("The relationships:\n");
        session.run(rel_query).list().forEach(record -> {
            record.values().forEach(value -> {
                schema.append("(:%s)-[:%s]->(:%s)\n".formatted(value.get("start").asString(), value.get("type").asString(), value.get("end").asString()));
            });
        });
//        System.out.println(schema);
        return schema.toString();
    }

    static final String node_properties_query = """
    CALL apoc.meta.data()
    YIELD label, other, elementType, type, property
    WHERE NOT type = "RELATIONSHIP" AND elementType = "node" /*AND NOT label IN $EXCLUDED_LABELS*/
    WITH label AS nodeLabel, collect({property:property, type:type}) AS properties
    RETURN {label: nodeLabel, properties: properties} AS output
    """;

    static final String rel_properties_query = """
    CALL apoc.meta.data()
    YIELD label, other, elementType, type, property
    WHERE NOT type = "RELATIONSHIP" AND elementType = "relationship" /*AND NOT label in $EXCLUDED_LABELS*/
    WITH label AS nodeLabel, collect({property:property, type:type}) AS properties
    RETURN {label: nodeLabel, properties: properties} AS output
    """;

    static final String rel_query = """
    CALL apoc.meta.data()
    YIELD label, other, elementType, type, property
    WHERE type = "RELATIONSHIP" AND elementType = "node"
    UNWIND other AS other_node
    /*WITH * WHERE NOT label IN $EXCLUDED_LABELS
        AND NOT other_node IN $EXCLUDED_LABELS*/
    RETURN {start: label, type: property, end: toString(other_node)} AS output
    """;

   static final String graphSample = """
   CALL apoc.meta.graphSample() 
   YIELD nodes, relationships
   RETURN nodes, [rel in relationships | { name:apoc.any.property (rel, 'type'), count: apoc.any.property(rel, 'count')}] AS relationships           
   """;

   static final String MY_CYPHER_GENERATION_TEMPLATE_PREFIX = """
    Task: Generate a syntactically correct cypher statement to query a graph database.
    Instructions:
    Use only the provided relationship types and properties in the schema.
    Do not use any other relationship types or properties that are not provided.
    Do not include parameters in the MATCH statement.
    Only include parameters in the WHERE statement.
    Always use "MATCH (p:Element)-[r:Uses]->(ss:Element)" to lookup "Uses" relationships.
    Always use "MATCH (p:Element)-[r:Contains]->(ss:Element)" to lookup "Contains" relationships.
    
    Schema:
    {schema}
    
    Note: Do not include any explanations or apologies in your responses.
    Do not respond to any questions that might ask anything else than for you to construct a Cypher statement.
    Do not include any text except the generated Cypher statement.    
    
    Here are a few examples of generated Cypher statements for particular questions:
    {examples}
    
    """;

   static final String MY_CYPHER_GENERATION_TEMPLATE_SUFFIX = """   
    User input: {question}
    Cypher query:
    """;

   static final String MY_CYPHER_QA_TEMPLATE = """
    You are an assistant that helps to form nice and human understandable answers.
    The context part contains the provided information that you must use to construct an answer.
    The provided information is authoritative, you must never doubt it or try to use your internal knowledge to correct it.
    Make the answer sound as a response to the question. 
    Do not mention that you based the result on the given context.
    Do not make up anything which does not exist in the provided context.
    
    Here is an example:
    Question: What software systems are used by customers?
    Context: Internet Banking System
    Helpful Answer: The "Internet Banking System" is used by customers.
    
    Follow this example when generating answers. If the provided context is empty, say that you don't know the answer.
    
    Context:
    {context}
    
    Question: {question}
    Helpful Answer:
    """;

   private static class Example {
       private String question;
       private String cypher;

       public Example(String question, String cypher){
           this.question = question;
           this.cypher = cypher;
       }
   }

   static final List<Example> CYPHER_EXAMPLES = List.of(
       new Example("what software systems are used by customers?","""
            MATCH (p:Element)-[r:Uses]->(ss:Element) 
            WHERE p.type="Person" AND p.tags CONTAINS "Customer" AND ss.type="SoftwareSystem" 
            RETURN distinct p.tags as tags, ss.name as name, ss.description as description
            """)
       ,new Example("what software systems are used by customers to withdraw cash?"
               ,"""
            MATCH (p:Element)-[r:Uses]->(ss:Element) 
            WHERE p.type="Person" AND p.tags CONTAINS "Customer" 
                AND (r.description CONTAINS 'withdraw cash' OR (ss.type="SoftwareSystem" AND ss.description CONTAINS 'withdraw cash'))
            RETURN distinct p.tags + " " + r.description + " " + ss.name as context, ss.name as SoftwareSystem, ss.description as description
            """));

}
