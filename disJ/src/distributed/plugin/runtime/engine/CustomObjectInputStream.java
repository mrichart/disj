package distributed.plugin.runtime.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

public class CustomObjectInputStream extends ObjectInputStream {

	   private ClassLoader loader;


	    /**
	     * @exception IOException Signals that an I/O exception of some
	     * sort has occurred.
	     * @exception StreamCorruptedException The object stream is corrupt.
	     */
	    public CustomObjectInputStream(InputStream in, ClassLoader theLoader) 
		    throws IOException {
		super(in);
		this.loader = theLoader;
	    }
	    
	    @SuppressWarnings("unchecked")
		protected Class resolveClass(ObjectStreamClass aClass) 
		    throws IOException, ClassNotFoundException {      
		if (loader == null) {
		    return super.resolveClass(aClass);
		} else {
		    String name = aClass.getName();
		    // Query the class loader ...    	
		    return Class.forName(name, false, loader);
		}
	    }

}
