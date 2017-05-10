package cop5556sp17;


import java.util.*;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	class values{
		public
			int valscope;
			Dec declar;
			public values() {
				valscope = 0;
				declar = null;
			}
			
			public values(int s, Dec dec){
				valscope = s;
				declar = dec;
			}
			
	}
	
	//TODO  add fields
	ArrayList<Integer> scope_stack;
	int next_scope, current_scope;
	HashMap<String, ArrayList<values> >hash_table;
	
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		current_scope = next_scope++;
		scope_stack.add(new Integer(current_scope));
	}
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		int stacktop=scope_stack.size()-1;
		scope_stack.remove(stacktop);
		current_scope = scope_stack.get(scope_stack.size()-1);
	}
	
	public boolean insert(String ident, Dec dec){
		
		ArrayList<values> array1 = hash_table.get(ident);
		if (array1 == null){
			array1 = new ArrayList<values>();
			hash_table.put(ident, array1);
		}
		for(int j=0; j<array1.size(); j++){
			if (current_scope == array1.get(j).valscope)
			return false;
		}
		array1.add(new values(current_scope, dec));
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		ArrayList<values> array = hash_table.get(ident);
		if(array==null)
			return null;
		for(int i=scope_stack.size() - 1; i >=0; i--){
			for(int j=0; j<array.size(); j++){
				if (scope_stack.get(i) == array.get(j).valscope){
					return array.get(j).declar;
				}
			}
		}
		
		return null;
	}
		
	public SymbolTable() {
		hash_table = new HashMap<>();
		scope_stack = new ArrayList<Integer>();
		current_scope = 0;
		scope_stack.add(current_scope);
		next_scope = 1;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}