package lexer;

import java.io.*;
import java.util.stream.Stream;

public class ScannerMain {
    public static final void main(String... args) throws Exception {
        ClassLoader cloader = ScannerMain.class.getClassLoader();   //클래스로더
        String command = "( + 2 3 ) ";
        testTokenStream(command);
    }

    // use tokens as a Stream
    private static void testTokenStream(String command) {
        Stream<Token> tokens = Scanner.stream(command);

        try(FileWriter writer = new FileWriter("output04.txt");
            PrintWriter pw  = new PrintWriter(writer))
        {
            tokens.map(ScannerMain::toString).forEach(pw::println);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String toString(Token token) {
        return String.format("%-3s: %s", token.type().name(), token.lexme());
    }
}
