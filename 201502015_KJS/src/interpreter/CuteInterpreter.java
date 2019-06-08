package interpreter;
import parser.ast.*;
import parser.parse.*;

import java.util.HashMap;
import java.util.List;

public class CuteInterpreter {
    HashMap<String, Node> table;

    public CuteInterpreter(){
        this.table = new HashMap<String, Node>();
    }

    public static void main(String[] args){
        ClassLoader cloader = ParserMain.class.getClassLoader();
        CuteParser cuteParser = new CuteParser("( + 2 3 ) ");   // 테스트용
        CuteInterpreter interpreter = new CuteInterpreter();
        Node parseTree = cuteParser.parseExpr();
        Node resultNode = interpreter.runExpr(parseTree);
        NodePrinter nodePrinter = new NodePrinter(resultNode);
        nodePrinter.prettyPrint();
    }

    public String runCommand(String command){
        CuteParser cuteParser = new CuteParser(command);
        Node parseTree = cuteParser.parseExpr();
        Node resultNode = this.runExpr(parseTree);
        NodePrinter nodePrinter = new NodePrinter(resultNode);
        //nodePrinter.prettyPrint();
        return nodePrinter.prettyResult();
    }

    private void errorLog(String err){
        System.out.println(err);
    }

    public Node runExpr(Node rootExpr){
        if(rootExpr == null){
            return null;
        }
        if(rootExpr instanceof IdNode)
            return lookupTable(rootExpr.toString());    // IdNode
        else if(rootExpr instanceof IntNode)
            return rootExpr;
        else if(rootExpr instanceof BooleanNode)
            return rootExpr;
        else if(rootExpr instanceof ListNode)
            return runList((ListNode)rootExpr);
        else
            errorLog("run Expr error");
        return null;
    }

    private Node runList(ListNode list){
        if(list.equals(ListNode.EMPTYLIST))
            return list;
        if(list.car() instanceof FunctionNode){
            return runFunction((FunctionNode) list.car(), (ListNode) stripList(list.cdr()));
        }
        if(list.car() instanceof BinaryOpNode){
            return runBinary(list);
        }
        return list;
    }

    private Node runFunction(FunctionNode operator, ListNode operand){
        ListNode listnode;
        // 함수 동작
        switch (operator.funcType){
            // car, cdr, cons 등에 대한 동작 구현
            // 에러가 없다고 가정하므로 처음 원소는 다 quoteNode이다.
            case CAR:
                Node headItem;
                //head가 IdNode일 경우
                if(operand.car() instanceof IdNode){
                    headItem = lookupTable(operand.car().toString());   // 테이블에서 해당 변수의 값을 가져온다.
                    return runFunction(operator, (ListNode)headItem);   // 가져온 해당 변수에 대해서 CAR을 실행한다.
                }
                //head가 QuoteNode일 경우
                else if(operand.car() instanceof QuoteNode ) {
                    listnode = (ListNode) runQuote(operand);    // Quote를 때어내 ListNode를 가져온다.
                    headItem = listnode.car();    // 가져온 ListNode에서 첫번째 원소를 가져와 반환한다.
                }
                // head가 ListNode일 경우
                else{
                    headItem = ((ListNode)runExpr(operand)).car();
                }

                if (headItem instanceof ListNode)   // 첫번째 원소가 리스트일 경우 Quote를 붙여 반환
                    return (new QuoteNode(headItem));
                return headItem;   // 그냥 ValueNode일 경우는 그냥 첫번째것을 반환
            case CDR:
                Node tailItem;
                // CAR과 동일하지만 tail을 반환한다
                //head가 IdNode일 경우
                if(operand.car() instanceof IdNode){
                    headItem = lookupTable(operand.car().toString());
                    return runFunction(operator, (ListNode)headItem);
                }
                // head가 QuoteNode일 경우
                else if(operand.car() instanceof QuoteNode ) {
                    listnode = (ListNode) runQuote(operand);    // quoteNode에서 quote를 땐 nodelist를 가져온다.
                    tailItem = listnode.cdr();          // 맨 처음 원소를 제외한 나머지 원소들
                }
                // head가 ListNode일 경우
                else {
                    tailItem = ((ListNode)runExpr(operand)).cdr();
                }
                if (tailItem instanceof ListNode)
                    return (new QuoteNode(tailItem));
                return tailItem;   // 그냥 ValueNode일 경우는 그냥 반환
            case CONS:
                Node head = operand.car();
                if(head instanceof IdNode)          // IdNode일 경우 loockupTable해서 변수를 불러온다.
                    head = lookupTable(head.toString());
                if(head instanceof ListNode)       // ListNode -> QuoteNode
                    head = runQuote((ListNode)head);   // Quote일 것이므로 벗겨준다.
                Node tail = operand.cdr().car();
                if(tail instanceof IdNode)          // IdNode일 경우 loockupTable해서 변수를 불러온다.
                    tail = lookupTable(tail.toString());
                if(tail instanceof ListNode)       // ListNode -> QuoteNode
                    tail = runQuote((ListNode)tail);   // Quote일 것이므로 벗겨준다.
                return  new QuoteNode(ListNode.cons(head, (ListNode) tail));   // cons로 합치고 다시 quote를 씌워준다.
            case NULL_Q:
                if(operand.car() instanceof IdNode)   // IdNode일 경우 loockupTable해서 변수를 불러온다.
                    operand = (ListNode) lookupTable(operand.car().toString());
                listnode = (ListNode)runQuote(operand);  // quoteNode에서 quote를 땐 nodelist를 가져온다.
                if(listnode == ListNode.EMPTYLIST)  // EMPTY LIST 이면 #T를 반환한다.
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;  // 그 외에는 모두 #F
            case ATOM_Q:
                Node atom = runQuote(operand);          // quote를 벗긴다.
                if(atom instanceof ValueNode)           // ListNode가 아니다. ValueNode이다.
                    return BooleanNode.TRUE_NODE;
                else if(atom instanceof ListNode && atom == ListNode.EMPTYLIST)     // null list은 atom 으로 취급된다.
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;  // 나머지는 ListNode이다.
            case EQ_Q:
                // 두 노드의 값이 같은지 확인한다.
                //List안의 값이 같아도 다른 객체를 참조하므로 F를 출력한다.
                Node firstNode = runQuote((ListNode)operand.car());
                Node secondNode = runQuote((ListNode)operand.cdr().car());

                if(firstNode.equals(secondNode))
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;
            case NOT:
                BooleanNode bool;
                if(operand.car() instanceof BooleanNode)        // BooleanNode일 경우
                    bool = (BooleanNode)operand.car();
                else
                    bool = (BooleanNode)runExpr(operand);      // BooleanNode가 아닐경우 runExpr로 BooleanNode 반환
                if(bool.toString() == "#T")
                    return BooleanNode.FALSE_NODE;
                else
                    return BooleanNode.TRUE_NODE;
            case COND:
                if(operand == ListNode.EMPTYLIST)   // 탈출
                    return null;
                BooleanNode condbool;

                if (operand.car() instanceof ListNode) {
                    ListNode headCond = (ListNode) operand.car();   // 현재 조건문
                    ListNode TailCond = operand.cdr();              // 다음 조건문들

                    if (headCond.car() instanceof BooleanNode)      // 첫번째 조건문에서 Boolean 값을 가져온다.
                        condbool = (BooleanNode) headCond.car();
                    else if (headCond.car() instanceof ListNode)    // 조건문이 연산식이라면 연산하여 결정
                        condbool = (BooleanNode) runExpr((ListNode) headCond.car());
                    else{   // headCond 자체로 연산식인 경우, 다음조건문은 없다.
                        condbool = (BooleanNode) runExpr((ListNode) headCond);
                        if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                            if (TailCond.car() instanceof ListNode)
                                return runExpr(TailCond.car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                            return TailCond.car();
                        }
                        return null;
                     }

                    if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                        if(headCond.cdr().car() instanceof ListNode)
                            return runExpr(headCond.cdr().car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                        return headCond.cdr().car();
                    }
                    else
                        return runFunction(operator, TailCond); // 다음 조건문으로 이동
                }
                else if(operand.car() instanceof BooleanNode){  // operand의 첫번째 원소가 ListNode가 아니므로, 다음 조건문이 없다.
                    condbool = (BooleanNode) operand.car();
                    if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                        if(operand.cdr().car() instanceof ListNode)
                            return runExpr(operand.cdr().car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                        return operand.cdr().car();
                    }
                    return null;
                }
            case DEFINE:
                Node variable = operand.car();   // 변수 ID
                if(!(variable instanceof IdNode))       // IdNode여야 한다. 사실 없어도 에러띄우면 되는 부분이긴 합니다.
                    return null;
                Node value = operand.cdr().car();
                // value에 해당되는 부분이 ListNode이면 runExpr를 한 결과를 반환합니다.
                if(value instanceof ListNode )
                    value = runExpr(value);
                insertTable(variable.toString(), value);    // id = value 를 테이블에 넣습니다.
                return  null;
            case LAMBDA:
            default:
                break;
        }
        return null;
    }

    // strip함수, 리스트 노드뒤에 EMPTYLIST가 붙어있으면 이를 때내어 단일 노드로 반환
    private Node stripList(ListNode node){
        if((node.car() instanceof ListNode) && (node.cdr() == ListNode.EMPTYLIST)){
            Node listNode = node.car();
            return listNode;
        } else {
            return node;
        }
    }

    private Node runBinary(ListNode list){
        BinaryOpNode operator = (BinaryOpNode) list.car();
        ListNode operand = list.cdr();

        // 피연산자
        Node A = operand.car();
        Node B = operand.cdr().car();

        // 피연산자가 IdNode일 경우, lookupTable로 정의된 변수가 있는지 확인
        if(A instanceof IdNode)
            A = lookupTable(A.toString());
        if(B instanceof IdNode)
            B = lookupTable(B.toString());

        // 피연산자들이 리스트 노드일 경우 runBinary를 통해 결과 Node로 바꿔준다.
        if(A instanceof ListNode)
            A = runBinary((ListNode)A);
        if(B instanceof ListNode)
            B = runBinary((ListNode)B);

        int var_a = ((IntNode) A).getValue().intValue();
        int var_b = ((IntNode) B).getValue().intValue();

        // 구현과정에 필요한 변수 및 함수 작업 가능
        switch (operator.binType){
            // +, - / 등에 대한 바이너리 연산 동작 구현
            case PLUS:
                return new IntNode("" + (var_a + var_b));
            case MINUS:
                return new IntNode("" + (var_a - var_b));
            case TIMES:
                return new IntNode("" + (var_a * var_b));
            case DIV:
                return new IntNode("" + (var_a / var_b));
            case LT:
                if(var_a < var_b)
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;
            case GT:
                if(var_a > var_b)
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;
            case EQ:
                if(var_a == var_b)
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;
            default:
                break;
        }
        return null;
    }

    // Quote 노드에서 node 값을 때준다.
    private Node runQuote(ListNode node){
        return ((QuoteNode) node.car()).nodeInside();
    }

    // table에 해당 변수 id를 value node로 binding해준다.
    private void insertTable(String id, Node value){
        this.table.put(id, value);
    }

    // table에서 해당되는 id의 node를 반환한다.
    private Node lookupTable(String id){
        return this.table.get(id);
    }
}
