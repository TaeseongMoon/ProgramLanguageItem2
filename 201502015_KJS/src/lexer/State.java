package lexer;

import static lexer.TokenType.*;
import static lexer.TransitionOutput.GOTO_ACCEPT_ID;
import static lexer.TransitionOutput.GOTO_ACCEPT_INT;
import static lexer.TransitionOutput.GOTO_EOS;
import static lexer.TransitionOutput.GOTO_FAILED;
import static lexer.TransitionOutput.GOTO_MATCHED;
import static lexer.TransitionOutput.GOTO_SHARP;
import static lexer.TransitionOutput.GOTO_SIGN;
import static lexer.TransitionOutput.GOTO_START;


enum State {
	START {
		@Override
		public TransitionOutput transit(ScanContext context) {
			//Char ch = context.getCharStream().nextChar();
			Char ch = context.getNextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR: //special charactor가 들어온 경우
					if ( v=='#') {  //boolean인 경우 상태반환  #이 온경우
						return GOTO_SHARP;
					}
					else if ( fromSpecialCharactor(v)==PLUS || fromSpecialCharactor(v)==MINUS ) { //부호인경우 상태반환 +,- 인 경우
						context.append(v);
						return GOTO_SIGN;
					}
					else if ( fromSpecialCharactor(v) == TIMES ) { 	// * 인 경우
						context.append(v);
						return GOTO_MATCHED(TIMES, context.getLexime());
					}
					else if ( fromSpecialCharactor(v) == DIV ) { 	//   ) 인 경우
						context.append(v);
						return GOTO_MATCHED(DIV, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == L_PAREN) {	//   ( 인 경우
						context.append(v);
						return GOTO_MATCHED(L_PAREN, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == R_PAREN) {		// ) 인 경우
						context.append(v);
						return GOTO_MATCHED(R_PAREN, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == LT) {		// < 인 경우
						context.append(v);
						return GOTO_MATCHED(LT, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == EQ) {		// = 인 경우
						context.append(v);
						return GOTO_MATCHED(EQ, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == GT) {		// >인 경우
						context.append(v);
						return GOTO_MATCHED(GT, context.getLexime());
					}
					else if( fromSpecialCharactor(v) == APOSTROPHE) {		// '인 경우
						context.append(v);
						return GOTO_MATCHED(APOSTROPHE, context.getLexime());
					}
					else { //그외에는 뭔가 잘못된 경우이므로 FAIL
						return GOTO_FAILED;
					}
				case WS:
					return GOTO_START;
				case END_OF_STREAM:
					return GOTO_EOS;
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_ID {
		@Override
		public TransitionOutput transit(ScanContext context) {
			//Char ch = context.getCharStream().nextChar();
			Char ch = context.getNextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_ID;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(Token.ofName(context.getLexime()));
				default:
					throw new AssertionError();
			}
		}
	},
	ACCEPT_INT {
		@Override
		public TransitionOutput transit(ScanContext context) {
			//Char ch = context.getCharStream().nextChar();
			Char ch = context.getNextChar();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(ch.value());
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
				case END_OF_STREAM:
					return GOTO_MATCHED(INT, context.getLexime());
				default:
					throw new AssertionError();
			}
		}
	},
	SHARP {
		@Override
		public TransitionOutput transit(ScanContext context) {
			//Char ch = context.getCharStream().nextChar();
			Char ch = context.getNextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					switch ( v ) {
						case 'T':
							context.append(v);
							return GOTO_MATCHED(TRUE, context.getLexime());
						case 'F':
							context.append(v);
							return GOTO_MATCHED(FALSE, context.getLexime());
						default:
							return GOTO_FAILED;
					}
				default:
					return GOTO_FAILED;
			}
		}
	},
	SIGN {
		@Override
		public TransitionOutput transit(ScanContext context) {
			//Char ch = context.getCharStream().nextChar();
			Char ch = context.getNextChar();
			char v = ch.value();
			switch ( ch.type() ) {
				case LETTER:
					return GOTO_FAILED;
				case DIGIT:
					context.append(v);
					return GOTO_ACCEPT_INT;
				case SPECIAL_CHAR:
					return GOTO_FAILED;
				case WS:
					String lexme = context.getLexime();
					switch ( lexme ) {
						case "+":
							return GOTO_MATCHED(PLUS, lexme);
						case "-":
							return GOTO_MATCHED(MINUS, lexme);
						default:
							throw new AssertionError();
					}
				case END_OF_STREAM:
					return GOTO_FAILED;
				default:
					throw new AssertionError();
			}
		}
	},
	MATCHED {
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	FAILED{
		@Override
		public TransitionOutput transit(ScanContext context) {
			throw new IllegalStateException("at final state");
		}
	},
	EOS {
		@Override
		public TransitionOutput transit(ScanContext context) {
			return GOTO_EOS;
		}
	};
	
	abstract TransitionOutput transit(ScanContext context);
}
