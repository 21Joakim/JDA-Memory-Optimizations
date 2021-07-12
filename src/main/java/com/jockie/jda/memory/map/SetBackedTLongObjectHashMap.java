package com.jockie.jda.memory.map;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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
 * Compatibility between {@link TLongObjectHashSet} and {@link TLongObjectMap}.
 * 
 * Most of this is just copied from {@link TLongObjectHashMap} with some modifications to
 * work together with {@link TLongObjectHashSet}
 */
public class SetBackedTLongObjectHashMap<T> implements TLongObjectMap<T> {
	
	private static class SetBackedTLongObjectHashMapIterator<T> extends TObjectHashIterator<T> implements TLongObjectIterator<T> {
		
		protected SetBackedTLongObjectHashMap<T> hash;
		
		public SetBackedTLongObjectHashMapIterator(SetBackedTLongObjectHashMap<T> hash) {
			super(hash.set);
			
			this.hash = hash;
		}
		
		@Override
		public void advance() {
			super.moveToNextIndex();
		}
		
		@Override
		public long key() {
			return this.hash.set.keyFunction.apply(this.value());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T value() {
			return (T) this.hash.set._set[this._index];
		}
		
		@Override
		public T setValue(T val) {
			T old = this.value();
			this.hash.set._set[this._index] = val;
			return old;
		}
	}
	
	private static class SetBackedTLongSetIterator<T> implements TLongIterator {

		protected SetBackedTLongObjectHashMapIterator<T> iterator;
		
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
	
	private final TObjectProcedure<T> PUT_ALL_PROC = new TObjectProcedure<T>() {
		public boolean execute(T value) {
			SetBackedTLongObjectHashMap.this.set.add(value);
			return true;
		}
	};
	
	protected TLongObjectHashSet<T> set;
	
	public SetBackedTLongObjectHashMap(Function<T, Long> keyFunction) {
		this.set = new TLongObjectHashSet<>(keyFunction);
	}
	
	@Override
	public long getNoEntryKey() {
		return 0;
	}
	
	@Override
	public int size() {
		return this.set.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.set.isEmpty();
	}
	
	@Override
	public boolean containsKey(long key) {
		return this.set.contains(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return this.set.contains(value);
	}
	
	@Override
	public T get(long key) {
		return this.set.get(key);
	}
	
	@Override
	public T put(long key, T value) {
		T old = this.set.get(key);
		this.set.add(value);
		return old;
	}
	
	@Override
	public T putIfAbsent(long key, T value) {
		T old = this.set.get(key);
		if(old == null) {
			this.set.add(value);
		}
		
		return old;
	}
	
	@Override
	public T remove(long key) {
		return this.set.removeGet(key);
	}
	
	@Override
	public void putAll(Map<? extends Long, ? extends T> map) {
		this.set.addAll(map.values());
	}
	
	@Override
	public void putAll(TLongObjectMap<? extends T> map) {
		map.forEachValue(this.PUT_ALL_PROC);
	}
	
	@Override
	public void clear() {
		this.set.clear();
	}
	
	@Override
	public TLongSet keySet() {
		return new KeyView();
	}
	
	@Override
	public long[] keys() {
		return this.set.keys();
	}
	
	@Override
	public long[] keys(long[] array) {
		return this.set.keys(array);
	}
	
	@Override
	public Collection<T> valueCollection() {
		return new ValueView();
	}
	
	@Override
	public Object[] values() {
		return this.set.toArray();
	}
	
	@Override
	public T[] values(T[] array) {
		return this.set.toArray(array);
	}
	
	@Override
	public TLongObjectIterator<T> iterator() {
		return new SetBackedTLongObjectHashMapIterator<>(this);
	}
	
	@Override
	public boolean forEachKey(TLongProcedure procedure) {
		Object[] values = this.set._set;
		for(int i = values.length; i-- > 0;) {
			Object value = values[i];
			if((value != THashSet.FREE && value != THashSet.REMOVED) && !procedure.execute(this.set.extractKey(value))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean forEachValue(TObjectProcedure<? super T> procedure) {
		return this.set.forEach(procedure);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean forEachEntry(TLongObjectProcedure<? super T> procedure) {
		Object[] values = this.set._set;
		for(int i = values.length; i-- > 0;) {
			Object value = values[i];
			if((value != THashSet.FREE && value != THashSet.REMOVED) && !procedure.execute(this.set.extractKey(value), (T) value)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void transformValues(TObjectFunction<T, T> function) {
		this.set.transformValues(function);
	}
	
	@Override
	public boolean retainEntries(TLongObjectProcedure<? super T> procedure) {
		return this.set.retainEntries(procedure);
	}
	
	@Override
	public int hashCode() {
		return this.set.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.set.equals(obj);
	}
	
	@Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        
        boolean first = true;
        
        Object[] values = this.set._set;
        for(int i = values.length; i-- > 0;) {
        	if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
                if(first) first = false;
                else buf.append(",");
                
            	@SuppressWarnings("unchecked")
				long key = this.set.keyFunction.apply((T) values[i]);
                buf.append(key);
                
                buf.append("=");
                buf.append(values[i]);
            }
        }
        
        return buf.toString();
    }
	
    class KeyView implements TLongSet {
    	
        public long getNoEntryValue() {
            return SetBackedTLongObjectHashMap.this.getNoEntryKey();
        }
        
        public int size() {
            return SetBackedTLongObjectHashMap.this.size();
        }
        
        public boolean isEmpty() {
            return SetBackedTLongObjectHashMap.this.isEmpty();
        }
        
        public boolean contains(long entry) {
            return SetBackedTLongObjectHashMap.this.containsKey(entry);
        }
        
        public TLongIterator iterator() {
            return new SetBackedTLongSetIterator<>(new SetBackedTLongObjectHashMapIterator<>(SetBackedTLongObjectHashMap.this));
        }
        
        public long[] toArray() {
            return SetBackedTLongObjectHashMap.this.keys();
        }
        
        public long[] toArray(long[] dest) {
            return SetBackedTLongObjectHashMap.this.keys(dest);
        }
        
        public boolean add(long entry) {
            throw new UnsupportedOperationException();
        }
        
        public boolean remove(long entry) {
            return null != SetBackedTLongObjectHashMap.this.remove(entry);
        }
        
        public boolean containsAll(Collection<?> collection) {
           for(Object element : collection) {
                if(!SetBackedTLongObjectHashMap.this.containsKey(((Long) element).longValue())) {
                    return false;
                }
            }
           
            return true;
        }
        
        public boolean containsAll( TLongCollection collection ) {
            if(collection == this) {
                return true;
            }
            
            TLongIterator iter = collection.iterator();
            while(iter.hasNext()) {
                if(!SetBackedTLongObjectHashMap.this.containsKey(iter.next())) {
                    return false;
                }
            }
            
            return true;
        }
        
        public boolean containsAll(long[] array) {
            for(long element : array) {
                if(!SetBackedTLongObjectHashMap.this.containsKey(element)) {
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
        
        public boolean retainAll( Collection<?> collection ) {
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
            Object[] values = SetBackedTLongObjectHashMap.this.set._set;
            
            for(int i = values.length; i-- > 0;) {
            	@SuppressWarnings("unchecked")
				long key = SetBackedTLongObjectHashMap.this.set.keyFunction.apply((T) values[i]);
                if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED) && (Arrays.binarySearch(array, key) < 0)) {
                	SetBackedTLongObjectHashMap.this.set.removeAt(i);
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
        
        public boolean removeAll( long[] array ) {
            boolean changed = false;
            for(int i = array.length; i-- > 0;) {
                if(this.remove(array[i])) {
                    changed = true;
                }
            }
            
            return changed;
        }
        
        public void clear() {
        	SetBackedTLongObjectHashMap.this.clear();
        }
        
        public boolean forEach(TLongProcedure procedure) {
            return SetBackedTLongObjectHashMap.this.forEachKey(procedure);
        }
        
        public boolean equals(Object other) {
            if(!(other instanceof TLongSet)) {
                return false;
            }
            
            final TLongSet that = (TLongSet) other;
            if(that.size() != this.size()) {
                return false;
            }
            
            Object[] values = SetBackedTLongObjectHashMap.this.set._set;
            for(int i = values.length; i-- > 0;) {
                if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
                	@SuppressWarnings("unchecked")
                	long key = SetBackedTLongObjectHashMap.this.set.keyFunction.apply((T) values[i]);
                    if(!that.contains(key)) {
                        return false;
                    }
                }
            }
            
            return true;
        }
        
        public int hashCode() {
            int hashcode = 0;
            
            Object[] values = SetBackedTLongObjectHashMap.this.set._set;
            for(int i = values.length; i-- > 0;) {
                if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
                	@SuppressWarnings("unchecked")
					long key = SetBackedTLongObjectHashMap.this.set.keyFunction.apply((T) values[i]);
                    hashcode += HashFunctions.hash(key);
                }
            }
            
            return hashcode;
        }
        
        public String toString() {
            final StringBuilder buf = new StringBuilder("{");
            
            boolean first = true;
            
            Object[] values = SetBackedTLongObjectHashMap.this.set._set;
            for(int i = values.length; i-- > 0;) {
            	if((values[i] != THashSet.FREE && values[i] != THashSet.REMOVED)) {
                    if(first) first = false;
                    else buf.append(",");
                    
                	@SuppressWarnings("unchecked")
					long key = SetBackedTLongObjectHashMap.this.set.keyFunction.apply((T) values[i]);
                    buf.append(key);
                }
            }
            
            return buf.toString();
        }
    }
	
	protected class ValueView extends SetBackedView<T> {
		
		@SuppressWarnings({"unchecked"})
		public Iterator<T> iterator() {
			return new SetBackedTLongObjectHashMapIterator<T>(SetBackedTLongObjectHashMap.this) {
				protected T objectAtIndex(int index) {
					return (T) SetBackedTLongObjectHashMap.this.set._set[index];
				}
			};
		}
		
		public boolean containsElement(T value) {
			return SetBackedTLongObjectHashMap.this.containsValue(value);
		}
		
		public boolean removeElement(T value) {
			Object[] values = SetBackedTLongObjectHashMap.this.set._set;
			
			for(int i = values.length; i-- > 0;) {
				if(values[i] != TObjectHash.FREE && values[i] != TObjectHash.REMOVED) {
					 if(value == values[i] || (null != values[i] && values[i].equals(value))) {
						 SetBackedTLongObjectHashMap.this.set.removeAt(i);
						 
						 return true;
					}
				}
			}
			
			return false;
		}
	}
	
	private abstract class SetBackedView<E> extends AbstractSet<E> implements Set<E>, Iterable<E> {
		
		public abstract Iterator<E> iterator();
		
		public abstract boolean removeElement(E key);
		public abstract boolean containsElement(E key);
		
		public boolean add(E obj) {
			throw new UnsupportedOperationException();
		}
		
		public boolean addAll(Collection<? extends E> collection) {
			throw new UnsupportedOperationException();
		}
		
		@SuppressWarnings({"unchecked"})
		public boolean contains(Object key) {
			return this.containsElement((E) key);
		}
		
		@SuppressWarnings({"unchecked"})
		public boolean remove(Object o) {
			return this.removeElement((E) o);
		}
		
		public void clear() {
			SetBackedTLongObjectHashMap.this.clear();
		}
		
		public int size() {
			return SetBackedTLongObjectHashMap.this.size();
		}
		
		public Object[] toArray() {
			Object[] result = new Object[this.size()];
			Iterator<E> e = this.iterator();
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
		
			Iterator<E> it = this.iterator();
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
			return SetBackedTLongObjectHashMap.this.isEmpty();
		}
		
		public boolean retainAll(Collection<?> collection) {
			boolean changed = false;
			Iterator<E> i = this.iterator();
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