package ast;

import coco.Token;

abstract class NamedNode extends Node {
	public abstract Token getName();
}
