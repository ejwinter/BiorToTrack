package edu.mayo.cim.bior.extractor


import com.google.common.base.Splitter
import com.google.common.io.Resources
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class ExtractorSpec extends Specification {

    def "writeAlamutGff3"(){
        given:
        Path catPath = Paths.get(Resources.getResource("catalog/x.vcf.tsv.bgz").toURI());
        and:
        StringWriter writer = new StringWriter();

        when:
        Extractor.writeAlamutGff3(catPath, ["ACC_NUM", "CLASS", "PHEN", "PubMed"], "192,192,192", "http://service/%{ACC_NUM}%", writer);

        then:
        Splitter.on(System.lineSeparator()).splitToList(writer.toString()).get(3) == "1\tx\t.\t874491\t874491\t.\t.\t.\tACC_NUM=CM1613954;CLASS=DM?;PHEN=Retinitis_pigmentosa;PubMed=27734943;link=http://service/CM1613954"

    }
}
