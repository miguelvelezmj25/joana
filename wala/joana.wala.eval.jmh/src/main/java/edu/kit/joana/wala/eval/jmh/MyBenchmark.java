/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.kit.joana.wala.eval.jmh;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import edu.kit.joana.util.collections.SimpleVector;
import edu.kit.joana.util.graph.AbstractJoanaGraph;
import edu.kit.joana.util.graph.IntegerIdentifiable;
import edu.kit.joana.util.graph.KnowsVertices;
import edu.kit.joana.wala.core.graphs.DominanceFrontiers;
import edu.kit.joana.wala.core.graphs.NTICDGraphPostdominanceFrontiers;
import edu.kit.joana.wala.util.WriteGraphToDot;

@Fork(value = 1, jvmArgsAppend = "-Xss128m")
public class MyBenchmark {
	
	@SuppressWarnings("serial")
	public static class EntryExitGraph extends AbstractJoanaGraph<Node, Edge> {
		public EntryExitGraph(EdgeFactory<Node, Edge> edgeFactory, int n) {
			super(edgeFactory, () -> new SimpleVector<>(0, n), Edge.class);
		}
		public Node entry;
		public Node exit;
	}
	
	@SuppressWarnings("serial")
	public static class CDG extends AbstractJoanaGraph<Node, Edge> {

	    public static CDG build(EntryExitGraph cfg, Node entry, Node exit, EdgeFactory<Node, Edge> edgeFactory) {
	        final CDG cdg = new CDG(cfg, entry, exit, edgeFactory);

	        cdg.build();

	        return cdg;
	    }

	    private final DirectedGraph<Node, Edge> cfg;
	    private final Node entry;
	    private final Node exit;

	    private CDG(final DirectedGraph<Node, Edge> cfg, Node entry, Node exit, EdgeFactory<Node, Edge> edgeFactory) {
	        super(edgeFactory, () -> new LinkedHashMap<>(cfg.vertexSet().size()), Edge.class);
	        this.cfg = cfg;
	        this.entry = entry;
	        this.exit = exit;
	    }

	    private void build() {
	        final DirectedGraph<Node, Edge> reversedCfg = new EdgeReversedGraph<Node, Edge>(cfg);
	        final DominanceFrontiers<Node, Edge> frontiers = DominanceFrontiers.compute(reversedCfg, exit);

	        for (final Node node : cfg.vertexSet()) {
	            addVertex(node);
	        }

	        for (final Node node : cfg.vertexSet()) {
	            for (final Node domFrontier : frontiers.getDominanceFrontier(node)) {
	                if (node != domFrontier) {
	                    // no self dependencies
	                    addEdge(domFrontier, node);
	                }
	            }
	        }
	    }
	}
	

	public static final class Node implements IntegerIdentifiable {
		private int id;
		
		public Node(int id) {
			this.id = id;
		}
		@Override
		public int getId() {
			return id;
		}
		@Override
		public String toString() {
			return Integer.toString(id);
		}
	}
	
	public static final class Edge implements KnowsVertices<Node> {
		private Node source;
		private Node target;
		
		public Edge(Node source, Node target) {
			this.source = source;
			this.target = target;
		}
		@Override
		public Node getSource() {
			return source;
		}
		@Override
		public Node getTarget() {
			return target;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}
		@Override
		public String toString() {
			return "(" + source.toString() + ", " + target.toString() + ")";
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Edge))
				return false;
			Edge other = (Edge) obj;
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
		
		
	}
	
	static void addEntryExit(EntryExitGraph graph, VertexFactory<Node> vertexFactory) {
		{
			final KosarajuStrongConnectivityInspector<Node, Edge> sccInspector = new KosarajuStrongConnectivityInspector<>(graph);
			final List<Set<Node>> sccs = sccInspector.stronglyConnectedSets();
			
			final Node exitNode = vertexFactory.createVertex();
			graph.addVertex(exitNode);
			graph.exit = exitNode;
			
			for (Set<Node> scc : sccs) {
				final boolean isSink = ! scc.stream().anyMatch(
					v -> graph.outgoingEdgesOf(v).stream().anyMatch(
						e -> !scc.contains(graph.getEdgeTarget(e))
					)
				);
				if (isSink) {
					final Node s = scc.iterator().next();
					graph.addEdge(s, exitNode);
				}
			}
		}
		{
			final KosarajuStrongConnectivityInspector<Node, Edge> sccInspector = new KosarajuStrongConnectivityInspector<>(graph);
			final List<Set<Node>> sccs = sccInspector.stronglyConnectedSets();
			
			final Node entryNode = vertexFactory.createVertex();
			graph.addVertex(entryNode);
			graph.entry = entryNode;
			
			for (Set<Node> scc : sccs) {
				final boolean isSink = ! scc.stream().anyMatch(
					v -> graph.incomingEdgesOf(v).stream().anyMatch(
						e -> !scc.contains(graph.getEdgeSource(e))
					)
				);
				if (isSink) {
					final Node s = scc.iterator().next();
					graph.addEdge(entryNode, s);
				}
			}
		}
		
	}
	
	public abstract static class RandomGraphs<G> {
		public final int seed = 42;
		
		public G randomGraph;
		
		public final EdgeFactory<Node, Edge> edgeFactory = new EdgeFactory<Node, Edge>() {
			@Override
			public Edge createEdge(Node sourceVertex, Node targetVertex) {
				return new Edge(sourceVertex, targetVertex);
			}
		};
		
		public final VertexFactory<Node> vertexFactory = new VertexFactory<MyBenchmark.Node>() {
			private int id = 0;
			@Override
			public Node createVertex() {
				return new Node(id++);
			}
		};
		
		public final int nrEdges(int nrNodes) {
			return (int)(((double)nrNodes) * 1.5);
		}
		
		public void dumpGraph(int n, DirectedGraph<Node, Edge> graph) {
			final String cfgFileName = WriteGraphToDot.sanitizeFileName(this.getClass().getSimpleName()+"-" + n + "-cfg.dot");
			try {
				WriteGraphToDot.write(graph, cfgFileName, e -> true, v -> Integer.toString(v.getId()));
			} catch (FileNotFoundException e) {
			}
		}

	}
	@State(Scope.Benchmark)
	public static class RandomGraphsArbitrary extends RandomGraphs<DirectedGraph<Node, Edge>> {
		@Param({"400000", "8000", "12000", "16000", "20000", "24000", "28000", "32000", "36000", "40000"})
		public int n;
		
		@Setup(Level.Trial)
		public void doSetup() {
			final Random random = new Random(seed + n);
			final RandomGraphGenerator<Node, Edge> generator = new RandomGraphGenerator<>(n, nrEdges(n), random.nextLong());
			@SuppressWarnings("serial")
			final DirectedGraph<Node, Edge> graph = new AbstractJoanaGraph<Node, Edge>(edgeFactory, () -> new SimpleVector<>(0, n), Edge.class) {};
			generator.generateGraph(graph, vertexFactory, null);
			
			randomGraph = graph;
			dumpGraph(n, graph);
		}
	}
	
	@State(Scope.Benchmark)
	public static class RandomGraphsWithUniqueExitNode extends RandomGraphs<EntryExitGraph> {
		@Param({"400000", "8000", "12000", "16000", "20000", "24000", "28000", "32000", "36000", "40000"})
		public int n;

		@Setup(Level.Trial)
		public void doSetup() {
			final Random random = new Random(n + seed);
			final RandomGraphGenerator<Node, Edge> generator = new RandomGraphGenerator<>(n, nrEdges(n), random.nextLong());
			final EntryExitGraph graph = new EntryExitGraph(edgeFactory, n + 2);
			generator.generateGraph(graph, vertexFactory, null);
			
			addEntryExit(graph, vertexFactory);
			
			randomGraph = graph;
			dumpGraph(n, graph);
		}
	}
	
	//@Benchmark
	@Warmup(iterations = 1, time = 5)
	@Measurement(iterations = 1, time = 5)
	@BenchmarkMode(Mode.AverageTime)
	public void testRandom(RandomGraphsArbitrary randomGraphs, Blackhole blackhole) {
		final DirectedGraph<Node, Edge> graph = randomGraphs.randomGraph;
		blackhole.consume(NTICDGraphPostdominanceFrontiers.compute(graph, randomGraphs.edgeFactory, Edge.class));
	}
	
	//@Benchmark
	@Warmup(iterations = 1, time = 5)
	@Measurement(iterations = 1, time = 5)
	@BenchmarkMode(Mode.AverageTime)
	public void testClassicCDGForRandomWithUniqueExitNode(RandomGraphsWithUniqueExitNode randomGraphs, Blackhole blackhole) {
		final EntryExitGraph graph = randomGraphs.randomGraph;
		blackhole.consume(CDG.build(graph, graph.entry, graph.exit, randomGraphs.edgeFactory));
	}
	
	@Benchmark
	@Warmup(iterations = 1, time = 5)
	@Measurement(iterations = 1, time = 5)
	@BenchmarkMode(Mode.AverageTime)
	public void testNTICDGraphPostdominanceFrontiersForRandomWithUniqueExitNode(RandomGraphsWithUniqueExitNode randomGraphs, Blackhole blackhole) {
		final EntryExitGraph graph = randomGraphs.randomGraph;
		blackhole.consume(NTICDGraphPostdominanceFrontiers.compute(graph, randomGraphs.edgeFactory, Edge.class));
	}
	

	public static void mainDebug(String[] args) throws RunnerException {
		final Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
		final RandomGraphsWithUniqueExitNode randomGraphs = new RandomGraphsWithUniqueExitNode();
		randomGraphs.doSetup();
		new MyBenchmark().testClassicCDGForRandomWithUniqueExitNode(                      randomGraphs, blackhole);
		new MyBenchmark().testNTICDGraphPostdominanceFrontiersForRandomWithUniqueExitNode(randomGraphs, blackhole);
	}
	
	//public static void mainPrintParam(String[] args) {
	public static void main(String[] args) {
		final int nr     = 10;
		final int stride = 4000; 
		boolean isFirst = true;
		System.out.print("@Param({");
		for (int i = 1; i <= nr; i++) {
			if (!isFirst) System.out.print(", ");
			isFirst = false;
			System.out.print("\""+(i*stride)+"\"");
		}
		System.out.println("})");
	}

	
	public static void mainManual(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(MyBenchmark.class.getSimpleName())
			.forks(1)
			.build();
		new Runner(opt).run();
	}
}
