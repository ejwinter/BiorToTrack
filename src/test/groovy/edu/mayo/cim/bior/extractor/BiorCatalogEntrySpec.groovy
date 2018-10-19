package edu.mayo.cim.bior.extractor

import spock.lang.Specification

import javax.annotation.Resources
import java.util.stream.Collectors

class BiorCatalogEntrySpec extends Specification {

    TextFileStreamer streamer

    def setup(){
        streamer = new TextFileStreamer(Resources.getResourceAsStream("/catalog/x.vcf.tsv.bgz"))
    }

    def "Parsing "(){

        when:
        List<BiorCatalogEntry> entries = streamer.lines()
                .map {l->BiorCatalogEntry.fromBioRLine(l) }
                .collect(Collectors.toList())

        then:
        BiorCatalogEntry lastEntry = entries[-1]
        lastEntry.getValue("CHROM") == "1"
        lastEntry.getValue("MUT") == "ALT"
        lastEntry.getValue("POS") == 957605
        lastEntry.getValue("PHEN") == "Congenital_myasthenic_syndrome_with_distal_muscle_weakness_&_atrophy"

    }
}
