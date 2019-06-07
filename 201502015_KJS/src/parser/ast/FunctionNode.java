package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;


// 여기를 작성하게 해도 좋을 듯 18.4.10
// binaryOpNode클래스를 보고 참고해서 작성
public class FunctionNode implements ValueNode{
	public enum FunctionType {
		ATOM_Q 	{ TokenType tokenType() {return TokenType.ATOM_Q;} },
		CAR 	{ TokenType tokenType() {return TokenType.CAR;} },
		CDR 	{ TokenType tokenType() {return TokenType.CDR;} },
		COND 	{ TokenType tokenType() {return TokenType.COND;} },
		CONS 	{ TokenType tokenType() {return TokenType.CONS;} },
		DEFINE 	{ TokenType tokenType() {return TokenType.DEFINE;} },
		EQ_Q 	{ TokenType tokenType() {return TokenType.EQ_Q;} },
		LAMBDA 	{ TokenType tokenType() {return TokenType.LAMBDA;} },
		NOT 	{ TokenType tokenType() {return TokenType.NOT;} },
		NULL_Q 	{ TokenType tokenType() {return TokenType.NULL_Q;} };

		private static Map<TokenType, FunctionType> fromTokenType = new HashMap<TokenType, FunctionType>();

		static {
			for (FunctionType bType : FunctionType.values()){
				fromTokenType.put(bType.tokenType(), bType);
			}
		}

		static FunctionType getFunctionType(TokenType tType){
			return fromTokenType.get(tType);
		}

		abstract TokenType tokenType();

	}
	public FunctionType funcType;

	@Override
	public String toString(){
		return (""+ funcType).toLowerCase();
	}	// FUNCTION: 태그와 type을 문자열로 만들어 반환

	public void setFuncType(TokenType tType) {
		FunctionType bType = FunctionType.getFunctionType(tType);
		funcType = bType;
	}
}
