package types;

import java.util.List;

import ast.Node;

import coco.Location;

public class TypeList {
	
	private Type[] types;
	
	public int size() {
		return types.length;
	}
	
	public Type get(int index) {
		return types[index];
	}
	
	@Override
	public String toString() {
		StringBuilder list = new StringBuilder();
		list.append("TypeList(");
		boolean first = true;
		for(Type type: types) {
			if(first) {
				first = false;
			} else {
				list.append(",");
			}
			list.append(type);
		}
		list.append(")");
		return list.toString();
	}
	
	public static TypeList fromList(List<Node> list) {
		TypeList typeList = new TypeList();
		typeList.types = new Type[list.size()];
		for(int i=0; i<list.size(); ++i) {
			typeList.types[i] = list.get(i).getType();
		}
		return typeList;
	}
	
	public static TypeList fromString(String str, Location location) {
		if(str.charAt(0) == '(') {
			str = str.substring(1);
		}
		if(str.charAt(str.length()-1) == ')') {
			str = str.substring(0, str.length()-1);
		}
		
		String[] types = str.split(",");
		if(str.equals("")) {
			types = new String[0];
		}
		
		TypeList typeList = new TypeList();
		typeList.types = new Type[types.length];
		for(int t=0; t<types.length; ++t) {
			typeList.types[t] = Type.fromString(types[t], location);
		}
		return typeList;
	}
	
}
