package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;



public class Scanner {

	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
	

	public static enum State{
		START,
		IN_IDENT,
		IN_INTLIT,
		IN_COMMENT;
	}

	ArrayList<Integer> indexInLine;

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		indexInLine = new ArrayList<Integer>();
	}


	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;

		//returns the text of this Token
		public String getText() {
			
			return chars.substring(pos, pos+length);
		}
		
		public boolean isKind(Kind k)
		{
			if(kind.equals(k))
			return true;
			else 
				return false;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			
			int index;
			index = Collections.binarySearch(indexInLine, pos);
			if(index < 0)
			{
				index=(-1)*(index+1)-1;
			}
			// pos = Collections.binarySearread();
			return new LinePos(index, pos-indexInLine.get(index));
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		
		public int intVal() throws NumberFormatException{
		
			return Integer.parseInt(chars.substring(pos, pos+length));
		}
		
	}

	
	
	public int ignoreSpace(int pos)
	{
		while(pos< chars.length() && chars.charAt(pos) != '\n' && Character.isWhitespace(chars.charAt(pos))){
			pos++;
		}
		return pos;
	}
	
	public Boolean intOverflowHandler(int sPos, int pos)
	{
		
		try {
			Integer.parseInt(chars.substring(sPos, pos));
			return false;
		} catch (Exception NumberFormatException) {
			// TODO: handle exception
			return true;
		}
		
		
	}
	
	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0;
		int length = chars.length();
		State state = State.START;
		char read;
		int sPos = 0; 
		indexInLine.add(0);
		
		while (pos < length) {
			read = chars.charAt(pos);
			
			switch (state) {
				case START: {
						pos = ignoreSpace(pos);
						if (pos >= length)
							break;
						else
							read = chars.charAt(pos);
						sPos = pos;
						
						switch (read) {
						
						case ';':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.SEMI, sPos, 1));
						} break;
						
						case '\n': {
							pos++;
							indexInLine.add(pos);
							
						} break;
						
						case '&': {
								tokens.add(new Token(Kind.AND, sPos, 1));
								pos++;
								state = State.START;
							} break;

						case '/': {
								pos++;
								if (pos >= length || chars.charAt(pos) != '*'){
									tokens.add(new Token(Kind.DIV, sPos, 1));
									state = State.START;
								}
								else{
									state = State.IN_COMMENT;
									pos++;
									}
							} break;

						case '+': {
							tokens.add(new Token(Kind.PLUS, sPos, 1));
							pos++;
							state = State.START;
						} break;
						
						case '*': {
							tokens.add(new Token(Kind.TIMES, sPos, 1));
							pos++;
							state = State.START;
						} break;
						
						case '!': {
							pos++;
							if (pos >= length || chars.charAt(pos) != '='){
								tokens.add(new Token(Kind.NOT, sPos, 1));
							}
							else{
								tokens.add(new Token(Kind.NOTEQUAL, sPos, 2));
								pos++;
							}
							state = State.START;
						} break;
						
						case '%': {
							tokens.add(new Token(Kind.MOD, sPos, 1));
							pos++;
							state = State.START;
						} break;		
						
						case '|': {
							pos++;
							if (pos >= length - 1 || chars.charAt(pos) != '-' || chars.charAt(pos+1) != '>'){
								tokens.add(new Token(Kind.OR, sPos, 1));
							}
							else{
								tokens.add(new Token(Kind.BARARROW, sPos, 3));
								pos+=2;
							}
							state = State.START;
						} break;
						
						case '-': {
							pos++;
							if (pos >= length || chars.charAt(pos) != '>'){
								tokens.add(new Token(Kind.MINUS, sPos, 1));
							}
							else{
								tokens.add(new Token(Kind.ARROW, sPos, 2));
								pos++;
							}
							state = State.START;
						} break;
						
						case '<': {
							pos++;
							if (pos >= length || (chars.charAt(pos) != '-' && chars.charAt(pos) != '=')){
								tokens.add(new Token(Kind.LT, sPos, 1));
							}
							else{
								if (chars.charAt(pos) == '=')
									tokens.add(new Token(Kind.LE, sPos, 2));
								else
									tokens.add(new Token(Kind.ASSIGN, sPos, 2));
								pos++;
							}
							state = State.START;
						} break;
						
						case '>': {
							pos++;
							if (pos >= length || chars.charAt(pos) != '='){
								tokens.add(new Token(Kind.GT, sPos, 1));
							}
							else{
								tokens.add(new Token(Kind.GE, sPos, 2));
								pos++;
							}
							state = State.START;
						} break;
						
						case '=': {
							pos++;
							if (pos >= length){
								pos--;
								throw new IllegalCharException( "illegal char " +read+" at pos "+pos);
							}
							else if (chars.charAt(pos) != '='){
								throw new IllegalCharException( "illegal char " +read+" at pos "+pos);
							}
							else{
								tokens.add(new Token(Kind.EQUAL, sPos, 2));
								pos++;
							}
							state = State.START;
						} break;

						case ',':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.COMMA, sPos, 1));
						} break;
						
						case '{':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.LBRACE, sPos, 1));
						} break;
						
						case '}':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.RBRACE, sPos, 1));
						} break;
						
						case '(':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.LPAREN, sPos, 1));
						} break;
						
						case ')':{
							pos++;
							state = State.START;
							tokens.add(new Token(Kind.RPAREN, sPos, 1));
						} break;
						
						case '0': {
							tokens.add(new Token(Kind.INT_LIT,sPos, 1));
							pos++;
							state = State.START;
						} break;
						
						default: { 
							if (Character.isDigit(read)){
								state = State.IN_INTLIT;pos++;
							}else if (Character.isJavaIdentifierStart(read)) {
								state = State.IN_IDENT;pos++;
							}else {
								throw new IllegalCharException( "illegal char " +read+" at pos "+pos);
								}
							}
						}
					}break;
					
					case IN_IDENT:{
						if (Character.isJavaIdentifierPart(read)) {
								pos++;
							} else {
								int isReserved = 0;
								String str = chars.substring(sPos, pos);
								for (Kind tk : Kind.values()){
									if (tk.getText().equals(str)){
										tokens.add(new Token(tk, sPos, pos - sPos));
										isReserved = 1;
										state = State.START;
//										break;
									}
								}
								
								if (isReserved == 0)
									tokens.add(new Token(Kind.IDENT, sPos, pos - sPos));
								state = State.START;
							}
					}break;
					
					case IN_INTLIT:{
						if (Character.isDigit(read)){
							state = State.IN_INTLIT;pos++;
						}else if (intOverflowHandler(sPos, pos)) {
							throw new IllegalNumberException("Number out of range for int type "+chars.substring(sPos, pos));
						} 
						else {
							tokens.add(new Token(Kind.INT_LIT, sPos, pos - sPos));
							state = State.START;
						}
					}break;

					case IN_COMMENT:{
						pos++;
						if (pos < length && read == '*' && chars.charAt(pos) == '/'){
							state = State.START;
							pos++;
						}
						else if (read == '\n'){
							indexInLine.add(pos);
						}
					}break;
					default:  assert false;
				}
			} 
			
			switch (state) {
			case IN_IDENT:
				int isReserved = 0;
				String str = chars.substring(sPos, pos);
				for (Kind tk : Kind.values()){
			
					if (tk.getText().equals(str)){
						tokens.add(new Token(tk, sPos, pos - sPos));
						isReserved = 1;
						state = State.START;
					}
				}
				if (isReserved == 0)
					tokens.add(new Token(Kind.IDENT, sPos, pos - sPos));
				state = State.START;
				break;
			
				case IN_INTLIT:{
					if (intOverflowHandler(sPos, pos)){
						throw new IllegalNumberException("Number out of range for int type "+chars.substring(sPos, pos));
					}
					else{
						tokens.add(new Token(Kind.INT_LIT, sPos, pos - sPos));
						
					}
				}break;

				default:
					break;
			}
		tokens.add(new Token(Kind.EOF, pos, 0));
		return this;  
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}


}