package com.jockie.jda.memory.utility.descriptor;

public enum DataType {
	VOID('V'),
	BOOLEAN('Z'),
	BYTE('B'),
	SHORT('S'),
	CHAR('C'),
	INT('I'),
	LONG('J'),
	FLOAT('F'),
	DOUBLE('D'),
	ARRAY('['),
	OBJECT('L');
	
	private final char symbol;
	
	private DataType(char symbol) {
		this.symbol = symbol;
	}
	
	public char getSymbol() {
		return this.symbol;
	}
	
	public static DataType fromSymbol(char symbol) {
		for(DataType dataType : DataType.values()) {
			if(dataType.symbol == symbol) {
				return dataType;
			}
		}
		
		return null;
	}
}