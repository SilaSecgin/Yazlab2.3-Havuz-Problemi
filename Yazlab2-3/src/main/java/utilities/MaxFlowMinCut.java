package utilities;

import java.util.*;

public class MaxFlowMinCut {
    private int[] parent;
    private Queue<Integer> queue;
    private int numberOfVertices;
    private boolean[] visited;
    private Set<Pair> cutSet;
    private ArrayList<Integer> reachable;
    private ArrayList<Integer> unreachable;
    public static String[] minCutList = new String[20];
    public static int i=0,j=0,k=0;
    public static String[][] stepsArray = new String[20][20];
    public static int[][] stepsArrayCapacity = new int[20][20];
    
    public MaxFlowMinCut(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.queue = new LinkedList<Integer>();
        parent = new int[numberOfVertices + 1];
        visited = new boolean[numberOfVertices + 1];
        cutSet = new HashSet<Pair>();
        reachable = new ArrayList<Integer>();
        unreachable = new ArrayList<Integer>();

    }

    public boolean bfs(int source, int goal, int graph[][]) {
        boolean pathFound = false;
        int destination, element;
        for (int vertex = 1; vertex <= numberOfVertices; vertex++) {
            parent[vertex] = -1;
            visited[vertex] = false;
        }
        queue.add(source);
        parent[source] = -1;
        visited[source] = true;

        while (!queue.isEmpty()) {
            element = queue.remove();
            destination = 1;
            while (destination <= numberOfVertices) {
                if (graph[element][destination] > 0 && !visited[destination]) {
                	System.out.println(graph[element][destination]+"-element ->"+element+ "dest ->"+destination);
                    parent[destination] = element;
                    queue.add(destination);
                    visited[destination] = true;
                    
                }
                destination++;
            }
        }

        if (visited[goal]) {
        	System.out.println("visited["+goal+"] "+visited[goal]);
            pathFound = true;
        }
        return pathFound;
    }

    public int maxFlowMinCut(int graph[][], int source, int destination) {
        int u, v;
        int maxFlow = 0;
        int pathFlow;
        int[][] residualGraph = new int[numberOfVertices + 1][numberOfVertices + 1];

        for (int sourceVertex = 1; sourceVertex <= numberOfVertices; sourceVertex++) {
            for (int destinationVertex = 1; destinationVertex <= numberOfVertices; destinationVertex++) {
                residualGraph[sourceVertex][destinationVertex] = graph[sourceVertex][destinationVertex];
                System.out.println("residualGraph["+sourceVertex+"]["+destinationVertex+"] "+residualGraph[sourceVertex][destinationVertex]);
                
            }
        }
 
        /*max flow*/
        while (bfs(source, destination, residualGraph)) {
            pathFlow = Integer.MAX_VALUE;
            for (v = destination; v != source; v = parent[v]) {
                u = parent[v];
               
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
                System.out.println("For 1  pathflow -> "+pathFlow+ " u ->"+u+" v ->"+v+" residualGraph["+u+"]["+v+"] -> "+residualGraph[u][v]);
                stepsArray[i][j] = u+""+v;
                stepsArrayCapacity[i][j] = pathFlow;
                j++;
                
            }
            for (v = destination; v != source; v = parent[v]) {
                u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
                System.out.println("For 2  pathflow -> "+pathFlow+" u ->"+u+" v ->"+v+" residualGraph["+v+"]["+u+"] -> "+residualGraph[v][u]+ "residualGraph["+u+"]["+v+"] -> "+residualGraph[u][v]);
            }
            System.out.println("Last  "+pathFlow);
            i++;
            maxFlow += pathFlow;
        }
 
        /*calculate the cut set*/
        for (int vertex = 1; vertex <= numberOfVertices; vertex++) {
            if (bfs(source, vertex, residualGraph)) {
                reachable.add(vertex);
            } else {
                unreachable.add(vertex);
            }
        }
        for (int i = 0; i < reachable.size(); i++) {
            for (int j = 0; j < unreachable.size(); j++) {
                if (graph[reachable.get(i)][unreachable.get(j)] > 0) {
                    cutSet.add(new Pair(reachable.get(i), unreachable.get(j)));
                }
            }
        }
        return maxFlow;
    }

    public void printCutSet() {
        Iterator<Pair> iterator = cutSet.iterator();
        int i =0;
        while (iterator.hasNext()) {
            Pair pair = iterator.next();
            System.out.println(pair.source + "-" + pair.destination);
            minCutList[i]=String.valueOf(pair.source) + "-" + String.valueOf(pair.destination);
            i++;
        }
    }

    public Set<Pair> getCutSet() {
        return cutSet;
    }
}

