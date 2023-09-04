package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;

public class SelfSynchronizedSetBackedChannelPermissionOverrideMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.FieldValue(value="overrides", readOnly=false) TLongObjectMap<?> overrides) {
		overrides = new SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor());
	}
}