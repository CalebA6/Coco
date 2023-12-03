package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Parameters /*extends Traversible*/ {
	
	private List<Parameter> parameters = new ArrayList<>();
	
	public Parameters(ReversibleScanner source, Variables variables) throws SyntaxException, RedefinitionException {
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		Token next = source.peek();
		if(next.kind() != Kind.CLOSE_PAREN) {
			parameters.add(new Parameter(source, variables));
			while(true) {
				try {
					ErrorChecker.mustBe(Kind.COMMA, "COMMA", source);
				} catch(SyntaxException e) {
					break;
				}
				parameters.add(new Parameter(source, variables));
			}
		}
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
	}
	
	public List<String> getNames() {
		List<String> names = new ArrayList<>();
		for(Parameter parameter: parameters) {
			names.add(parameter.getName().lexeme());
		}
		return names;
	}
	
	public String getTypes() {
		StringBuilder types = new StringBuilder();
		types.append("(");
		if(parameters.size() > 0) {
			types.append(parameters.get(0).getTypeString());
		}
		for(int param=1; param<parameters.size(); ++param) {
			types.append(",");
			types.append(parameters.get(param).getTypeString());
		}
		types.append(")");
		return types.toString();
	}
	
	@Override
	public String toString() {
		String str = "(";
		if(parameters.size() > 0) {
			str += parameters.get(0).getTypeString();
		}
		for(int param=1; param<parameters.size(); ++param) {
			str += "," + parameters.get(param).getTypeString();
		}
		str += ")";
		return str;
	}
	
}
