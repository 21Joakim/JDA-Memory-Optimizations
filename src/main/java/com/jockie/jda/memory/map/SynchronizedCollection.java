package com.jockie.jda.memory.map;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * A clone of gnu.trove.impl.sync.SynchronizedCollection with public visibility
 */
public class SynchronizedCollection<T> implements Collection<T>, Serializable {
	
	private static final long serialVersionUID = -2012322470150332518L;
	
	protected final Collection<T> collection;
	protected final Object mutex;
	
	public SynchronizedCollection(Collection<T> collection, Object mutex) {
		this.collection = collection;
		this.mutex = mutex;
	}
	
	@Override
	public int size() {
		synchronized(this.mutex) {
			return this.collection.size();
		}
	}
	
	@Override
	public boolean isEmpty() {
		synchronized(this.mutex) {
			return this.collection.isEmpty();
		}
	}
	
	@Override
	public boolean contains(Object o) {
		synchronized(this.mutex) {
			return this.collection.contains(o);
		}
	}
	
	@Override
	public Object[] toArray() {
		synchronized(this.mutex) {
			return this.collection.toArray();
		}
	}
	
	@Override
	public <T2> T2[] toArray(T2[] a) {
		synchronized(this.mutex) {
			return this.collection.toArray(a);
		}
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.collection.iterator(); // Must be manually synched by user!
	}

	public boolean add(T e) {
		synchronized(this.mutex) {
			return this.collection.add(e);
		}
	}
	
	public boolean remove(Object o) {
		synchronized(this.mutex) {
			return this.collection.remove(o);
		}
	}

	public boolean containsAll(Collection<?> collection) {
		synchronized(this.mutex) {
			return this.collection.containsAll(collection);
		}
	}
	
	public boolean addAll(Collection<? extends T> collection) {
		synchronized(this.mutex) {
			return this.collection.addAll(collection);
		}
	}
	
	public boolean removeAll(Collection<?> collection) {
		synchronized(this.mutex) {
			return this.collection.removeAll(collection);
		}
	}
	
	public boolean retainAll(Collection<?> collection) {
		synchronized(this.mutex) {
			return this.collection.retainAll(collection);
		}
	}
	
	public void clear() {
		synchronized(this.mutex) {
			this.collection.clear();
		}
	}
	
	public String toString() {
		synchronized(this.mutex) {
			return this.collection.toString();
		}
	}
	
	private void writeObject(ObjectOutputStream s) throws IOException {
		synchronized(this.mutex) {
			s.defaultWriteObject();
		}
	}
}