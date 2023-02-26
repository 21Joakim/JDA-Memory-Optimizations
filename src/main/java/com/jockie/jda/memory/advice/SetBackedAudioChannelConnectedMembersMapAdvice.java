package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SynchronizedSnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;

public class SetBackedAudioChannelConnectedMembersMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.FieldValue(value="connectedMembers", readOnly=false) TLongObjectMap<?> connectedMembers) {
		connectedMembers = new SynchronizedSnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor(), new Object());
	}
}