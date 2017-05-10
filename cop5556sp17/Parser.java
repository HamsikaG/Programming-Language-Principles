package cop5556sp17;
import cop5556sp17.AST.*;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import java.util.ArrayList;



public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode node=program();
		matchEOF();
		return node;
	}

	Expression expression() throws SyntaxException {
		Token first=t;
		Expression exp1,exp2;
		exp1=term();
		while (t.isKind(LT) ||t.isKind(LE) || t.isKind(GT) || t.isKind(GE) ||t.isKind(EQUAL) || t.isKind(NOTEQUAL)) 
		{
			Token op=t;
			switch (t.kind) 
			{
				case LT:
				case LE:
				case GT:
				case GE:
				case EQUAL:
				case NOTEQUAL:
					consume();
					break;
					default:
						throw new SyntaxException("Illegal expression");
						
			}
			exp2=term();
			exp1=new BinaryExpression(first, exp1, op, exp2);
		}
			return exp1;
	}

	Expression term() throws SyntaxException {
		Token first=t;
		Expression exp1,exp2;
		exp1=elem();
		while (t.isKind(PLUS) ||t.isKind(MINUS) || t.isKind(OR) ) 
		{  
			Token op=t;

			switch (t.kind) 
			{
				case PLUS:
				case MINUS:
				case OR:
					consume();
					break;
					default:
						throw new SyntaxException("Illegal term");
						
			}
			exp2=elem();
			exp1=new BinaryExpression(first, exp1, op, exp2);
		}
		return exp1;
	}

	Expression elem() throws SyntaxException {
		Token first=t;
		Expression exp1,exp2;
		exp1=factor();
		while (t.isKind(TIMES) ||t.isKind(DIV) || t.isKind(AND)||t.isKind(MOD) ) 
		{  
			Token op=t;
			switch (t.kind) 
			{
				case TIMES:
				case DIV:
				case AND:
				case MOD:
					consume();
					break;
					default:
						throw new SyntaxException("Illegal elem");
						
			}
			exp2=factor();
			exp1=new BinaryExpression(first, exp1, op, exp2);
		} 
		
return exp1;

	}

	Expression factor() throws SyntaxException {
		Token first=t;
		Expression exp1;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			 first=t;
			exp1=new IdentExpression(first);
			consume();
			break;
		}
			
		case INT_LIT: {
			 first=t;
			exp1=new IntLitExpression(first);
			consume();
			break;
		}
			
		case KW_TRUE:
		case KW_FALSE: {
			 first=t;
			exp1=new BooleanLitExpression(first);
			consume();
			break;
		}
			
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			 first=t;
			exp1=new ConstantExpression(first);
			consume();
			break;
		}
			
		case LPAREN: {
			first=t;
			consume();
			exp1=expression();
			match(RPAREN);
			break;
		}
			
		default:
			throw new SyntaxException("illegal factor");
		}
		return exp1;
	}

	Block block() throws SyntaxException {
		ArrayList<Dec> dec_List=new ArrayList<>();
		ArrayList<Statement> statement_List=new ArrayList<>();
		Token first = t;
		match(LBRACE);
		while(!t.isKind(RBRACE))
		{
		if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE) || t.isKind(KW_FRAME))
			dec_List.add(dec());
		else
			statement_List.add(statement());
	}
		match(RBRACE);
		return new Block(first, dec_List, statement_List);
	}
	
	
	Program program() throws SyntaxException {
		ArrayList<ParamDec> paramDecList = new ArrayList<>();
		Block block;
		Token first = t;
		match(IDENT);
	if(t.isKind(LBRACE)){
		block=block();
	}
	else 
	{
		paramDecList.add(paramDec());
		while(t.isKind(COMMA))
			{
				switch (t.kind) {
				case COMMA:
					match(COMMA);
					paramDecList.add(paramDec());
					break;

				default:
					throw new SyntaxException("illegal program");
					}
			
			}
		block = block();
					
	}
	return new Program(first, paramDecList,block);

}


	ParamDec paramDec() throws SyntaxException {
		Token first = t;
		Token ident;
		if(t.isKind(KW_URL) || t.isKind(KW_BOOLEAN)||t.isKind(KW_FILE) || t.isKind(KW_INTEGER))
		{
		switch (t.kind) 
		{
			case KW_URL:
			case KW_FILE:
			case KW_INTEGER:
			case KW_BOOLEAN:
				consume();
				break;
				default:
					throw new SyntaxException("Illegal start of paramDec");
					
		}
		if(t.kind==IDENT){
			ident=t;
			match(IDENT);
		}
		else
		throw new SyntaxException("Expected IDENT");
		}
		else 
			throw new SyntaxException("Illegal start of paramDec!!");
		return new ParamDec(first, ident);
	}

	Dec dec() throws SyntaxException {
		Token first=t;
		Token ident;
	if(t.isKind(KW_INTEGER)|| t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE)|| t.isKind(KW_FRAME))
	{
		
			consume();
			ident=t;
			match(IDENT);	
	}	
	else
		throw new SyntaxException("Illegal Dec");
	return new Dec(first, ident);
	
	}
	

	Statement statement() throws SyntaxException {
Token first=t;
   Statement stat;
		if(t.isKind(OP_SLEEP))
		{
			consume();
			stat= new SleepStatement(first, expression());
			match(SEMI);
		}
		else if(t.isKind(KW_WHILE)){
			stat=whileStatement();
		}
		else if(t.isKind(KW_IF))
			stat=ifStatement();
		else if(t.isKind(IDENT))
		{
				Token token=scanner.peek();
				if(token.isKind(ASSIGN))
				{
					stat=assign();
					match(SEMI);
				}
				else {
					stat=chain();
					match(SEMI);
				}
	}
		else if(t.isKind(OP_BLUR)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SHOW)||t.isKind(OP_CONVOLVE)||t.isKind(OP_GRAY)||t.isKind(KW_YLOC)||t.isKind(KW_XLOC)||t.isKind(KW_MOVE)||t.isKind(KW_HIDE)||t.isKind(KW_SCALE))
		{
			stat=chain();
		match(SEMI);
		}
else {
	throw new SyntaxException("Illegal statement");

}
		return stat;
	}
	
	
	WhileStatement whileStatement() throws SyntaxException {
		Token first=t;
		Expression e;
		Block b;
		if(t.isKind(KW_WHILE))
			consume();
		else
			throw new SyntaxException("Expected KW_WHILE");
		if(t.isKind(LPAREN))
				consume();
			else
				throw new SyntaxException("Expected LPAREN");
		e= expression();
			if(t.isKind(RPAREN))
				consume();
			else
				throw new SyntaxException("Expected RPAREN");
       b= block();	
        return new WhileStatement(first, e, b);
	}
	
	IfStatement ifStatement() throws SyntaxException {
		Expression e;
		Block block;
		Token first=t;
		if(t.isKind(KW_IF)){
			consume();
		}
		else
			throw new SyntaxException("Expected KW_IF");
		if(t.isKind(LPAREN)){
			consume();
		}
			else
				throw new SyntaxException("Expected LPAREN");
		e=expression();
			if(t.isKind(RPAREN)){
				consume();
			}
			else
				throw new SyntaxException("Expected RPAREN");
        block=block();	
        return new IfStatement(first, e, block);
	}
	Chain chain() throws SyntaxException {
		Token first=t;
		Chain chain1;
		ChainElem chain2;
		chain1=chainElem();
		Token op=t;
		arrowOp();
		chain2=chainElem();
		chain1= new BinaryChain(first, chain1, op, chain2);
		while(t.isKind(ARROW)||t.isKind(BARARROW))
		{
			switch (t.kind) {
			case ARROW:
				op=t;
				consume();
				chain2=chainElem();
				chain1= new BinaryChain(first, chain1, op, chain2);

				break;
			case BARARROW:
				op=t;
				consume();
				chain2=chainElem();
				chain1= new BinaryChain(first, chain1, op, chain2);

				break;
			default:
				throw new SyntaxException("illegal chain");
				}
		}
		return chain1;
	}
	
AssignmentStatement assign() throws SyntaxException {
Token first=t;
IdentLValue ident;
Expression exp;
		if(t.isKind(IDENT)){
			    ident=new IdentLValue(first);
				consume();
		}
		else
			throw new SyntaxException("Expected IDENT");
		if(t.isKind(ASSIGN)){
		    ident=new IdentLValue(first);
			consume();
	}
	else
		throw new SyntaxException("Expected ASSIGN");
		exp=expression();
		return new AssignmentStatement(first, ident, exp);
	}
	
ChainElem chainElem() throws SyntaxException {
		Token first=t;
		ChainElem chainElem;
		if(t.isKind(IDENT))
		{
	    first=t;
		consume();
		chainElem=new IdentChain(first);
		}
	    else if(t.isKind(OP_BLUR)||t.isKind(OP_CONVOLVE)||t.isKind(OP_GRAY))
	    {
	    first=t;
		consume();
		chainElem=new FilterOpChain(first, arg());
	    }
	else if(t.isKind(KW_SHOW)||t.isKind(KW_MOVE)||t.isKind(KW_HIDE)||t.isKind(KW_YLOC)||t.isKind(KW_XLOC))
	{
		first=t;
		consume();
		chainElem=new FrameOpChain(first, arg());
	}
	else if(t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
	{
		first=t;
		consume();
		chainElem=new ImageOpChain(first, arg());
	}
	else 
		throw new SyntaxException("Illegal chainElem");
	return chainElem;	
	}
	
	void arrowOp() throws SyntaxException {
		if(t.isKind(ARROW)||t.isKind(BARARROW))
		{
			consume();
		}
		else
			throw new SyntaxException("Illegal arrowOp");

	}

Tuple arg() throws SyntaxException {

Token first=t;		
ArrayList<Expression> arg_list=new ArrayList<Expression>();
if (t.isKind(LPAREN)){ 
			consume();
			arg_list.add(expression());
			while(t.isKind(COMMA))
			{
							switch (t.kind) {
							case COMMA:
								match(COMMA);
								arg_list.add(expression());
								break;
							default:
								throw new SyntaxException("illegal arg");
								}
				}
				 
					match(RPAREN);
					return new Tuple(first, arg_list);

		}
				else 
					return new Tuple(first, arg_list);
}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
//			System.out.println(kind);
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
