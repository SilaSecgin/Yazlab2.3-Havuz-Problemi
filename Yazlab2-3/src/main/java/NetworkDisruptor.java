import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.Viewer;

import utilities.MaxFlowMinCut;
import utilities.Pair;

import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

public class NetworkDisruptor {

	public static Graph graph, graph2;
	public static int[][] capacityArray;
	public static String[][] stepsArray;
	public static FordFulkersonAlgorithm fordFulkersonAlgorithm = new FordFulkersonAlgorithm();
	public static String[] minCutList2 = new String[20];
	public static String[] controlSteps = new String[40];
	public static int[][] stepsArrayCapacity = new int[20][20];
	Timer timer = new Timer();
	public static int s = 0;
	/* Graph renklendirme stringi */
	protected static String styleSheet = "node {" + "   fill-color: pink;" + " size:20px; "
			+ "text-background-mode:none;" + "}" + "node.marked {" + "   fill-color: red;" + "}" + "node:clicked {\n"
			+ "        fill-color: red;\n" + "    }" + "edge {" + "text-color:red;" + "text-alignment:above;"
			+ "fill-color: brown;" + "}";

	/* Graphlarin Yaratilmasi */
	private static void initializeGraph() {
		graph = new MultiGraph("Transportation Network Disruption");
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		graph2 = new MultiGraph("Transportation Network Disruption");
		graph2.addAttribute("ui.stylesheet", styleSheet);
		graph2.addAttribute("ui.quality");
		graph2.addAttribute("ui.antialias");

	}
	/* Graphlarin consola basýlmasý */
	private static void printCapacityArray() {
		for (int i = 1; i <= graph.getNodeCount(); i++) {
			for (int j = 1; j <= graph.getNodeCount(); j++) {
				System.out.printf("%10d", capacityArray[i][j]);
			}
			System.out.println();
		}
	}
	/* Graphlar baðlantýlarýnýn belirlenmesi */
	private static void setLabels() {

		for (Node node : graph) {
			node.addAttribute("ui.label", node.getId());
		}

		for (Node node : graph2) {
			node.addAttribute("ui.label", node.getId());
		}

		for (Edge edge : graph.getEachEdge()) {
			Node nodeFirst = graph.getNode(Character.toString(edge.getId().charAt(0)));
			Node nodeSecond = graph.getNode(Character.toString(edge.getId().charAt(1)));
			double flow = fordFulkersonAlgorithm.getFlow(nodeFirst, nodeSecond);
			double capacity = fordFulkersonAlgorithm.getCapacity(nodeFirst, nodeSecond);
			edge.setAttribute("label", capacity + ", " + flow);

			// fill array!
			capacityArray[Integer.parseInt(nodeFirst.getId())][Integer.parseInt(nodeSecond.getId())] = (int) capacity;
		}
	}
	/* Frame ekranýnda girilen matrix elemanlarýnýn nodes.dgs üzerinden grapha eklenmesi */
	private static void loadFromDgsFile() {
		try {
			FileSource source = FileSourceFactory.sourceFor("data/nodes.dgs");
			source.addSink(graph);
			source.addSink(graph2);
			source.begin("data/nodes.dgs");
			while (source.nextEvents()) {
			}
			;
			source.end();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/* Graph baðlantýlarýnýn aðýrlýklarýnýn belirlenmesi */
	private static void setCapacityFromEdges() {
		for (Edge edge : graph.getEachEdge()) {
			String nodeFirst = Character.toString(edge.getId().charAt(0));
			String nodeSecond = Character.toString(edge.getId().charAt(1));
			double weight = Double.parseDouble(edge.getAttribute("weight").toString());
			fordFulkersonAlgorithm.setCapacity(nodeFirst, nodeSecond, weight);
		}

	}
	/* Graph baðlantý kapasitelerinin 0 lanmasý */
	private static void initializeCapacityArray() {
		capacityArray = new int[graph.getNodeCount() + 1][graph.getNodeCount() + 1];
		for (int i = 1; i < graph.getNodeCount(); i++) {
			for (int j = 1; j < graph.getNodeCount(); j++) {
				capacityArray[i][j] = 0;
			}
		}
	}
	
	/* Graph min-cutlarýnýn renklendirilmesi */
	public static void colorEdges(Set<Pair> cutSet) {
		Iterator<Pair> iterator = cutSet.iterator();
		while (iterator.hasNext()) {
			Pair pair = iterator.next();
			graph.getEdge(pair.source + "" + pair.destination).addAttribute("ui.style", "fill-color: rgb(0,255,0);");

		}
	}
	
	/* Graph Stream in baþlatýlmasý */
	public void start(int sourceT, int sink) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		initializeGraph();
		loadFromDgsFile();

		fordFulkersonAlgorithm.init(graph, Integer.toString(sourceT), Integer.toString(sink));

		setCapacityFromEdges();
		fordFulkersonAlgorithm.compute();
		System.out.println("Max flow is : " + fordFulkersonAlgorithm.getMaximumFlow());

		initializeCapacityArray();

		setLabels();

		printCapacityArray();

		MaxFlowMinCut maxFlowMinCut = new MaxFlowMinCut(graph.getNodeCount());
		maxFlowMinCut.maxFlowMinCut(capacityArray, sourceT, sink);

		maxFlowMinCut.printCutSet();

		minCutList2 = MaxFlowMinCut.minCutList;

		stepsArray = MaxFlowMinCut.stepsArray;

		stepsArrayCapacity = MaxFlowMinCut.stepsArrayCapacity;

		graph2.display();
		
		/* Timer ile birlikte sýrayla adýmlarýn gösterilmesi ve son graphýn gösterilmesi */
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {

				removeOldEdge();
				/* Graph2 adýmlarýnýn gösterilmesi */
				for (int i = 0; i < stepsArray.length; i++) {
					for (int j = stepsArray.length - 1; j >= 0; j--) {
						if (stepsArray[i][j] != null) {
							graph2.addEdge(stepsArray[i][j], stepsArray[i][j].substring(0, 1),
									stepsArray[i][j].substring(1, 2), true);
							/* Graph2 adýmlarýný gösterirken aðýrlýðýn da yazýlmasý */
							for (Edge edge : graph2.getEachEdge()) {

								// buffer eðer daha önce oluþmuþsa buffera bir daha girme
								String buffer = "";
								String control = Character.toString(edge.getId().charAt(0))
										+ Character.toString(edge.getId().charAt(1));
								for (int k = 0; k < stepsArray.length; k++) {
									if (control.equals(controlSteps[k])) {
										buffer = "true";
										break;
									}
								}

								if (!buffer.equals("true")) {
									controlSteps[s + 1] = Character.toString(edge.getId().charAt(0))
											+ Character.toString(edge.getId().charAt(1));
									
									edge.setAttribute("label", stepsArrayCapacity[i][j]);

								}
								s++;
							}/* Graph2 adýmlarýný gösterirken her 3 saniye de bir kez edge in çizilmesi */
							try {
								Thread.sleep(6000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					/* Graph2 adýmlarýný gösterirken diðer adýma geçerken önceki f baðlantýlarýnýn kaldýrýlmasý */
					for (int j = 0; j < stepsArray.length; j++) {
						if (stepsArray[i][j] != null) {
							graph2.removeEdge(stepsArray[i][j]);
							controlSteps = new String[40];
						}
					}
				}
				/* Graphýn gösterilmesi */
				graph.display();
				timer.cancel();
			};
		};
		timer.schedule(timerTask, 3000, 5000);

		/* Min-cut edgelerinin renklendirilmesi */
		Set<Pair> cutSet = maxFlowMinCut.getCutSet();
		colorEdges(cutSet);

	}
	/* Graph2 üzerindeki baðlantýlarýn kaldýrýlmasý */
	protected void removeOldEdge() {
		// TODO Auto-generated method stub

		Frame frame = new Frame();

		for (int i = 0; i < frame.matrixSize; i++) {
			for (int j = 0; j < frame.matrixSize; j++) {
				if (!frame.MatrixArray[i][j].getText().equals("")) {
					graph2.removeEdge(String.valueOf(i + 1) + String.valueOf(j + 1));
				}
			}
		}

	}

}
