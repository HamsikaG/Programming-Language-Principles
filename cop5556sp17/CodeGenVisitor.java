package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.w3c.dom.css.ElementCSSInlineStyle;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		slotVarArray = new Stack<Integer>();
		slot = 1;
	}
	Stack<Integer> slotVarArray;
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slot;
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();

		int i = 0;
		for(int j=0;j<params.size();j++)
		{
			ParamDec dec=params.get(j);
			dec.setSlot(i++);
			cw.visitField(0, dec.getIdent().getText(), dec.getType().getJVMTypeDesc(), null, null);
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();

		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getExpType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, 0);

        if (binaryChain.getArrow().isKind(Kind.BARARROW)) {
              mv.visitInsn(DUP);
        }

        else if (binaryChain.getE0().getChainType() == TypeName.FILE) {
                     mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
                                   PLPRuntimeImageIO.readFromFileDesc, false);
        }
        else if (binaryChain.getE0().getChainType() == URL) {
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
                          PLPRuntimeImageIO.readFromURLSig, false);
}


        if (binaryChain.getArrow().isKind(Kind.BARARROW))
              binaryChain.getE1().visit(this, 3);
             else
              binaryChain.getE1().visit(this, 1);


        if (binaryChain.getE1() instanceof IdentChain)
        {
              IdentChain identChain = (IdentChain) binaryChain.getE1();

              if (!(identChain.getDec() instanceof ParamDec))
              {
                  if (identChain.getDec().getType()==TypeName.INTEGER)
                         mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
                  else
                         mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
              }
              else
              {
            	  mv.visitVarInsn(ALOAD, 0);
                  mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
                                identChain.getDec().getType().getJVMTypeDesc());

              }
        }

		return null;

	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		TypeName exp0 = binaryExpression.getE0().getExpType();
		TypeName exp1 = binaryExpression.getE1().getExpType();
		Token op = binaryExpression.getOp();
		if(op.kind==TIMES)
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((exp0 == TypeName.INTEGER) && (exp1 == TypeName.INTEGER)) {
				mv.visitInsn(IMUL);

			} else if ((exp0 == TypeName.INTEGER) && (exp1 == TypeName.IMAGE)) {
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
           }
			else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
		}
		else if(op.kind==PLUS)
		{			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (exp0 == TypeName.INTEGER)
				mv.visitInsn(IADD);
			else
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
		}
		else if(op.kind==MINUS)
		{	binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (exp0 == TypeName.INTEGER) {
				mv.visitInsn(ISUB);

			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
		}


		else if(op.kind==DIV)
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((exp0 == TypeName.INTEGER) && (exp1 == TypeName.INTEGER)) {
				mv.visitInsn(IDIV);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			}
		}
		else if(op.kind==AND)
		{
			binaryExpression.getE0().visit(this, arg);
			Label lab1 = new Label();
			mv.visitJumpInsn(IFEQ, lab1);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFEQ, lab1);
			mv.visitInsn(ICONST_1);
			Label lab2 = new Label();
			mv.visitJumpInsn(GOTO, lab2);
			mv.visitLabel(lab1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(lab2);
		}
		else {

		switch (op.kind) {
		case LT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label LTlabel1 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, LTlabel1);
			mv.visitInsn(ICONST_1);
			Label LTLabel2 = new Label();
			mv.visitJumpInsn(GOTO, LTLabel2);
			mv.visitLabel(LTlabel1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(LTLabel2);
			break;

		case LE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label lel1 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, lel1);
			mv.visitInsn(ICONST_1);
			Label le_l2 = new Label();
			mv.visitJumpInsn(GOTO, le_l2);
			mv.visitLabel(lel1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(le_l2);
			break;

		case GT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label gt_l1 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, gt_l1);
			mv.visitInsn(ICONST_1);
			Label gt_l2 = new Label();
			mv.visitJumpInsn(GOTO, gt_l2);
			mv.visitLabel(gt_l1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(gt_l2);
			break;

		case GE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label gelabel1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, gelabel1);
			mv.visitInsn(ICONST_1);
			Label gelabel2 = new Label();
			mv.visitJumpInsn(GOTO, gelabel2);
			mv.visitLabel(gelabel1);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(gelabel2);
			break;

		case EQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (!(exp0 == TypeName.INTEGER || exp0 == TypeName.BOOLEAN)){
				Label equall1 = new Label();
				mv.visitJumpInsn(IF_ACMPNE, equall1);
				mv.visitInsn(ICONST_1);
				Label equall2 = new Label();
				mv.visitJumpInsn(GOTO, equall2);
				mv.visitLabel(equall1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(equall2);
			} else {
				Label eqlab1 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, eqlab1);
				mv.visitInsn(ICONST_1);
				Label eqlab2 = new Label();
				mv.visitJumpInsn(GOTO, eqlab2);
				mv.visitLabel(eqlab1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(eqlab2);
			}
			break;

		case NOTEQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (!(exp0 == TypeName.INTEGER || exp0 == TypeName.BOOLEAN)) {
				Label notequal_l1 = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, notequal_l1);
				mv.visitInsn(ICONST_1);
				Label notequal_l2 = new Label();
				mv.visitJumpInsn(GOTO, notequal_l2);
				mv.visitLabel(notequal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(notequal_l2);
			}
			else {
				Label notequal_l1 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, notequal_l1);
				mv.visitInsn(ICONST_1);
				Label notequal_l2 = new Label();
				mv.visitJumpInsn(GOTO, notequal_l2);
				mv.visitLabel(notequal_l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(notequal_l2);
			}
			break;
		case MOD:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((exp0 == TypeName.INTEGER) && (exp1 == TypeName.INTEGER)) {
				mv.visitInsn(IREM);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			}
			break;
		case OR:
			binaryExpression.getE0().visit(this, arg);
			Label label1 = new Label();
			mv.visitJumpInsn(IFNE, label1);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFNE, label1);
			mv.visitInsn(ICONST_0);
			Label label2 = new Label();
			mv.visitJumpInsn(GOTO, label2);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(label2);
			break;

		default:
			break;
		}
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label blockstart = new Label();
		mv.visitLineNumber(block.getFirstToken().getLinePos().line, blockstart);
		mv.visitLabel(blockstart);
		for(int i=0;i<block.getDecs().size();i++)
		{
			Dec dec=block.getDecs().get(i);
			dec.visit(this, mv);

		}
		for(int k=0;k< block.getStatements().size();k++)
		{
			Statement statement=block.getStatements().get(k);
			statement.visit(this, mv);
			if (statement instanceof BinaryChain) {
				mv.visitInsn(POP);
			}

		}
		Label bend = new Label();
		mv.visitLineNumber(0, bend);
		mv.visitLabel(bend);
		for (Dec dec : block.getDecs()) {
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getType().getJVMTypeDesc(), null, blockstart,
					bend, dec.getSlot());
			slot--;
			slotVarArray.pop();
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(booleanLitExpression.getValue()==false)
		mv.visitInsn(ICONST_0);
		else if(booleanLitExpression.getValue()==true)
		mv.visitInsn(ICONST_1);

		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		} else if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		slotVarArray.push(slot);
		slot++;
		declaration.setSlot(slotVarArray.peek());
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		switch (filterOpChain.getFirstToken().kind) {
		case OP_BLUR:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			break;
		case OP_GRAY:
			if ((int) arg != 3)
				mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			break;
		case OP_CONVOLVE:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,
					false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		if(frameOpChain.getFirstToken().kind==KW_SHOW)
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,	false);
		else if(frameOpChain.getFirstToken().kind==Kind.KW_HIDE)
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,false);
		else if(frameOpChain.getFirstToken().kind==Kind.KW_MOVE)
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);

		else if(frameOpChain.getFirstToken().kind==Kind.KW_XLOC)
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,false);

		else if(frameOpChain.getFirstToken().kind==Kind.KW_YLOC)
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,false);

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Boolean part = (int) arg == 1;
		if (part) {
			if (identChain.getDec() instanceof ParamDec) {
				if(identChain.getDec().getType()==TypeName.INTEGER){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType().getJVMTypeDesc());
					identChain.getDec().setInit(true);
				}
				else if(identChain.getDec().getType()==TypeName.FILE){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setInit(true);

				}
			} else {
				if(identChain.getDec().getType()==TypeName.INTEGER)
				{
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
					identChain.getDec().setInit(true);
				}
				else if(identChain.getDec().getType()==TypeName.IMAGE){
					mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
					identChain.getDec().setInit(true);
				}
				else if(identChain.getDec().getType()==TypeName.FILE){
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setInit(true);

				}
				else if(identChain.getDec().getType()==TypeName.FRAME){
					{
					System.out.println("visitIdentChain CODEGEN " + identChain.getFirstToken().getText() + " " + identChain.getDec());
					if (identChain.getDec().init) {
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
				} else {
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
						identChain.getDec().setInit(true);
					}
					}
				}
			}
		}
		else
		{
			if (!(identChain.getDec() instanceof ParamDec)) {
				if (identChain.getDec().getType() == TypeName.FRAME) {
					if (identChain.getDec().init)
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					else
						mv.visitInsn(ACONST_NULL);
				} else
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
			}
			else
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
						identChain.getDec().getType().getJVMTypeDesc());

			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {

		if (!(identExpression.getDec() instanceof ParamDec))
		{
			if (identExpression.getExpType() == TypeName.INTEGER || identExpression.getExpType() == TypeName.BOOLEAN)
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			 else
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
		}
		else
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(), identExpression.getDec().getType().getJVMTypeDesc());

		}

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		TypeName tName;
		if (identX.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),identX.getDec().getType().getJVMTypeDesc());
		} else {

			tName=identX.getDec().getType() ;
switch (tName) {
case IMAGE:
	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
			PLPRuntimeImageOps.copyImageSig, false);
	mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
	identX.getDec().setInit(true);
	break;

case INTEGER:
case BOOLEAN:
	mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
	identX.getDec().setInit(true);
	break;

default:
	mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
	identX.getDec().setInit(true);
	break;
}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg);
		Label False = new Label();
		mv.visitJumpInsn(IFEQ, False);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(False);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().kind==Kind.OP_WIDTH)
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
		else if(imageOpChain.getFirstToken().kind==Kind.OP_HEIGHT)
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
		else if(imageOpChain.getFirstToken().kind==Kind.KW_SCALE)
		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);

		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		MethodVisitor mv = (MethodVisitor) arg;

		TypeName name = paramDec.getType();

		if(name==TypeName.INTEGER){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDec.getSlot());
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(name==TypeName.BOOLEAN)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDec.getSlot());
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if(name==TypeName.FILE)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDec.getSlot());
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}
		else if(name==TypeName.URL)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDec.getSlot());
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {

		int i=0;
		while(i<tuple.getExprList().size()){
		Expression exp = tuple.getExprList().get(i);
		i++;
			exp.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label guard = new Label();
		mv.visitJumpInsn(GOTO, guard);
		Label body = new Label();
		mv.visitLabel(body);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(guard);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, body);
		return null;
	}

}
