package com.jockie.jda.memory.transformer.discord.imageid;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.jockie.jda.memory.utility.descriptor.DataType;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Opcodes;

public class ImageIdClassFileTransformer implements ClassFileTransformer {
	
	protected String className;
	protected String fieldName;
	
	public ImageIdClassFileTransformer(String className, String fieldName) {
		this.className = className.replace('.', '/');
		this.fieldName = fieldName;
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if(className.equals(this.className)) {
			ClassReader classReader = new ClassReader(classfileBuffer);
			ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
			
			ClassVisitor classVisitor = new ImageIdFieldClassVisitor(Opcodes.ASM9, classWriter, this.className, this.fieldName);
			classReader.accept(classVisitor, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			
			classVisitor.visitField(Opcodes.ACC_PROTECTED, String.format("%sLower", this.fieldName), String.valueOf(DataType.LONG.getSymbol()), null, 0L);
			classVisitor.visitField(Opcodes.ACC_PROTECTED, String.format("%sUpper", this.fieldName), String.valueOf(DataType.LONG.getSymbol()), null, 0L);
			classVisitor.visitField(Opcodes.ACC_PROTECTED, String.format("%sAnimated", this.fieldName), String.valueOf(DataType.BOOLEAN.getSymbol()), null, false);
			return classWriter.toByteArray();
		}
		
		return null;
	}
}