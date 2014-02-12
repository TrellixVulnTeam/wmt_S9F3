package edu.colorado.csdms.wmt.client.data;

import java.util.Vector;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A GWT JavaScript overlay (JSO) type that describes ports that a WMT component
 * model provides and uses, with "id" and "required" attributes. Declares JSNI
 * methods to access these attributes from a JSON and modify them in memory.
 * 
 * @see <a
 *      href="http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsOverlay.html">http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsOverlay.html</a>
 * @author Mark Piper (mark.piper@colorado.edu)
 */
public class PortJSO extends JavaScriptObject {

  // Overlay types have protected, no-arg, constructors.
  protected PortJSO() {
  }

  /**
   * JSNI method to get the "id" attribute of a Port. If no port is present,
   * null is returned.
   */
  public final native String getId() /*-{
		return (typeof this.id == 'undefined') ? null : this.id;
  }-*/;

  /**
   * JSNI method to get the "required" attribute of a Port. If this attribute is
   * not present, false is returned. Note that the return is a JS boolean, not a
   * J Boolean.
   */
  public final native boolean isRequired() /*-{
		return (typeof this.required == 'undefined') ? false : this.required;
  }-*/;

  /**
   * A non-JSNI method for stringifying the attributes of a Port. Must be final.
   */
  public final Vector<String> toStringVector() {

    Vector<String> retVal = new Vector<String>();
    Boolean isRequired = isRequired();
    retVal.add("id: " + getId());
    retVal.add("required: " + isRequired.toString());
    return retVal;
  }
}