import interpreter.CuteInterpreter;
import interpreter.UndefinedException;
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
            try {
                System.out.println("… " + interpreter.runCommand(input));   // 해당되는 결과 출력
            } catch (UndefinedException e){
                System.out.println(e.getMessage()); //  정의되지않은 변수, 함수 오류 처리
            } catch (Exception e){         // 문법 오류, 또는 잘못된 계산의 오류 처리
                System.out.println("SyntaxError: invalid syntax");
            }
        }
    }
}
