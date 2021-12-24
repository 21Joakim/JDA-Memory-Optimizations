package com.jockie.jda.memory.transformer.remove;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Opcodes;

public class RemoveFieldClassFileTransformer implements ClassFileTransformer {
	
	public static Set<String> getDefaultMethodNames(String fieldName) {
		String titledFieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		
		Set<String> methodNames = new HashSet<>();
		methodNames.add(String.format("set%s", titledFieldName));
		methodNames.add(String.format("get%s", titledFieldName));
		methodNames.add(String.format("is%s", titledFieldName));
		
		return methodNames;
	}
	
	protected String className;
	protected String fieldName;
	protected Set<String> methodNames;
	
	public RemoveFieldClassFileTransformer(String className, String fieldName) {
		this(className, fieldName, RemoveFieldClassFileTransformer.getDefaultMethodNames(fieldName));
	}
	
	public RemoveFieldClassFileTransformer(String className, String fieldName, Set<String> methodNames) {
		this.className = className.replace('.', '/');
		this.fieldName = fieldName;
		this.methodNames = methodNames;
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if(className.equals(this.className)) {
			ClassReader classReader = new ClassReader(classfileBuffer);
			ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
			
			classReader.accept(new RemoveFieldClassVisitor(Opcodes.ASM9, classWriter, className, this.fieldName, this.methodNames), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			return classWriter.toByteArray();
		}
		
		return null;
	}
}