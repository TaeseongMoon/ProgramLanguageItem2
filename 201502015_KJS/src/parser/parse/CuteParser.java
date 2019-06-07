package parser.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import parser.ast.*;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

public class CuteParser {
	private Iterator<Token> tokens;
	private static Node END_OF_LIST = new Node(){};

	/*
	public CuteParser(File file) {
		try {
			tokens = Scanner.scan(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	*/
	public CuteParser(String command) {
		tokens = Scanner.scan(command);
	}

	private Token getNextToken() {
		if (!tokens.hasNext())
			return null;
		return tokens.next();
	}

	public Node parseExpr() {
		Token t = getNextToken();
		if (t == null) {
			System.out.println("No more token");
			return null;
		}
		TokenType tType = t.type();
		String tLexeme = t.lexme();

		switch (tType) {
			case ID:
				return new IdNode(tLexeme);
			case INT:
				if (tLexeme == null)
					System.out.println("???");
				return new IntNode(tLexeme);

			// BinaryOpNode에 대하여 작성
			// +, -, /, *가 해당
			case DIV:
			case EQ:
			case MINUS:
			case GT:
			case PLUS:
			case TIMES:
			case LT:
				BinaryOpNode binaryOpNode = new BinaryOpNode();	// BinaryOpNode를 생성
				binaryOpNode.setValue(tType);					// 입력된 tType을 value로 설정
				return binaryOpNode;							// BinaryOpNode로 리턴

			// FunctionNode에 대하여 작성
			// 키워드가 FunctionNode에 해당
			case ATOM_Q:
			case CAR:
			case CDR:
			case COND:
			case CONS:
			case DEFINE:
			case EQ_Q:
			case LAMBDA:
			case NOT:
			case NULL_Q:
				FunctionNode FuncNode = new FunctionNode();	// FunctionNode를 생성
				FuncNode.setFuncType(tType);					// tType으로 값 설정
				return FuncNode;							// 반환

			// 새로 구현된 BooleanNode Case
			case FALSE:
				return BooleanNode.FALSE_NODE;
			case TRUE:
				return BooleanNode.TRUE_NODE;
			// 새로 구현된 L_PAREN, R_PAREN Case
			case L_PAREN:
				return parseExprList();
			case R_PAREN:
				return END_OF_LIST;
			// 새로 추가된 APOSTROPHE, QUOTE
			case APOSTROPHE:
				QuoteNode quoteNode = new QuoteNode(parseExpr());
				//ListNode listNode = ListNode.cons(quoteNode, ListNode.ENDLIST);
				ListNode listNode = ListNode.cons(quoteNode, ListNode.EMPTYLIST);
				return listNode;
			case QUOTE:
				return new QuoteNode(parseExpr());
			default:
				System.out.println("Parsing Error!");
				return null;
		}

	}

	// List의 value를 생성하는 메소드
	private ListNode parseExprList() {
		Node head = parseExpr();
		if (head == null)
			return null;
		if(head == END_OF_LIST) // if next token is RPAREN
			return ListNode.EMPTYLIST;
		ListNode tail = parseExprList();
		if(tail == null)
			return null;
		return ListNode.cons(head, tail);
	}
}
