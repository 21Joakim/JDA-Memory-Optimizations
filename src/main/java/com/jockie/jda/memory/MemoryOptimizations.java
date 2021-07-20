package com.jockie.jda.memory;

import java.lang.instrument.Instrumentation;

import com.jockie.jda.memory.advice.InternAdvice;
import com.jockie.jda.memory.advice.SetBackedAbstractChannelPermissionOverrideMapAdvice;
import com.jockie.jda.memory.advice.SetBackedSnowflakeCacheViewImplAdvice;
import com.jockie.jda.memory.map.TLongObjectHashSet;
import com.jockie.jda.memory.transformer.RemoveFieldClassFileTransformer;

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
	 * Removes the manager field from {@link net.dv8tion.jda.internal.entities.GuildImpl GuildImpl},
	 * {@link net.dv8tion.jda.internal.entities.AbstractChannelImpl AbstractChannelImpl},
	 * {@link net.dv8tion.jda.internal.entities.EmoteImpl EmoteImpl},
	 * {@link net.dv8tion.jda.internal.entities.RoleImpl RoleImpl},
	 * {@link net.dv8tion.jda.internal.entities.StageInstanceImpl StageInstanceImpl},
	 * {@link net.dv8tion.jda.internal.entities.PermissionOverrideImpl PermissionOverrideImpl}.
	 * <br><br>
	 * <b>Note:</b> Do not use this if you use the getManager method on any of the mentioned classes, these can be installed
	 * indivdually instead.
	 * 
	 * @see #removeField(Instrumentation, String, String)
	 */
	public static void removeAllManagerFields(Instrumentation instrumentation) {
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.GuildImpl", "manager");
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.AbstractChannelImpl", "manager");
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.EmoteImpl", "manager");
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.RoleImpl", "manager");
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.StageInstanceImpl", "manager");
		MemoryOptimizations.removeField(instrumentation, "net.dv8tion.jda.internal.entities.PermissionOverrideImpl", "manager");
	}
	
	/**
	 * @see #removeField(Instrumentation, String, String)
	 */
	public static void removeField(String className, String field) {
		MemoryOptimizations.removeField(ByteBuddyAgent.install(), className, field);
	}
	
	/**
	 * Removes the specified field from the specified class, it will overwrite any existing getter and setter method
	 * with a no-op version, getters will return the default value for the return type, object/array = null and number = 0.
	 * <br><br>
	 * <b>Note:</b> Must be run before the class is loaded, preferably as early as possible, in the main method or
	 * a static initializer. This must also be run before installing any of the optimizations.
	 * <br>
	 * <b>Note:</b> Do not use {@link Class#getName()} to get the path as that will load the class and
	 * result in this not doing anything.
	 * <br>
	 * <b>Note:</b> This will not error or cause any problems if you give it an invalid class or field name
	 * <br><br>
	 * <b>Savings:</b> The amount of bytes the data type you removed uses, for objects you save 8 bytes per entity
	 * (or 4 bytes if you are using <a href="https://wiki.openjdk.java.net/display/HotSpot/CompressedOops">CompressedOops</a>)
	 * regardless if it is set to null or otherwise.
	 */
	public static void removeField(Instrumentation instrumentation, String className, String fieldName) {
		instrumentation.addTransformer(new RemoveFieldClassFileTransformer(className, fieldName));
	}
	
	/**
	 * Install all available non-breaking optimizations
	 */
	public static void installOptimizations() {
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
	 * <br><br>
	 * <b>Experimental</b>
	 * <br>
	 * 1. The performance impact of this is currently unknown.
	 * <br>
	 * 2. This has currently not been extensively tested, we don't know if this
	 * produces the exact same result as a {@link TLongObjectHashMap}.
	 * <br><br>
	 * <b>Savings:</b> 8 bytes for basically every Discord entity
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
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedPermissionOverrideMapOptimization() {
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
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
	 * <br><br>
	 * <b>Savings:</b> That is a bit complicated
	 */
	public static void installInternOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installInternAdvice(instrumentation, UserImpl.class, "setName");
		/* 
		 * It may seem like the avatarId is uncessary to intern but becuase JDA does not have
		 * a globally unique user cache (they have one per shard) users are often in the memory
		 * multiple times.
		 * 
		 * The exact savings and whether this has more of a negative impact than positive is unknown.
		 */
		MemoryOptimizations.installInternAdvice(instrumentation, UserImpl.class, "setAvatarId");
		MemoryOptimizations.installInternAdvice(instrumentation, GuildImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, MemberImpl.class, "setNickname");
		MemoryOptimizations.installInternAdvice(instrumentation, RoleImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, EmoteImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, AbstractChannelImpl.class, "setName");
	}
	
	/**
	 * @see #installInternAdvice(Instrumentation, Class, String)
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