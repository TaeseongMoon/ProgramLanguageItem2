package parser.ast;

import java.util.HashMap;
import java.util.Map;

import lexer.TokenType;

public class FunctionNode implements Node{
	//FunctionNode처리를 위한 enum
	public enum FunctionType {
		ATOM_Q  { TokenType tokenType() {return TokenType.ATOM_Q;}  },
		CAR 	{ TokenType tokenType() {return TokenType.CAR;	 } 	},
		CDR 	{ TokenType tokenType() {return TokenType.CDR;	 } 	},
		COND	{ TokenType tokenType() {return TokenType.COND;	 } 	},
		CONS 	{ TokenType tokenType() {return TokenType.CONS;  } 	},
		DEFINE  { TokenType tokenType() {return TokenType.DEFINE;}  },
		EQ_Q 	{ TokenType tokenType() {return TokenType.EQ_Q;  } 	},
		LAMBDA  { TokenType tokenType() {return TokenType.LAMBDA;}  },
		NOT 	{ TokenType tokenType() {return TokenType.NOT;   } 	},
		NULL_Q  { TokenType tokenType() {return TokenType.NULL_Q;}  };
		//HashMap으로 fromTokenType생성
		//key는 TokenType, value는 FunctionType을 받는다.
		private static Map<TokenType, FunctionType> fromTokenType = new HashMap<TokenType, FunctionType>();
		static {
			for (FunctionType fType : FunctionType.values()){
				fromTokenType.put ( fType.tokenType(), fType);
			}
		}
		static FunctionType getFunctionType(TokenType tType) {
			return fromTokenType.get(tType);
		}
		//abstract 메소드로 tokenType메소드를 선언해주어 enum의 각 인스턴스가 자체구현할 수 있도록 해준다.
		abstract TokenType tokenType();
	}
	public FunctionType funcType;
	
	//FunctionType의 getFunctionType함수를 이용해 Type을 가져와 value로 set해준다.
public void setValue(TokenType tType) {
	FunctionType fType = FunctionType.getFunctionType(tType);
	funcType = fType;
}
	@Override
	public String toString(){
		return funcType.name();
	}

}
