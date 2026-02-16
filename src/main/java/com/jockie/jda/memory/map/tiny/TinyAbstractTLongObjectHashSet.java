package com.jockie.jda.memory.map.tiny;

import com.jockie.jda.memory.map.tiny.trove.TinyTHashSet;

import gnu.trove.function.TObjectFunction;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;

/**
 * Somewhat of a hybrid between a Set and a Map. It stores the Objects in a Set but
 * it uses a property from the Object ({@link #extractKey(Object)}) to get the hash.
 */
public abstract class TinyAbstractTLongObjectHashSet<T> extends TinyTHashSet<T> {
	
	public static final class TinyToLongKeyArrayProcedure<T> implements TObjectProcedure<T> {
		
		private final TinyAbstractTLongObjectHashSet<T> set;
		
		private final long[] target;
		private int pos = 0;
		
		public TinyToLongKeyArrayProcedure(TinyAbstractTLongObjectHashSet<T> set, final long[] target) {
			this.set = set;
			this.target = target;
		}
		
		public final boolean execute(T value) {
			this.target[this.pos++] = this.set.extractKey(value);
			return true;
		}
	}
	
	public abstract long extractKey(Object object);
	
	public TinyAbstractTLongObjectHashSet() {
		super();
	}
	
	public TinyAbstractTLongObjectHashSet(int initialCapacity) {
		super(initialCapacity);
	}
	
	public TinyAbstractTLongObjectHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	protected int hash(Object notnull) {
		return Long.hashCode(this.extractKey(notnull));
	}
	
	protected int hashKey(long key) {
		return Long.hashCode(key);
	}
	
	@Override
	protected boolean equals(Object notnull, Object two) {
		if(two == null || two == REMOVED) {
			return false;
		}
		
		return this.extractKey(notnull) == this.extractKey(two);
	}
	
	protected boolean equalsKey(long key, Object two) {
		if(two == null || two == REMOVED) {
			return false;
		}
		
		return key == this.extractKey(two);
	}
	
	public boolean contains(long key) {
		return this.indexKey(key) >= 0;
	}
	
	@SuppressWarnings("unchecked")
	public T replace(T obj) {
		int index = this.insertKey(obj);
		if(index < 0) {
			index = -index - 1;
			
			Object value = this._set[index];
			if(value == obj) {
				return obj;
			}
			
			/* 
			 * Since the slot is already occupied by the old value
			 * we don't need to do anything else, just replacing
			 * it is fine.
			 */
			this._set[index] = obj;
			return (T) value;
		}
		
		this.postInsertHook(this.consumeFreeSlot);
		return null;
	}
	
	@Override
	public boolean add(T obj) {
		return this.replace(obj) != obj;
	}
	
	@SuppressWarnings("unchecked")
	public T get(long key) {
		int i = this.indexKey(key);
		if(i >= 0) {
			return (T) this._set[i];
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T removeGet(long key) {
		int i = this.indexKey(key);
		if(i >= 0) {
			T value = (T) this._set[i];
			this.removeAt(i);
			return value;
		}
		
		return null;
	}
	
	@Override
	protected void removeAt(int index) {
		super.removeAt(index);
	}
	
	public long[] keys() {
		long[] result = new long[this.size()];
		
		this.forEach(new TinyToLongKeyArrayProcedure<>(this, result));
		return result;
	}
	
	public long[] keys(long[] a) {
		int size = this.size();
		if(a.length < size) {
			a = new long[size];
		}
		
		this.forEach(new TinyToLongKeyArrayProcedure<>(this, a));
		return a;
	}
	
	@SuppressWarnings("unchecked")
	public boolean retainEntries(TLongObjectProcedure<? super T> procedure) {
		boolean modified = false;
		Object[] values = this._set;
		
		/* TODO: Not sure if we should disable this here or not, that's just what they do in TLongObjectHashMap */
		// Temporarily disable compaction. This is a fix for bug #1738760
		this.tempDisableAutoCompaction();
		try {
			for(int i = values.length; i-- > 0;) {
				Object value = values[i];
				if((value != TinyTHashSet.FREE && value != TinyTHashSet.REMOVED) && !procedure.execute(this.extractKey(value), (T) value)) {
					this.removeAt(i);
					modified = true;
				}
			}
		}finally{
			this.reenableAutoCompaction(true);
		}
		
		return modified;
	}
	
	@SuppressWarnings("unchecked")
	public void transformValues(TObjectFunction<T, T> function) {
		Object[] values = this._set;
		for(int i = values.length; i-- > 0;) {
			Object value = values[i];
			if(value != TinyTHashSet.FREE && value != TinyTHashSet.REMOVED) {
				values[i] = function.execute((T) values[i]);
			}
		}
	}
	
	/* The methods below are copied from gnu.trove.impl.hash.TObjectHash */
	
	/**
	 * Locates the index of key.
	 *
	 * @param key an <code>Object</code> value
	 * @return the index of objKey or -1 if it isn't in the set.
	 */
	protected int indexKey(long key) {
		final int hash = this.hashKey(key) & 0x7fffffff;
		int index = hash % this._set.length;
		
		Object current = this._set[index];
		if(current == FREE) {
			return -1;
		}
		
		if(this.equalsKey(key, current)) {
			return index;
		}
		
		return this.indexRehashedKey(key, index, hash, current);
	}
	
	/**
	 * Locates the index of key.
	 *
	 * @param key target key, know to be non-null
	 * @param index we start from
	 * @param hash
	 * @param current
	 * @return
	 */
	private int indexRehashedKey(long key, int index, int hash, Object current) {
		final Object[] set = this._set;
		final int length = set.length;
		
		// NOTE: here it has to be REMOVED or FULL (some user-given value)
		// see Knuth, p. 529
		int probe = 1 + (hash % (length - 2));
		final int loopIndex = index;
		do {
			index -= probe;
			if(index < 0) {
				index += length;
			}
			
			current = set[index];
			if(current == FREE) {
				return -1;
			}
			
			if(this.equalsKey(key, current)) {
				return index;
			}
		}while(index != loopIndex);
		
		return -1;
	}
}