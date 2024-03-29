package interpreter;
import parser.ast.*;
import parser.parse.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CuteInterpreter {
    HashMap<String, Node> table;

    public CuteInterpreter(){
        this.table = new HashMap<String, Node>();
    }

    public static void main(String[] args) throws UndefinedException {
        ClassLoader cloader = ParserMain.class.getClassLoader();
        CuteParser cuteParser = new CuteParser("( + 2 3 ) ");   // 테스트용
        CuteInterpreter interpreter = new CuteInterpreter();
        Node parseTree = cuteParser.parseExpr();
        Node resultNode = interpreter.runExpr(parseTree);
        NodePrinter nodePrinter = new NodePrinter(resultNode);
        nodePrinter.prettyPrint();
    }

    public String runCommand(String command) throws UndefinedException {
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

    public Node runExpr(Node rootExpr) throws UndefinedException {
        if(rootExpr == null){
            return null;
        }
        if(rootExpr instanceof IdNode) {
            Node var = lookupTable(rootExpr.toString());
            if(var == null)
                throw new UndefinedException(rootExpr.toString());
            return var;    // IdNode
        }
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

    private Node runList(ListNode list) throws UndefinedException {
        if(list.equals(ListNode.EMPTYLIST))
            return list;
        if(list.car() instanceof IdNode) {  // 첫번째가 id노드라면 table에서 lookup해와서 교체해준다.
            list = ListNode.cons(runExpr(list.car()), list.cdr());
        }
        if(list.car() instanceof FunctionNode){
            return runFunction((FunctionNode) list.car(), (ListNode) stripList(list.cdr()));
        }
        if(list.car() instanceof BinaryOpNode){
            return runBinary(list);
        }
        // List의 첫번째가 FuncionNode나 BinaryOpNode가 아니라 List이면 lamdbaExpression인지 검사해본다.
        if(list.car() instanceof ListNode && ((ListNode) list.car()).car() instanceof FunctionNode){
            FunctionNode operand = (FunctionNode) ((ListNode) list.car()).car();   // operand 추출
            if(operand.funcType == FunctionNode.FunctionType.LAMBDA)    // operand가 lambda이다.
                return runLambda((ListNode) ((ListNode) list.car()).cdr(), list.cdr());
        }
        return list;
    }

    private Node runFunction(FunctionNode operator, ListNode operand) throws UndefinedException {
        ListNode listnode;
        // 함수 동작
        switch (operator.funcType){
            // car, cdr, cons 등에 대한 동작 구현
            // 에러가 없다고 가정하므로 처음 원소는 다 quoteNode이다.
            case CAR:
                Node headItem;
                //head가 IdNode일 경우
                if(operand.car() instanceof IdNode){
                    headItem = runExpr(operand.car());   // 테이블에서 해당 변수의 값을 가져온다.
                    return runFunction(operator, (ListNode)headItem);   // 가져온 해당 변수에 대해서 CAR을 실행한다.
                }
                //head가 QuoteNode일 경우
                else if(operand.car() instanceof QuoteNode ) {
                    listnode = (ListNode) runQuote(operand);    // Quote를 때어내 ListNode를 가져온다.
                    headItem = listnode.car();    // 가져온 ListNode에서 첫번째 원소를 가져와 반환한다
                }
                // head가 FunctionNode일 경우, 즉 operand가 리스트가 아닌 경우
                else{
                    headItem = ((QuoteNode)runExpr(operand)).nodeInside();  // runExpr 결과 QuoteNode가 반환되므로, ListNode를 꺼내옴
                    headItem = ((ListNode)headItem).car();
                }

                if (headItem instanceof ListNode)   // 첫번째 원소가 리스트일 경우 Quote를 붙여 반환
                    return (new QuoteNode(headItem));
                return headItem;   // 그냥 ValueNode일 경우는 그냥 첫번째것을 반환
            case CDR:
                Node tailItem;
                // CAR과 동일하지만 tail을 반환한다
                //head가 IdNode일 경우
                if(operand.car() instanceof IdNode){
                    headItem = runExpr(operand.car());
                    return runFunction(operator, (ListNode)headItem);
                }
                // head가 QuoteNode일 경우
                else if(operand.car() instanceof QuoteNode ) {
                    listnode = (ListNode) runQuote(operand);    // quoteNode에서 quote를 땐 nodelist를 가져온다.
                    tailItem = listnode.cdr();          // 맨 처음 원소를 제외한 나머지 원소들
                }
                // head가 FunctionNode일 경우, 즉 operand가 리스트가 아닌 경우
                else {
                    tailItem = ((QuoteNode)runExpr(operand)).nodeInside();  // runExpr 결과 QuoteNode가 반환되므로, ListNode를 꺼내옴
                    tailItem = ((ListNode)tailItem).cdr();
                }
                if (tailItem instanceof ListNode)
                    return (new QuoteNode(tailItem));
                return tailItem;   // 그냥 ValueNode일 경우는 그냥 반환
            case CONS:
                Node head = operand.car();
                if(head instanceof IdNode) {          // IdNode일 경우 runExpr로 loockupTable해서 변수를 불러온다.
                    head = runExpr(head);
                }
                if(head instanceof ListNode) {
                    if(! (((ListNode) head).car() instanceof QuoteNode)) // ListNode이면서 QuoteNode가 아니라면
                        head = runExpr(head);   // QuoteNode가 아니라면 runExpr 결과를 반환해준다.
                    if(head instanceof QuoteNode) {
                        head = ((QuoteNode) head).nodeInside();   // Quote일 것이므로 벗겨준다.
                    }
                }
                Node tail = operand.cdr().car();
                if(tail instanceof IdNode)          // IdNode일 경우 runExpr로 loockupTable해서 변수를 불러온다.
                    tail = runExpr(tail);
                if(tail instanceof ListNode) {
                    if(! (((ListNode) tail).car() instanceof QuoteNode))     // ListNode이면서 QuoteNode가 아니라면
                        tail = runExpr(tail);    // QuoteNode가 아니라면 runExpr 결과를 반환해준다.
                    if(tail instanceof QuoteNode) {
                        tail = ((QuoteNode) tail).nodeInside();   // Quote일 것이므로 벗겨준다.
                    }
                }
                return  new QuoteNode(ListNode.cons(head, (ListNode) tail));   // cons로 합치고 다시 quote를 씌워준다.
            case NULL_Q:
                if(operand.car() instanceof IdNode)   // IdNode일 경우 runExpr로 loockupTable해서 변수를 불러온다.
                    operand = (ListNode) runExpr(operand.car());
                if(operand.car() instanceof QuoteNode)
                    listnode = (ListNode)runQuote(operand);  // quoteNode에서 quote를 땐 nodelist를 가져온다.
                else
                    listnode = (ListNode) ((QuoteNode)runExpr(operand)).nodeInside();

                if(listnode == ListNode.EMPTYLIST)  // EMPTY LIST 이면 #T를 반환한다.
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;  // 그 외에는 모두 #F
            case ATOM_Q:
                Node atom = null;
                if(runExpr(operand) instanceof QuoteNode)  // 쿼트노드가 나오면 List로 씌어서 넣어준다.
                    operand = ListNode.cons(runExpr(operand), ListNode.EMPTYLIST);

                if(operand.car() instanceof IdNode) {// IdNode일 경우 runExpr로 loockupTable해서 변수를 불러온다.
                    if(runExpr(operand.car()) instanceof ListNode) // 변수를 가져온다.
                        operand = (ListNode)runExpr(operand.car());
                    else
                        return BooleanNode.TRUE_NODE;   // 가져온 변수가 ListNode가 아니면 atom이다.
                }
                if(operand.car() instanceof QuoteNode)  // operand는 ListNode이다.
                    atom = runQuote(operand);          // quote를 벗긴다.
                else
                    return BooleanNode.TRUE_NODE;

                if(atom instanceof ValueNode)           // ListNode가 아니다. ValueNode이다.
                    return BooleanNode.TRUE_NODE;
                else if(atom instanceof ListNode && atom == ListNode.EMPTYLIST)     // null list은 atom 으로 취급된다.
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;  // 나머지는 ListNode이다.
            case EQ_Q:
                // 두 노드의 값이 같은지 확인한다.
                //List안의 값이 같아도 다른 객체를 참조하므로 F를 출력한다.
                Node firstNode = runExpr(operand.car());    // runExpr를 통한 최종 결과값을 반환받는다.
                Node secondNode = runExpr(operand.cdr().car());
                // 먼저 Table에 define되있는지 확인한다.
                if(firstNode instanceof IdNode)
                    firstNode = runExpr(firstNode);
                if(secondNode instanceof IdNode)
                    secondNode = runExpr(secondNode);

                if(firstNode instanceof ListNode)
                    firstNode = runQuote((ListNode)firstNode);
                else
                    firstNode = ((QuoteNode)firstNode).nodeInside();
                if(secondNode instanceof ListNode)
                    secondNode = runQuote((ListNode)secondNode);
                else
                    secondNode = ((QuoteNode)secondNode).nodeInside();

                if(firstNode.equals(secondNode))
                    return BooleanNode.TRUE_NODE;
                else
                    return BooleanNode.FALSE_NODE;
            case NOT:
                BooleanNode bool;
                if(operand.car() instanceof BooleanNode)        // BooleanNode일 경우
                    bool = (BooleanNode)operand.car();
                else if(operand.car() instanceof IdNode)        // IdNode일 경우
                    bool = (BooleanNode) runExpr(operand.car());
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

                    if(headCond.car() instanceof IdNode) // IdNode일 경우, runExpr로 loockupTable에서 가져온다.
                        condbool = (BooleanNode) runExpr(headCond.car());
                    else if (headCond.car() instanceof BooleanNode)      // 첫번째 조건문에서 Boolean 값을 가져온다.
                        condbool = (BooleanNode) headCond.car();
                    else if (headCond.car() instanceof ListNode)    // 조건문이 연산식이라면 연산하여 결정
                        condbool = (BooleanNode) runExpr((ListNode) headCond.car());
                    else{   // headCond 자체로 연산식인 경우, 다음조건문은 없다.
                        condbool = (BooleanNode) runExpr((ListNode) headCond);
                        if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                            if (TailCond.car() instanceof ListNode)
                                return runExpr(TailCond.car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                            else if(TailCond.car()  instanceof IdNode)
                                return runExpr(TailCond.car());  // 출력값이 IdNode이면 runExpr로 lookupTable
                            else
                                return TailCond.car();
                        }
                        return null;
                     }

                    if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                        if(headCond.cdr().car() instanceof ListNode)
                            return runExpr(headCond.cdr().car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                        else if(headCond.cdr().car() instanceof IdNode)
                            return runExpr(headCond.cdr().car());  // 출력값이 IdNode이면 runExpr로 lookupTable
                        else
                            return headCond.cdr().car();
                    }
                    else
                        return runFunction(operator, TailCond); // 다음 조건문으로 이동
                }
                else {  // operand의 첫번째 원소가 ListNode가 아니므로, 다음 조건문이 없다.
                    if(operand.car() instanceof IdNode) // IdNode일 경우, runExpr로 loockupTable에서 가져온다.
                        condbool = (BooleanNode) runExpr(operand.car());
                    else
                        condbool = (BooleanNode) operand.car();
                    if (condbool.toString() == "#T") {  // 조건문 결과 #T이면 출력값 결정
                        if(operand.cdr().car() instanceof ListNode)
                            return runExpr(operand.cdr().car());   // 출력값이 ListNode이면 runExpr를 실행한 결과를 반환
                        else if(operand.cdr().car() instanceof IdNode)
                            return runExpr(operand.cdr().car());  // 출력값이 IdNode이면 runExpr로 lookupTable
                        else
                            return operand.cdr().car();
                    }
                    return null;
                }
            case DEFINE:
                Node variable = operand.car();   // 변수 ID
                if(!(variable instanceof IdNode))       // IdNode여야 한다. 사실 없어도 에러띄우면 되는 부분이긴 합니다.
                    return null;
                Node value = runExpr(operand.cdr().car()); // runExpr를 한 결과를 반환합니다.

                if(value instanceof QuoteNode)  // 쿼트노드가 나오면 List로 씌어서 넣어준다.
                    value = ListNode.cons(value, ListNode.EMPTYLIST);
                insertTable(variable.toString(), value);    // id = value 를 테이블에 넣습니다.
                return  null;
            case LAMBDA:    // LAMBDA가 runLambda가 아니라 runFunction에서 실행되면 인자가 없다는 것이므로, 돌려준다.
                return ListNode.cons(operator, operand);
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

    private Node runBinary(ListNode list) throws UndefinedException {
        BinaryOpNode operator = (BinaryOpNode) list.car();
        ListNode operand = list.cdr();

        // 피연산자
        Node A = operand.car();
        Node B = operand.cdr().car();

        // 피연산자들을 runExpr를 통해 계산가능한 값인 최종 Node로 바꿔준다.
        A = runExpr(A);
        B = runExpr(B);

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

    /*
     Deep Copy HashMap, lambda 실행시 사용됩니다.
     임시로 기존의 table을 prevTable에 복사해두고, lambda로 함수인자 x 등을 table에 binding합니다.
     lambda가 끝난 후 다시 prevTable의 값을 table로 복사해줍니다.
      */
    private void deepCopyTable(HashMap<String, Node> prevTable){
        for (HashMap.Entry<String, Node> copy : this.table.entrySet()) {
            prevTable.put(copy.getKey(), copy.getValue());
        }
    }
    private void deepCopyPrevTable(HashMap<String, Node> prevTable){
        this.table.clear();
        for (HashMap.Entry<String, Node> copy : prevTable.entrySet()) {
            this.table.put(copy.getKey(), copy.getValue());
        }
    }

    // lambda의 formal parameter에 actual parameter를 바인딩해줍니다.
    private Node runLambda(ListNode operand, ListNode actual_para) throws UndefinedException {
        ListNode formal_para = (ListNode) operand.car();    // formal parameter 를 가져옵니다.
        ListNode FuncBody = (ListNode) operand.cdr(); // 함수 몸체(body)를 가져옵니다.
        Node value;

        HashMap<String, Node> prevTable = new HashMap<String, Node>();  // lambda 인자 바인딩을 위한 임시 table 저장소입니다.
        deepCopyTable(prevTable);   // prevTable에 table값의 값을 백업
        while(formal_para.car() != null){
            value = runExpr(actual_para.car());
            if(value instanceof QuoteNode)  // 쿼트노드가 나오면 List로 씌어서 넣어준다.
                value = ListNode.cons(value, ListNode.EMPTYLIST);
            insertTable(formal_para.car().toString(), value);   // formal <- actual 바인딩
            formal_para = formal_para.cdr();
            actual_para = actual_para.cdr();
        }

        Node result = null;
        while (FuncBody.car() != null) {
            result = runExpr(FuncBody.car());    // 함수 몸체를 하나씩 runExpr합니다.
            FuncBody = FuncBody.cdr();
        }
        deepCopyPrevTable(prevTable);       // lambda가 끝났으니, 이전 table로 돌려줍니다.

        return result;  // 함수의 실행결과는 마지막 runExpr로 반환됩니다.
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
