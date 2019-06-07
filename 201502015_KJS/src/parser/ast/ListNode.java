package parser.ast;

// 새로 수정된 ListNode (기존에 ENDLIST가 하던 기능을 EMPTYLIST가 대체)
public interface ListNode extends Node{
	ListNode EMPTYLIST = new ListNode() {
		@Override
		public Node car(){
			return null;
		}
		@Override
		public ListNode cdr(){
			return null;
		}
	};

	static ListNode cons(Node head, ListNode tail){
		return new ListNode() {
			@Override
			public Node car() {
				return head;
			}
			@Override
			public ListNode cdr() {
				return tail;
			}
		};
	}
	Node car();
	ListNode cdr();
}
