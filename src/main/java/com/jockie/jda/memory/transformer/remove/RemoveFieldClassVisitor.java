package com.jockie.jda.memory.transformer.remove;

import com.jockie.jda.memory.transformer.noop.ReturnDefaultMethodVisitor;
import com.jockie.jda.memory.transformer.noop.ReturnThisMethodVisitor;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.FieldVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class RemoveFieldClassVisitor extends ClassVisitor {
	
	protected String fieldName;
	protected String titledFieldName;
	
	public RemoveFieldClassVisitor(int api, ClassVisitor classVisitor, String fieldName) {
		super(api, classVisitor);
		
		this.fieldName = fieldName;
		this.titledFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}
	
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    	MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
    	
    	if(name.equals("set" + this.titledFieldName)) {
    		/* 
    		 * TODO: JDA setter methods return itself (at least for methods in GuildImpl etc), 
    		 * we should probably add some sort of support for methods that don't
    		 */ 
    		return new ReturnThisMethodVisitor(Opcodes.ASM9, delegate);
    	}
    	
    	if(name.equals("get" + this.titledFieldName) || name.equals("is" + this.titledFieldName)) {
    		return new ReturnDefaultMethodVisitor(Opcodes.ASM9, delegate, descriptor);
    	}
    	
    	return delegate;
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    	if(name.equals(this.fieldName)) {
    		return null;
    	}
    	
    	return super.visitField(access, name, descriptor, signature, value);
    }
}