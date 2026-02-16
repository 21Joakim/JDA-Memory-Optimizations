package com.jockie.jda.memory.map.tiny;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.jockie.jda.memory.map.SynchronizedCollection;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.sync.TSynchronizedLongSet;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.entities.ISnowflake;

/**
 * This is the equivalent of creating a {@link TinySynchronizedSnowflakeSetBackedTLongObjectHashMap} without a
 * separate mutex Object, the mutex field is instead completely removed in favour of always synchronizing on
 * itself (this), this saves 1 reference (4 bytes with CompressedOops or 8 bytes without).
 */
public class TinySelfSynchronizedSnowflakeSetBackedTLongObjectHashMap<T extends ISnowflake> extends TinySnowflakeSetBackedTLongObjectHashMap<T> implements Serializable {
	
	public TinySelfSynchronizedSnowflakeSetBackedTLongObjectHashMap() {
		super();
	}
	
	public TinySelfSynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public TinySelfSynchronizedSnowflakeSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	public int size() {
		synchronized(this) {
			return super.size();
		}
	}
	
	@Override
	public boolean isEmpty(){
		synchronized(this) {
			return super.isEmpty();
		}
	}
	
	@Override
	public boolean containsKey(long key) {
		synchronized(this) {
			return super.containsKey(key);
		}
	}
	
	@Override
	public boolean containsValue(Object value) {
		synchronized(this) {
			return super.containsValue(value);
		}
	}
	
	@Override
	public T get(long key) {
		synchronized(this) {
			return super.get(key);
		}
	}
	
	@Override
	public T put(long key, T value) {
		synchronized(this) {
			return super.put(key, value);
		}
	}
	
	@Override
	public T remove(long key) {
		synchronized(this) {
			return super.remove(key);
		}
	}
	
	@Override
	public void putAll(Map<? extends Long, ? extends T> map) {
		synchronized(this) {
			super.putAll(map);
		}
	}
	
	@Override
	public void putAll(TLongObjectMap<? extends T> map) {
		synchronized(this) {
			super.putAll(map);
		}
	}
	
	@Override
	public void clear() {
		synchronized(this) {
			super.clear();
		}
	}
	
	@Override
	public TLongSet keySet() {
		return new TSynchronizedLongSet(super.keySet(), this);
	}
	
	@Override
	public long[] keys() {
		synchronized(this) {
			return super.keys();
		}
	}
	
	@Override
	public long[] keys(long[] array) {
		synchronized(this) {
			return super.keys(array);
		}
	}
	
	@Override
	public Collection<T> valueCollection() {
		return new SynchronizedCollection<T>(super.valueCollection(), this);
	}
	
	@Override
	public Object[] values() {
		synchronized(this) {
			return super.values();
		}
	}
	
	@Override
	public T[] values(T[] array) {
		synchronized(this) {
			return super.values(array);
		}
	}
	
	@Override
	public TinySetBackedTLongObjectHashMapIterator<T> iterator() {
		return super.iterator(); // Must be manually synched by user!
	}
	
	// unchanging over the life of the map, no need to lock
	@Override
	public long getNoEntryKey() {
		return super.getNoEntryKey();
	}
	
	@Override
	public T putIfAbsent(long key, T value) {
		synchronized(this) {
			return super.putIfAbsent(key, value);
		}
	}
	
	@Override
	public boolean forEachKey(TLongProcedure procedure) {
		synchronized(this) {
			return super.forEachKey(procedure);
		}
	}
	
	@Override
	public boolean forEachValue(TObjectProcedure<? super T> procedure) {
		synchronized(this) {
			return super.forEachValue(procedure);
		}
	}
	
	@Override
	public boolean forEachEntry(TLongObjectProcedure<? super T> procedure) {
		synchronized(this) {
			return super.forEachEntry(procedure);
		}
	}
	
	@Override
	public void transformValues(TObjectFunction<T, T> function) {
		synchronized(this) {
			super.transformValues(function);
		}
	}
	
	@Override
	public boolean retainEntries(TLongObjectProcedure<? super T> procedure) {
		synchronized(this) {
			return super.retainEntries(procedure);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		synchronized(this) {
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		synchronized(this) {
			return super.hashCode();
		}
	}
	
	@Override
	public String toString() {
		synchronized(this) {
			return super.toString();
		}
	}
	
	private void writeObject(ObjectOutputStream s) throws IOException {
		synchronized(this) {
			s.defaultWriteObject();
		}
	}
}