# BiorToTrack

This program will take a BioR catalog (http://bioinformaticstools.mayo.edu/research/bior/) and can turn its entries
into a GFF3 file.  The primary intent is to create a GFF3 file that can be loaded into Alamut (https://www.interactive-biosoftware.com/alamut-visual/)
as a track.

We could support other file types (e.g. BED) but this is currently just a quick and dirty utility.



A catalog with this entry

```
1	865595	865595	{"_landmark":"1","_type":"variant","_minBP":865595,"_maxBP":865595,"_refAllele":"A","_altAlleles":["G"],"CHROM":"1","POS":865595,"ID":["CM1613956"],"REF":"A","ALT":"G","CLASS":["DM?"],"MUT":["ALT"],"GENE":"SAMD11","STRAND":"+","DNA":"NM_152486.2:c.133A>G","PROT":"NP_689699.2:p.K45E","PHEN":["Retinitis_pigmentosa"],"ACC_NUM":["CM1613956"],"PubMed":["27734943"],"OMIM_ID":["616765"],"RSID":["rs903331232"],"NewEntryDate":["2016-11-10"],"VariantType":["Mutation"],"AAchange":"Lys45Glu","HGVS":"133A>G","TAG":["DM?"],"Rankscore":[0.21],"Author":"Corton","JournalName":"Scientific reports"}
```

Would get turned into the following.

```
##gff-version 3
##alamut:source=x:name=CLASS:color=192,192,192
1       x   .       865595  865595  .       .       .       CLASS=DM?;ID=CM1613956;ACC_NUM=CM1613956;PHEN=Retinitis_pigmentosa;PubMed=27734943;link=https://portal.biobase-international.com/hgmd/pro/mut.php?acc=CM1613956
```


## How to use it

### Build it
After cloning this repository you can build using Apache Maven 3.x.

```bash
cd BiorToTrack
mvn clean package
```

### Run it

#### Getting the help screen.
Assumming you have a Java JRE 8+ on your path...
```bash
java -jar target/BiorCatalogExtractor-1.0-SNAPSHOT.jar -h

Option                     Description
------                     -----------
-a, --attributes <String>  A comma delimited list of the paths to within the
                             catalog to pull out fields to include.
-c, --catalog-file <File>  The file that is the catalog.tsv often ending in .
                             tsv.bgz
--color [String]           A color in R,G,B format. R,G,B between 0 and 255.
                             (default: 192,192,192)
-h, --help                 Display help.
-o, --output-file <File>   The output file.  If not specified it will go to
                             standard out.
-u, --url <String>         A url template that should include a %{FIELD_NAME}%
                             in it.  We will create a URL= attribute with that
                             template filled out.
```

#### Transforming a catalog

This will pull the CLASS, ID, ACC_NUM, PHEN, and PubMed attributes.

```bash
java -jar target/BiorCatalogExtractor-1.0-SNAPSHOT.jar \
    # select which attributes to pull out of catalog
    --attributes CLASS,ID,ACC_NUM,PHEN,PubMed \
    # adds a link= attribute using this as a pattern for each entry in bed
    --url https://linkToweb.com?acc=%{ACC_NUM}% \
    # specify the input
    --catalog-file /path/to/catalog.vcf.tsv.bgz
    # the output file, if not specified it will go to stdout
    --output-file /path/to/output.gff3
```
