package interpreter;

import parser.ast.*;
import parser.parse.CuteParser;
import parser.parse.NodePrinter;
import parser.parse.ParserMain;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CuteInterpreter {
	
	static Map<String,Node> defineHash = new HashMap<String,Node>();
	
	private void errorLog(String err) {
		System.out.println(err);
	 }
	
	 public Node runExpr(Node rootExpr) {
		 
		 Node n = lookupTable(rootExpr.toString());
		 if(n != null) {
			 rootExpr = n;
		 }
		 
		 if (rootExpr == null)
			 return null;
		 if (rootExpr instanceof IdNode)
			 return rootExpr;
		 else if (rootExpr instanceof IntNode)
			 return rootExpr;
		 else if (rootExpr instanceof BooleanNode)
			 return rootExpr;
		 else if (rootExpr instanceof ListNode)
			 return runList((ListNode) rootExpr);
		 else
			 
			 errorLog("run Expr error");
		 return null;
	 }
	 
	 private Node runList(ListNode list) {
		 if(list.equals(ListNode.EMPTYLIST))
			 return list;
		 
		 if(list.car() instanceof FunctionNode){
			 return runFunction((FunctionNode)list.car(), (ListNode)stripList(list.cdr()));
		 }
		 
		 if(list.car() instanceof BinaryOpNode){
			 return runBinary(list);
		 }
		 
		 return list;
	 }
	 
	 
	 private Node runFunction(FunctionNode operator, ListNode operand) {
	
		 switch (operator.funcType){
		 
			case CAR:
				//head가 QuoteNode일 경우 
				if(operand.car() instanceof QuoteNode ) {
					return ((ListNode)runQuote(operand)).car();
				}
				// head가 ListNode일 경우
				else  {
					return ((ListNode)runExpr(operand)).car();
				}
				
			case CDR:
				// CAR과 동일하지만 tail을 반환한다
				// tail이 QuoteNode일 경우
				if(operand.car() instanceof QuoteNode ) {
					return  ((ListNode)runQuote(operand)).cdr();
				}
				//tail이 ListNode일 경우
				else {
					return ((ListNode)runExpr(operand)).cdr();
				} 
			
			case CONS:
				//한개의 원소(head)와 한개의 리스트(tail)을 붙여서 새로운 리스트를 만들어 리턴한다.
				Node Hhead = operand.car(); //head
				Node Thead = operand.cdr().car(); //tail의 head 
				
				//tail이 ListNode라면 해당 thead의 head가 quote일때와 그냥 list일때 나눠 노드 값을 바꿔 저장한다.
				if(Thead instanceof ListNode) {
					if(((ListNode)Thead).car() instanceof QuoteNode) {
						Thead = runQuote((ListNode) Thead);
					}
					else {
						Thead = runExpr(Thead);
					}
				}
				//head가 ListNode일때 해당 head의 head가 quote일때 list일때를 노드 값을 바꿔 저장한다.
				if(Hhead instanceof ListNode) {
					if(((ListNode)Hhead).car() instanceof QuoteNode) {
						Hhead = runQuote((ListNode)Hhead);
					}
					else {
						Hhead = runExpr(Hhead);
					}
				}
				//바뀐 Hhead와 Thead를 ListNode의 cons함수를 호출
				return ListNode.cons(Hhead, (ListNode)Thead);
				
			
			case NULL_Q:
				// List가 null인지 확인한다.
				if(runQuote(operand) instanceof ListNode) {
					// QuoteNode에 ListNode의 내부가 null이면 (비어있으면) #T를 반환한다.
					if (((ListNode) runQuote(operand)).car()== null &&((ListNode) runQuote(operand)).cdr()== null) {
						return BooleanNode.TRUE_NODE;
					}
				}
				// ListNode에 그 외의 노드가 존재하면 #F
				return BooleanNode.FALSE_NODE;
					
			
			
			case ATOM_Q:
				// Quote에 저장된 노드가 ListNode인지 확인한다.
				// Quote에 저장된 노드가 ListNode일때 비어있는 List라면 True
				if(runQuote(operand) instanceof ListNode ) {
					if(((ListNode)runQuote(operand)) == ListNode.EMPTYLIST)
						return BooleanNode.TRUE_NODE;
					else
						return BooleanNode.FALSE_NODE;
					// 그 외에는 #F를 반환한다.
				} 
				else {
					return BooleanNode.TRUE_NODE;
				}		
				
			case NOT:
				// BooleanNode일 경우
				// #T -> #F , #F -> #T
				if(operand.car() instanceof BooleanNode) {
					if(((BooleanNode)operand.car()).equals(BooleanNode.TRUE_NODE)) {
						return BooleanNode.FALSE_NODE;
					} else {
						return BooleanNode.TRUE_NODE;
					} 
				}
				
				// ListNode일 경우 runBinary를 통해 계산결과에 반환된 BooleanNode(n)의 반대값을 반환시킨다.
				// #T -> #F, #F -> #T
				else {
					// runBinary
					if (((BooleanNode)runExpr(operand)).equals((BooleanNode.TRUE_NODE))) {
						return BooleanNode.FALSE_NODE;
					}
					else {
						return BooleanNode.TRUE_NODE;
					}
				}
				

			
			case EQ_Q:	
				// 두 노드의 값이 같은지 확인한다.
				//List안의 값이 같아도 다른 객체를 참조하므로 F를 출력한다. 
				Node hhead = operand.car();  // operand의 head
				Node thead = operand.cdr().car(); // operand 의 tail
				//만약 head가 ListNode이고 해당 노드의 car가 QuoteNode일때  ListNode이면 false를 반환하도록 한다.
				// ( eq? ` ( a b ) ` ( a b ) ) --> #F의 조건을 맞춰주기 위함 
				if(hhead instanceof ListNode) { 
					hhead = runExpr(hhead);
					if(((ListNode) hhead).car() instanceof QuoteNode) { 
						hhead = runQuote((ListNode)hhead); 
						if(hhead instanceof ListNode) 
							return BooleanNode.FALSE_NODE;
					}
				}
				//위와 같은 방식으로 false를 리턴해주도록 한다. 
				if(thead instanceof ListNode) { 
					thead = runExpr(thead); 
					if(((ListNode) thead).car() instanceof QuoteNode) { 
						thead = runQuote((ListNode)thead);
						if(thead instanceof ListNode)
							return BooleanNode.FALSE_NODE; 
					}
				}

							
				// head가 ListNode가 아닐때 : 두 노드의 값을 비교
				if (!(hhead instanceof ListNode) && !(thead instanceof ListNode)) {
					// 값이 다르다면 #F를 반환한다.
					if(!hhead.toString().equals((thead).toString())){
						return BooleanNode.FALSE_NODE;
					}
					// 값이 같다면 #T를 반환한다.
					else  {
						return BooleanNode.TRUE_NODE;
					}
				}
				//이외의 경우에는 #F반환한다. 
		 		return BooleanNode.FALSE_NODE;

			case COND:
				
				// 처음 주어지는 노드는 무조건 ListNode
				// ex ) ( cond ( #T 4 ) ( #F 3 ) )
				if (operand.car() instanceof ListNode) { 
					// 내부에 BinaryOpNode가 있을 경우에는 계산 실행
					// 값이 #T 이면 조건의 값을 반환 , #F이면 null 반환
					if(((ListNode)operand.car()).car() instanceof BinaryOpNode) {
						// runBinary를 통해서 nested된 구조도 계산된 상태로 반환된다.
						if (runBinary((ListNode)operand.car()).equals(BooleanNode.TRUE_NODE)){
							return ((ListNode)operand.cdr()).car();
						} else {
							return null;
						}
					}
					
					// BooleanNode일 경우 계산
					if (((ListNode)operand.car()).car() instanceof BooleanNode) {
						if(((ListNode)operand.car()).car().equals(BooleanNode.TRUE_NODE)) {
							return ((ListNode)operand.car()).cdr().car();
						} else {
							// 첫번째 노드가 #F 이면 다음 노드로 이동해서 실행
							return runFunction(operator, operand.cdr());
						}
					}
					
					// ListNode가 중첩된 구조일 경우 ListNode안으로 이동
					// 중첩된 구조에서 반환된 n의 값이 null이 아니라면 해당 값 반환
					if (((ListNode)operand.car()).car() instanceof ListNode) {
						// Node n 을 통해서 값을 확인 #F 이면 null
						Node n = runFunction(operator, (ListNode)operand.car());
						if(n!=null){
							return n;
						}
						else {
							// 첫번째 노드가 #F 이면 다음 노드로 이동해서 실행
							return runFunction(operator, operand.cdr());
						}
					}
				}
				break;
		
			case DEFINE:
				
				Node assignList = operand.cdr().car();
				
				if(assignList instanceof ListNode) {
					if(((ListNode) assignList).car() instanceof QuoteNode) {
						defineHash.put(operand.car().toString(),assignList);
					}
					else {
						Node n = runBinary((ListNode)assignList);
					
					
					if (n instanceof IntNode || n instanceof BooleanNode) {
						defineHash.put(operand.car().toString(), n);
					}
					}
				}else {
					defineHash.put(operand.car().toString(), operand.cdr().car());
				}return null;
				
			 default:
				 break;	
		 }
		 
		return null;
	 }
	 
	 private Node stripList(ListNode node) {
		 if(node.car() instanceof ListNode && node.cdr() == ListNode.EMPTYLIST) {
			 Node listNode = node.car();
			 return listNode;
		 }else {
			 return node;
		 }
	 }
	 
	private Node runBinary(ListNode list) {
		
		BinaryOpNode operator = (BinaryOpNode) list.car();
		
		int res;
		// 첫번째 노드와 두번째 노드 저장
		Node a = list.cdr().car();
		Node b = list.cdr().cdr().car();
		Node rest = list.cdr().cdr().cdr();
		
		// ListNode일 경우 재귀로 runBinary의 결과노드를 저장
		if(a.toString() != null) {
			IntNode n = (IntNode) lookupTable(a.toString());
			if(n!=null) {
				a = n;
			}
		}
		
		if(b !=null && b.toString() != null) {
			IntNode n = (IntNode) lookupTable(b.toString());
			if(n!=null) {
				b =(IntNode) n;
			}
		}
		//재귀적으로 내부 ListNode들의 값 처리
		if ( a instanceof ListNode) {
			a = runBinary((ListNode) a);
		}
		if( b instanceof ListNode && rest == null) {
			if ( ((ListNode)b).car() instanceof BinaryOpNode)
				b = runBinary((ListNode)b);
		}else if ( b instanceof ListNode && rest != null) {
			b = runBinary(ListNode.cons(operator, list.cdr().cdr()));
		}
		
		switch (operator.binType){
			case PLUS:
				// 합의 결과를 IntNode객체로 반환 
				res = (((IntNode)a).getValue() + ((IntNode)b).getValue());
				return new IntNode(String.valueOf(res));
				
			case MINUS:
				//뺄셈의 결과를 IntNode객체로 반환 
				res = (((IntNode)a).getValue() - ((IntNode)b).getValue());
				return new IntNode(String.valueOf(res));
			
			case TIMES:
				// 곱의 결과를 IntNode객체로 반환 
				res = (((IntNode)a).getValue() * ((IntNode)b).getValue());
				return new IntNode(String.valueOf(res));
				
			case DIV:
				//나눗셈의 결과를 IntNode객체로 반환 
				res = (((IntNode)a).getValue() / ((IntNode)b).getValue());
				return new IntNode(String.valueOf(res));

			// 값의 비교
			case LT:
				if(((IntNode)a).getValue() < ((IntNode)b).getValue()) {
					return BooleanNode.TRUE_NODE;
				} else {
					return BooleanNode.FALSE_NODE;
				}
			case GT:
				if(((IntNode)a).getValue() > ((IntNode)b).getValue()) {
					return BooleanNode.TRUE_NODE;
				} else {
					return BooleanNode.FALSE_NODE;
				}
			case EQ:
				if(((IntNode)a).getValue() == ((IntNode)b).getValue()) {
					return BooleanNode.TRUE_NODE;
				} else {
					return BooleanNode.FALSE_NODE;
				}
				
			 default:
				 break;
		}
		 return null;
	 }
	
	
	 private Node runQuote(ListNode node) {
		 return ((QuoteNode)node.car()).nodeInside();
	 }
	 
	 private Node lookupTable(String id) {
		 Node n = null;
		 for(String key: defineHash.keySet()) {
			 if(key.equals(id)) {
				 n = defineHash.get(key);
				 return n;
			 }
		 }
		 return null;
	 }
//	 private Node insertTable(String id, Node value) {
//	 return 
//	 }
}