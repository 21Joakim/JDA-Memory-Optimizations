package com.jockie.jda.memory.utility.descriptor;

public class DescriptorType {
	
	private final DataType dataType;
	private final String className;
	
	private final DescriptorType componentType;
	
	public DescriptorType(DataType dataType, String className, DescriptorType componentType) {
		this.dataType = dataType;
		this.className = className;
		this.componentType = componentType;
	}
	
	public DataType getDataType() {
		return this.dataType;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public DescriptorType getComponentType() {
		return this.componentType;
	}
}