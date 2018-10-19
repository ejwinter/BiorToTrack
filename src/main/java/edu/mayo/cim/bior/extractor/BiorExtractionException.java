package edu.mayo.cim.bior.extractor;

import java.io.IOException;

public class BiorExtractionException extends RuntimeException {
    public BiorExtractionException(String message, IOException e) {
        super(message,e);
    }
}
