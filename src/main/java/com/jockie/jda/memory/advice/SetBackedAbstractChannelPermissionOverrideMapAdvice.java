package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SynchronizedSnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.internal.entities.AbstractStandardGuildChannelImpl;

public class SetBackedAbstractChannelPermissionOverrideMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.This AbstractStandardGuildChannelImpl<?> self, @Advice.FieldValue(value="overrides", readOnly=false) TLongObjectMap<?> overrides) {
		overrides = new SynchronizedSnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor(), new Object());
	}
}