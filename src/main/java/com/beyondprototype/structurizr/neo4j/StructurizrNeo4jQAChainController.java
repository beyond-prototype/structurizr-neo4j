package com.beyondprototype.structurizr.neo4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class StructurizrNeo4jQAChainController {

    private StructurizrNeo4jQAChain chain;

    @Autowired
    public StructurizrNeo4jQAChainController(StructurizrNeo4jQAChain chain){
        this.chain = chain;
    }
    @GetMapping(value = "/ai/qa")
    public Object qa(@RequestParam(value = "message") String message){

        StructurizrNeo4jQAChain.QaOutput qaOutput = chain.invoke(message);
        return qaOutput;

//        ArrayList<String> output = new ArrayList<>();
//        output.add("Question: %s".formatted(qaOutput.getQuestion()));
//        output.add("CypherStatement: %s".formatted(qaOutput.getCypherStatement()));
//        output.add("CypherQueryResult: %s".formatted(qaOutput.getCypherQueryResult()));
//        output.add("SimilaritySearchResult: %s".formatted(qaOutput.getSimilaritySearchResult()));
//        output.add("Answer: %s".formatted(qaOutput.getAnswer()));
//
//        return output;
    }
    //http://localhost:8080/ai/qa?message=what%20software%20systems%20are%20used%20by%20staff
}
