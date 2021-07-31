package com.jockie.jda.memory.transformer.noop;

import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class ReturnThisMethodVisitor extends MethodVisitor {
	
	public ReturnThisMethodVisitor(int api, MethodVisitor methodVisitor) {
		super(api, methodVisitor);
	}
	
	@Override
	public void visitCode() {
		super.visitVarInsn(Opcodes.ALOAD, 0);
		super.visitInsn(Opcodes.ARETURN);
	}
}