package coco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Scanner implements Iterator<Token> {

    private BufferedReader input;   // buffered reader to read file
    private boolean closed; // flag for whether reader is closed or not

    private int lineNum;    // current line number
    private int charPos;    // character offset on current line
    private int prevLineCharPos;

    private String scan;    // current lexeme being scanned in
    private int nextChar;   // contains the next char (-1 == EOF)
    private Queue<Integer> prevChars = new LinkedList<>();

    // reader will be a FileReader over the source file
    public Scanner (Reader reader) {
        input = new BufferedReader(reader);
        lineNum = 1;
        charPos = 0;
        closed = false;
    }

    // signal an error message
    public void Error (String msg, Exception e) {
        System.err.println("Scanner: Line - " + lineNum + ", Char - " + charPos);
        if (e != null) {
            e.printStackTrace();
        }
        System.err.println(msg);
    }

    /*
     * helper function for reading a single char from input
     * can be used to catch and handle any IOExceptions,
     * advance the charPos or lineNum, etc.
     */
    private int readChar () {
        try {
        	if(prevChars.size() > 0) {
        		nextChar = prevChars.poll();
        	} else {
        		nextChar = input.read();
        	}
        	if(nextChar == '\n') {
        		++lineNum;
        		prevLineCharPos = charPos;
        		charPos = 0;
        	} else {
        		++charPos;
        	}
			return nextChar;
		} catch (IOException e) {
			Error("Failed to read input", e);
			return -1;
		}
    }
    
    private void reverse() {
    	if(nextChar == '\n') {
    		charPos = prevLineCharPos;
    		--lineNum;
    	} else {
    		--charPos;
    	}
    	prevChars.add(nextChar);
    }

    /*
     * function to query whether or not more characters can be read
     * depends on closed and nextChar
     */
    @Override
    public boolean hasNext () {
        return !closed;
    }

    /*
     *	returns next Token from input
     *
     *  invariants:
     *  1. call assumes that nextChar is already holding an unread character
     *  2. return leaves nextChar containing an untokenized character
     *  3. closes reader when emitting EOF
     */
    @Override
    public Token next () {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        int in = readChar();
        
        while((in == ' ') || (in == '\t') || (in == '\n') || (in == '\r')) {
        	in = readChar();
        }
        
        if(in == '/') {
        	int next = readChar();
        	if(next == '/') {
        		while(in != '\n' && in != -1) {
        			in = readChar();
        		}
        		return next();
        	} else if(next == '*') {
        		int last = 0;
        		while((in != '/' || last != '*') && in != -1) {
        			last = in;
        			in = readChar();
        		}
        		if(in == -1) {
        			return new Token("", lineNum, charPos);
        		}
        		return next();
        	} else {
        		reverse();
        	}
        }
        
        if(in == -1) {
        	closed = true;
        	return Token.EOF(lineNum, charPos);
        }
        
        int startChar = charPos;
        int startLine = lineNum;
        
        StringBuilder lexeme = new StringBuilder();
        lexeme.append((char)in);
        
        switch(in) {
	        case '^': 
	        case '*': 
	        case '/': 
	        case '%': 
	        case '=': 
	        case '!': 
	        case '<': 
	        case '>': 
	        	lexeme.append(possibleEquals());
	        	break;
	        case '+': 
	        	lexeme.append(incOrEquals());
	        	break;
	        case '-': 
	        	lexeme.append(decOrNumOrEquals());
	        	break;
	        case '(': 
	        case ')': 
	        case '{': 
	        case '}': 
	        case '[': 
	        case ']': 
	        case ',': 
	        case ':': 
	        case ';': 
	        case '.': 
	        	break;
        	default: 
        		if(Character.isLetter(in)) {
        			lexeme.append(identChars());
        		} else if(Character.isDigit(in)) {
        			lexeme.append(decimalNumber());
        		} else {
        			while(true) {
        				in = readChar();
        				if((in == ' ') || (in == '\t') || (in == '\n') || (in == '\r') || (in == -1)) break;
        				lexeme.append(in);
        			}
        		}
        		break;
        }
        
        scan = lexeme.toString();
        Token token = new Token(scan, startLine, startChar);
        return token;
    }
    
    private StringBuilder identChars() {
    	int in = readChar();
    	if(Character.isLetterOrDigit(in) || (in == '_')) {
    		StringBuilder lexeme = identChars();
    		lexeme.insert(0, (char)in);
    		return lexeme;
    	} else {
    		if((in == ' ') || (in == '\t') || (in == '\n') || (in == '\r') || (in == -1) || inUseSymbol(in)) {
        		reverse();
        		return new StringBuilder();
    		} else {
        		StringBuilder lexeme = identChars();
        		lexeme.insert(0, (char)in);
        		return lexeme;
    		}
    	}
    }
    
    private StringBuilder possibleEquals() {
    	int in = readChar();
    	StringBuilder lexeme = new StringBuilder();
    	if(in == '=') {
    		lexeme.append('=');
    	} else {
    		reverse();
    	}
    	return lexeme;
    }
    
    private StringBuilder incOrEquals() {
    	int in = readChar();
    	StringBuilder lexeme = new StringBuilder();
    	if(in == '+') {
    		lexeme.append('+');
    	} else if(in == '=') {
    		lexeme.append('=');
    	} else {
    		reverse();
    	}
    	return lexeme;
    }
    
    private StringBuilder decOrNumOrEquals() {
    	int in = readChar();
    	StringBuilder lexeme;
    	if(in == '-') {
    		lexeme = new StringBuilder();
    		lexeme.append('-');
    	} else if(in == '=') {
    		lexeme = new StringBuilder();
    		lexeme.append('=');
    	} else if(Character.isDigit(in)) {
    		lexeme = decimalNumber();
    		lexeme.insert(0, (char)in);
    	} else {
    		lexeme = new StringBuilder();
    		reverse();
    	}
    	return lexeme;
    }
    
    private StringBuilder decimalNumber() {
    	int in = readChar();
    	StringBuilder lexeme;
    	if(Character.isDigit(in)) {
    		lexeme = decimalNumber();
    		lexeme.insert(0, (char)in);
    	} else if(in == '.') {
    		lexeme = number();
    		lexeme.insert(0, '.');
    	} else {
    		lexeme = new StringBuilder();
    		reverse();
    	}
    	return lexeme;
    }
    
    private StringBuilder number() {
    	int in = readChar();
    	StringBuilder lexeme;
    	if(Character.isDigit(in)) {
    		lexeme = number();
    		lexeme.insert(0, (char)in);
    	} else {
    		lexeme = new StringBuilder();
    		reverse();
    	}
    	return lexeme;
    }
    
    private boolean inUseSymbol(int c) {
    	switch(c) {
        case '^': 
        case '*': 
        case '/': 
        case '%': 
        case '=': 
        case '!': 
        case '<': 
        case '>': 
        case '+': 
        case '-': 
        case '(': 
        case ')': 
        case '{': 
        case '}': 
        case '[': 
        case ']': 
        case ',': 
        case ':': 
        case ';': 
        case '.': 
        	return true;
    	}
    	return false;
    }
    
}
