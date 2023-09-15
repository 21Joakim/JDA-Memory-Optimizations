package com.jockie.jda.memory.map;

import net.dv8tion.jda.api.entities.ISnowflake;

public class SnowflakeSetBackedTLongObjectHashMap<T extends ISnowflake> extends AbstractSetBackedTLongObjectHashMap<T> {
	
	public SnowflakeSetBackedTLongObjectHashMap() {
		super();
	}
	
	public SnowflakeSetBackedTLongObjectHashMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public SnowflakeSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public long extractKey(Object object) {
		return ((T) object).getIdLong();
	}
}