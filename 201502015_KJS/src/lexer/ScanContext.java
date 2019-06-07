package lexer;

import java.io.File;
import java.io.FileNotFoundException;


class ScanContext {
	// CharStream -> String
	private String input;
	private StringBuilder builder;

	//ScanContext(File file) throws FileNotFoundException {
	ScanContext(String coomand) {
		this.input = coomand;
		this.builder = new StringBuilder();
	}

	// 수정한 부분
	//CharStream getCharStream() { return input; }
	String getString() { return input; }
	void setString(String setInput) {
		this.input = setInput;
	}

	// 새로운 함수, String을 하나씩 읽어반환한다.
	Char getNextChar(){
		Char ch;
		if(this.input.length()==0)	// this.input의 길이가 0이라면 Char.end()를 반환
			ch = Char.end();
		else {			// 길이가 0이 아니라면 스트링의 제일 앞문자를 반환
			ch = Char.of(this.input.charAt(0));
			this.setString(this.input.substring(1));
		}
		return ch;
	}

	String getLexime() {
		String str = builder.toString();
		builder.setLength(0);
		return str;
	}

	void append(char ch) {
		builder.append(ch);
	}
}
