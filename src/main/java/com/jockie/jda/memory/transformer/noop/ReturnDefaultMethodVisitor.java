package com.jockie.jda.memory.transformer.noop;

import com.jockie.jda.memory.utility.descriptor.DescriptorType;
import com.jockie.jda.memory.utility.descriptor.DescriptorUtility;

import net.bytebuddy.jar.asm.MethodVisitor;

public class ReturnDefaultMethodVisitor extends MethodVisitor {
	
	protected int valueOpcode;
	protected int returnOpcode;
	
	public ReturnDefaultMethodVisitor(int api, MethodVisitor methodVisitor, String descriptor) {
		this(api, methodVisitor, DescriptorUtility.parseMethodReturnType(descriptor));
	}
	
	public ReturnDefaultMethodVisitor(int api, MethodVisitor methodVisitor, DescriptorType returnType) {
		super(api, methodVisitor);
		
		this.valueOpcode = returnType.getDataType().getDefaultValueOpcode();
		this.returnOpcode = returnType.getDataType().getReturnOpcode();
	}
	
	@Override
	public void visitCode() {
		super.visitInsn(this.valueOpcode);
		super.visitInsn(this.returnOpcode);
	}
}