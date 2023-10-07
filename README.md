# JDA-Memory-Optimizations

Experimental extension library for [JDA](https://github.com/DV8FromTheWorld/JDA/) which attempts to reduce the amount of memory used.

## Content
* [Bots Using This](#bots-using-this)
* [Download](#download)
	* [Gradle](#gradle)
	* [Maven](#maven)
* [Usage](#usage)
	* [Code](#code)
	* [Launch Options](#launch-options)
* [Other Tips](#other-tips)
	* [CompressedOops](#compressedoops)
* [Optimizations](#optimizations)

## Bots Using This
* Jockie Music
* Sx4

## Download
[![](https://jitpack.io/v/21Joakim/JDA-Memory-Optimizations.svg)](https://jitpack.io/#21Joakim/JDA-Memory-Optimizations)

### Gradle
```groovy
repositories {
	jcenter()
	maven { url 'https://jitpack.io' }
}

dependencies {
	implementation 'com.github.21Joakim:JDA-Memory-Optimizations:VERSION'
}
```

### Maven
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	<groupId>com.github.21Joakim</groupId>
	<artifactId>JDA-Memory-Optimizations</artifactId>
	<version>VERSION</version>
</dependency>
```

## Usage

### Code
This should be run as early as possible, it will install all non-breaking optimizations.

```java
MemoryOptimizations.installOptimizations();
```

If you want to remove a field yourself, you can use the `MemoryOptimizations#removeField` method, this must be run **before the class is loaded**, which means you can not do `TextChannelImpl.class.getName()` to get the path!

```
MemoryOptimizations.removeField("net.dv8tion.jda.internal.entities.TextChannelImpl", "topic");
```

### Launch Options

This uses [byte-buddy](https://github.com/raphw/byte-buddy) to change how some JDA internals work, as such you may have to add the byte-buddy agent as a `-javaagent` launch option, this step may not always be necessary but to be certain the optimizations work it should be performed.

```bash
java -javaagent:/path/to/agent.jar -jar Bot.jar
```

## Other Tips
### CompressedOops
Do everything you can to stay under 32 GB in max heap, the reason for this is very simple, [CompressedOops](https://wiki.openjdk.java.net/display/HotSpot/CompressedOops), which is enabled by default for heaps below 32 GB, this will make all Object references 4 bytes instead of 8 which can be a huge saving in memory. If your current max heap is set to just above 32 GB you are most likely wasting memory and reducing it to below 32 GB will have a positive effect on your memory usage.

You should set the heap to a maximum of 31 GB to be safe, 32 GB may work but you should validate this before using it in production.

## Optimizations
Some of the optimizations this library performs. Unless otherwise specified these are implemented in a non-breaking way.
### Custom set backed map
By using a custom set backed map implementation for `TLongObjectMap` which gets the key from the stored object directly (e.g `User#getIdLong()`) we can remove the keys array which saves at least `elements * 8 bytes (size of long)` + all the additional elements allocated depending on the load factor.

This implementation is used for all `SnowflakeCacheViewImpl` instances and affects near every single JDA entity (guilds, users, channels, roles, emojis, permission overrides, etc) which means there can be a significant saving.
### Load Factor
The load factor of all `TLongObjectMap` instances created by JDA was changed from `0.5` to `0.75`, this reduces the number of unused elements at a slight performance cost. You can change this as you see fit with `MemoryOptimizations#setLoadFactor(float)`.
### Storing `UserImpl#avatarId` and `GuildImpl#iconId` in a smaller data format
Instead of storing the hex based image id as a `String` we use two `long`s and one `boolean` (for whether it is animated or not) which considerably reduces the amount of data a user in memory uses.
### Custom self-synchronized `TLongObjectMap` (disabled by default)
Instead of synchronizing on a separate mutex Object we synchronize directly on the instance itself saving a whole `Object` (16 bytes) for every single synchronized map (2 instances for every audio channel and 1 instance for every other channel).

This is disabled by default because it could technically be breaking if you synchronize on the instance itself, however, it is incredibly unlikely you do in which case this is safe to enable with `MemoryOptimizations#setSelfSynchronized(boolean)` for some nice savings.

**Note:** This affects JDA's `MiscUtil#newLongMap()`
### String#intern (disabled by default)
Using `String#intern`, to remove duplicate instances, on fields which commonly have duplicate values, such as `UserImpl#name`, `GuildImpl#name`, `MemberImpl#nickname`, `RoleImpl#name`, `EmoteImpl#name` and `AbstractChannelImpl#name`.
