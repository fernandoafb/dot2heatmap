package com.fbraz.dot2heatmap.src;

public class ColorClass {
	
	public String color;
	public double startEdge;
	public double endEdge;
	public double startNode;
	public double endNode;
	
	public ColorClass(String c, double startEdge, double endEdge, double startNode, double endNode) {
		color = c;
		this.startEdge = startEdge;
		this.endNode = endNode;
		this.startNode = startNode;
		this.endEdge = endEdge;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Color: ").append(color).append("\n");
		sb.append("Node Start: ").append(startNode);
		sb.append(" - Node End: ").append(endNode).append("\n");
		sb.append("Edge Start: ").append(startEdge);
		sb.append(" - Edge End: ").append(endEdge).append("\n");
		return sb.toString();
	}

}
