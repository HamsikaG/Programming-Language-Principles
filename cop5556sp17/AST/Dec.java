package cop5556sp17.AST;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public class Dec extends ASTNode {

	final Token ident;
    int slot;
    public boolean init;
	public Dec(Token firstToken, Token ident) {
		super(firstToken);
		init = false;
		this.ident = ident;
	}


	public TypeName getType() throws SyntaxException {
		return Type.getTypeName(firstToken);
	}
	public void setInit(boolean t) {
		init = t;
	}


	public Token getIdent() {
		return ident;
	}
	public void setSlot(int i) {
		slot = i;
	}

	public int getSlot() {
		return slot;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

}
