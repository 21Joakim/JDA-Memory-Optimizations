package com.jockie.jda.memory.utility.descriptor;

public class DescriptorUtility {
	
	private DescriptorUtility() {}
	
	private static DescriptorType parseType(String descriptor, int index) {
		char symbol = descriptor.charAt(index);
		
		DataType dataType = DataType.fromSymbol(symbol);
		if(dataType == null) {
			throw new IllegalArgumentException(String.format("Unknown data type for symbol: %s (descriptor: %s)", symbol, descriptor));
		}
		
		if(dataType.equals(DataType.ARRAY)) {
			return new DescriptorType(dataType, null, DescriptorUtility.parseType(descriptor, index));
		}
		
		if(dataType.equals(DataType.OBJECT)) {
			return new DescriptorType(dataType, descriptor.substring(++index, descriptor.indexOf(';', index)), null);
		}
		
		return new DescriptorType(dataType, null, null);
	}
	
	public static DescriptorType parseMethodReturnType(String descriptor) {
		return DescriptorUtility.parseType(descriptor, descriptor.indexOf(')') + 1);
	}
	
	public static DescriptorType parseFieldType(String descriptor) {
		return DescriptorUtility.parseType(descriptor, 0);
	}
}