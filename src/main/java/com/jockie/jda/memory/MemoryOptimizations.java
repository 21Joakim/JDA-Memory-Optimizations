package com.jockie.jda.memory;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashSet;

import com.jockie.jda.memory.advice.InternAdvice;
import com.jockie.jda.memory.advice.SelfSynchronizedSetBackedAudioChannelConnectedMembersMapAdvice;
import com.jockie.jda.memory.advice.SelfSynchronizedSetBackedChannelPermissionOverrideMapAdvice;
import com.jockie.jda.memory.advice.SelfUserImplCopyOfAdvice;
import com.jockie.jda.memory.advice.SetBackedAudioChannelConnectedMembersMapAdvice;
import com.jockie.jda.memory.advice.SetBackedChannelCacheViewImplAdvice;
import com.jockie.jda.memory.advice.SetBackedChannelPermissionOverrideMapAdvice;
import com.jockie.jda.memory.advice.SetBackedSnowflakeCacheViewImplAdvice;
import com.jockie.jda.memory.map.AbstractTLongObjectHashSet;
import com.jockie.jda.memory.map.SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap;
import com.jockie.jda.memory.map.SynchronizedSnowflakeSetBackedTLongObjectHashMap;
import com.jockie.jda.memory.transformer.discord.imageid.ImageIdClassFileTransformer;
import com.jockie.jda.memory.transformer.remove.RemoveFieldClassFileTransformer;

import gnu.trove.map.hash.TLongObjectHashMap;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Listener;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.matcher.ElementMatchers;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateSlowmodeEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.RoleImpl;
import net.dv8tion.jda.internal.entities.SelfUserImpl;
import net.dv8tion.jda.internal.entities.UserImpl;
import net.dv8tion.jda.internal.entities.channel.AbstractChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.CategoryImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.ForumChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.MediaChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.StageChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl;
import net.dv8tion.jda.internal.entities.channel.concrete.VoiceChannelImpl;
import net.dv8tion.jda.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import net.dv8tion.jda.internal.entities.emoji.RichCustomEmojiImpl;
import net.dv8tion.jda.internal.utils.cache.AbstractCacheView;
import net.dv8tion.jda.internal.utils.cache.ChannelCacheViewImpl;

public class MemoryOptimizations {
	
	private MemoryOptimizations() {}
	
	private static float loadFactor = 0.75F;
	private static boolean selfSynchronized = false;
	
	private static Listener listener = Listener.StreamWriting.toSystemError().withErrorsOnly();
	
	/**
	 * @see #getLoadFactor()
	 */
	public static void setLoadFactor(float loadFactor) {
		MemoryOptimizations.loadFactor = loadFactor;
	}
	
	/**
	 * @return the load factor which will be used by all JDA maps when installing the map optimization
	 * ({@link #installSetBackedSnowflakeCacheViewOptimization()}), by default this is set to 0.75, compared
	 * to the original default of 0.5.
	 */
	public static float getLoadFactor() {
		return MemoryOptimizations.loadFactor;
	}
	
	/**
	 * @see #isSelfSynchronized()
	 */
	public static void setSelfSynchronized(boolean selfSynchronized) {
		MemoryOptimizations.selfSynchronized = selfSynchronized;
	}
	
	/**
	 * <b>NOTE</b> This must be set before calling {@link MemoryOptimizations#installOptimizations()} or similar
	 * <br><br>
	 * <b>NOTE</b> This is technically a breaking change if you rely on the previous behaviour which is
	 * why it is disabled by default, there is currently no code in JDA itself which relies on this behaviour so
	 * it is safe to enable if you do not synchronize on those objects (which realistically you probably do not).
	 * <br><br>
	 * 
	 * This affects optimizations such as
	 * {@link #installSetBackedAbstractChannelPermissionOverrideMapOptimization(Instrumentation)}
	 * and
	 * {@link #installSetBackedVoiceChannelConnectedMembersMapOptimization(Instrumentation)}
	 * which normally use {@link MiscUtil#newLongMap()}.
	 * <br><br>
	 * 
	 * This should save 24 bytes (or 16 bytes with CompressedOops) per channel and double that if it's
	 * a voice (audio/stage) channel.
	 * 
	 * @return whether or not optimizations which replace a synchronized collection should use
	 * itself as the mutex instead of a separate object.
	 * 
	 * @see SynchronizedSnowflakeSetBackedTLongObjectHashMap
	 * @see SelfSynchronizedSnowflakeSetBackedTLongObjectHashMap
	 */
	public static boolean isSelfSynchronized() {
		return MemoryOptimizations.selfSynchronized;
	}
	
	/**
	 * Set the byte-buddy listener, this can be used to inform you about errors or when a transformation (optimization) is installed.
	 */
	public static void setListener(Listener listener) {
		MemoryOptimizations.listener = listener;
	}
	
	/**
	 * @see #installImageIdOptimization(Instrumentation)
	 */
	public static void installImageIdOptimization() {
		MemoryOptimizations.installImageIdOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * See {@link #installImageIdOptimization(Instrumentation, String, String)} for considerations and more information!
	 * <br><br>
	 * <b>Savings:</b> +39 bytes for every user and guild object with an avatar/icon and -9 bytes (or -13 bytes if you are using 
	 * <a href="https://wiki.openjdk.java.net/display/HotSpot/CompressedOops">CompressedOops</a>) for every empty avatar/icon.
	 * 
	 * @param instrumentation the instrumentation used to install the optimization
	 * 
	 * @see #installImageIdOptimization(Instrumentation, String, String)
	 */
	public static void installImageIdOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.UserImpl", "avatarId");
		MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.GuildImpl", "iconId");
		
		/*
		 * These are normally not populated enough to benefit from the optimization
		 * 
		 * MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.GuildImpl", "splashId");
		 * MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.GuildImpl", "banner");
		 * 
		 * It can also be installed for these, please note the effectiveness has not been checked for them
		 * 
		 * MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.MemberImpl", "avatarId");
		 * MemoryOptimizations.installImageIdOptimization(instrumentation, "net.dv8tion.jda.internal.entities.RoleIcon", "iconId");
		 */
		
		/* Fixes DefaultShardManager only starting a single shard */
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(SelfUserImpl.class))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(SelfUserImplCopyOfAdvice.class)
				.on(ElementMatchers.named("copyOf"))))
			.installOn(instrumentation);
	}
	
	/**
	 * @see #installImageIdOptimization(Instrumentation, String, String)
	 */
	public static void installImageIdOptimization(String className, String fieldName) {
		MemoryOptimizations.installImageIdOptimization(ByteBuddyAgent.install(), className, fieldName);
	}
	
	/**
	 * <b>Note:</b> Must be run before the class is loaded, preferably as early as possible, in the main method or
	 * a static initializer. This must also be run before installing any of the optimizations.
	 * <br>
	 * <b>Note:</b> Do not use {@link Class#getName()} to get the path as that will load the class and
	 * result in this not doing anything.
	 * <br>
	 * <b>Note:</b> This will not error or cause any problems if you give it an invalid class or field name
	 * <br><br>
	 * <b>Consideration:</b> An empty String is 8 bytes (or 4 bytes if you are using CompressedOops) and an empty custom image id is 17 bytes, 
	 * if a field has an overwhelming amount of empty values it may not be worth using this optimization as it might have 
	 * a negative effect on your memory usage.
	 * <br>
	 * <b>Consideration:</b> If you have a large amount of duplicate users (JDA only stores unique users per shard, so if you have a user across multiple
	 * shards you will be stroing it multiple times in memory) it may be better to use an intern the avatarId instead of this
	 * optimization, if you have over 50% (rough estimate, you may see success with lower ratios) unique users this optimization 
	 * will be better than intern. The best would be if you had a unique global user cache with this optimization.
	 * <br><br>
	 * <b>Savings:</b> +39 bytes for every image id and -9 bytes (or -13 bytes if you are using 
	 * <a href="https://wiki.openjdk.java.net/display/HotSpot/CompressedOops">CompressedOops</a>) for every empty image id.
	 * <br>
	 * <b>Savings in-depth:</b> A 32 character ascii String will use 56 bytes (8 for the Object, 4 for the cached hashcode, 12 for the char array, and 1 byte
	 * per character totaling 32 bytes) of memory, our optimization will use 17 bytes (2 longs and 1 boolean for whether the image is animated or not) saving us
	 * 39 bytes. This is not without drawbacks though, as an empty image id with our optimization will still use 17 bytes (because a primtive data type will still
	 * take up the same amount of memory even if it's "empty") and an empty Object (null reference) will only take up the space for the reference, which is 8 bytes
	 * or (4 bytes if you are using CompressedOops).
	 */
	/* 
	 * TODO: Consider an alternative solution which stores the ImageId object instead of the 3 value fields.
	 * The reason we would want to do this is to negate the memory increase from empty image ids, the only problem with that solution 
	 * is that it also reduces the memory savings, savings would go down from 39 bytes to 31 bytes and the loss for empty image ids
	 * would go down from 13 bytes to 0 bytes. Whether this solution would be overall net positive depends on the ratio of filled/empty
	 * image ids. We could make use of both solutions, one for the commonly filled values and one for the commonly empty values, which
	 * would give us the best of both worlds.
	 */
	public static void installImageIdOptimization(Instrumentation instrumentation, String className, String fieldName) {
		instrumentation.addTransformer(new ImageIdClassFileTransformer(className, fieldName));
	}
	
	/**
	 * @see #removeField(Instrumentation, String, String, String[])
	 */
	@Deprecated
	public static void removeField(String className, String field) {
		MemoryOptimizations.removeField(ByteBuddyAgent.install(), className, field);
	}
	
	/**
	 * @see #removeField(Instrumentation, String, String, String[])
	 */
	@Deprecated
	public static void removeField(String className, String field, String... relatedMethods) {
		MemoryOptimizations.removeField(ByteBuddyAgent.install(), className, field, relatedMethods);
	}
	
	/**
	 * @see #removeField(Instrumentation, String, String, String[])
	 */
	@Deprecated
	public static void removeField(Instrumentation instrumentation, String className, String fieldName) {
		instrumentation.addTransformer(new RemoveFieldClassFileTransformer(className, fieldName));
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
	 * <b>Note:</b> This is only supported for private fields currently, attempting to use it on a public/protected
	 * field will work but if anything accesses the field outside of the class itself it will error.
	 * <b>Note:</b> This will not error or cause any problems if you give it an invalid class or field name.
	 * <br>
	 * <br><br>
	 * <b>Savings:</b> The amount of bytes the data type you removed uses, for objects you save 8 bytes per entity
	 * (or 4 bytes if you are using <a href="https://wiki.openjdk.java.net/display/HotSpot/CompressedOops">CompressedOops</a>)
	 * regardless if it is set to null or otherwise.
	 * 
	 * @deprecated Avoid using this unless you have extensively tested the behaviour after removing a field, e.g
	 * <br>1. ensure that the field was actually removed
	 * <br>2. ensure the getter/setter method does not produce an error and returns the default value
	 * <br>3. ensure JDA does not produce a larger than normal amount of events, especially for the specific field you removed,
	 * e.g, if you removed {@link TextChannelImpl#slowmode} ensure that you do not receive a {@link ChannelUpdateSlowmodeEvent}
	 * for every channel update event in a channel which has a non-default (0) slowmode value.
	 * <br><b>The plan is to replace this with a more robust solution in the future, for now we recommend against using this</b>
	 * 
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param relatedMethods the methods related to this field that need to be replaced with noop variants, by default this
	 * will be set to "isFieldName", "getFieldName" and "setFieldName"
	 */
	@Deprecated
	public static void removeField(Instrumentation instrumentation, String className, String fieldName, String... relatedMethods) {
		instrumentation.addTransformer(new RemoveFieldClassFileTransformer(className, fieldName, new HashSet<>(Arrays.asList(relatedMethods))));
	}
	
	/**
	 * Install all available non-breaking optimizations
	 */
	public static void installOptimizations() {
		MemoryOptimizations.installOptimizations(ByteBuddyAgent.install());
	}
	
	/**
	 * Install all available non-breaking optimizations
	 */
	public static void installOptimizations(Instrumentation instrumentation) {
		MemoryOptimizations.installImageIdOptimization(instrumentation);
		
		/*
		 * Let's not install the intern optimization by default, String#intern
		 * can cause issues depending on the setup and the default should just
		 * be reserved for optimizations without any (or very minimal) side-effects.
		 * 
		 * MemoryOptimizations.installInternOptimization(instrumentation);
		 */
		
		MemoryOptimizations.installSetBackedSnowflakeCacheViewOptimization(instrumentation);
		MemoryOptimizations.installSetBackedChannelCacheViewOptimization(instrumentation);
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(instrumentation);
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(instrumentation);
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedSnowflakeCacheViewOptimization() {
		MemoryOptimizations.installSetBackedSnowflakeCacheViewOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * Replaces the default {@link AbstractCacheView} elements field ({@link TLongObjectHashMap} with
	 * our custom Set implementation ({@link AbstractTLongObjectHashSet}) which is somewhat of a hybrid between
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
	 * 
	 * @param instrumentation the instrumentation used to install the optimization
	 */
	public static void installSetBackedSnowflakeCacheViewOptimization(Instrumentation instrumentation) {
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(AbstractCacheView.class))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(SetBackedSnowflakeCacheViewImplAdvice.class)
				.on(ElementMatchers.isConstructor())))
			.installOn(instrumentation);
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedChannelCacheViewOptimization() {
		MemoryOptimizations.installSetBackedChannelCacheViewOptimization(ByteBuddyAgent.install());
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedChannelCacheViewOptimization(Instrumentation instrumentation) {
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(ChannelCacheViewImpl.class))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(SetBackedChannelCacheViewImplAdvice.class)
				.on(ElementMatchers.isConstructor())))
			.installOn(instrumentation);
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization() {
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(ByteBuddyAgent.install(), MemoryOptimizations.isSelfSynchronized());
	}
	
	/**
	 * @param selfSynchronized whether or not the replaced synchronized collection should use
	 * itself as the mutex instead of a separate object. See {@link #isSelfSynchronized()} for
	 * more information
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization(boolean selfSynchronized) {
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(ByteBuddyAgent.install(), selfSynchronized);
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(instrumentation, MemoryOptimizations.isSelfSynchronized());
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param selfSynchronized whether or not the replaced synchronized collection should use
	 * itself as the mutex instead of a separate object. See {@link #isSelfSynchronized()} for
	 * more information
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization(Instrumentation instrumentation, boolean selfSynchronized) {
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(instrumentation, VoiceChannelImpl.class, selfSynchronized);
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(instrumentation, StageChannelImpl.class, selfSynchronized);
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param clazz the class to install the optimization on
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization(Instrumentation instrumentation, Class<? extends AudioChannel> clazz) {
		MemoryOptimizations.installSetBackedVoiceChannelConnectedMembersMapOptimization(instrumentation, clazz, MemoryOptimizations.isSelfSynchronized());
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param clazz the class to install the optimization on
	 * @param selfSynchronized whether or not the replaced synchronized collection should use
	 * itself as the mutex instead of a separate object. See {@link #isSelfSynchronized()} for
	 * more information
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 * @deprecated no longer necessary since JDA version 5.5.0
	 */
	@Deprecated
	public static void installSetBackedVoiceChannelConnectedMembersMapOptimization(Instrumentation instrumentation, Class<? extends AudioChannel> clazz, boolean selfSynchronized) {
		if(Integer.parseInt(JDAInfo.VERSION_MAJOR) >= 5 && Integer.parseInt(JDAInfo.VERSION_MINOR) >= 5) {
			return;
		}
		
		Class<?> adviceClass = selfSynchronized
			? SelfSynchronizedSetBackedAudioChannelConnectedMembersMapAdvice.class
			: SetBackedAudioChannelConnectedMembersMapAdvice.class;
		
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(clazz))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(adviceClass)
				.on(ElementMatchers.isConstructor())))
			.installOn(instrumentation);
	}
	
	/**
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedAbstractChannelPermissionOverrideMapOptimization() {
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(ByteBuddyAgent.install(), MemoryOptimizations.isSelfSynchronized());
	}
	
	/**
	 * @param selfSynchronized whether or not the replaced synchronized collection should use
	 * itself as the mutex instead of a separate object. See {@link #isSelfSynchronized()} for
	 * more information
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedAbstractChannelPermissionOverrideMapOptimization(boolean selfSynchronized) {
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(ByteBuddyAgent.install(), selfSynchronized);
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedAbstractChannelPermissionOverrideMapOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installSetBackedAbstractChannelPermissionOverrideMapOptimization(instrumentation, MemoryOptimizations.isSelfSynchronized());
	}
	
	/**
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param selfSynchronized whether or not the replaced synchronized collection should use
	 * itself as the mutex instead of a separate object. See {@link #isSelfSynchronized()} for
	 * more information
	 * 
	 * @see #installSetBackedSnowflakeCacheViewOptimization(Instrumentation)
	 */
	public static void installSetBackedAbstractChannelPermissionOverrideMapOptimization(Instrumentation instrumentation, boolean selfSynchronized) {
		Class<?> adviceClass = selfSynchronized
			? SelfSynchronizedSetBackedChannelPermissionOverrideMapAdvice.class
			: SetBackedChannelPermissionOverrideMapAdvice.class;
		
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(AbstractStandardGuildChannelImpl.class)
				.or(ElementMatchers.is(CategoryImpl.class))
				.or(ElementMatchers.is(ForumChannelImpl.class))
				.or(ElementMatchers.is(MediaChannelImpl.class)))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(adviceClass)
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
	 * 
	 * @param instrumentation the instrumentation used to install the optimization
	 */
	public static void installInternOptimization(Instrumentation instrumentation) {
		MemoryOptimizations.installInternAdvice(instrumentation, UserImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, GuildImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, MemberImpl.class, "setNickname");
		MemoryOptimizations.installInternAdvice(instrumentation, RoleImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, RichCustomEmojiImpl.class, "setName");
		MemoryOptimizations.installInternAdvice(instrumentation, AbstractChannelImpl.class, "setName");
	}
	
	/**
	 * @param clazz the class to install the optimization on
	 * @param methodName the method to install the optimization on
	 * 
	 * @see #installInternAdvice(Instrumentation, Class, String)
	 */
	public static void installInternAdvice(Class<?> clazz, String methodName) {
		MemoryOptimizations.installInternAdvice(ByteBuddyAgent.install(), clazz, methodName);
	}
	
	/**
	 * Replace the argument in a String setter method with {@link String#intern()}
	 * 
	 * @param instrumentation the instrumentation used to install the optimization
	 * @param clazz the class to install the optimization on
	 * @param methodName the method to install the optimization on
	 */
	public static void installInternAdvice(Instrumentation instrumentation, Class<?> clazz, String methodName) {
		new AgentBuilder.Default()
			.disableClassFormatChanges()
			.with(MemoryOptimizations.listener)
			.with(RedefinitionStrategy.RETRANSFORMATION)
			.type(ElementMatchers.is(clazz))
			.transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder.visit(Advice
				.to(InternAdvice.class, ClassFileLocator.ForClassLoader.ofSystemLoader())
				.on(ElementMatchers.named(methodName))))
			.installOn(instrumentation);
	}
}