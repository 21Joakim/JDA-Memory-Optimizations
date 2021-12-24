package com.jockie.jda.memory.map;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import gnu.trove.TCollections;
import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.sync.TSynchronizedLongSet;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.entities.ISnowflake;

/**
 * This is the equivalent of calling {@link TCollections#synchronizedMap(TLongObjectMap)} with a
 * {@link SnowflakeSetBackedTLongObjectHashMap} with one key change, {@link #keySet()} and {@link #valueCollection()}
 * are no longer cached and are instead created when called. While I am not yet entirely sure what the impact of this
 * change will be my initial assessment is that there should not be any major difference, at least not for the use case of JDA.
 * <br><br>
 * Another reason to have this is to avoid the memory overhead of a new object (the synchronized map) and the reference
 * to the original map, which normally would not be worth optimizing for but when you have as many maps as a Discord
 * bot has at scale it makes a difference.
 */
public class SynchronizedSnowflakeSetBackedTLongObjectHashMap<T extends ISnowflake> extends SnowflakeSetBackedTLongObjectHashMap<T> implements Serializable {
	
	protected final Object mutex;
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap() {
		super();
		
		this.mutex = this;
	}
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity) {
		super(initialCapacity);
		
		this.mutex = this;
	}
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		
		this.mutex = this;
	}
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap(Object mutex) {
		super();
		
		this.mutex = mutex;
	}
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity, Object mutex) {
		super(initialCapacity);
		
		this.mutex = mutex;
	}
	
	public SynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor, Object mutex) {
		super(initialCapacity, loadFactor);
		
		this.mutex = mutex;
	}
	
	@Override
	public int size() {
		synchronized(this.mutex) {
			return super.size();
		}
	}
	
	@Override
	public boolean isEmpty(){
		synchronized(this.mutex) {
			return super.isEmpty();
		}
	}
	
	@Override
	public boolean containsKey(long key) {
		synchronized(this.mutex) {
			return super.containsKey(key);
		}
	}
	
	@Override
	public boolean containsValue(Object value) {
		synchronized(this.mutex) {
			return super.containsValue(value);
		}
	}
	
	@Override
	public T get(long key) {
		synchronized(this.mutex) {
			return super.get(key);
		}
	}
	
	@Override
	public T put(long key, T value) {
		synchronized(this.mutex) {
			return super.put(key, value);
		}
	}
	
	@Override
	public T remove(long key) {
		synchronized(this.mutex) {
			return super.remove(key);
		}
	}
	
	@Override
	public void putAll(Map<? extends Long, ? extends T> map) {
		synchronized(this.mutex) {
			super.putAll(map);
		}
	}
	
	@Override
	public void putAll(TLongObjectMap<? extends T> map) {
		synchronized(this.mutex) {
			super.putAll(map);
		}
	}
	
	@Override
	public void clear() {
		synchronized(this.mutex) {
			super.clear();
		}
	}
	
	@Override
	public TLongSet keySet() {
		return new TSynchronizedLongSet(super.keySet(), this.mutex);
	}
	
	@Override
	public long[] keys() {
		synchronized(this.mutex) {
			return super.keys();
		}
	}
	
	@Override
	public long[] keys(long[] array) {
		synchronized(this.mutex) {
			return super.keys(array);
		}
	}
	
	@Override
	public Collection<T> valueCollection() {
		return new SynchronizedCollection<T>(super.valueCollection(), this.mutex);
	}
	
	@Override
	public Object[] values() {
		synchronized(this.mutex) {
			return super.values();
		}
	}
	
	@Override
	public T[] values(T[] array) {
		synchronized(this.mutex) {
			return super.values(array);
		}
	}
	
	@Override
	public SetBackedTLongObjectHashMapIterator<T> iterator() {
		return super.iterator(); // Must be manually synched by user!
	}
	
	// unchanging over the life of the map, no need to lock
	@Override
	public long getNoEntryKey() {
		return super.getNoEntryKey();
	}
	
	@Override
	public T putIfAbsent(long key, T value) {
		synchronized(this.mutex) {
			return super.putIfAbsent(key, value);
		}
	}
	
	@Override
	public boolean forEachKey(TLongProcedure procedure) {
		synchronized(this.mutex) {
			return super.forEachKey(procedure);
		}
	}
	
	@Override
	public boolean forEachValue(TObjectProcedure<? super T> procedure) {
		synchronized(this.mutex) {
			return super.forEachValue(procedure);
		}
	}
	
	@Override
	public boolean forEachEntry(TLongObjectProcedure<? super T> procedure) {
		synchronized(this.mutex) {
			return super.forEachEntry(procedure);
		}
	}
	
	@Override
	public void transformValues(TObjectFunction<T, T> function) {
		synchronized(this.mutex) {
			super.transformValues(function);
		}
	}
	
	@Override
	public boolean retainEntries(TLongObjectProcedure<? super T> procedure) {
		synchronized(this.mutex) {
			return super.retainEntries(procedure);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		synchronized(this.mutex) {
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		synchronized(this.mutex) {
			return super.hashCode();
		}
	}
	
	@Override
	public String toString() {
		synchronized(this.mutex) {
			return super.toString();
		}
	}
	
	private void writeObject(ObjectOutputStream s) throws IOException {
		synchronized(this.mutex) {
			s.defaultWriteObject();
		}
	}
}