package eu.monnetproject.stl;

import java.util.LinkedList;

public class Test2 {

	public static Iterable<String> getList() {
		LinkedList<String> linkedList = new LinkedList<String>();
		linkedList.add("the first");
		linkedList.add("the last");
		linkedList.add("eternity");
		return linkedList;
	}
	
	public static void main(String[] args) {
		Iterable<String> list = getList();
		for(String el:list)
			System.out.println(el);
	}
	
}
