package edu.mayo.cim.bior.extractor;

import java.nio.file.Path;

public class BiorCatalog {

    private final Path path;

    private final String name;

    private final Path catalogPath;

    public BiorCatalog(Path path, String name) {
        this.path = path;
        this.name = name;

        this.catalogPath = path.resolve(name + ".vcf.tsv.bgz");
    }


    public Path getPath() {

        return path;
    }

    public String getName() {
        return name;
    }

    public Path getCatalogPath() {
        return catalogPath;
    }
}
