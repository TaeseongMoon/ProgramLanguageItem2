import interpreter.CuteInterpreter;
import parser.parse.ParserMain;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        CuteInterpreter interpreter = new CuteInterpreter();
        String input;

        while(true) {
            System.out.print("> ");     // prompt 띄우기
            input = sc.nextLine();      // Cute Expression을 입력
            System.out.println("… " + interpreter.runCommand(input));   // 해당되는 결과 출력
        }
    }
}
