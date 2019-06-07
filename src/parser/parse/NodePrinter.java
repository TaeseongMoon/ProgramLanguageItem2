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

	//ListNode�� ó��
	private void printList (ListNode listNode) {

		if (listNode == ListNode.EMPTYLIST) {
			return ;
		}
		//listNode�� head�� printNode�� ȣ���ϰ� listNode�� tail�� printList�� ȣ���Ѵ�.
		printNode(listNode.car());             
		printList(listNode.cdr());
	}
	private void printNode(QuoteNode quoteNode) {
		
		if (quoteNode.nodeInside() == null)
			return;
		//apostrophe �� append���ְ� ������ ������ ���ڷ� printNode�� ȣ�����ش�.
		printNode(quoteNode.nodeInside());
	}
	
	private void printNode (Node node) {
		if (node == null)
			return;
		
		if(node instanceof ListNode) {                       //���� ��尡 listNode��� 
			if(((ListNode) node).car() instanceof QuoteNode) // head�� quoteNode��� ��ȣ ���� printListȣ��
				printList((ListNode) node);
			else {                                           // �ƴ϶�� ��ȣ�� �Բ� printList ȣ��
				sb.append("( ");
				printList((ListNode) node);
				sb.append(") ");
			}
		}
		else if(node instanceof QuoteNode) {                 //���� ��尡 quoteNode��� 
			printNode((QuoteNode) node);                     //quoteNode�μ��� printNode �Լ��� ȣ���Ͽ� aphostrophe �� ����ϵ������ش�.
		}
		else {                                               //�̿��� ValueNode �ڽĳ����̶�� �ش� ��带 ����Ѵ�.
			sb.append( node + " " );
		}
	}
	public void prettyPrint(){
		printNode(root);
		System.out.print(sb.toString());
	}
}
