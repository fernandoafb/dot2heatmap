package com.fbraz.dot2heatmap.src;

import java.util.ArrayList;

public class Value {
	
	public String name;
	public ArrayList<String> value;
	public ValueType type;
	public String color;
	
	public Value() {
		value = new ArrayList<String>();
	}
	
	public Value(String n, String v, ValueType t) {
		this();
		name = n;
		value.add(v);
		type = t;
	}
	
	public Value(String n, String[] v, ValueType t) {
		this();
		name = n;
		for (int i = 0; i < v.length; i++) {
			value.add(v[i].trim());
		}
		type = t;
	}
	
}
