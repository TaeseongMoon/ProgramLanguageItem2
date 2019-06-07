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
			
			
			//ParserMainŬ������ �ִ� ���丮(/bin/parser/parse/)�� command.txt������ ����
			String path = ParserMain.class.getResource("").getPath();
			File file = new File(path+"command.txt");
			file.createNewFile();
			
			
			//������� �Է°��� ���Ͽ� �����Ѵ�. ���� �Է°��� exit()�̶�� �ݺ����� �������ش�.
			Writer objWriter = new BufferedWriter(new FileWriter(file));
			String input = scan.nextLine();
			if(input.equals("exit()")) break;
			objWriter.write(input);
			objWriter.flush();
			objWriter.close();
			
			
			//���� interpreteró�� ���Ϸ� input�� �޾ƿ´�.
			CuteParser cuteParser = new CuteParser(file);
			CuteInterpreter interpreter = new CuteInterpreter();
			Node parseTree = cuteParser.parseExpr();
			Node resultNode = interpreter.runExpr(parseTree);
			
			
			//...�� �Բ� ������� prettyPrint�� ������ش�. 
			System.out.print("...  ");
			NodePrinter nodeprinter = new NodePrinter(resultNode);
			nodeprinter.prettyPrint();
			System.out.println("");
		}
    }
}
