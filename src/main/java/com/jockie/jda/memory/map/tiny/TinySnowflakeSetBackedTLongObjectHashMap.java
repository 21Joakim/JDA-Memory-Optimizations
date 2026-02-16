package com.jockie.jda.memory.map.tiny;

import net.dv8tion.jda.api.entities.ISnowflake;

public class TinySnowflakeSetBackedTLongObjectHashMap<T extends ISnowflake> extends TinyAbstractSetBackedTLongObjectHashMap<T> {
	
	public TinySnowflakeSetBackedTLongObjectHashMap() {
		super();
	}
	
	public TinySnowflakeSetBackedTLongObjectHashMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public TinySnowflakeSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public long extractKey(Object object) {
		return ((T) object).getIdLong();
	}
}