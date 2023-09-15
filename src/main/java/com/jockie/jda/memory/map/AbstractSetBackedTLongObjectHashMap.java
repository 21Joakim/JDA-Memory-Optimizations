package com.jockie.jda.memory.map;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gnu.trove.TLongCollection;
import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.THashSet;

/**
 * Compatibility between {@link AbstractTLongObjectHashSet} and {@link TLongObjectMap}.
 * 
 * Most of this is just copied from {@link TLongObjectHashMap} with some modifications to
 * work together with {@link AbstractTLongObjectHashSet}
 */
public abstract class AbstractSetBackedTLongObjectHashMap<T> extends AbstractTLongObjectHashSet<T> implements TLongObjectMap<T> {
	
	protected static class SetBackedTLongObjectHashMapIterator<T> extends TObjectHashIterator<T> implements TLongObjectIterator<T> {
		
		protected final AbstractSetBackedTLongObjectHashMap<T> hash;
		
		public SetBackedTLongObjectHashMapIterator(AbstractSetBackedTLongObjectHashMap<T> hash) {
			super(hash);
			
			this.hash = hash;
		}
		
		@Override
		public void advance() {
			super.moveToNextIndex();
		}
		
		@Override
		public long key() {
			return this.hash.extractKey(this.value());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T value() {
			return (T) this.hash._set[this._index];
		}
		
		@Override
		public T setValue(T val) {
			T old = this.value();
			
			long key = this.key();
			long newKey = this.hash.extractKey(val);
			if(key != newKey) {
				throw new UnsupportedOperationException();
			}
			
			this.hash._set[this._index] = val;
			return old;
		}
	}
	
	protected static class SetBackedTLongSetIterator<T> implements TLongIterator {
		
		protected final SetBackedTLongObjectHashMapIterator<T> iterator;
		
		public SetBackedTLongSetIterator(SetBackedTLongObjectHashMapIterator<T> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public long next() {
			this.iterator.next();
			return this.iterator.key();
		}
		
		@Override
		public boolean hasNext() {
			return this.iterator.hasNext();
		}
		
		@Override
		public void remove() {
			this.iterator.remove();
		}
	}
	
	protected static class SetBackedTObjectProcedure<T> implements TObjectProcedure<T> {
		
		protected final AbstractSetBackedTLongObjectHashMap<T> map;
		
		public SetBackedTObjectProcedure(AbstractSetBackedTLongObjectHashMap<T> map) {
			this.map = map;
		}
		
		@Override
		public boolean execute(T value) {
			this.map.add(value);
			return true;
		}
	}
	
	public abstract long extractKey(Object object);
	
	public AbstractSetBackedTLongObjectHashMap() {
		super();
	}
	
	public AbstractSetBackedTLongObjectHashMap(int initialCapacity) {
		super(initialCapacity);
	}
	
	public AbstractSetBackedTLongObjectHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	@Override
	public long getNoEntryKey() {
		return 0;
	}
	
	@Override
	public boolean containsKey(long key) {
		return super.contains(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return super.contains(value);
	}
	
	@Override
	public T put(long key, T value) {
		T old = this.get(key);
		this.add(value);
		return old;
	}
	
	@Override
	public T putIfAbsent(long key, T value) {
		T old = this.get(key);
		if(old == null) {
			this.add(value);
		}
		
		return old;
	}
	
	@Override
	public T remove(long key) {
		return super.removeGet(key);
	}
	
	@Override
	public void putAll(Map<? extends Long, ? extends T> map) {
		super.addAll(map.values());
	}
	
	@Override
	public void putAll(TLongObjectMap<? extends T> map) {
		map.forEachValue(new SetBackedTObjectProcedure<>(this));
	}
	
	@Override
	public TLongSet keySet() {
		return new KeyView<>(this);
	}
	
	@Override
	public Collection<T> valueCollection() {
		return new ValueView<>(this);
	}
	
	@Override
	public Object[] values() {
		return super.toArray();
	}
	
	@Override
	public T[] values(T[] array) {
		return super.toArray(array);
	}
	
	@Override
	public SetBackedTLongObjectHashMapIterator<T> iterator() {
		return new SetBackedTLongObjectHashMapIterator<>(this);
	}
	
	@Override
	public boolean forEachKey(TLongProcedure procedure) {
		Object[] values = this._set;
		for(int i = values.length; i-- > 0;) {
			Object value = values[i];
			if((value != THashSet.FREE && value != THashSet.REMOVED) && !procedure.execute(this.extractKey(value))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean forEachValue(TObjectProcedure<? super T> procedure) {
		return super.forEach(procedure);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean forEachEntry(TLongObjectProcedure<? super T> procedure) {
		Object[] values = this._set;
		for(int i = values.length; i-- > 0;) {
			Object value = values[i];
			if((value != THashSet.FREE && value != THashSet.REMOVED) && !procedure.execute(this.extractKey(value), (T) value)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void transformValues(TObjectFunction<T, T> function) {
		super.transformValues(function);
	}
	
	@Override
	public boolean retainEntries(TLongObjectProcedure<? super T> procedure) {
		return super.retainEntries(procedure);
	};
	
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder("{");
		
		boolean first = true;
		
		Object[] values = this._set;
		for(int i = values.length; i-- > 0;) {
			if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
				if(first) {
					first = false;
				}else{
					buf.append(",");
				}
				
				buf.append(this.extractKey(values[i]));
				buf.append("=");
				buf.append(values[i]);
			}
		}
		
		buf.append("}");
		return buf.toString();
	}
	
	protected static class KeyView<T> implements TLongSet {
		
		protected final AbstractSetBackedTLongObjectHashMap<T> map;
		
		public KeyView(AbstractSetBackedTLongObjectHashMap<T> map) {
			this.map = map;
		}
		
		public long getNoEntryValue() {
			return this.map.getNoEntryKey();
		}
		
		public int size() {
			return this.map.size();
		}
		
		public boolean isEmpty() {
			return this.map.isEmpty();
		}
		
		public boolean contains(long entry) {
			return this.map.containsKey(entry);
		}
		
		public TLongIterator iterator() {
			return new SetBackedTLongSetIterator<>(new SetBackedTLongObjectHashMapIterator<>(this.map));
		}
		
		public long[] toArray() {
			return this.map.keys();
		}
		
		public long[] toArray(long[] dest) {
			return this.map.keys(dest);
		}
		
		public boolean add(long entry) {
			throw new UnsupportedOperationException();
		}
		
		public boolean remove(long entry) {
			return null != this.map.remove(entry);
		}
		
		public boolean containsAll(Collection<?> collection) {
			for(Object element : collection) {
				if(!this.map.containsKey(((Long) element).longValue())) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean containsAll(TLongCollection collection) {
			if(collection == this) {
				return true;
			}
			
			TLongIterator iter = collection.iterator();
			while(iter.hasNext()) {
				if(!this.map.containsKey(iter.next())) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean containsAll(long[] array) {
			for(long element : array) {
				if(!this.map.containsKey(element)) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean addAll(Collection<? extends Long> collection) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(TLongCollection collection) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(long[] array) {
			throw new UnsupportedOperationException();
		}
		
		public boolean retainAll(Collection<?> collection) {
			boolean modified = false;
			TLongIterator iter = this.iterator();
			while(iter.hasNext()) {
				//noinspection SuspiciousMethodCalls
				if(!collection.contains(Long.valueOf(iter.next()))) {
					iter.remove();
					modified = true;
				}
			}
			
			return modified;
		}
		
		public boolean retainAll(TLongCollection collection) {
			if(this == collection) {
				return false;
			}
			
			boolean modified = false;
			TLongIterator iter = this.iterator();
			while(iter.hasNext()) {
				if(!collection.contains(iter.next())) {
					iter.remove();
					modified = true;
				}
			}
			
			return modified;
		}
		
		public boolean retainAll(long[] array) {
			boolean changed = false;
			Arrays.sort(array);
			Object[] values = this.map._set;
			
			for(int i = values.length; i-- > 0;) {
				if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED) && (Arrays.binarySearch(array, this.map.extractKey(values[i])) < 0)) {
					this.map.removeAt(i);
					changed = true;
				}
			}
			
			return changed;
		}
		
		public boolean removeAll(Collection<?> collection) {
			boolean changed = false;
			for(Object element : collection) {
				if(element instanceof Long) {
					long c = ((Long) element).longValue();
					if(this.remove(c)) {
						changed = true;
					}
				}
			}
			
			return changed;
		}
		
		public boolean removeAll(TLongCollection collection) {
			if(collection == this) {
				this.clear();
				return true;
			}
			
			boolean changed = false;
			TLongIterator iter = collection.iterator();
			while(iter.hasNext()) {
				long element = iter.next();
				if(this.remove(element)) {
					changed = true;
				}
			}
			
			return changed;
		}
		
		public boolean removeAll(long[] array) {
			boolean changed = false;
			for(int i = array.length; i-- > 0;) {
				if(this.remove(array[i])) {
					changed = true;
				}
			}
			
			return changed;
		}
		
		public void clear() {
			this.map.clear();
		}
		
		public boolean forEach(TLongProcedure procedure) {
			return this.map.forEachKey(procedure);
		}
		
		public boolean equals(Object other) {
			if(!(other instanceof TLongSet)) {
				return false;
			}
			
			final TLongSet that = (TLongSet) other;
			if(that.size() != this.size()) {
				return false;
			}
			
			Object[] values = this.map._set;
			for(int i = values.length; i-- > 0;) {
				if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
					if(!that.contains(this.map.extractKey(values[i]))) {
						return false;
					}
				}
			}
			
			return true;
		}
		
		public int hashCode() {
			int hashcode = 0;
			
			Object[] values = this.map._set;
			for(int i = values.length; i-- > 0;) {
				if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
					hashcode += HashFunctions.hash(this.map.extractKey(values[i]));
				}
			}
			
			return hashcode;
		}
		
		public String toString() {
			final StringBuilder buf = new StringBuilder("{");
			
			boolean first = true;
			
			Object[] values = this.map._set;
			for(int i = values.length; i-- > 0;) {
				if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
					if(first) {
						first = false;
					}else{
						buf.append(",");
					}
					
					buf.append(this.map.extractKey(values[i]));
				}
			}
			
			buf.append("}");
			return buf.toString();
		}
	}
	
	protected static class ValueView<T> extends SetBackedView<T> {
		
		public ValueView(AbstractSetBackedTLongObjectHashMap<T> map) {
			super(map);
		}
		
		@SuppressWarnings({"unchecked"})
		public Iterator<T> iterator() {
			return new SetBackedTLongObjectHashMapIterator<T>(this.map) {
				protected T objectAtIndex(int index) {
					return (T) ValueView.this.map._set[index];
				}
			};
		}
		
		public boolean containsElement(T value) {
			return this.map.containsValue(value);
		}
		
		public boolean removeElement(T value) {
			Object[] values = this.map._set;
			
			for(int i = values.length; i-- > 0;) {
				if(values[i] != TObjectHash.FREE && values[i] != TObjectHash.REMOVED) {
					 if(value == values[i] || (null != values[i] && values[i].equals(value))) {
						 this.map.removeAt(i);
						 
						 return true;
					}
				}
			}
			
			return false;
		}
	}
	
	protected static abstract class SetBackedView<T> extends AbstractSet<T> implements Set<T>, Iterable<T> {
		
		protected final AbstractSetBackedTLongObjectHashMap<T> map;
		
		public SetBackedView(AbstractSetBackedTLongObjectHashMap<T> map) {
			this.map = map;
		}
		
		public abstract Iterator<T> iterator();
		
		public abstract boolean removeElement(T key);
		public abstract boolean containsElement(T key);
		
		public boolean add(T obj) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection<? extends T> collection) {
			throw new UnsupportedOperationException();
		}
		
		@SuppressWarnings({"unchecked"})
		public boolean contains(Object key) {
			return this.containsElement((T) key);
		}
		
		@SuppressWarnings({"unchecked"})
		public boolean remove(Object o) {
			return this.removeElement((T) o);
		}
		
		public void clear() {
			this.map.clear();
		}
		
		public int size() {
			return this.map.size();
		}
		
		public Object[] toArray() {
			Object[] result = new Object[this.size()];
			Iterator<T> e = this.iterator();
			for(int i = 0; e.hasNext(); i++) {
				result[i] = e.next();
			}
			
			return result;
		}
		
		@SuppressWarnings({"unchecked"})
		public <V> V[] toArray(V[] a) {
			int size = this.size();
			if(a.length < size) {
				a = (V[]) Array.newInstance(a.getClass().getComponentType(), size);
			}
			
			Iterator<T> it = this.iterator();
			Object[] result = a;
			for(int i = 0; i < size; i++) {
				result[i] = it.next();
			}
			
			if(a.length > size) {
				a[size] = null;
			}
			
			return a;
		}
		
		public boolean isEmpty() {
			return this.map.isEmpty();
		}
		
		public boolean retainAll(Collection<?> collection) {
			boolean changed = false;
			Iterator<T> i = this.iterator();
			while(i.hasNext()) {
				if(!collection.contains(i.next())) {
					i.remove();
					changed = true;
				}
			}
			
			return changed;
		}
	}
}