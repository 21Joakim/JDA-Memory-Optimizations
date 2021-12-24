package com.jockie.jda.memory.transformer.remove;

import java.util.Objects;

import com.jockie.jda.memory.utility.descriptor.DescriptorType;
import com.jockie.jda.memory.utility.descriptor.DescriptorUtility;

import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class RemoveFieldMethodVisitor extends MethodVisitor {
	
	protected String fieldName;
	
	public RemoveFieldMethodVisitor(int api, MethodVisitor delegate, String fieldName) {
		super(api, delegate);
		
		this.fieldName = fieldName;
	}
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
		if(Objects.equals(this.fieldName, name)) {
			if(opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
				DescriptorType type = DescriptorUtility.parseFieldType(descriptor);
				
				super.visitInsn(type.getDataType().getDefaultValueOpcode());
			}
		}else{
			super.visitFieldInsn(opcode, owner, name, descriptor);
		}
	}
}