package cop5556sp17.AST;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public class IdentLValue extends ASTNode {
	public Dec dec;
	public TypeName typeName;

	public IdentLValue(Token firstToken) {
		super(firstToken);
	}

	@Override
	public String toString() {
		return "IdentLValue [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentLValue(this,arg);
	}

	public String getText() {
		return firstToken.getText();
	}
	public void setDec(Dec d) {
		dec = d;
	}

	public Dec getDec() {
		return dec;
	}

	public TypeName getType() throws SyntaxException{
		return dec.getType();
	}

	public void setType(TypeName type) {
		// TODO Auto-generated method stub
		typeName=type;
	}
}
