/** AbstractCollectionProxy.java.

	Purpose:
		
	Description:
		
	History:
		3:01:37 PM Dec 25, 2014, Created by jumperchen

Copyright (C) 2014 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.bind.proxy;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.ImmutableElements;
import org.zkoss.bind.sys.FormBinding;
import org.zkoss.bind.sys.SavePropertyBinding;
import org.zkoss.lang.Objects;
import org.zkoss.util.Pair;

/**
 * This class provides a skeletal implementation of the <tt>Collection</tt>
 * 
 * @author jumperchen
 * @since 8.0.0
 */
public abstract class AbstractCollectionProxy<E>
		implements Collection<E>, Proxy, FormProxyObject, Serializable {
	private Collection<E> _cache;
	private static final long serialVersionUID = 20141225142801L;
	private Object _origin;
	protected boolean _dirty;
	protected boolean isImmutableElements;
	//ZK-3185: Enable form validation with reference and collection binding
	private ProxyNode _node;

	public AbstractCollectionProxy(Collection<E> origin, Annotation[] callerAnnots) {
		_origin = origin;
		_cache = initCache();
		if (callerAnnots != null) {
			for (Annotation annot : callerAnnots) {
				if (annot.annotationType().isAssignableFrom(ImmutableElements.class)) {
					isImmutableElements = true;
					break;
				}
			}
		}
	}

	protected abstract Collection<E> initCache();

	@SuppressWarnings("unchecked")
	protected <T extends Collection<E>> T getCache() {
		return (T) _cache;
	}

	public Object getOriginObject() {
		return _origin;
	}

	protected Object replaceOrigin(Object origin) {
		Object old = _origin;
		_origin = origin;
		return old;
	}

	//F80: formProxyObject support notifyChange with Form.isDirty
	public void onDirtyChange() {
		ProxyHelper.callOnDirtyChange(_node);
	}

	public void onDataChange(Object o) {
		ProxyHelper.callOnDataChange(_node, new Object[]{o, "."});
	}

	protected void setDirty(boolean d) {
		if (_dirty != d) {
			_dirty = d;
			onDirtyChange();
		}
		if (d)
			onDataChange(this);
	}

	public void setHandler(MethodHandler mi) {
		throw new UnsupportedOperationException("Not support!");
	}

	public int size() {
		return _cache.size();
	}

	public boolean isEmpty() {
		return _cache.isEmpty();
	}

	public boolean contains(Object o) {
		Iterator<E> e = iterator();
		if (o == null) {
			while (e.hasNext())
				if (testEquals(e.next(), null))
					return true;
		} else {
			while (e.hasNext())
				if (testEquals(o, e.next()))
					return true;
		}
		return false;
	}

	public void clear() {
		setDirty(true);
		_cache.clear();
	}

	public Iterator<E> iterator() {
		return _cache.iterator();
	}

	public Object[] toArray() {
		return _cache.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return _cache.toArray(a);
	}

	public boolean add(E e) {
		if (_cache.add(createProxyObject(e))) {
			setDirty(true);
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		if (_cache.remove(createProxyObject(o))) {
			setDirty(true);
			return true;
		}
		return false;
	}

	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		Iterator<? extends E> e = c.iterator();
		while (e.hasNext()) {
			if (add(e.next()))
				modified = true;
		}
		if (modified)
			setDirty(true);
		return modified;
	}

	public boolean containsAll(Collection<?> c) {
		Iterator<?> e = c.iterator();
		while (e.hasNext())
			if (!contains(e.next()))
				return false;
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<?> e = iterator();
		c = createProxyObject(c); // use a proxy object to compare
		while (e.hasNext()) {
			if (c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		if (modified)
			setDirty(true);
		return modified;
	}

	public boolean isFormDirty() {
		if (_dirty)
			return true;
		Iterator<?> e = iterator();
		while (e.hasNext()) {
			Object o = e.next();
			if (o instanceof FormProxyObject) {
				if (((FormProxyObject) o).isFormDirty())
					return true;
			}
		}
		return false;
	}

	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> e = iterator();
		c = createProxyObject(c); // use a proxy object to compare
		while (e.hasNext()) {
			if (!c.contains(e.next())) {
				e.remove();
				modified = true;
			}
		}
		if (modified)
			setDirty(true);
		return modified;
	}

	@SuppressWarnings("unchecked")
	public void resetFromOrigin() {
		getCache().clear();
		setDirty(false);
		for (E e : (Collection<E>) getOriginObject()) {
			getCache().add(createProxyObject(e));
		}
	}

	@SuppressWarnings("unchecked")
	public void submitToOrigin(BindContext ctx) {
		Collection<E> origin = ((Collection<E>) getOriginObject());
		origin.clear();
		for (E e : getCache()) {
			if (e instanceof FormProxyObject) {
				FormProxyObject proxy = (FormProxyObject) e;
				proxy.submitToOrigin(ctx);
				origin.add((E) proxy.getOriginObject());
			} else {
				origin.add(e);
			}
		}
		this.setDirty(false);
	}

	protected static boolean testEquals(Object e, Object o) {
		if (e == o) {
			return true;
		}
		if (e instanceof FormProxyObject) {
			Object eo = ((FormProxyObject) e).getOriginObject();
			Object oo = o;
			if (o instanceof FormProxyObject) {
				oo = ((FormProxyObject) o).getOriginObject();
			}
			if (Objects.equals(eo, oo))
				return true;
		} else if (o instanceof FormProxyObject) {
			if (Objects.equals(e, ((FormProxyObject) o).getOriginObject()))
				return true;
		} else {
			if (Objects.equals(e, o))
				return true;
		}
		return false;
	}

	public void setFormOwner(Object owner, FormBinding binding) {
		throw new IllegalAccessError("Not supported");
	}

	private <T extends Object> T createProxyObject(T t) {
		T p = isImmutableElements ? t : ProxyHelper.createProxyIfAny(t);
		if (p instanceof FormProxyObject)
			((FormProxyObject) p).setPath("[$INDEX$]", _node);
		return p;
	}

	public void cacheSavePropertyBinding(String property, SavePropertyBinding s) {
		ProxyHelper.cacheSavePropertyBinding(_node, _node.getProperty() + property, s);
	}

	public Set<Pair<String, SavePropertyBinding>> collectCachedSavePropertyBinding() {
		throw new UnsupportedOperationException("Not support!");
	}

	public void setPath(String property, ProxyNode parent) {
		if (property == null && _node != null) { // means update
			_node.setParent(parent);
		} else {
			_node = new ProxyNodeImpl(property, parent);
			for (E e : _cache) {
				if (e instanceof FormProxyObject)
					((FormProxyObject) e).setPath("[$INDEX$]", _node);
			}
		}
	}
}
