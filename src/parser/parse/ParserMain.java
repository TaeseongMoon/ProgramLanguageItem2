package parser.parse;

import java.io.*;
import java.io.Writer;
import java.util.Scanner;

import interpreter.CuteInterpreter;
import parser.ast.*;

public class ParserMain {
	public static final void main(String... args) throws Exception {
		while(true) {
			System.out.print(">   ");
			Scanner scan = new Scanner(System.in);
			
			
			//ParserMain클래스가 있는 디렉토리(/bin/parser/parse/)에 command.txt파일을 생성
			String path = ParserMain.class.getResource("").getPath();
			File file = new File(path+"command.txt");
			file.createNewFile();
			
			
			//사용자의 입력값을 파일에 저장한다. 만약 입력값이 exit()이라면 반복문을 종료해준다.
			Writer objWriter = new BufferedWriter(new FileWriter(file));
			String input = scan.nextLine();
			if(input.equals("exit()")) break;
			objWriter.write(input);
			objWriter.flush();
			objWriter.close();
			
			
			//이전 interpreter처럼 파일로 input을 받아온다.
			CuteParser cuteParser = new CuteParser(file);
			CuteInterpreter interpreter = new CuteInterpreter();
			Node parseTree = cuteParser.parseExpr();
			Node resultNode = interpreter.runExpr(parseTree);
			
			
			//...와 함께 결과값을 prettyPrint로 출력해준다. 
			System.out.print("...  ");
			NodePrinter nodeprinter = new NodePrinter(resultNode);
			nodeprinter.prettyPrint();
			System.out.println("");
		}
    }
}
