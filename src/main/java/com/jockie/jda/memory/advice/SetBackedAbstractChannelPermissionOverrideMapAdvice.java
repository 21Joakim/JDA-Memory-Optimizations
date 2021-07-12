package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.map.SetBackedTLongObjectHashMap;

import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.ISnowflake;

public class SetBackedAbstractChannelPermissionOverrideMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.This AbstractChannel self, @Advice.FieldValue(value="overrides", readOnly=false) TLongObjectMap<?> overrides) {
		overrides = new TSynchronizedLongObjectMap<>(new SetBackedTLongObjectHashMap<>(ISnowflake::getIdLong), new Object());
	}
}