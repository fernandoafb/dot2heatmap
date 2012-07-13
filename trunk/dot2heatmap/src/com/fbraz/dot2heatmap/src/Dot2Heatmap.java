package com.fbraz.dot2heatmap.src;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dot2Heatmap {

	private String dotFilename;
	private String valuesFilename;
	private String heatmapFilename;
	private ArrayList<String> heatmapFile;
	
	private Boolean dot2texFlag = false;
	private Boolean pdflatexFlag = false;
	private String texFilename;
	
	private String startColor;
	private String endColor;
	private Integer numClasses;
	private ArrayList<ColorClass> classes;
	private ArrayList<Value> values;
	//private ArrayList<Node> nodes;
	private double totalNodes = 0.0;
	//private ArrayList<Edge> edges;
	private double totalEdges = 0.0;
	
	public ValueType type = ValueType.NODE;

	public Dot2Heatmap() {
		heatmapFile = new ArrayList<String>();
		classes = new ArrayList<ColorClass>();
		values = new ArrayList<Value>();
		//nodes = new ArrayList<Node>();
		//edges = new ArrayList<Edge>();
	}
	
	public static Color hex2Rgb(String cor) {
	    return new Color(
	            Integer.valueOf( cor.substring( 0, 2 ), 16 ),
	            Integer.valueOf( cor.substring( 2, 4 ), 16 ),
	            Integer.valueOf( cor.substring( 4, 6 ), 16 ) );
	}
	
	public String hexString(int h) {
		String hs = Integer.toHexString(h);
		if (hs.length()==1) {
			hs = "0"+hs;
		}
		return hs;
	}
	
	public void makeColorClasses() {
		Color start = hex2Rgb(startColor.substring(1));
		int sR = start.getRed();
		int sG = start.getGreen();
		int sB = start.getBlue();
		Color end = hex2Rgb(endColor.substring(1));
		int eR = end.getRed();
		int eG = end.getGreen();
		int eB = end.getBlue();
		int deltaR = (eR-sR)/(numClasses);
		int deltaG = (eG-sG)/(numClasses);
		int deltaB = (eB-sB)/(numClasses);
		double deltaEdge = totalEdges/(numClasses);
		double deltaNode = totalNodes/(numClasses);
		for (int i = 0; i < numClasses; i++) {
			Color c = new Color(sR+(i*deltaR),sG+(i*deltaG),sB+(i*deltaB));
			String str = "#" + hexString(c.getRed());
			str += hexString(c.getGreen());
			str += hexString(c.getBlue());
			ColorClass classe = new ColorClass(str,i*deltaEdge,(i+1)*deltaEdge,i*deltaNode,(i+1)*deltaNode);
			classes.add(classe);
		}
	}

	public boolean readDot() {
		return readDot(dotFilename);
	}

	public Matcher dotMatcher;
	public Pattern dotPattern = Pattern.compile("\\s*.+\\s+\\[.*hmname=\".+\".*];");
	
	public Value findValue(String vname) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).name.equals(vname))
				return values.get(i);
		}
		return null;
	}

	public boolean readDot(String dotFilename) {
		System.out.println("Reading dot file...");
		try {
			FileReader fileReader = new FileReader(new File(dotFilename));
			BufferedReader buffReader = new BufferedReader(fileReader);
			String line = buffReader.readLine();
			String originalLine = line;
			while (line != null) {
				dotMatcher = dotPattern.matcher(line);
				while (dotMatcher.find()) {
					int iend = dotMatcher.end();
					line = line.substring(dotMatcher.start(),dotMatcher.end());
					line = line.substring(line.indexOf("hmname"), line.length());
					line = line.substring(line.indexOf("\"")+1, line.length());
					line = line.substring(0, line.indexOf("\""));
					Value v = findValue(line);
					if (v == null) continue;
					if (originalLine.contains("->")) {
						line = originalLine.substring(0,iend-2) + ",style=\"line width=5pt\",color=\"" + v.color + "\"" +
								originalLine.substring(iend-2,originalLine.length()-1);
					}
					else {
						line = originalLine.substring(0,iend-2) + ",color=\"" + v.color + "\"" +
								originalLine.substring(iend-2,originalLine.length());
					}
				}
				heatmapFile.add(line);
				line = buffReader.readLine();
				originalLine = line;
			}
			buffReader.close();
			fileReader.close();
		} catch (Exception e) {
			System.out.println("\nFAIL [Dot input file]:\tImpossible to open or does not exist.\n");
			return Boolean.FALSE;
		}
		System.out.println("Dot file read.\n");
		return Boolean.TRUE;
	}
	
	private boolean isBlank(String s) {
		return !(s != null && !s.isEmpty());
	}

	public boolean isInteger(String str) {
		return str.matches("^-?[0-9]+(\\.[0-9]+)?$");
	}
	
	public boolean readValues() {
		return readValues(valuesFilename);
	}
	
	public void readValue(String line) {
		if (!isBlank(line)) {
			line = line.trim();
			if (line.equals("#Nodes")) type = ValueType.NODE;
			if (line.equals("#Edges")) type = ValueType.EDGE;
			String[] aux = line.split("=");
			if (aux.length <= 1) return;
			aux[0] = aux[0].trim();
			aux[1] = aux[1].trim();
			aux[1] = aux[1].replace(",",".");
			aux[1] = aux[1].replace("%","");
			if (aux[0].equals("start_color")) { startColor = aux[1]; return; }
			if (aux[0].equals("end_color")) { endColor = aux[1]; return; }
			if (aux[0].equals("num_classes")) { 
				try {
					numClasses = Integer.parseInt(aux[1]);
				}
				catch (Exception e) {
					numClasses = 2;
				}
				return;
			}
			if (aux[1].contains("}")) {
				aux[1] = aux[1].substring(aux[1].indexOf("{")+1,aux[1].lastIndexOf("}"));
				aux[1] = aux[1].trim();
				String[] valuesList = aux[1].split(";");
				values.add(new Value(aux[0], valuesList, type));
				double te = 0.0;
				for (int i = 0; i < valuesList.length; i++) {
					te += Double.parseDouble(valuesList[i]);
					//totalEdges += Double.parseDouble(valuesList[i]);
				}
				if (totalEdges < te) 
					totalEdges = te;
				
				return;
			}
			if (aux[0].startsWith("@")) {
				values.add(new Value(aux[0],aux[1], type));
				if (totalNodes < Double.parseDouble(aux[1])) {
					totalNodes = Double.parseDouble(aux[1])+1;					
				}
				return;
			}
		}		
	}
	
	public boolean readValues(String valuesFilename) {
		System.out.println("Reading values file...");
		try {
			FileReader fileReader = new FileReader(new File(valuesFilename));
			BufferedReader buffReader = new BufferedReader(fileReader);
			String line = buffReader.readLine();
			while (line != null) {
				readValue(line);
				line = buffReader.readLine();
			}
			buffReader.close();
			fileReader.close();
		} catch (Exception e) {
			System.out.println("\nFAIL [Values input file]:\tImpossible to open or does not exist.\n");
			return Boolean.FALSE;
		}
		System.out.println("Values file read.\n");
		return Boolean.TRUE;		
	}

	public boolean writeHeatmap() {
		return writeHeatmap(heatmapFilename);
	}

	public boolean writeHeatmap(String outputFilename) {
		System.out.println("Writing heat map file...");
		try {
			FileWriter fileWriter = new FileWriter(new File(outputFilename));
			PrintWriter printWriter = new PrintWriter(fileWriter,true);
			for (String s : heatmapFile)
				printWriter.println(s);
			printWriter.close();
			fileWriter.close();
		} catch (Exception e) {
			System.out.println("\nFAIL [Heatmap output file]:\tImpossible to open or does not exist.\n");
			return Boolean.FALSE;
		}
		System.out.println("Heat map file written.\n");
		return Boolean.TRUE;
	}

	private void getOpts(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				continue;
			if (args[i].equals("-d") && i + 1 < args.length) {
				dotFilename = args[i + 1];
				continue;
			}
			if (args[i].equals("-dot2tex")) {
				dot2texFlag = true;
				continue;
			}
			if (args[i].equals("-h") && i + 1 < args.length) {
				heatmapFilename = args[i + 1];
				continue;
			}
			if (args[i].equals("-pdflatex")) {
				pdflatexFlag = true;
				continue;
			}
			if (args[i].equals("-v") && i + 1 < args.length) {
				valuesFilename = args[i + 1];
				continue;
			}
		}
	}
	
	public void makeTexFilename() {
		if (dotFilename.contains(".")) {
			texFilename = dotFilename.substring(0, dotFilename.lastIndexOf("."));
		}
		else {
			texFilename = dotFilename;
		}
		texFilename += ".tex";
	}
	
	public String makeDot2TexCmd() {
		StringBuffer sb = new StringBuffer();
		sb.append("dot2tex ").append(heatmapFilename);
		sb.append(" --crop --tikzedgelabels -ftikz -tmath > ");
		sb.append(texFilename);
		return sb.toString();
	}
	
	public String makePdflatexCmd() {
		return "pdflatex " + texFilename;
	}
	
	public boolean dot2tex() {
		makeTexFilename();
		String cmd = makeDot2TexCmd();
		System.out.println("Running dot2tex command...");
		System.out.println(cmd);
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				return Boolean.FALSE;
			}
			String line = null;
			while ( (line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			System.out.println("\nFAIL [dot2tex]:\tFail to run dot2tex.\n");
			return Boolean.FALSE;
		}
		System.out.println("dot2tex command run successfully.\n");
		return Boolean.TRUE;
	}
	
	public boolean pdflatex() {
		String cmd = makePdflatexCmd();
		System.out.println("Running pdflatex command...");
		System.out.println(cmd);
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				return Boolean.FALSE;
			}
		} catch (Exception e) {
			System.out.println("\nFAIL [pdflatex]:\tFail to run pdflatex.\n");
			return Boolean.FALSE;
		}
		System.out.println("pdflatex command run successfully\n");
		return Boolean.TRUE;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		return sb.toString();
	}
	
	public void calcValuesColors() {
		for (Value v : values) {
			for (int i = classes.size()-1; i >= 0; i--) {
				if (v.type.equals(ValueType.NODE) && 
						classes.get(i).startNode <= Double.parseDouble(v.value.get(0)) && classes.get(i).endNode > Double.parseDouble(v.value.get(0))) {
					v.color = classes.get(i).color;
				}
				else if (v.type.equals(ValueType.EDGE) && 
						classes.get(i).startEdge <= (Double.parseDouble(v.value.get(0))+Double.parseDouble(v.value.get(1))) && 
						classes.get(i).endEdge > (Double.parseDouble(v.value.get(0))+Double.parseDouble(v.value.get(1)))) {
					v.color = classes.get(i).color;
				}
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			StringBuffer sb = new StringBuffer();
			sb.append("\nERROR [Invalid arguments] - ");
			sb.append("Format: java -jar dot2heatmap.jar ");
			sb.append("-d <dot_input_file> ");
			sb.append("-h <heatmap_output_file> ");
			sb.append("-v <values_input_file>");
			sb.append("\n\n");
			System.out.println(sb.toString());
			return;
		}

		Dot2Heatmap dot2heatmap = new Dot2Heatmap();
		dot2heatmap.getOpts(args);

		boolean ok = true;
		ok = dot2heatmap.readValues();
		if (ok) {
			dot2heatmap.makeColorClasses();
			dot2heatmap.calcValuesColors();
		}
		ok = dot2heatmap.readDot();
		if (ok) dot2heatmap.writeHeatmap();
		else { return; }
		
		System.out.println("Total Nodes: "+dot2heatmap.totalNodes);
		System.out.println("Total Edges: "+dot2heatmap.totalEdges);
		System.out.println();
		
		for (ColorClass c : dot2heatmap.classes){
			System.out.println(c);
		}

		if (dot2heatmap.dot2texFlag) {
			dot2heatmap.dot2tex();
		}
		else { return; }
		
		if (dot2heatmap.pdflatexFlag) {
			dot2heatmap.pdflatex();
		}
		else { return; }
		
	}

}