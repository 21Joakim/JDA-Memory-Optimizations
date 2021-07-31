package com.jockie.jda.memory.transformer.discord.imageid;

import com.jockie.jda.memory.transformer.noop.ReturnThisMethodVisitor;
import com.jockie.jda.memory.utility.descriptor.DataType;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.FieldVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class ImageIdFieldClassVisitor extends ClassVisitor {
	
	protected String className;
	
	protected String fieldName;
	protected String titledFieldName;
	
	public ImageIdFieldClassVisitor(int api, ClassVisitor classVisitor, String className, String fieldName) {
		super(api, classVisitor);
		
		this.className = className;
		
		this.fieldName = fieldName;
		this.titledFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}
	
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    	MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
    	
    	if(name.equals("set" + this.titledFieldName)) {
    		ImageIdFieldClassVisitor self = this;
    		
    		return new ReturnThisMethodVisitor(Opcodes.ASM9, delegate) {
    			@Override
    			public void visitCode() {
    				super.visitVarInsn(Opcodes.ALOAD, 1);
    				
    				String imageId = ImageId.class.getName().replace('.', '/');
    				super.visitMethodInsn(Opcodes.INVOKESTATIC, imageId, "convertToImageId", String.format("(Ljava/lang/String;)L%s;", imageId), false);
    				
    				super.visitVarInsn(Opcodes.ASTORE, 2);
    				
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitVarInsn(Opcodes.ALOAD, 2);
    				super.visitFieldInsn(Opcodes.GETFIELD, imageId, "animated", String.valueOf(DataType.BOOLEAN.getSymbol()));
    				super.visitFieldInsn(Opcodes.PUTFIELD, self.className, String.format("%sAnimated", self.fieldName), String.valueOf(DataType.BOOLEAN.getSymbol()));
    				
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitVarInsn(Opcodes.ALOAD, 2);
    				super.visitFieldInsn(Opcodes.GETFIELD, imageId, "lower", String.valueOf(DataType.LONG.getSymbol()));
    				super.visitFieldInsn(Opcodes.PUTFIELD, self.className, String.format("%sLower", self.fieldName), String.valueOf(DataType.LONG.getSymbol()));
    				
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitVarInsn(Opcodes.ALOAD, 2);
    				super.visitFieldInsn(Opcodes.GETFIELD, imageId, "upper", String.valueOf(DataType.LONG.getSymbol()));
    				super.visitFieldInsn(Opcodes.PUTFIELD, self.className, String.format("%sUpper", self.fieldName), String.valueOf(DataType.LONG.getSymbol()));
    				
    				/* Return this */
    				super.visitCode();
    			}
    		};
    	}
    	
    	if(name.equals("get" + this.titledFieldName)) {
    		ImageIdFieldClassVisitor self = this;
    		
    		return new MethodVisitor(Opcodes.ASM9, delegate) {
    			@Override
    			public void visitCode() {
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitFieldInsn(Opcodes.GETFIELD, self.className, String.format("%sAnimated", self.fieldName), String.valueOf(DataType.BOOLEAN.getSymbol()));
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitFieldInsn(Opcodes.GETFIELD, self.className, String.format("%sLower", self.fieldName), String.valueOf(DataType.LONG.getSymbol()));
    				super.visitVarInsn(Opcodes.ALOAD, 0);
    				super.visitFieldInsn(Opcodes.GETFIELD, self.className, String.format("%sUpper", self.fieldName),  String.valueOf(DataType.LONG.getSymbol()));
    				super.visitMethodInsn(Opcodes.INVOKESTATIC, ImageId.class.getName().replace('.', '/'), "convertToString", "(ZJJ)Ljava/lang/String;", false);
    				super.visitInsn(Opcodes.ARETURN);
    			}
    		};
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