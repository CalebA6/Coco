package ast;

import coco.Token;

abstract class NamedNode extends CheckableNode {
	public abstract Token getName();
}
