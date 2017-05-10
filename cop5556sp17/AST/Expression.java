package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	public TypeName type;
	private Dec dec;
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;
	
	public TypeName getExpType(){
		return type;
	}

	public void setExpType(TypeName t){
		type = t;
	}
	
	public Dec getDec(){
		return dec;
	}
	
	public void setDec(Dec d){
		dec = d;
	}
}
