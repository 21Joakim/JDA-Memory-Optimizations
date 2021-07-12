package com.jockie.jda.memory.advice;

import net.bytebuddy.asm.Advice;

public class InternAdvice {
	
	@Advice.OnMethodEnter
	public static void enter(@Advice.Argument(value=0, readOnly=false) String argument) {
		if(argument != null) {
			argument = argument.intern();
		}
	}
}