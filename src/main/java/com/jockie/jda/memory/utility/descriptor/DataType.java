package com.jockie.jda.memory.utility.descriptor;

import net.bytebuddy.jar.asm.Opcodes;

public enum DataType {
	VOID('V', Opcodes.NOP, Opcodes.RETURN),
	
	BOOLEAN('Z', Opcodes.ICONST_0, Opcodes.IRETURN),
	BYTE('B', Opcodes.ICONST_0, Opcodes.IRETURN),
	SHORT('S', Opcodes.ICONST_0, Opcodes.IRETURN),
	CHAR('C', Opcodes.ICONST_0, Opcodes.IRETURN),
	INT('I', Opcodes.ICONST_0, Opcodes.IRETURN),
	
	LONG('J', Opcodes.LCONST_0, Opcodes.LRETURN),
	
	FLOAT('F', Opcodes.FCONST_0, Opcodes.FRETURN),
	
	DOUBLE('D', Opcodes.DCONST_0, Opcodes.DRETURN),
	
	ARRAY('[', Opcodes.ACONST_NULL, Opcodes.ARETURN),
	OBJECT('L', Opcodes.ACONST_NULL, Opcodes.ARETURN);
	
	private final char symbol;
	
	private final int defaultValueOpcode;
	private final int returnOpcode;
	
	private DataType(char symbol, int defaultValueOpcode, int returnOpcode) {
		this.symbol = symbol;
		
		this.defaultValueOpcode = defaultValueOpcode;
		this.returnOpcode = returnOpcode;
	}
	
	public char getSymbol() {
		return this.symbol;
	}
	
	public int getDefaultValueOpcode() {
		return this.defaultValueOpcode;
	}
	
	public int getReturnOpcode() {
		return this.returnOpcode;
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