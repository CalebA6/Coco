package coco;

import java.util.Stack;

public class ReversibleScanner {
	
	private Stack<Token> needRereading;
	private Scanner scanner;
	private Token lastToken;
	
	public ReversibleScanner(Scanner scanner) {
		this.scanner = scanner;
		needRereading = new Stack<>();
	}
	
	public boolean hasNext() {
		return scanner.hasNext();
	}
	
	public Token next() {
		if(needRereading.size() > 0) {
			lastToken = needRereading.pop();
		} else {
			lastToken = scanner.next();
		}
		return lastToken;
	}
	
	public Token peek() {
		Token token = next();
		needRereading.add(token);
		return token;
	}
	
	public Token last() {
		return lastToken;
	}
	
	public void push(Token token) {
		needRereading.add(token);
	}
	
}
