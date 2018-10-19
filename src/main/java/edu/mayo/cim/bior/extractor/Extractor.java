package edu.mayo.cim.bior.extractor;

import com.google.common.base.Splitter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Extractor {

    private final static OptionParser OPTION_PARSER;
    public static final Pattern URL_TEMPLATE_PATTERN = Pattern.compile(".*%\\{(.*)\\}%.*");

    static{
        OPTION_PARSER = new OptionParser();

        OPTION_PARSER.acceptsAll(Arrays.asList("catalog-file", "c"), "The file that is the catalog.tsv often ending in .tsv.bgz")
                .withRequiredArg()
                .ofType(File.class);

        OPTION_PARSER.acceptsAll(Arrays.asList("attributes", "a"), "A comma delimited list of the paths to within the catalog to pull out fields to include.")
                .withRequiredArg()
                .withValuesSeparatedBy(',')
                .ofType(String.class);

        OPTION_PARSER.accepts("color", "A color in R,G,B format. R,G,B between 0 and 255.")
                .withOptionalArg()
                .defaultsTo("192,192,192")
                .ofType(String.class);

        OPTION_PARSER.acceptsAll(Arrays.asList("output-file", "o"), "The output file.  If not specified it will go to standard out.")
                .withRequiredArg()
                .ofType(File.class);

        OPTION_PARSER.acceptsAll(Arrays.asList("url", "u"), "A url template that should include a %{FIELD_NAME}% in it.  We will create a URL= attribute with that template filled out.")
                .withRequiredArg()
                .ofType(String.class);

        OPTION_PARSER.acceptsAll(Arrays.asList("help", "h"), "Display help.").forHelp();
    }

    public static void main(String[] args) throws IOException {

        OptionSet options = extractOptions(args);

        Path catalogFile = ((File)options.valueOf("catalog-file")).toPath();

        if(!Files.isRegularFile(catalogFile)){
            printUsageAndDie("The catalog-file presented does not exist.");
        }

        List<String> attributes = options.valuesOf("attributes").stream().map(o->(String)o).collect(Collectors.toList());

        String url = (options.has("url") ? (String) options.valueOf("url") : null);

        if(options.has("output-file")){
            try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(((File)options.valueOf("output-file")).toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))){
                writeAlamutGff3(catalogFile, attributes, (String) options.valueOf("color"), url, writer);
            }
        }else {
            writeAlamutGff3(catalogFile, attributes, (String) options.valueOf("color"), url, new PrintWriter(System.out));
        }

    }

    private static void writeAlamutGff3(Path catalogFile, List<String> attributes, String color, String urlTemplate, Writer writeTo) {
        try(TextFileStreamer streamer = TextFileStreamer.fromPath(catalogFile)){
            String catalogName = Splitter.on('.').splitToList(catalogFile.getFileName().toString()).get(0);
            writeTo.append("##gff-version 3").append(System.lineSeparator());
            writeTo.append(String.format("##alamut:source=%s:name=%s:color=%s", catalogName, attributes.get(0), color)).append(System.lineSeparator());
            streamer.lines()
                    .map(BiorCatalogEntry::fromBioRLine)
                    .forEach(entry -> {
                        StringBuilder line = new StringBuilder(300);
                        line.append(entry.getReference()).append('\t');
                        line.append(catalogName).append('\t');
                        line.append(".\t");
                        line.append(entry.getStart()).append('\t');
                        line.append(entry.getEnd()).append('\t');
                        line.append(".\t");
                        line.append(".\t");
                        line.append(".\t");
                        line.append(attributes.stream()
                                .map(a->a+"="+entry.getValue(a))
                                .collect(Collectors.joining(";")));

                        if(urlTemplate != null){
                            line.append(';').append("link=").append(getUrl(urlTemplate, entry));
                        }

                        try {
                            writeTo.append(line.toString()).append(System.lineSeparator());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String getUrl(String urlTemplate, BiorCatalogEntry entry) {
        String templateAttributeName = null;
        Matcher patternMatch = URL_TEMPLATE_PATTERN.matcher(urlTemplate);
        if(patternMatch.matches()){
            templateAttributeName = patternMatch.group(1);
        }
        String completedTemplate = urlTemplate;
        if(templateAttributeName != null) {
            String attributeValue = entry.getStringValue(templateAttributeName);
            completedTemplate = urlTemplate.replace("%{" + templateAttributeName + "}%", attributeValue);
        }
        return completedTemplate;
    }

    private static OptionSet extractOptions(String[] args) {
        try {
            OptionSet optionSet = OPTION_PARSER.parse(args);
            if(optionSet.hasArgument("help")){
                printUsage();
            }

            if(!optionSet.hasArgument("catalog-file")){
                printUsageAndDie("The catalog-file must be provided.");
            }

            if(!optionSet.hasArgument("attributes")){
                printUsageAndDie("You must specify at least attribute.");
            }
            return optionSet;
        }catch(OptionException e){
            printUsageAndDie(e.getMessage());
            e.printStackTrace(System.err);
        }
        return null;
    }

    private static void printUsage() {
        try {
            OPTION_PARSER.printHelpOn(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void printUsageAndDie(String message){
        System.err.println(message);
        try {
            OPTION_PARSER.printHelpOn(System.out);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.exit(1);
    }
}
