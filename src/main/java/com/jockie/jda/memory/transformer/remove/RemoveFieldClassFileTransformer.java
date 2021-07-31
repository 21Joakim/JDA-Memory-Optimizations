package com.jockie.jda.memory.transformer.remove;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Opcodes;

public class RemoveFieldClassFileTransformer implements ClassFileTransformer {
	
	protected String className;
	protected String fieldName;
	
	public RemoveFieldClassFileTransformer(String className, String fieldName) {
		this.className = className.replace('.', '/');
		this.fieldName = fieldName;
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if(className.equals(this.className)) {
			ClassReader classReader = new ClassReader(classfileBuffer);
			ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
			
			classReader.accept(new RemoveFieldClassVisitor(Opcodes.ASM9, classWriter, this.fieldName), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			return classWriter.toByteArray();
		}
		
		return null;
	}
}