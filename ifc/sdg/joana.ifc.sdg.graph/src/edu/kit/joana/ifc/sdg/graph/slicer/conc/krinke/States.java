/**
 * This file is part of the Joana IFC project. It is developed at the
 * Programming Paradigms Group of the Karlsruhe Institute of Technology.
 *
 * For further details on licensing please read the information at
 * http://joana.ipd.kit.edu or contact the authors.
 */
package edu.kit.joana.ifc.sdg.graph.slicer.conc.krinke;

import edu.kit.joana.ifc.sdg.graph.slicer.graph.Context;
import edu.kit.joana.ifc.sdg.graph.slicer.graph.DynamicContextManager.DynamicContext;

/** Represents execution states of threads.
 *
 * -- Created on December 5, 2005
 *
 * @author  Dennis Giffhorn
 */
public class States<C extends Context<C>> implements Cloneable {
    // the execution states
    private C[] states;

    /** Creates a new instance of States.
     * @param  TAmount of threads.
     */
    public States(C[] initial) {
        states = initial;
    }

    /** Clones these States.
     * @return  A deep-copy of this States.
     */
    public States<C> clone() {
        States<C> clone = new States<>(states.clone());

        for (int i = 0; i < states.length; i++) {
        	C cl = states[i].copy();

            clone.set(i, cl);
        }

        return clone;
    }

    /** Creates new States:
     * The current States are cloned.
     * In the clone, the execution state of the given thread is set to the given Context.
     *
     * @param thread  The thread.
     * @param context  The new execution state.
     * @return  The updated execution states.
     */
    public States<C> modifyStates(int thread, C context) {
        States<C> clone = clone();

        clone.set(thread, context);

        return clone;
    }

    /** Returns the execution state of the given thread.
     * @param thread  The thread.
     * @return  Its execution state.
     */
    public C state(int thread) {
        return states[thread];
    }

    /** Sets the execution state of the given thread to the given Context.
     *
     * @param thread  The thread.
     * @param context  The new execution state.
     */
    public void set(int thread, C newState) {
        states[thread] = newState;
    }

    /** Returns the size of the states.
     * @return  The size equals the amount of threads.
     */
    public int size() {
        return states.length;
    }

    /** Returns the execution state of the given thread.
     * @param thread  The thread.
     * @return  Its execution state.
     */
    public C get(int thread) {
        return states[thread];
    }

	/**
	 * Compares this States with given States.
	 * 
	 * @param s
	 *            The States to compare with.
	 * @return true if 'this' and s are equal.
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		else if (!(o instanceof States)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		States<C> s = (States<C>) o;

		if (s.size() != size()) {
			return false;
		}

		for (int i = 0; i < size(); i++) {
			if (!s.get(i).equals(get(i))) {
				return false;
			}
		}

		return true;
	}

    /** Returns a textual representation of this States.
     * @return A textual representation of this States.
     */
    public String toString() {
        String str = "";

        for (int i = 0; i < states.length; i++) {
            str += "Thread "+i+" : "+states[i]+"\n";
        }

        return str;
    }

    public int hashCode() {
    	int hc = 1;

    	for (Context<C> c : states) {
            hc = 31*hc +(c==null ? 0 : c.hashCode());
        }

    	return hc;
    }
}
