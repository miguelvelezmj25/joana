/**
 * This file is part of the Joana IFC project. It is developed at the
 * Programming Paradigms Group of the Karlsruhe Institute of Technology.
 *
 * For further details on licensing please read the information at
 * http://joana.ipd.kit.edu or contact the authors.
 */
package joana.api.testdata.javannotations;

import edu.kit.joana.ui.annotations.Sink;
import edu.kit.joana.ui.annotations.Source;

/**
 * @autho Martin Hecker <martin.hecker@kit.edu>
 */
public class MethodParameterAnnotationsSink2 {
	
	@Source(level = "high")
	static int in;
	
	
	public static int foo(A rofl, @Sink(level="low") int out) {
		return rofl.x;
	}
	
	public static int bar() {
		return in;
	}
	
	static class A {
		int x;
	}
	
	public static void main(String[] args) {
		foo(new A(), bar());
	}

}
