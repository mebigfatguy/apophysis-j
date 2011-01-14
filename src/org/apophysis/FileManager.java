package org.apophysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class	FileManager implements Constants {


/*****************************************************************************/
//	FIELDS

FileManager()
{
}

/*****************************************************************************/

static	boolean	saveEntry(String text, String title, String filename)
{

File file = new File(filename);

System.out.println("file "+filename+" exists "+file.exists());
if(file.exists())
	return replaceEntry(text,title,filename);
else
	return writeEntry(text,title,filename);

}

/*****************************************************************************/

static	boolean	writeEntry(String text, String title, String filename)
{
boolean ok = false;

try	{
	File file = new File(filename);
	PrintWriter w = new PrintWriter(new FileWriter(file));
	w.println(title+" {");
	w.print(text);
	w.println("}");
	w.close();
	ok = true;
	}
catch(IOException ex)
	{
	ex.printStackTrace();
	}

return ok;

}	//	End of method	writeEntry

/*****************************************************************************/

static	boolean	replaceEntry(String text, String title, String filename)
{
boolean ok = false;

boolean replaced = false;

try	{
	File file = new File(filename);
	File temp = File.createTempFile("apo","temp");
	BufferedReader r = new BufferedReader(new FileReader(file));
	PrintWriter w = new PrintWriter(new FileWriter(temp));
	while(true)
		{
		String line = r.readLine();
		if(line==null) break;
		if(line.trim().endsWith("{"))
			{
			String ename = line.trim();
			ename = ename.substring(0,ename.length()-1).trim();
			if(ename.equals(title))
				{
				// skip the entry to be replaced
				while(true)
					{
					line = r.readLine();
					if(line==null) break;
					if(line.trim().startsWith("}")) break;
					}

				// replace by new one
				w.println(title+" {");
				w.print(text);
				w.println("}");	

				replaced = true;
				}
			else
				w.println(line);
			}
		else
			w.println(line);
		}

	if(!replaced)
		{
		w.println(title+" {");
		w.print(text);
		w.println("}");
		}

	r.close();
	w.close();

	ok = true;	
	// remove old file and replace by new one
	ok = ok && file.delete();
	ok = ok && temp.renameTo(file);
	ok = true;
	}
catch(IOException ex)
	{
	ex.printStackTrace();
	}

return ok;

}	//	End of method	replaceEntry

/*****************************************************************************/

}	//	End of class	FileManager
