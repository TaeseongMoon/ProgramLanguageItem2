package lexer;


public enum TokenType {
	INT,
	ID, 
	TRUE, FALSE, NOT,
	PLUS, MINUS, TIMES, DIV,   //special chractor
	LT, GT, EQ, APOSTROPHE,    //special chractor
	L_PAREN, R_PAREN,QUESTION, //special chractor
	DEFINE, LAMBDA, COND, QUOTE,
	CAR, CDR, CONS,
	ATOM_Q, NULL_Q, EQ_Q; 
	
	static TokenType fromSpecialCharactor(char ch) {
		switch ( ch ) {              //special character중 각자 대응되는 타입반환
			case '+':
				return PLUS;
			case '-':
				return MINUS;
			case '*':
				return TIMES;
			case '/':
				return DIV;
			case '<':
				return LT;
			case '>':
				return GT;
			case '=':
				return EQ;
			case '\'': case '`':      //apostrophe가 두가지유형이 있어 둘다 넣어주었다.
				return APOSTROPHE;
			case '(':
				return L_PAREN;
			case ')':
				return R_PAREN;
			default:
				throw new IllegalArgumentException("unregistered char: " + ch);
		}
	}
}
