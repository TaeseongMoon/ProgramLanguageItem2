package lexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Scanner {
    /*
    // return tokens as an Iterator
    public static Iterator<Token> scan(File file) throws FileNotFoundException {
        ScanContext context = new ScanContext(file);    // 공백단위로 끓어 읽어 반환
        return new TokenIterator(context);
    }

    // return tokens as a Stream 
    public static Stream<Token> stream(File file) throws FileNotFoundException {
        Iterator<Token> tokens = scan(file);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(tokens, Spliterator.ORDERED), false);
    }
    */

    public static Iterator<Token> scan(String command) {
        ScanContext context = new ScanContext(command);    // 공백단위로 끓어 읽어 반환
        return new TokenIterator(context);
    }

    // return tokens as a Stream
    public static Stream<Token> stream(String command) {
        Iterator<Token> tokens = scan(command);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(tokens, Spliterator.ORDERED), false);
    }
}