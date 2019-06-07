package parser.parse;

import java.io.PrintStream;
import java.io.*;

import parser.ast.*;

public class NodePrinter {
	private final String OUTPUT_FILENAME = "output08.txt";
	private StringBuffer sb = new StringBuffer();
	private Node root;
	public NodePrinter(Node root){
		this.root = root;
	}

	// ListNode, QuoteNode, Node에 대한 printNode 함수를 각각 overload 형식으로 작성
	private void printList(ListNode listNode) {
		if(listNode == listNode.EMPTYLIST){
			//sb.append("( )");
			return ;
		}
//		if (listNode == ListNode.ENDLIST){	// 재귀 탈출
//			return ;
//		}
		// 이후 부분은 주어진 출력 형식에 맞게 코드를 작성하시오.
		printNode(listNode.car());	// 리스트노드의 첫번째 원소는 printNode로 출력하고
		printList(listNode.cdr());	// 나머지 리스트는 재귀적 printList를 이용해서 계속해서 출력한다. 탈출은 ListNode.ENDLIST일때이다.
	}
	private void printNode(QuoteNode quoteNode) {
		if (quoteNode.nodeInside() == null){
			return ;
		}
		// 이후 부분은 주어진 출력 형식에 맞게 코드를 작성하시오.
		sb.append("'");	// 쿼드 ' 추가
		printNode(quoteNode.nodeInside());	// 쿼트노드로 된 리스트들을 다시 출력
	}

	private void printNode(Node node) {
		if(node == null)
			return ;
		// 이후 부분은 주어진 출력 형식에 맞게 코드를 작성하시오.
		if (node instanceof ListNode) {	// 리스트노드라면
			if(((ListNode) node).car() instanceof QuoteNode){	// 만약 리스트노드의 첫번째 원소가 QuoteNode라면,
				printNode((QuoteNode) ((ListNode) node).car()); // 쿼트노드 출력함수 ( )는 양옆에 치지않는다.
			} else {
				sb.append("( ");
				printList((ListNode) node);    // ListNode로 출력 양쪽 괄호 ( ) 추가.
				sb.append(") ");
			}
		}
		else if (node instanceof QuoteNode) {	// QuoteNode라면
			printNode((QuoteNode) node);		// 위의 QuoteNode를 인자로받는 printNode를 호출
		}
		else{	// ListNode도 QuoteNode도 아니라면
			sb.append(node+" ");		// ValueNode이므로 sb에 추가
		}
	}
   
	public void prettyPrint(){
		printNode(root);

		try(FileWriter fw = new FileWriter(OUTPUT_FILENAME);
			PrintWriter pw = new PrintWriter(fw)){
			pw.write(sb.toString());
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public String prettyResult(){
		printNode(root);
		return sb.toString();
	}
}
