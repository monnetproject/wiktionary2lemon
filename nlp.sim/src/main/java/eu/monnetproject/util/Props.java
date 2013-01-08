package eu.monnetproject.util;

import java.util.*;

/**
 * Utility function to syntactically sugar properties for OSGi.
 * This allows you to create a property map as follows
 * <code>Props.prop("key1","value1")</code><br/>
 * <code>     .prop("key2","value2")</code>
 */
public final class Props {
	public static PropsMap prop(String key, Object value) {
		PropsMap pm = new PropsMap();
		pm.put(key,value);
		return pm;
	}
	
	public static class PropsMap extends Hashtable<String,Object> {
		
		public PropsMap prop(String key, Object value) {
			put(key,value);
			return this;
		}
	}
}