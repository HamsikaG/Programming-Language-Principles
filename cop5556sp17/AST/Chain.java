
package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public TypeName type;
	public Chain(Token firstToken) {
		super(firstToken);
		type = null;
	}
	
	public void setChainType(TypeName t){
		type = t;
	}
	
	public TypeName getChainType(){
		return type;
	}

}
