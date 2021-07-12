package com.jockie.jda.memory;

import java.lang.instrument.Instrumentation;

import com.jockie.jda.memory.advice.InternAdvice;
import com.jockie.jda.memory.advice.SetBackedAbstractChannelPermissionOverrideMapAdvice;
import com.jockie.jda.memory.advice.SetBackedSnowflakeCacheViewImplAdvice;
import com.jockie.jda.memory.map.TLongObjectHashSet;

import gnu.trove.map.hash.TLongObjectHashMap;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.dv8tion.jda.internal.entities.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.EmoteImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.RoleImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;

public class MemoryOptimizations {
	
	/**
	 * Install all available memory optimizations
	 */
	public static void installAll() {
		Instrumentation instrumentation = ByteBuddyAgent.install();
		
		MemoryOptimizations.installInternOptimization(instrumentation);
		MemoryOptimizations.installSetBackedSnowflakeCacheViewOptimization(instrumentation);
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(instrumentation);
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedSnowflakeCacheViewOptimization() {
		MemoryOptimizations.installSetBackedSnowflakeCacheViewOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * Replaces the default {@link AbstractCacheView} elements field ({@link TLongObjectHashMap} with
	 * our custom Set implementation ({@link TLongObjectHashSet}) which is somewhat of a hybrid between
	 * a Map and Set data type.
	 * 
	 * <b>Experimental</b>
	 * <br>
	 * 1. The performance impact of this is currently unknown.
	 * <br>
	 * 2. This has currently not been extensively tested, we don't know if this
	 * produces the exact same result as a {@link TLongObjectHashMap}.
	 */
	public static void installSetBackedSnowflakeCacheViewOptimization(Instrumentation instrumentation) {
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(AbstractCacheView.class))
			.transform((builder, typeDescription, classLoader, module) -> builder.visit(Advice
				.to(SetBackedSnowflakeCacheViewImplAdvice.class)
				.on(ElementMatchers.isConstructor())))
			.installOn(instrumentation);
	}
	
	/**
	 * @see {@link #installSetBackedSnowflakeCacheViewOptimization(Instrumentation))}
	 */
	public static void installSetBackedPermissionOverrideMapOptimization() {
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * @see {@link #installSetBackedSnowflakeCacheViewOptimization(Instrumentation))}
	 */
	public static void installSetBackedAbstractChannelPermissionOverrideMapOptimization(Instrumentation instrumentation) {
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(AbstractChannelImpl.class))
			.transform((builder, typeDescription, classLoader, module) -> builder.visit(Advice
				.to(SetBackedAbstractChannelPermissionOverrideMapAdvice.class)
				.on(ElementMatchers.isConstructor())))
			.installOn(instrumentation);
	}
	
	/**
	 * @see #installInternOptimization(Instrumentation)
	 */
	public static void installInternOptimization() {
		MemoryOptimizations.installInternOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * Replaces setX (for String methods) to use {@link String#intern()} for fields that are commonly duplicate
	 */
	public static void installInternOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installInternAdvice(instrumentation, UserImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, UserImpl.class, "setAvatarId");
		MemoryOptimizations.installInternAdvice(instrumentation, GuildImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, MemberImpl.class, "setNickname");
		MemoryOptimizations.installInternAdvice(instrumentation, RoleImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, EmoteImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, AbstractChannelImpl.class, "setName");
	}
	
	/**
	 * @see {@link #installInternAdvice(Instrumentation, Class, String)}
	 */
	public static void installInternAdvice(Class<?> clazz, String methodName) {
		MemoryOptimizations.installInternAdvice(ByteBuddyAgent.install(), clazz, methodName);
	}
	
	/**
	 * Replace the argument in a String setter method with {@link String#intern()}
	 */
	public static void installInternAdvice(Instrumentation instrumentation, Class<?> clazz, String methodName) {
		new AgentBuilder.Default()
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.disableClassFormatChanges()
			.type(ElementMatchers.is(clazz))
			.transform((builder, typeDescription, classLoader, module) -> builder.visit(Advice
				.to(InternAdvice.class, ClassFileLocator.ForClassLoader.ofSystemLoader())
				.on(ElementMatchers.named(methodName))))
			.installOn(instrumentation);
	}
}