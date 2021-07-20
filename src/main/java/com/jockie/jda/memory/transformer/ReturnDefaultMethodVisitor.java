package com.jockie.jda.memory.transformer;

import com.jockie.jda.memory.utility.descriptor.DataType;
import com.jockie.jda.memory.utility.descriptor.DescriptorType;
import com.jockie.jda.memory.utility.descriptor.DescriptorUtility;

import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class ReturnDefaultMethodVisitor extends MethodVisitor {
	
	public static int getDefaultValueOpcode(DataType dataType) {
		switch(dataType) {
			case VOID: return Opcodes.NOP;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case SHORT:
			case INT:
			case LONG: return Opcodes.ICONST_0;
			case FLOAT: return Opcodes.FCONST_0;
			case DOUBLE: return Opcodes.DCONST_0;
			case OBJECT:
			case ARRAY: return Opcodes.ACONST_NULL;
		}
		
		throw new UnsupportedOperationException(String.format("Unsupported return type: %s", dataType));
	}
	
	public static int getReturnOpcode(DataType dataType) {
		switch(dataType) {
			case VOID: return Opcodes.RETURN;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case SHORT:
			case INT: return Opcodes.IRETURN;
			case LONG: return Opcodes.LRETURN;
			case FLOAT: return Opcodes.FRETURN;
			case DOUBLE: return Opcodes.DRETURN;
			case OBJECT:
			case ARRAY: return Opcodes.ARETURN;
		}
		
		throw new UnsupportedOperationException(String.format("Unsupported return type: %s", dataType));
	}
	
	protected int valueOpcode;
	protected int returnOpcode;
	
	public ReturnDefaultMethodVisitor(int api, MethodVisitor methodVisitor, String descriptor) {
		this(api, methodVisitor, DescriptorUtility.parseMethodReturnType(descriptor));
	}
	
	public ReturnDefaultMethodVisitor(int api, MethodVisitor methodVisitor, DescriptorType returnType) {
		super(api, methodVisitor);
		
		this.valueOpcode = ReturnDefaultMethodVisitor.getDefaultValueOpcode(returnType.getDataType());
		this.returnOpcode = ReturnDefaultMethodVisitor.getReturnOpcode(returnType.getDataType());
	}
	
	@Override
	public void visitCode() {
		super.visitInsn(this.valueOpcode);
		super.visitInsn(this.returnOpcode);
	}
}