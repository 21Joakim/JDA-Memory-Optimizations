package com.jockie.jda.memory.advice;

import com.jockie.jda.memory.MemoryOptimizations;
import com.jockie.jda.memory.map.SnowflakeSetBackedTLongObjectHashMap;

import gnu.trove.map.TLongObjectMap;
import net.bytebuddy.asm.Advice;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;

public class SetBackedSnowflakeCacheViewImplAdvice {
	
	@Advice.OnMethodExit
	public static void exit(@Advice.This AbstractCacheView<?> self, @Advice.FieldValue(value="elements", readOnly=false) TLongObjectMap<?> elements) {
		if(self instanceof SnowflakeCacheViewImpl) {
			elements = new SnowflakeSetBackedTLongObjectHashMap<>(0, MemoryOptimizations.getLoadFactor());
		}
	}
}