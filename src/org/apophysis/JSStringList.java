package org.apophysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class	JSStringList implements Constants {

/*****************************************************************************/
//	FIELDS

public int Count = 0;
public String Strings[] = new String[0];

private Vector lines = new Vector();

/*****************************************************************************/

public JSStringList()
{
}

/*****************************************************************************/

public JSStringList Create()
{
return new JSStringList();
}

/*****************************************************************************/

public void Add(String line)
{
lines.addElement(line);
updateFields();
}

/*****************************************************************************/

private void updateFields()
{
Count = lines.size();
Strings = new String[Count];
for(int i=0;i<Count;i++)
	Strings[i] = (String)lines.elementAt(i);
}

/*****************************************************************************/

public void SaveToFile(String filename)
{
try	{
	PrintWriter w = new PrintWriter(new FileWriter(filename));
	int n = lines.size();
	for(int i=0;i<n;i++)
		w.println((String)lines.elementAt(i));
	w.close();
	}
catch(IOException ex)
	{
	ex.printStackTrace();
	}
}

/*****************************************************************************/

public void LoadFromFile(String filename) throws IOException
{
lines = new Vector();

BufferedReader r = new BufferedReader(new FileReader(filename));
while(true)
	{
	String line = r.readLine();
	if(line==null) break;
	lines.addElement(line);
	}
r.close();

updateFields();
}

/*****************************************************************************/


}	//	End of class	JSStringList
