package com.jockie.jda.memory.advice;

import java.util.EnumMap;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;

public class SetBackedChannelCacheViewImplAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.This ChannelCacheViewImpl<?> self, @Advice.FieldValue("caches") EnumMap<ChannelType, TLongObjectMap<?>> caches) {
		for(ChannelType channelType : caches.keySet()) {
			caches.put(channelType, new SnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor()));
		}
	}
}