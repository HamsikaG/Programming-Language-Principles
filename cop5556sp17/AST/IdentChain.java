package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentChain extends ChainElem {
Dec dec;

	public IdentChain(Token firstToken) {
		super(firstToken);
	}

	public void setDec(Dec d) {
		this.dec = d;
	}

	public Dec getDec() {
		return dec;
	}

	@Override
	public String toString() {
		return "IdentChain [firstToken=" + firstToken + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentChain(this, arg);
	}

}
