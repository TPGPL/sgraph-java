import java.util.LinkedList;

public class BreadthFirstSearch {
    private final boolean[] visitedNodes;
    private final LinkedList<Node> queue;

    public BreadthFirstSearch(int nodeNumber) {
        if (nodeNumber <= 0)
            throw new IllegalArgumentException("BreadthFirstSearch: The number of nodes in graph must be positive.");

        visitedNodes = new boolean[nodeNumber];
        queue = new LinkedList<>();
    }

    public void run(Node startNode)
    {
        Node parsedNode;

        queue.add(startNode);
        visitedNodes[startNode.getIndex()] = true;

        while(queue.size() != 0)
        {
            parsedNode = queue.removeFirst();

            for (Node n : parsedNode.getConnectedNodes())
            {
                if (!visitedNodes[n.getIndex()])
                {
                    queue.add(n);
                    visitedNodes[n.getIndex()] = true;
                }
            }
        }
    }

    public boolean hasNotVisitedNode()
    {
        for (boolean visitedNode : visitedNodes) {
            if (!visitedNode)
                return true;
        }

        return false;
    }

    public int getNotVisitedNode()
    {
        for (int i = 0; i < visitedNodes.length; i++)
            if (!visitedNodes[i])
                return i;

        return -1;
    }

    public boolean wasNodeVisited(Node node)
    {
        return visitedNodes[node.getIndex()];
    }
}
