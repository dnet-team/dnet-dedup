package eu.dnetlib.pace;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GraphDraw extends JFrame {
    int width;
    int height;

    ArrayList<Node> nodes;
    ArrayList<edge> edges;

    public GraphDraw() { //Constructor
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nodes = new ArrayList<Node>();
        edges = new ArrayList<edge>();
        width = 15;
        height = 15;
    }

    public GraphDraw(String name) { //Construct with label
        this.setTitle(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nodes = new ArrayList<Node>();
        edges = new ArrayList<edge>();
        width = 15;
        height = 15;
    }

    class Node {
        int x, y;
        String name;
        Color color;

        public Node(String myName, int myX, int myY) {
            x = myX;
            y = myY;
            name = myName;
            color = Color.white;
        }

        public Node(String myName, int myX, int myY, Color myColor) {
            x = myX;
            y = myY;
            name = myName;
            color = myColor;
        }
    }

    class edge {
        int i,j;

        public edge(int ii, int jj) {
            i = ii;
            j = jj;
        }
    }

    public void addNode(String name, int x, int y) {
        //add a node at pixel (x,y)
        nodes.add(new Node(name,x,y));
        this.repaint();
    }
    public void addEdge(int i, int j) {
        //add an edge between nodes i and j
        edges.add(new edge(i,j));
        this.repaint();
    }

    public void paint(Graphics g) { // draw the nodes and edges
        FontMetrics f = g.getFontMetrics();
        int nodeHeight = Math.max(height, f.getHeight());
        g.setColor(Color.black);
        for (edge e : edges) {
            g.drawLine(nodes.get(e.i).x, nodes.get(e.i).y,
                    nodes.get(e.j).x, nodes.get(e.j).y);
        }

        for (Node n : nodes) {
            n.name = n.name.substring(0, 20);
            int nodeWidth = Math.max(width, f.stringWidth(n.name)+width/2);
            g.setColor(n.color);
            g.fillRoundRect(n.x-nodeWidth/2, n.y-nodeHeight/2,
                    nodeWidth, nodeHeight, 2, 2);
            g.setColor(Color.black);
            g.drawRoundRect(n.x-nodeWidth/2, n.y-nodeHeight/2, nodeWidth, nodeHeight, 2, 2);

            g.drawString(n.name, n.x-f.stringWidth(n.name)/2,
                    n.y+f.getHeight()/5);
        }
    }
}

class testGraphDraw {
    //Here is some example syntax for the GraphDraw class
    public static void main(String[] args) {
        GraphDraw frame = new GraphDraw("Test Window");

        frame.setSize(400,300);

        frame.setVisible(true);

        frame.addNode("a", 50,50);
        frame.addNode("b", 100,100);
        frame.addNode("longNode", 200,200);
        frame.addEdge(0,1);
        frame.addEdge(0,2);
    }
}
