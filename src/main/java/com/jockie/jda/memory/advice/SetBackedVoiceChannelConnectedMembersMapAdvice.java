package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.map.SetBackedTLongObjectHashMap;

import gnu.trove.impl.sync.TSynchronizedLongObjectMap;
import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.internal.entities.VoiceChannelImpl;

public class SetBackedVoiceChannelConnectedMembersMapAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.This VoiceChannelImpl self, @Advice.FieldValue(value="connectedMembers", readOnly=false) TLongObjectMap<?> connectedMembers) {
		connectedMembers = new TSynchronizedLongObjectMap<>(new SetBackedTLongObjectHashMap<>(ISnowflake::getIdLong), new Object());
	}
}