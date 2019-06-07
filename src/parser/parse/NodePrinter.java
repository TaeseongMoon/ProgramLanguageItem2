package parser.parse;

import java.io.*;

import parser.ast.*;

public class NodePrinter {
	//private final String OUTPUT_FILENAME = "output08.txt";
	private StringBuffer sb = new StringBuffer();
	private Node root;
	public NodePrinter(Node root) {
		this.root = root;
		if(this.root instanceof ListNode) {
			sb.append("`");
		}
	}

	//ListNode의 처리
	private void printList (ListNode listNode) {

		if (listNode == ListNode.EMPTYLIST) {
			return ;
		}
		//listNode의 head를 printNode로 호출하고 listNode의 tail은 printList로 호출한다.
		printNode(listNode.car());             
		printList(listNode.cdr());
	}
	private void printNode(QuoteNode quoteNode) {
		
		if (quoteNode.nodeInside() == null)
			return;
		//apostrophe 을 append해주고 나머지 노드들을 인자로 printNode를 호출해준다.
		printNode(quoteNode.nodeInside());
	}
	
	private void printNode (Node node) {
		if (node == null)
			return;
		
		if(node instanceof ListNode) {                       //현재 노드가 listNode라면 
			if(((ListNode) node).car() instanceof QuoteNode) // head가 quoteNode라면 괄호 없이 printList호출
				printList((ListNode) node);
			else {                                           // 아니라면 괄호와 함께 printList 호출
				sb.append("( ");
				printList((ListNode) node);
				sb.append(") ");
			}
		}
		else if(node instanceof QuoteNode) {                 //현재 노드가 quoteNode라면 
			printNode((QuoteNode) node);                     //quoteNode인수의 printNode 함수를 호출하여 aphostrophe 을 출력하도록해준다.
		}
		else {                                               //이외의 ValueNode 자식노드들이라면 해당 노드를 출력한다.
			sb.append( node + " " );
		}
	}
	public void prettyPrint(){
		printNode(root);
		System.out.print(sb.toString());
	}
}
