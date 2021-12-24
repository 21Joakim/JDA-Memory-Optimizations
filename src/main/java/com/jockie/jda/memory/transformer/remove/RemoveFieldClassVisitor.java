package com.jockie.jda.memory.transformer.remove;

import java.util.Objects;
import java.util.Set;

import com.jockie.jda.memory.transformer.noop.ReturnDefaultMethodVisitor;
import com.jockie.jda.memory.transformer.noop.ReturnThisMethodVisitor;
import com.jockie.jda.memory.utility.descriptor.DescriptorType;
import com.jockie.jda.memory.utility.descriptor.DescriptorUtility;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.FieldVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class RemoveFieldClassVisitor extends ClassVisitor {
	
	protected String className;
	protected String fieldName;
	protected Set<String> methodNames;
	
	public RemoveFieldClassVisitor(int api, ClassVisitor classVisitor, String className, String fieldName, Set<String> methodNames) {
		super(api, classVisitor);
		
		this.className = className;
		this.fieldName = fieldName;
		this.methodNames = methodNames;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
		
		if(this.methodNames != null && this.methodNames.contains(name)) {
			DescriptorType returnType = DescriptorUtility.parseMethodReturnType(descriptor);
			
			/* Assume builder pattern if the return type is the same as the class */
			if(Objects.equals(this.className, returnType.getClassName())) {
				return new ReturnThisMethodVisitor(Opcodes.ASM9, delegate);
			}else{
				return new ReturnDefaultMethodVisitor(Opcodes.ASM9, delegate, returnType);
			}
		}
		
		return new RemoveFieldMethodVisitor(Opcodes.ASM9, delegate, this.fieldName);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if(Objects.equals(this.fieldName, name)) {
			return null;
		}
		
		return super.visitField(access, name, descriptor, signature, value);
	}
}