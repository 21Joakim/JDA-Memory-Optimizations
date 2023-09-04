package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;

public class SelfSynchronizedSetBackedAudioChannelConnectedMembersMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.FieldValue(value="connectedMembers", readOnly=false) TLongObjectMap<?> connectedMembers) {
		connectedMembers = new SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor());
	}
}