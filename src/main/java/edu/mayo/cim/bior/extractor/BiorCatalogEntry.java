package edu.mayo.cim.bior.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BiorCatalogEntry {
    private final static Splitter SPLITTER_TAB = Splitter.on('\t').trimResults();
    private final static Splitter SPLITTER_DOT = Splitter.on('.').trimResults();

    private final static ObjectMapper OBJECT_MAPPER;
    static{
        OBJECT_MAPPER = new ObjectMapper();
    }

    private final String reference;
    private final long start;
    private final long end;
    private final JsonNode jsonNode;

    public BiorCatalogEntry(String reference, long start, long end, JsonNode jsonNode) {
        this.reference = reference;
        this.start = start;
        this.end = end;
        this.jsonNode = jsonNode;
    }

    public static BiorCatalogEntry fromBioRLine(String line){
        List<String> splitLine = SPLITTER_TAB.splitToList(line);
        try {
            return new BiorCatalogEntry(
                    splitLine.get(0),
                    Long.valueOf(splitLine.get(1)),
                    Long.valueOf(splitLine.get(2)),
                    OBJECT_MAPPER.readTree(splitLine.get(3)));
        } catch (IOException e) {
            throw new BiorExtractionException("We had trouble parsing tjson.", e);
        }
    }

    public String getReference() {
        return reference;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    private JsonNode getJsonNode() {
        return jsonNode;
    }

    private JsonNode getJsonNode(String path){
        JsonNode resolved = jsonNode;
        for(String pathSegment : SPLITTER_DOT.splitToList(path)){
            resolved = jsonNode.get(pathSegment);
        }
        return resolved;
    }

    public Object getValue(String path){
        JsonNode node = getJsonNode(path);

        switch (node.getNodeType()){
            case NULL:
                return null;
            case STRING:
                return node.asText();
            case NUMBER:
                if(node.asText().matches("\\d+")){
                    return node.asLong();
                }else{
                    return node.asDouble();
                }
            case BOOLEAN:
                return node.asBoolean();
            case ARRAY:
                return ImmutableList.copyOf(node)
                        .stream()
                        .map(JsonNode::asText)
                        .collect(Collectors.joining(","));
            default:
                return node.toString();
        }
    }

    public String getStringValue(String path) {
        Object value = getValue(path);
        if(value == null){
            return null;
        }else if(value instanceof String){
            return (String)value;
        }else {
            return String.valueOf(path);
        }
    }
}
