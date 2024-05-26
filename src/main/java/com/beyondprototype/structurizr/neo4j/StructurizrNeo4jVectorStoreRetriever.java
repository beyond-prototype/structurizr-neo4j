package com.beyondprototype.structurizr.neo4j;

import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Neo4jVectorFilterExpressionConverter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructurizrNeo4jVectorStoreRetriever {

    private static final String QUERY_RELATIONSHIP_INDEX = """
    CALL db.index.vector.queryRelationships("Relationship_index", $numberOfNearestNeighbours, $embeddingValue)
    YIELD relationship, score
    WHERE %s
    RETURN relationship, score
    """;

    private static final String QUERY_ELEMENT_INDEX = """
    CALL db.index.vector.queryNodes("Element_index", $numberOfNearestNeighbours, $embeddingValue)
    YIELD node, score
    WHERE %s
    RETURN node, score
    """;

    private static final String QUERY_PERSON = """
    MATCH (e:Element)
    WHERE e.type='Person' AND %s
    RETURN e.tags as tags, e.name as name, e.description as description
    """;

    private static final String QUERY_SOFTWARE_SYSTEM = """
    MATCH (e:Element)
    WHERE e.type='SoftwareSystem' AND %s
    RETURN e.tags as tags, e.name as name, e.description as description
    """;

    private Session session;
    private final EmbeddingClient embeddingClient;

    public StructurizrNeo4jVectorStoreRetriever(Session session, EmbeddingClient embeddingClient) {
        this.session = session;
        this.embeddingClient = embeddingClient;
    }

    private final Neo4jVectorFilterExpressionConverter filterExpressionConverter = new Neo4jVectorFilterExpressionConverter();

    private List<Document> fromExistingRelationshipIndex(SearchRequest request, Value embedding){

        StringBuilder condition = new StringBuilder("score >= $threshold");

        return session.run(QUERY_RELATIONSHIP_INDEX.formatted(condition), Map.of( "numberOfNearestNeighbours", SearchRequest.DEFAULT_TOP_K,
                        "embeddingValue", embedding,
                        "threshold", request.getSimilarityThreshold()))
                .list(this::relationshipToDocument);
    }

    private List<Document> fromExistingElementIndex(SearchRequest request, Value embedding) {

        StringBuilder condition = new StringBuilder("score >= $threshold");
        if (request.hasFilterExpression()) {
            condition.append(" AND ")
                    .append(this.filterExpressionConverter.convertExpression(request.getFilterExpression()));
        }

        return session
                .run(QUERY_ELEMENT_INDEX.formatted(condition), Map.of( "numberOfNearestNeighbours", SearchRequest.DEFAULT_TOP_K,
                        "embeddingValue", embedding,
                        "threshold", request.getSimilarityThreshold()))
                .list(this::elementToDocument);
    }

    private static float[] toFloatArray(List<Double> embeddingDouble) {
        float[] embeddingFloat = new float[embeddingDouble.size()];
        int i = 0;
        for (Double d : embeddingDouble) {
            embeddingFloat[i++] = d.floatValue();
        }
        return embeddingFloat;
    }

    private Document elementToDocument(org.neo4j.driver.Record neoRecord) {

        var node = neoRecord.get("node").asNode();
        var score = neoRecord.get("score").asFloat();

        StringBuilder text = new StringBuilder();
        List<String> textNodeProperties = List.of("name", "type", "description");
        textNodeProperties.forEach( p -> {
            text.append(p).append(":\"").append(node.get(p).asString()).append("\" ");
        });

        var metadata = new HashMap<String, Object>();
//        metadata.put("distance", 1 - score);
//        node.keys().forEach(key -> {
//            if (key.startsWith("metadata.")) {
//                metaData.put(key.substring(key.indexOf(".") + 1), node.get(key).asObject());
//            }
//        });
        metadata.put("score", score);
        metadata.put("tags", node.get("tags").asString());
        metadata.put("name", node.get("name").asString());
        metadata.put("type", node.get("type").asString());
        metadata.put("parent", node.get("parent").asString());
        metadata.put("source", node.get("source").asString());

        return new Document(node.get("name").asString(), text.toString(), Map.copyOf(metadata));
    }

    private Document relationshipToDocument(org.neo4j.driver.Record neoRecord) {

        var relationship = neoRecord.get("relationship").asRelationship();
        var score = neoRecord.get("score").asFloat();

        var metadata = new HashMap<String, Object>();
        metadata.put("score", score);

        StringBuilder text = new StringBuilder();
//        List<String> textNodeProperties = List.of("consumer", "description", "provider");
//        textNodeProperties.forEach( p -> {
//            text.append(relationship.get(p).asString()).append(" ");
//        });
        text.append("\"%s\" %s \"%s\"".formatted(relationship.get("consumer").asString(), relationship.get("description").asString(), relationship.get("provider").asString()));

        relationship.keys().forEach(key -> {
            if(!key.equals("embedding")){
                metadata.put(key, relationship.get(key).asString());
            }
        });

        return new Document("relationship", text.toString(), Map.copyOf(metadata));
    }

    public List<Document> similaritySearch(String query){
        return similaritySearch(SearchRequest.query(query));
    }

    public List<Document> similaritySearch(SearchRequest request){
        Assert.isTrue(request.getTopK() > 0, "The number of documents to returned must be greater than zero");
        Assert.isTrue(request.getSimilarityThreshold() >= 0 && request.getSimilarityThreshold() <= 1,
                "The similarity score is bounded between 0 and 1; least to most similar respectively.");

        var embedding = Values.value(toFloatArray(this.embeddingClient.embed(request.getQuery())));

        SearchRequest request1 = SearchRequest.query(request.getQuery()).withSimilarityThreshold(request.getSimilarityThreshold());

        List<Document> results = fromExistingElementIndex(request1, embedding);

        results.addAll(fromExistingRelationshipIndex(request1, embedding));

        //filter topK documents
        results.sort((doc1, doc2) -> {
            Float s1 = (Float) doc1.getMetadata().get("score");
            Float s2 = (Float) doc2.getMetadata().get("score");
            return s2.compareTo(s1);
        });

        int topK = request.getTopK();
        if (results.size() > topK) {
            return results.subList(0, topK);
        }

        return results;
    }
}
