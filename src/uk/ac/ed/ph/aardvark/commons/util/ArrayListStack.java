/* $Id: ArrayListStack.java,v 1.1 2008/04/04 09:52:51 dmckain Exp $
 *
 * Copyright (c) 2003 - 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.aardvark.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

/**
 * Trivial extension of {@link ArrayList} that mixes in the stack-like bits from
 * {@link java.util.Stack} whilst still being non-synchronized.
 *
 * @author  David McKain
 * @version $Revision: 1.1 $
 */
public class ArrayListStack<E> extends ArrayList<E> {
    
    private static final long serialVersionUID = -4103564522108783429L;
    
    public ArrayListStack() {
        super();
    }

    public ArrayListStack(Collection<? extends E> c) {
        super(c);
    }

    public ArrayListStack(int initialCapacity) {
        super(initialCapacity);
    }
    
    //---------------------------------------------------

    public E push(E element) {
        super.add(element);
        return element;
    }
    
    public E peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return get(size()-1);
    }
    
    public E pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        E result = remove(size()-1);
        return result;
    }
    
    public int search(Object object) {
        int index = indexOf(object);
        return index==-1 ? -1 : index+1;
    }
}
