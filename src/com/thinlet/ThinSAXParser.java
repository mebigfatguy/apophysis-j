package	com.thinlet;

import java.util.Hashtable;

public	interface	ThinSAXParser	{

void startElement(String name, Hashtable attributes);
void characters(String text);
void endElement();

}
