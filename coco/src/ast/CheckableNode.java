package ast;

import coco.NonexistantVariableException;

abstract class CheckableNode extends Node {
	public abstract void checkFunctionCalls(AST parent);
}
