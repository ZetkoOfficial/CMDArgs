package com.zetkoofficial.cmdargs.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * This object handles the argument parsing and variable setting.
 * @author ZetkoOfficial
 *
 */
public class ArgumentBuilder {
	
	private String[] args;
	private Class<?> holder;
	private List<Field> argumentFields;
	
	/**
	 * @param args The arguments which should be parsed.
	 * @param holder The class which holds the objects annotated by {@link com.zetkoofficial.cmdargs.object.Argument Argument}. 
	 */
	public ArgumentBuilder(String[] args, Class<?> holder) {
		this.args = args;
		this.holder = holder;
		this.argumentFields = new ArrayList<>();
	}
	
	/**
	 * This method sets the fields to the values specified by the arguments {@link java.lang.String String[]}. <br>
	 * It also initializes this object so other methods can be called.
	 * @return This object.
	 */
	public ArgumentBuilder build() {
		Field[] fields = holder.getFields();
		for(Field f : fields) {
			if(f.isAnnotationPresent(Argument.class) && Modifier.isStatic(f.getModifiers())) {
				argumentFields.add(f);
				Argument arg = f.getAnnotationsByType(Argument.class)[0];
				Object o = search(f.getType(), arg);
				if(o != null) {
					try {
						f.set(null, o);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
		}
		
		return this;
	}
	
	/**
	 * @param fieldName The name of the field.
	 * @return The {@link com.zetkoofficial.cmdargs.object.Argument Argument} which is in that field. 
	 */
	public Argument getArgument(String fieldName) {	
		Argument arg = null;
		for(Field f : argumentFields) {
			if(f.getName().equals(fieldName)) {
				arg = f.getAnnotationsByType(Argument.class)[0];
			}
		}
		
		return arg;
	}
	
	private String findValue(Argument arg) {	
		String val = "";
		boolean found = false;
		for(String s : args) {
			if(found) {
				if(s.contains("-"))
					break;
				val += s + " ";
			}
			for(String v : arg.verboseTags()) {
				if(s.equals("--" + v)) {
					found = true;
					continue;
				}
			}
			for(String v : arg.shortTags()) {
				if(s.equals("-" + v)) {
					found = true;
					continue;
				}
			}
		}
		
		if (val.isEmpty()) {
			return null;
		}
		
		return val.substring(0, val.length() -1);
	}
	
	private String getArgumentInfo(Argument arg, Class<?> type) {
		StringBuilder sb = new StringBuilder();
		for(String vTag : arg.verboseTags()) {
			sb.append("--").append(vTag).append(", ");
		}
		for(String sTag : arg.shortTags()) {
			sb.append("-").append(sTag).append(", ");
		}
		sb = sb.delete(sb.length() - 2, sb.length());
		sb.append("\n").append(arg.info()).append("\n").append(type.getSimpleName());
		return sb.toString();
	}
	
	/**
	 * @param fieldName The name of a field with an {@link com.zetkoofficial.cmdargs.object.Argument Argument}.
	 * @return The formated info of the {@link com.zetkoofficial.cmdargs.object.Argument Argument} of the field.
	 */
	public String getArgumentInfo(String fieldName) {
		Class<?> type = null;
		for(Field f : argumentFields) {
			if(f.getName().equals(fieldName)) {
				type = f.getType();
			}
		}
		
		return getArgumentInfo(getArgument(fieldName), type);
	}
	
	private Object search(Class<?> type, Argument arg) {	
		String val = findValue(arg);
		
		if(val != null) {	
			try {
				if(type.isArray()) {
					String[] vals = val.split(" ");
					Object newVal = Array.newInstance(type.getComponentType(), vals.length);
					for(int i = 0; i < vals.length; i++) {
						Array.set(newVal, i, getObjectFromString(vals[i], type.getComponentType()));
					}
					return newVal;
				}else {
					return getObjectFromString(val, type);
				}
				
			} catch (Exception e) {
				System.out.println("Invalid argument type!");
				System.out.printf("Could not parse \"%s\" to %s.\n", val, type.getSimpleName());
				System.out.println();
			}
		}
		
		return null;
	}
	
	private static <T> T getObjectFromString(String s, Class<T> object) throws Exception {
		return object.getConstructor(String.class).newInstance(s);
	}
	
}
