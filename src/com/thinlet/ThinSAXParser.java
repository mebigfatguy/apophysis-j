package com.thinlet;

import java.util.Map;

public interface ThinSAXParser {

	void startElement(String name, Map<String, String> attributes);

	void characters(String text);

	void endElement();

}
