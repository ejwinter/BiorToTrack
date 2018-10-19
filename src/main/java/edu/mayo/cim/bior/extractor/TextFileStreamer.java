package edu.mayo.cim.bior.extractor;

import htsjdk.samtools.util.BlockCompressedInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class TextFileStreamer implements AutoCloseable {

    private final InputStream inputStream;

    public TextFileStreamer(InputStream inputStream) throws IOException {
        inputStream = new BufferedInputStream(inputStream);
        if(BlockCompressedInputStream.isValidFile(inputStream)){

            this.inputStream = new BlockCompressedInputStream(inputStream);
        }else if(isGZipped(inputStream)){
            this.inputStream = new GZIPInputStream(inputStream);
        }else {
            this.inputStream = inputStream;
        }
    }

    public Stream<String> lines(){
        return new BufferedReader(new InputStreamReader(inputStream)).lines();
    }

    public static TextFileStreamer fromPath(Path path) throws IOException {
        return new TextFileStreamer(Files.newInputStream(path, StandardOpenOption.READ));
    }

    private static boolean isGZipped(InputStream in) {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(2);
        int magic = 0;
        try {
            magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
            in.reset();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
