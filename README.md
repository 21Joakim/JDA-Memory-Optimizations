# JDA-Memory-Optimizations

Experimental extension library for [JDA](https://github.com/DV8FromTheWorld/JDA/) which attempts to reduce the amount of memory used.

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
This should be run as early as possible.

```java
MemoryOptimizations.installAll();
```

### Launch Options
This uses [byte-buddy](https://github.com/raphw/byte-buddy) to change how some JDA internals work, as such you may have to add the byte-buddy agent as a `-javaagent` launch option, this step may not always be necessary but to be certain the optimizations work it should be performed.

```bash
java -javaagent:/path/to/agent.jar -jar Bot.jar
```