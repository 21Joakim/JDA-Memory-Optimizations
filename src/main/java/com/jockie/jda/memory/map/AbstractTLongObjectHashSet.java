package com.jockie.jda.memory.map;

import gnu.trove.function.TObjectFunction;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

/**
 * Somewhat of a hybrid between a Set and a Map. It stores the Objects in a Set but
 * it uses a property from the Object ({@link #extractKey(Object)} to get the hash.
 */
public abstract class AbstractTLongObjectHashSet<T> extends THashSet<T> {
	
	public static final class ToLongKeyArrayProcedure<T> implements TObjectProcedure<T> {
		
		private final AbstractTLongObjectHashSet<T> set;
		
		private final long[] target;
		private int pos = 0;
		
		public ToLongKeyArrayProcedure(AbstractTLongObjectHashSet<T> set, final long[] target) {
			this.set = set;
			this.target = target;
		}
		
		public final boolean execute(T value) {
			this.target[this.pos++] = this.set.extractKey(value);
			return true;
		}
	}
	
	public abstract long extractKey(Object object);
	
	public AbstractTLongObjectHashSet() {
		super();
	}
	
	public AbstractTLongObjectHashSet(int initialCapacity) {
		super(initialCapacity);
	}
	
	public AbstractTLongObjectHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	protected int hash(Object notnull) {
		return Long.hashCode(this.extractKey(notnull));
	}
	
	@Override
	protected boolean equals(Object notnull, Object two) {
		if(two == null || two == REMOVED) {
			return false;
		}
		
		return this.extractKey(notnull) == this.extractKey(two);
	}
	
	@SuppressWarnings("unchecked")
	public T get(long key) {
		int i = this.index(key);
		if(i >= 0) {
			return (T) this._set[i];
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public T removeGet(long key) {
		int i = this.index(key);
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
		
		this.forEach(new ToLongKeyArrayProcedure<>(this, result));
		return result;
	}
	
	public long[] keys(long[] a) {
		int size = this.size();
		if(a.length < size) {
			a = new long[size];
		}
		
		this.forEach(new ToLongKeyArrayProcedure<>(this, a));
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
				if((value != THashSet.FREE && value != THashSet.REMOVED) && !procedure.execute(this.extractKey(value), (T) value)) {
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
			if(value != THashSet.FREE && value != THashSet.REMOVED) {
				values[i] = function.execute((T) values[i]);
			}
		}
	}
}