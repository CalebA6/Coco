package coco;

import java.util.HashMap;
import java.util.Map;

public class Token {

    public enum Kind {
        // boolean operators
        AND("and"),
        OR("or"),
        NOT("not"),

        // arithmetic operators
        POW("^"),

        MUL("*"),
        DIV("/"),
        MOD("%"),

        ADD("+"),
        SUB("-"),

        // relational operators
        EQUAL_TO("=="),
        NOT_EQUAL("!="),
        LESS_THAN("<"),
        LESS_EQUAL("<="),
        GREATER_EQUAL(">="),
        GREATER_THAN(">"),

        // assignment operators
        ASSIGN("="),
        ADD_ASSIGN("+="),
        SUB_ASSIGN("-="),
        MUL_ASSIGN("*="),
        DIV_ASSIGN("/="),
        MOD_ASSIGN("%="),
        POW_ASSIGN("^="),

        // unary increment/decrement
        UNI_INC("++"),
        UNI_DEC("--"),

        // primitive types
        VOID("void"),
        BOOL("bool"),
        INT("int"),
        FLOAT("float"),

        // boolean literals
        TRUE("true"),
        FALSE("false"),

        // region delimiters
        OPEN_PAREN("("),
        CLOSE_PAREN(")"),
        OPEN_BRACE("{"),
        CLOSE_BRACE("}"),
        OPEN_BRACKET("["),
        CLOSE_BRACKET("]"),

        // field/record delimiters
        COMMA(","),
        COLON(":"),
        SEMICOLON(";"),
        PERIOD("."),

        // control flow statements
        IF("if"),
        THEN("then"),
        ELSE("else"),
        FI("fi"),

        WHILE("while"),
        DO("do"),
        OD("od"),

        REPEAT("repeat"),
        UNTIL("until"),

        CALL("call"),
        RETURN("return"),

        // keywords
        MAIN("main"),
        FUNC("function"),

        // special cases
        INT_VAL(),
        FLOAT_VAL(),
        IDENT(),

        EOF(),

        ERROR();

        private String defaultLexeme;

        Kind () {
            defaultLexeme = "";
        }

        Kind (String lexeme) {
            defaultLexeme = lexeme;
        }

        public boolean hasStaticLexeme () {
            return defaultLexeme != null;
        }

        // OPTIONAL: convenience function - boolean matches (String lexeme)
        //           to report whether a Token.Kind has the given lexeme
        //           may be useful
    }

    private int lineNum;
    private int charPos;
    Kind kind;  // package-private
    private String lexeme = "";
    private static Map<String, Kind> map = new HashMap<>();
    static {
        for(Kind kind: Kind.values()) {
        	if(kind.defaultLexeme != "") {
        		map.put(kind.defaultLexeme, kind);
        	}
        }
    }


    // TODO: implement remaining factory functions for handling special cases (EOF below)

    public static Token EOF (int linePos, int charPos) {
        Token tok = new Token(linePos, charPos);
        tok.kind = Kind.EOF;
        return tok;
    }

    private Token (int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;

        // no lexeme provide, signal error
        this.kind = Kind.ERROR;
        this.lexeme = "No Lexeme Given";
    }

    public Token (String lexeme, int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;

        // TODO: based on the given lexeme determine and set the actual kind
        if(map.containsKey(lexeme)) {
        	this.kind = map.get(lexeme);
        	this.lexeme = lexeme;
        	return;
        } else if(isIntVal(lexeme)) {
        	this.kind = Kind.INT_VAL;
        	this.lexeme = lexeme;
        	return;
        } else if(isFloatVal(lexeme)) {
        	this.kind = Kind.FLOAT_VAL;
        	this.lexeme = lexeme;
        	return;
        } else if(isIdent(lexeme)) {
        	this.kind = Kind.IDENT;
        	this.lexeme = lexeme;
        	return;
        }

        // if we don't match anything, signal error
        this.kind = Kind.ERROR;
        this.lexeme = "Unrecognized lexeme: " + lexeme;
    }

    public int lineNumber () {
        return lineNum;
    }

    public int charPosition () {
        return charPos;
    }

    public String lexeme () {
        return this.lexeme;
    }

    public Kind kind () {
        return this.kind;
    }

    // TODO: function to query a token about its kind - boolean is (Token.Kind kind)
    
    private boolean isIntVal(String lexeme) {
    	if(lexeme.length() == 0) return false;
    	
    	int index = 0;
    	if(lexeme.charAt(index) == '-') {
    		++index;
    	}
    	
    	while(index < lexeme.length()) {
    		if(!Character.isDigit(lexeme.charAt(index))) {
    			return false;
    		}
    		++index;
    	}
    	return true;
    }
    
    private boolean isFloatVal(String lexeme) {
    	String[] parts = lexeme.split("\\.");
    	if(parts.length != 2) {
    		return false;
    	}
    	
    	if(!isIntVal(parts[0])) return false;
    	
    	for(int i=0; i<parts[1].length(); ++i) {
    		if(!Character.isDigit(parts[1].charAt(i))) return false;
    	}
    	
    	return true;
    }
    
    private static boolean isIdent(String lexeme) {
    	if((lexeme.length() == 0) || !Character.isLetter(lexeme.charAt(0))) {
    		return false;
    	}
    	
    	for(int i=1; i<lexeme.length(); ++i) {
    		if(!Character.isLetterOrDigit(lexeme.charAt(i)) && (lexeme.charAt(i) != '_')) {
    			return false;
    		}
    	}
    	return true;
    }

    // OPTIONAL: add any additional helper or convenience methods
    //           that you find make for a cleaner design

    @Override
    public String toString () {
        return "Line: " + lineNum + ", Char: " + charPos + ", Lexeme: " + lexeme;
    }
}
