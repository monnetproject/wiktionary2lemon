package eu.monnetproject.webaccess.dictionary.wiktionary.util;
import java.util.*;

/**
 * Provides a number of Perl like functions for string manipulation
 *
 * @author John McCrae
 */
public class Strings {
    /** The end of record of token 
     * @see #chomp(String)
     */
    public static char inputRecordSeperator = '\n';
    
    /** Convert the first character to lower case */
    public static String toLCFirst(String s) {
        if(s.length() == 0)
            return s;
        return s.substring(0,1).toLowerCase() + s.substring(1);
    }
    
    /** Convert the first character to upper case */
    public static String toUCFirst(String s) {
        if(s.length() == 0)
            return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
    
    /** Remove the final character if it is inputRecordSeperator */
    public static String chomp(String s) {
        if(s.length() == 0)
            return s;
        if(s.charAt(s.length() - 1) == inputRecordSeperator)
            return s.substring(0,s.length() - 1);
        else
            return s;
    }
    
    /** Remove the final character */
    public static String chop(String s) {
        return s.substring(0,s.length() - 1);
    }
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, String[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, Object[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i].toString();
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, char[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, int[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, long[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, float[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
    
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, double[] strings) {
        String rval = "";
        for(int i = 0; i < strings.length; i++) {
            rval = rval + strings[i];
            if(i < strings.length - 1)
                rval = rval + seperator;
        }
        return rval;
    }
   
    /** Join a set of strings together. Example join(", ", {"Hello","World!}) = "Hello, World!" */
    public static String join(String seperator, Collection strings) {
        String rval = "";
        Iterator<Object> iter = strings.iterator();
        while(iter.hasNext()) {
            rval = rval + iter.next().toString();
            if(iter.hasNext())
                rval = rval + seperator;
        }
        return rval;
    }
    
    /** Escape all regex meta-characters */
    public static String quoteMeta(String s) {
        return s.replaceAll("([\\.\\[\\]\\^\\$\\|\\?\\(\\)\\\\\\+\\{\\}\\*])","\\\\$1");
    }
    
    /** Create a new string by repeating s n-times */
    public static String repString(String s, int n) {
        String rval = "";
        for(int i = 0; i < n; i++) {
            rval = rval + s;
        }
        return rval;
    }
    
	/*
	    * Returns the URI fragment (the part after the '#' or '/')
	    * @param object Object that resolves to a URI w/ toString()
	    * @return fragment after the '#' or the entire string.
	*/
	public static String fragment(Object object) {
	      if (object == null)
	         return null;
	      String uri = object.toString();
	      
	      int pound = uri.lastIndexOf('#');
	      int find = uri.lastIndexOf('/');
	      int colon = uri.lastIndexOf(':'); 
	     	      
	      //As in URI syntax fragment is optional (if that is the case we return the last particle after colon)         
	      return (pound == (-1)) ? (find == (-1)) ? (colon == (-1)) ? uri : uri.substring(colon + 1) : uri.substring(find + 1) : uri.substring(pound + 1);
	}

}
