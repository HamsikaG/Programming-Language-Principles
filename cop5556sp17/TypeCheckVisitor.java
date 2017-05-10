
package cop5556sp17;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.AST.*;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;



public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Token tt = binaryChain.getFirstToken();
		Chain chain = binaryChain.getE0();
		chain.visit(this, arg);
		ChainElem chainElem = binaryChain.getE1();
		chainElem.visit(this, arg);
		Token t = binaryChain.getArrow();
		if (t.isKind(ARROW)){
			if (chain.type.equals(URL) && chainElem.type.equals(IMAGE))
				binaryChain.setChainType(IMAGE);
			else if (chain.type.equals(FILE) && chainElem.type.equals(IMAGE))
				binaryChain.setChainType(IMAGE);
			else if (chain.type.equals(FRAME) && (chainElem.getFirstToken().isKind(KW_XLOC)||chainElem.getFirstToken().isKind(KW_YLOC)) && (chainElem.getFirstToken().isKind(KW_SHOW)||chainElem.getFirstToken().isKind(KW_HIDE)||chainElem.getFirstToken().isKind(KW_MOVE)||chainElem.getFirstToken().isKind(KW_XLOC)||chainElem.getFirstToken().isKind(KW_YLOC)))
				binaryChain.setChainType(INTEGER);
			else if (chain.type.equals(FRAME) &&(chainElem.getFirstToken().isKind(KW_SHOW)||chainElem.getFirstToken().isKind(KW_HIDE)||chainElem.getFirstToken().isKind(KW_MOVE))  &&(chainElem.getFirstToken().isKind(KW_SHOW)||chainElem.getFirstToken().isKind(KW_HIDE)||chainElem.getFirstToken().isKind(KW_MOVE)||chainElem.getFirstToken().isKind(KW_XLOC)||chainElem.getFirstToken().isKind(KW_YLOC)))
				binaryChain.setChainType(FRAME);
			else if (chain.type.equals(IMAGE) &&(chainElem.getFirstToken().isKind(OP_WIDTH)||chainElem.getFirstToken().isKind(OP_HEIGHT)) && (chainElem.getFirstToken().isKind(OP_WIDTH)||chainElem.getFirstToken().isKind(OP_HEIGHT)||chainElem.getFirstToken().isKind(KW_SCALE)))
						binaryChain.setChainType(INTEGER);
			else if (chain.type.equals(IMAGE) && chainElem.type.equals(FRAME))
						binaryChain.setChainType(FRAME);
			else if (chain.type.equals(IMAGE) &&chainElem.type.equals(FILE))
						binaryChain.setChainType(NONE);
			else if (chain.type.equals(TypeName.IMAGE) &&(chainElem.getFirstToken().isKind(OP_GRAY)||chainElem.getFirstToken().isKind(OP_BLUR)||chainElem.getFirstToken().isKind(OP_CONVOLVE))  &&(chainElem.getFirstToken().isKind(OP_GRAY)||chainElem.getFirstToken().isKind(OP_BLUR)||chainElem.getFirstToken().isKind(OP_CONVOLVE)))
						binaryChain.setChainType(IMAGE);
			else if (chain.type.equals(TypeName.IMAGE) &&chainElem.getFirstToken().isKind(KW_SCALE) && (chainElem.getFirstToken().isKind(OP_WIDTH)||chainElem.getFirstToken().isKind(OP_HEIGHT)||chainElem.getFirstToken().isKind(KW_SCALE)))
						binaryChain.setChainType(IMAGE);
			else if(chain.type.equals(IMAGE)  && chainElem.getFirstToken().isKind(IDENT) && chainElem.type.equals(IMAGE))
				binaryChain.setChainType(IMAGE);

		else if(chain.type.equals(INTEGER) && chainElem.getFirstToken().isKind(IDENT) && chainElem.type.equals(INTEGER))
			binaryChain.setChainType(INTEGER);
			else
				throw new TypeCheckException("Error!");

		}
		else if(t.isKind(BARARROW)){
			if (chain.type.equals(TypeName.IMAGE) &&(chainElem.getFirstToken().isKind(OP_GRAY)||chainElem.getFirstToken().isKind(OP_BLUR)||chainElem.getFirstToken().isKind(OP_CONVOLVE))  &&(chainElem.getFirstToken().isKind(OP_GRAY)||chainElem.getFirstToken().isKind(OP_BLUR)||chainElem.getFirstToken().isKind(OP_CONVOLVE)))
				binaryChain.setChainType(IMAGE);
			else
				throw new TypeCheckException("Error!");
		}
		else
			throw new TypeCheckException("Error");
		return null;
	}
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression e0,e1;
		e0= binaryExpression.getE0();
	    e1= binaryExpression.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		Token t = binaryExpression.getOp();
		if(t.kind.equals(PLUS) ||t.kind.equals(MINUS)){
			if (e0.type.equals(INTEGER) && e1.type.equals(INTEGER)){
			binaryExpression.type = INTEGER;
			} else if (e0.type.equals(IMAGE) && e1.type.equals(IMAGE)){
			binaryExpression.type = IMAGE;
			} else {
			throw new TypeCheckException("Type check Error");
			}
			} else if(t.kind.equals(TIMES)){
			if (e0.type.equals(INTEGER) && e1.type.equals(INTEGER)){
			binaryExpression.type = INTEGER;
			} else if (e0.type.equals(INTEGER) && e1.type.equals(IMAGE)){
			binaryExpression.type = IMAGE;
			} else if (e0.type.equals(IMAGE) && e1.type.equals(INTEGER)){
			binaryExpression.type = IMAGE;
			} else {
			throw new TypeCheckException("Type check Error");
			}
			}

//			else if(t.kind.equals(DIV)){
//			if (e0.type.equals(INTEGER) && e1.type.equals(INTEGER)){
//			binaryExpression.type = INTEGER;
//			} else {
//			throw new TypeCheckException("Type check Error");
//			}
//			}

			else if(t.kind.equals(DIV) || t.kind.equals(MOD))
			{
				if (e0.type.equals(INTEGER) && e1.type.equals(INTEGER))
					binaryExpression.setExpType(INTEGER);
				else if ((e1.type.equals(INTEGER)) && (e0.type.equals(IMAGE)))
				{
					binaryExpression.setExpType(IMAGE);
				}
				else
					throw new TypeCheckException("Error");
			}


			else if(t.kind.equals(LT) || t.kind.equals(GT) || t.kind.equals(LE) || t.kind.equals(GE) ){
			if (e0.type.equals(INTEGER) && e1.type.equals(INTEGER)){
			binaryExpression.type = BOOLEAN;
			} else if (e0.type.equals(BOOLEAN) && e1.type.equals(BOOLEAN)){
			binaryExpression.type = BOOLEAN;
			} else {
			throw new TypeCheckException("Type check Error");
			}
			} else if(t.kind.equals(EQUAL) || t.kind.equals(NOTEQUAL)){
			if (e0.type.equals(e1.type)){
			binaryExpression.type = BOOLEAN;
			} else {
			throw new TypeCheckException("Type check Error");
			}
			} else if (t.kind.equals(AND) || t.kind.equals(OR))
			{
				if (e0.getExpType().equals(TypeName.BOOLEAN) && e1.getExpType().equals(TypeName.BOOLEAN))
				{
					binaryExpression.setExpType(BOOLEAN);
				}
				else
				{
					throw new TypeCheckException("Type check Error");
				}
			}
			else {
			throw new TypeCheckException("Type check Error");
			}
			return null;

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {

		symtab.enterScope();
		int i,j;
		ArrayList<Dec> dec = block.getDecs();
		ArrayList<Statement>stat = block.getStatements();
		for ( i=0, j=0; i<dec.size() && j<stat.size();){

			if (dec.get(i).firstToken.pos > stat.get(j).firstToken.pos){
				stat.get(j).visit(this, arg);
				j++;
			}
			else{
				dec.get(i).visit(this, arg);
				i++;
			}
		}

		for (; i<dec.size(); i++){
			dec.get(i).visit(this, arg);
		}

		for (; j<stat.size(); j++){
			stat.get(j).visit(this, arg);
		}

		symtab.leaveScope();
		return null;
	}
	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setExpType(BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		if (filterOpChain.getArg().getExprList().size() != 0){
			throw new TypeCheckException("Error");
		}
		filterOpChain.setChainType(TypeName.IMAGE);
		return null;
	}
	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Token t = frameOpChain.firstToken;
		Tuple tup = frameOpChain.getArg();
		//frameOpChain.setChainType(Type.getTypeName(t));
		if (t.isKind(KW_SHOW)||t.isKind(KW_HIDE))
		{
			if (tup.getExprList().size() != 0)
			throw new TypeCheckException("Error!");
			frameOpChain.setChainType(NONE);
		}
		else if(t.isKind(KW_XLOC)||t.isKind(KW_YLOC))
		{
			if (tup.getExprList().size()!= 0)
			throw new TypeCheckException("Error!");
			frameOpChain.setChainType(TypeName.INTEGER);
		}
		else if(t.isKind(KW_MOVE))
		{
		    	if (tup.getExprList().size()!= 2)
				throw new TypeCheckException("Error!");
				frameOpChain.setChainType(NONE);
                tup.visit(this, arg);
		}
		else throw new TypeCheckException("Error!!");
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if (dec == null)
			throw new TypeCheckException("Error!");
		identChain.setChainType(dec.getType());
		identChain.setDec(dec);
		System.out.println("visitIdentChain IdentChain " + identChain.getFirstToken().getText() + " " + identChain.getDec());
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec d = symtab.lookup(identExpression.getFirstToken().getText());
		if (d == null)
			throw new TypeCheckException("Error");
		identExpression.setExpType(d.getType() );
		identExpression.setDec(d);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Expression expr = ifStatement.getE();
		expr.visit(this, arg);
		if (!expr.getExpType().equals(TypeName.BOOLEAN) )
			throw new TypeCheckException("Error");
		Block block = ifStatement.getB();
		block.visit(this, arg);
		return null;
	}
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setExpType(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression expr = sleepStatement.getE();
		expr.visit(this, arg);
		if (!(expr.getExpType().equals(TypeName.INTEGER)))
			throw new TypeCheckException("Error");
		return null;
	}


	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// Implemented this
		Expression expr = whileStatement.getE();
		expr.visit(this, arg);
		if (!(expr.getExpType().equals(TypeName.BOOLEAN)))
			throw new TypeCheckException("Error");
		Block block = whileStatement.getB();
		block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {

		if(symtab.insert(declaration.getIdent().getText(), declaration)){
			//declaration.get
			return null;
		}
		else
			throw new TypeCheckException("Error!");
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> params = program.getParams();
		for (int i=0; i<params.size(); i++)
		params.get(i).visit(this, arg);
		Block block = program.getB();
		block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		IdentLValue id = assignStatement.getVar();
	id.visit(this, arg);
		Expression e = assignStatement.getE();
		e.visit(this, arg);
		System.out.println("ID type is: "+id.getType()+"\nExp type is: "+e.getExpType()+"\n");
		if (! e.getExpType().equals(id.getType())){
			throw new TypeCheckException("Error");
		}
		return null;
	}


	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {

		Dec d = symtab.lookup(identX.getText());
		if (d == null)
			throw new TypeCheckException("Error");
		else
			identX.setDec(d);
		identX.setType(d.getType());
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {

		if(symtab.insert(paramDec.getIdent().getText(), paramDec))
		return null;
		else
			throw new TypeCheckException("Error!");
	}


	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setExpType(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token t = imageOpChain.firstToken;
		Tuple tup = imageOpChain.getArg();
		//imageOpChain.setChainType(Type.getTypeName(t));
		if (t.isKind(OP_WIDTH) ||t.isKind(OP_HEIGHT)){
			if (tup.getExprList().size()!= 0)
			throw new TypeCheckException("Error!:TypeCheckException");
			imageOpChain.setChainType(TypeName.INTEGER);
		}
		else if (t.isKind(KW_SCALE)){
			if (tup.getExprList().size()!= 1)
			throw new TypeCheckException("Error!:TypeCheckException");
			imageOpChain.setChainType(TypeName.IMAGE);
			tup.visit(this,arg);
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> exprs = tuple.getExprList();
		Expression e;
		for (int i=0;i<exprs.size();i++){
			e=exprs.get(i);
			e.visit(this, arg);
			if(!e.getExpType().equals(TypeName.INTEGER))
				throw new TypeCheckException("Error!");
		}
		return null;
	}

}
