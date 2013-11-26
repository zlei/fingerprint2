package com.lighthousesignal.lsslib.pathfinding;

import java.util.Vector;

public class Path {

	private Vector<MapNode> nodes;

	public Path() {
		nodes = new Vector<MapNode>();
	}

	public void addNode(int id, double pointX, double pointY) {
		MapNode n = new MapNode();
		n.id = id;
		n.px = pointX;
		n.py = pointY;
		nodes.add(n);
	}
	
	public int getLength() {
		return nodes.size();
	}
	
	public Vector<MapNode> getNodes() {
		return nodes;
	}

	
}
