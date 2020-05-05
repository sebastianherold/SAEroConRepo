package org.processmining.log.csv;

import java.io.IOException;

public interface ICSVWriter {

	void writeNext(String[] value);
	
	void close() throws IOException;
	
}
