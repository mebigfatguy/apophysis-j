
/*

    Apophysis-j Copyright (C) 2008 Jean-Francois Bouzereau

	based on Apophysis ( http://www.apophysis.org )
	Apophysis Copyright (C) 2001-2004 Mark Townsend
	Apophysis Copyright (C) 2005-2006 Ronald Hordijk, Piotr Borys, Peter Sdobnov
	Apophysis Copyright (C) 2007 Piotr Borys, Peter Sdobnov

	based on Flam3 ( http://www.flam3.com )
    Copyright (C) 1992-2006  Scott Draves <source@flam3.com>

    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/



package org.apophysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;

public class Main extends MyThinlet
	implements Constants, ThreadTarget, DropTargetListener {

/*****************************************************************************/
//	CONSTANTS

static final double SNAP_ANGLE = 15.0*Math.PI/180.0;
static final int CORNER_SIZE = 32;

/*****************************************************************************/
//	FIELDS


//	ScriptForm Fields

String lastParseError;
int numTransforms;
int activeTransform;
String lastError;
double color;
boolean stopped,resetLocation,updateIt;
String paramFile;
String filelist[];

long startTime;

Object canvas;
int imagewidth,imageheight;	// image dimension

int fmousemovestate = msUsual;
MouseRect click = new MouseRect();
MouseRect select = new MouseRect();
double rotateangle, clickangle;
boolean drawselection = true;
BasicStroke dots,basic;


RenderThread renderthread;

ControlPoint renderCP = null;;

double center[] = new double[2];
double viewpos[] = new double[2];
double viewoldpos[] = new double[2];
double viewscale;

Timer timer = null;

Image image = null;

Font font = new Font("Helvetica",Font.PLAIN,12);

public int panelwidth = 0, panelheight = 0;


Color grayf0 = new Color(0xF0,0xF0,0xF0);
Color grayc0 = new Color(0xC0,0xC0,0xC0);

Vector cps;

int splitpos = 160;


int xview = 0;
int yview = 0;

// for undo / redo
Vector history;
int undoindex;


// for drag and drop
DropTarget droptarget = null;
URL dropurl = null;

/*****************************************************************************/

Main(String title, String xmlfile, int width, int height)
	throws Exception
{
super(title,xmlfile,width,height);

checkEnvironment();

initStrokes();

canvas = find("MainCanvas");

for(int i=0;i<Global.mainTriangles.length;i++)
	Global.mainTriangles[i] = new Triangle();

//Global.readSettings();

history = new Vector();
undoindex = 0;

droptarget = new DropTarget(this,this);

XForm.registerPluginVariations(this);

Global.readSettings();


// get sheep variations
(new Sheep()).start();

Global.mainCP = new ControlPoint();

renderCP = new ControlPoint();

restoreWindowPosition();

}

/*****************************************************************************/

void  checkEnvironment()
{
File home = new File(System.getProperty("user.home"));
File adir = new File(home,DIRNAME);

if(!adir.exists())
    adir.mkdir();

if(adir.exists()&&adir.isDirectory())
	{
	Global.apopath = adir.getAbsolutePath();
	
	// check if old config file
	File fcon = new File(home,".apophysis.conf");
	if(fcon.exists())
		{
		File fnew = new File(Global.apopath,CONFNAME);
		fcon.renameTo(fnew);
		}	

	// check old render preset file
	File fpreset = new File(home,PRSTNAME);
	if(fpreset.exists())
		{
		File fnew = new File(Global.apopath,PRSTNAME);
		fpreset.renameTo(fnew);
		}

	// check if plugin directory exists
	File fplugin = new File(Global.apopath,PLUGNAME);
	if(!fplugin.exists())
		fplugin.mkdir();

	// check if sub-directory apophysis exists
	String pname = getClass().getPackage().getName();
	if(fplugin.exists()&&fplugin.isDirectory())
		{
		File fapo = new File(fplugin,pname);
		if(!fapo.exists())
			fapo.mkdir();
		}	
	}

}   //  End of method   checkEnvironement
    
/*****************************************************************************/

public void show()
{
super.show();

if(Global.apopath==null)
	{
	File home = new File(System.getProperty("user.home"));
	File dir = new File(home,DIRNAME);
	String dirname = dir.getAbsolutePath();	
	alert("Cannot create directory "+dirname,new QuitTask());
	return;
	}

buildVariationMenu();

Global.editor.buildVariationList();
Global.editor.buildParameterList();

updateFavorites();

int nv = XForm.getNrVariations();


int quality = (int)Global.defSampleDensity;
setString(find("tbQualityBox"),"text",""+quality);

undoindex = 0;
history.removeAllElements();
updateUndoControls();

fmousemovestate = msDrag;
setBoolean(find("tbDrag"),"selected",true);

timer = new Timer();
timer.start();

Global.mainCP.width = Global.panelWidth;
Global.mainCP.height = Global.panelHeight;


mnuRandomBatchClick();

setMenuShortcuts();

}

/*****************************************************************************/

void dumpVariations()
{
int nv = XForm.getNrVariations();
for(int i=0;i<nv;i++)
	System.out.println(XForm.getVariation(i).getName());
}

/*****************************************************************************/

public void mnuOpenClick()
{

//Global.editor.stopped = true;

Task task = new OpenFileTask();
Global.opendialog = new OpenDialog(this, Global.browserPath,task);

Global.opendialog.addFilter("Flame files (*.flame)","*.flame");
Global.opendialog.addFilter("Fla files (*.fla)","*.fla");
Global.opendialog.addFilter("Ultrafractal files (*.upr)","*upr");
Global.opendialog.addFilter("JPEG image files (*.jpg)","*.jpg");
Global.opendialog.addFilter("PNG image files (*.png)","*.png");
//Global.opendialog.addFilter("IFS files (*.ifs)","*.ifs");

Global.opendialog.show();

}	//	End of method	mnuOpenClick

/*****************************************************************************/

Vector  openFLAFile(String filename)
{
ControlPoint cp = new ControlPoint();
String temp = null;
int ixform =0;

Vector newcps = new Vector();

try	{
	BufferedReader r = new BufferedReader(new FileReader(filename));

	while(true)
		{
		String line = r.readLine();
		if(line==null) break;

		cp.nxforms = 0;

		// name of flame
		StringTokenizer tk = new StringTokenizer(line);
		if(tk.countTokens()!=2) break;
		cp.name = tk.nextToken();
		temp = tk.nextToken();
		if(!temp.equals("{")) break;

		while(true)
			{
			line = r.readLine();
			if(line==null) break;

			tk = new StringTokenizer(line);
			int n = tk.countTokens();
			while(tk.hasMoreTokens())
				{
				temp = tk.nextToken();
				if(temp.equals("time"))
					cp.time = Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("zoom"))
					cp.zoom = Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("angle"))
					cp.fangle = Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("image_size"))
					{
					cp.width = Integer.parseInt(tk.nextToken());
					cp.height = Integer.parseInt(tk.nextToken());
					}
				else if(temp.equals("center"))
					{
					cp.center[0] = Double.valueOf(tk.nextToken()).doubleValue();
					cp.center[1] = Double.valueOf(tk.nextToken()).doubleValue();
					}
				else if(temp.equals("pixels_per_unit"))
					cp.pixels_per_unit =
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("spatial_oversample"))
					cp.spatial_oversample =
						Integer.parseInt(tk.nextToken());
				else if(temp.equals("spatial_filter_radius"))
					cp.spatial_filter_radius =
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("sample_density"))
					cp.sample_density = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("background"))
					{
					cp.background[0] = Integer.parseInt(tk.nextToken());
					cp.background[1] = Integer.parseInt(tk.nextToken());
					cp.background[2] = Integer.parseInt(tk.nextToken());
					}
				else if(temp.equals("brightness"))
					cp.brightness = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("gamma"))
					cp.gamma = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("vibrancy"))
					cp.vibrancy = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("hue_rotation"))
					cp.hue_rotation = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("finalzformenabled"))
					cp.finalXformEnabled = (!tk.nextToken().equals("0"));
				else if(temp.equals("xform"))
					ixform = Integer.parseInt(tk.nextToken());	
				else if(temp.equals("density"))
					cp.xform[ixform].density = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("color"))
					cp.xform[ixform].color = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("symmetry"))
					cp.xform[ixform].symmetry = 
						Double.valueOf(tk.nextToken()).doubleValue();
				else if(temp.equals("vars"))
					{
					int iv = 0;
					while(tk.hasMoreTokens())
						cp.xform[ixform].vars[iv++] = 
							Double.valueOf(tk.nextToken()).doubleValue();
					break;
					}
				else if(temp.equals("coefs"))
					{
					cp.xform[ixform].c00 = 
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].c01 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].c10 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].c11 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].c20 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].c21 =
							Double.valueOf(tk.nextToken()).doubleValue();
					}
				else if(temp.equals("post"))
					{
					cp.xform[ixform].p00 = 
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].p01 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].p10 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].p11 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].p20 =
							Double.valueOf(tk.nextToken()).doubleValue();
					cp.xform[ixform].p21 =
							Double.valueOf(tk.nextToken()).doubleValue();
					}
				else if(temp.equals("palette:"))
					{
					for(int i=0;i<256;i++)
						{
						line = r.readLine();
						tk = new StringTokenizer(line);
						cp.cmap[i][0] = Integer.parseInt(tk.nextToken());
						cp.cmap[i][1] = Integer.parseInt(tk.nextToken());
						cp.cmap[i][2] = Integer.parseInt(tk.nextToken());
						}
					}
				}
			}
		}

	r.close();	

	cp.nxforms = 0;
	for(int i=0;i<cp.xform.length;i++)
		if(cp.xform[i].density!=0)
			cp.nxforms = i+1;

	if(cp.nxforms>0)
		newcps.addElement(cp);
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

return newcps;

}	//	End of method	openFLAFile

/*****************************************************************************/

Vector openJPGFile(String filename)
{
String comment = CommentExtractor.readJpegComment(filename);
if(comment.length()==0)	
	{
	// try to open the corresponding flame file
	int i = filename.lastIndexOf('.');
	if(i>0)
		filename = filename.substring(0,i)+".flame";
	else
		filename = filename+".flame";
	return openXMLFile(filename);
	}
else
	{
	int i = comment.indexOf("<flame");
	if(i<0) return new Vector();
	
	if(i>0) comment = comment.substring(i);
	return readXML(new StringReader(comment));
	}

}	//	End of method	openJPGFile

/*****************************************************************************/

Vector openPNGFile(String filename)
{
String comment = CommentExtractor.readPngComment(filename);
if(comment.length()==0)	
	{
	// try to open the corresponding flame file
	int i = filename.lastIndexOf('.');
	if(i>0)
		filename = filename.substring(0,i)+".flame";
	else
		filename = filename+".flame";
	return openXMLFile(filename);
	}
else
	{
	int i = comment.indexOf("<flame");
	if(i<0) return new Vector();

	if(i>0) comment = comment.substring(i);
	return readXML(new StringReader(comment));
	}

}	//	End of method	openPNGFile

/*****************************************************************************/

void openUPRFile(String filename)
{
int i;

cps = new Vector();

try	{
	BufferedReader r = new BufferedReader(new FileReader(filename));
	
	while(true)
		{
		String line = r.readLine();
		if(line==null) break;

		if(line.trim().endsWith("{"))
			{
			// read parameters 
			Hashtable h = new Hashtable();
			while(true)
				{
				line = r.readLine();
				if(line==null) break;
				i = line.indexOf("gradient:");
				if(i>=0) break;
				StringTokenizer tk = new StringTokenizer(line);
				while(tk.hasMoreTokens())
					{
					String token = tk.nextToken();
					i = token.indexOf('=');
					if(i<0) continue;
					String key = token.substring(0,i);
					String val = token.substring(i+1);
					h.put(key,val);
					}
				}

			Vector v = new Vector();
			// read gradient
			while(true)
				{
				line = r.readLine();
				if(line==null) break;
				if(line.startsWith("}")) break;

				StringTokenizer tk = new StringTokenizer(line);
				if(tk.countTokens()!=2) continue;

				String token = tk.nextToken();
				i = token.indexOf('=');
				if(i<0) continue;
				if(!token.substring(0,i).equals("index")) continue;	
				int index = Integer.parseInt(token.substring(i+1));
				if(index<0) continue;
				if(index>=400) continue;
	
				token = tk.nextToken();
				i = token.indexOf('=');
				if(i<0) continue;
				if(!token.substring(0,i).equals("color")) continue;
				int color = Integer.parseInt(token.substring(i+1));
				v.addElement(new int[]{index,color});
				}	
			ControlPoint cp = new ControlPoint(h,v);
			cps.addElement(cp);
			}
		}

	r.close();

    Global.openFile = filename;
    Global.openFileType = ftUPR;
                    
    updateFlameList();
                    
    // select the first flame and force drawing
                
    Object list = find("ListView");
    setBoolean(getItem(list,0),"selected",true);
    listViewChange(list);

	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	openUPRFile

/*****************************************************************************/

Vector  openXMLFile(String filename)
{
Vector v = new Vector();
try	{
	v = readXML(new FileReader(filename));
	}
catch(Exception ex)
	{
	}
return v;
}

/*****************************************************************************/

Vector readXML(Reader reader)
{
Vector mycps = new Vector();

try	{
	BufferedReader r = new BufferedReader(reader);
	
	while(true)
		{
		ControlPoint cp = readControlPoint(r);
		if(cp==null) break;
		mycps.addElement(cp);
		}
	r.close();


	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

return mycps;


}	//	End of method	OpenFile

/*****************************************************************************/

ControlPoint readControlPoint(BufferedReader r) throws Exception
{
ControlPoint cp = null;
String line = null;
Vector unknown = new Vector();

while(true)
	{
	line = r.readLine();
	if(line==null) return null;
	int i = line.indexOf("<flame ");
	if(i<0) continue;
	line = line.substring(i).trim();
	break;
	}

XmlTag tag = new XmlTag(line);
cp = new ControlPoint(tag);	

while(true)
	{
	line = r.readLine();
	if(line==null) break;
	if(line.indexOf("<")<0) continue;

	if(line.indexOf("</flame")>=0) break;

	tag = new XmlTag(line.trim());
	if(tag.getName().equals("xform"))
		{
		cp.addXForm(tag);
		appendUnknown(unknown,tag);
		}
	else if(tag.getName().equals("finalxform"))
		{
		cp.addFinalXForm(tag);
		appendUnknown(unknown,tag);
		}
	else if(tag.getName().equals("color"))
		cp.addColor(tag);
	else if(tag.getName().equals("palette")&&(!tag.isClosed()))
		{
		while(true)
			{
			line = r.readLine();
			if(line==null) break;
			if(line.indexOf("</palette")>=0) break;
			tag.appendData(line.trim());
			}
		cp.setPalette(tag.getInt("count",0),tag.getData());
		}
	else if(tag.getName().equals("colors"))
		{
		while(true)
			{
			line = r.readLine();
			if(line==null) break;
			int k = line.indexOf("\"/>");
			if(k>0)
				{
				tag.appendAttribute("data",line.substring(0,k).trim());
				break;
				}
			else
				{
				tag.appendAttribute("data",line.trim());
				}
			}
		cp.setPalette(tag.getInt("count",0),tag.getAttribute("data"));
		}
	}

if(unknown.size()>0)
	cp.unknown = unknown;

return cp;

}	//	End of method	readControlPoint

/*****************************************************************************/

void appendUnknown(Vector v, XmlTag tag)
{
Enumeration e = tag.getUnreclaimedKeys();
while(e.hasMoreElements())
	{
	String s = (String)e.nextElement();
	int k = s.indexOf('_');
	if(k>0) s = s.substring(0,k);
	
	if(!v.contains(s)) v.addElement(s);
	}
}

/*****************************************************************************/

void updateFlameList()
{
Object title = find("ListTitle");
setString(title,"text",cps.size()+" flame"+(cps.size()!=1?"s":""));
setString(title,"tooltip",Global.openFile);

Object list = find("ListView");

removeAll(list);

int ncp = cps.size();
for(int i=0;i<ncp;i++)
	{
	ControlPoint cp = (ControlPoint)cps.elementAt(i);

	Object item = createImpl("item");
	setString(item,"text",cp.name);
	setFont(item,font);
	if(cp.unknown!=null)
		{
		setColor(item,"foreground",Color.red);
		setString(item,"tooltip","unknown variation");
		}
		
	add(list,item);	
	}

requestFocus(list);

}	//	End of method	updateFlameList

/*****************************************************************************/

Object buildPopup()
{
Object popup = createImpl("popupmenu");

Object item1 = createImpl("menuitem");
setString(item1,"text","Delete");
add(popup,item1);

Object item2 = createImpl("menuitem");
setString(item2,"text","Rename");
add(popup,item2);

return popup;
}

/*****************************************************************************/

public void mnuSaveAsClick()
{
String filename = Global.mainCP.name+".flame";
filename = filename.replace(' ','_');

Task task = new SaveFileTask(Global.mainCP);
Global.savedialog = new SaveDialog(this, Global.browserPath,filename,task);
Global.savedialog.warning = "Append to";
Global.savedialog.show();

}	//	End of method	mnuSaveAsClick

/*****************************************************************************/

public void mnuSaveAllAsClick()
{
String filename = "apophysis.flame";

Task task = new SaveFileTask(null);
Global.savedialog = new SaveDialog(this,Global.browserPath,filename,task);
Global.savedialog.warning = "Append to";
Global.savedialog.show();

}	//	End of method	mnuSaveAllAsClick

/*****************************************************************************/

public void saveXMLFile(ControlPoint cp, String filename) 
{ 
Vector mycps = new Vector();
try	{
	mycps = readXML(new FileReader(filename));
	}
catch(Exception ex)
	{
	}

if(cp!=null)
	addFlame(mycps,cp);
else
	{
	int nc = cps.size();
	for(int i=0;i<nc;i++)
		addFlame(mycps,(ControlPoint)cps.elementAt(i));
	}

try	{
	File file = new File(filename);
	String title = file.getName();
	int k = title.indexOf('.');
	if(k>0) title = title.substring(0,k);

	PrintWriter w = new PrintWriter(new FileWriter(file));

	w.println("<Flames name=\""+title+"\">");

	int nc = mycps.size();
	for(int i=0;i<nc;i++)
		{
		cp = (ControlPoint)mycps.elementAt(i);
		cp.save(w);
		}

	// plplpl added the missing terminal ">" !
	w.println("</Flames>");	
	w.close();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	saveXMLFlame

/*****************************************************************************/

void addFlame(Vector v, ControlPoint newcp)
{
// check if flame already exists
int n = v.size();
for(int i=n-1;i>=0;i--)
	{
	ControlPoint oldcp = (ControlPoint)v.elementAt(i);
	if(oldcp.name.equals(newcp.name))
		v.removeElementAt(i);
	}

v.addElement(newcp);

}	//	End of method	addFlame

/*****************************************************************************/

public void mnuExitClick()
{

if(Global.confirmExit)
	confirm("Do you really want to quit? All unsaved data will be lost!",
		new QuitTask());
else
	{
	quitApplication();
	System.exit(0);
	}

}	//	End of method	mnuExitClick

/*****************************************************************************/

public void listViewDoubleClick(Object list)
{
int index = getSelectedIndex(list);
if(index<0) { beep(); return; }

ControlPoint cp = (ControlPoint)cps.elementAt(index);

ask("Name of the flame :",cp.name, new FlameRenameTask(index));

}	//	End of method	listViewDoubleClick

/*****************************************************************************/

void renameFlame(int index, String newname)
{

ControlPoint cp = (ControlPoint)cps.elementAt(index);
cp.name = newname;

updateFlameList();

Object list = find("ListView");
setBoolean(getItem(list,index),"selected",true);

setStatus(newname);

}

/*****************************************************************************/

public void listViewChange(Object list)
{
int index = getSelectedIndex(list);
if(index<0) return;

Global.mainCP = (ControlPoint)cps.elementAt(index);

clearUndo();

Global.transforms = Global.mainCP.trianglesFromCP(Global.mainTriangles);

updateWindows();

setStatus(Global.mainCP.name);

timer.enable();

resizeImage();

}	//	End of method	listViewChange

/*****************************************************************************/

public void updateWindows()
{
if(Global.editor.visible()) Global.editor.updateDisplay();
if(Global.adjust.visible()) Global.adjust.updateDisplay();
if(Global.mutate.visible()) Global.mutate.updateDisplay();
}

/*****************************************************************************/

Point getPositionInDesktop(Object component)
{
int x,y;

Rectangle r = getRectangle(component,"bounds");
x = r.x;
y = r.y;
while(true)
	{
	component = getParent(component);
	if(component==null) break;
	r = getRectangle(component,"bounds");
	x += r.x;
	y += r.y;	
	}

return new Point(x,y);

}	//	End of method	getPositionInDesktop

/*****************************************************************************/

public void drawMainCanvas(Graphics g, Rectangle bounds)
{

if(Global.showTransparency)
	{
	g.setColor(grayf0);
	g.fillRect(0,0,bounds.width,bounds.height);
	g.setColor(grayc0);
	int w = (bounds.width-1)>>3;
	int h = (bounds.height-1)>>3;
	for(int i=0;i<=w;i++)
		for(int j=0;j<=h;j++)
			if(((i+j)%2)==1)
				g.fillRect(i<<3,j<<3,8,8);
	}
else
	{
	g.setColor(new Color(
		Global.mainCP.background[0],
		Global.mainCP.background[1],
		Global.mainCP.background[2]));
	g.fillRect(0,0,bounds.width,bounds.height);
	}

if(image!=null)
	g.drawImage(image,xview,yview,imagewidth,imageheight,null);


}

/*****************************************************************************/

public void mnuEditorClick()
{
Global.editor.show();
}	//	End of method	mnuEditorClick

/*****************************************************************************/

public void mnuGradClick()
{
Global.adjust.updateDisplay();
Global.adjust.setTab(2);
Global.adjust.show();
}	//	End of method	mnuGradClick

/*****************************************************************************/

void saveFlame()
{
ControlPoint cp = new ControlPoint();
cp.clone(Global.mainCP);
history.addElement(cp);

}	//	End of method	saveFlame

/*****************************************************************************/

void loadFlame(int index)
{
Global.mainCP = (ControlPoint)history.elementAt(index);

center[0] = Global.mainCP.center[0];
center[1] = Global.mainCP.center[1];

Global.transforms = Global.mainCP.trianglesFromCP(Global.mainTriangles);

timer.enable();

updateWindows();

}

/*****************************************************************************/

public void clearUndo()
{
history.removeAllElements();
undoindex = 0;
updateUndoControls();
}

/*****************************************************************************/

public void updateUndo()
{
while(history.size()>undoindex)
	history.removeElement(history.elementAt(history.size()-1));
saveFlame();
undoindex++;
updateUndoControls();
}

/*****************************************************************************/

void updateUndoControls()
{
setBoolean(find("mnuUndo"),"enabled",undoindex>0);
setBoolean(find("btnUndo"),"enabled",undoindex>0);
setBoolean(find("mnuPopupUndo"),"enabled",undoindex>0);

setBoolean(find("mnuRedo"),"enabled",undoindex<history.size()-1);
setBoolean(find("btnRedo"),"enabled",undoindex<history.size()-1);
setBoolean(find("mnuPopupRedo"),"enabled",undoindex<history.size()-1);

if(Global.editor!=null)
	Global.editor.updateUndoControls(undoindex,history.size()-1);

if(Global.adjust!=null)
	Global.adjust.updateUndoControls(undoindex,history.size()-1);

}	//	End of method	updateUndo

/*****************************************************************************/

public void undo()
{
if(undoindex==history.size())
	saveFlame();

stopThread();

undoindex--;
loadFlame(undoindex);

updateUndoControls();

}	//	End of method	undo

/*****************************************************************************/

public void redo()
{
// should not happen
if(undoindex>=history.size()) return;

stopThread();

undoindex++;

loadFlame(undoindex);

updateUndoControls();

}	//	End of method	redo

/*****************************************************************************/

public void mnuAdjustClick()
{

Global.adjust.updateDisplay();
Global.adjust.setTab(0);
Global.adjust.show();

}	//	End of method	mnuAdjustClick

/*****************************************************************************/

public void mnuMutateClick()
{
Global.mutate.show();
Global.mutate.updateDisplay();
}

/*****************************************************************************/

void redrawTimerTimer()
{

if(fmousemovestate==msZoomWindowMove) return;
if(fmousemovestate==msZoomOutWindowMove) return;
if(fmousemovestate==msDragMove) return;
if(fmousemovestate==msRotateMove) return;

timer.disable();

drawFlame();

}	//	End of method	redrawTimerTimer

/*****************************************************************************/

void drawFlame()
{
timer.disable();

if(renderthread!=null)
	{
	renderthread.terminate();
	}


renderCP.clone(Global.mainCP);

renderCP.adjustScale(imagewidth,imageheight);

renderCP.sample_density = Global.defSampleDensity;


renderCP.spatial_oversample = 1;
renderCP.spatial_filter_radius = 0.001;
renderCP.transparency = true;

viewoldpos[0] = viewpos[0];
viewoldpos[1] = viewpos[1];

startTime = System.currentTimeMillis();
setString(find("Status0"),"text","");

renderthread = new RenderThread(this);

renderthread.setCP(renderCP);

renderthread.start();


}	//	End of method	drawFlame

/*****************************************************************************/

public void mnuOptionsClick()
{

Global.options.show();

}

/*****************************************************************************/

public void mnuOptionsClick2()
{

stopThread();
timer.enable();
setString(find("tbQualityBox"),"text",""+Global.defSampleDensity);

updateWindows();

}	//	End of method	mnuOptionsClick

/*****************************************************************************/

public void btShowAlphaClick(Object button)
{

Global.showTransparency = getBoolean(button,"selected");

repaint();

}	//	End of method	btShowAlphaClick

/*****************************************************************************/

public void mnuSmoothGradientClick()
{

Task task = new SmoothPaletteTask();
Global.opendialog = new OpenDialog(this, Global.browserPath,task);

Global.opendialog.addFilter("JPEG images (*.jpg,*.jpeg)","*.jpg;*.jpeg");
Global.opendialog.addFilter("GIF images (*.gif)","*.gif");
Global.opendialog.addFilter("PNG images (*.png)","*.png");

Global.opendialog.show();


}	//	End of method	smoothGradientClick

/*****************************************************************************/

void smoothPalette()
{
int cmap_best[] = new int[256];
int original[] = new int[256];
int clist[] = new int[256];
int len=0,len_best=0,as_is=0,swapd=0;
int p,total,j,rand,tryit,i0,i1,x,y,i,iw,ih;

total = Global.numTries*Global.tryLength/100;
p = 0;

try	{
	File file = new File(Global.opendialog.filename);
	if(!file.exists()) { beep(); return; }

	Image image = ImageIO.read(file);
	iw = image.getWidth(null);
	ih = image.getHeight(null);

	BufferedImage bimage = new BufferedImage(iw,ih,BufferedImage.TYPE_INT_RGB);
	Graphics g = bimage.createGraphics();
	g.drawImage(image,0,0,null);
	g.dispose();
	image = null;

	// pick 256 random pixels
	for(i=0;i<256;i++)
		{
		x = (int)(Math.random()*iw);
		y = (int)(Math.random()*ih);
		clist[i] = bimage.getRGB(x,y);
		}

	System.arraycopy(clist,0,original,0,clist.length);
	System.arraycopy(clist,0,cmap_best,0,clist.length);

	for(tryit=1;tryit<=Global.numTries;tryit++)
		{
		System.arraycopy(original,0,clist,0,original.length);

		// scramble
		for(i=0;i<256;i++)
			{
			rand = (int)(Math.random()*256);
			int z = clist[i];
			clist[i] = clist[rand];
			clist[rand] = z;	
			}

		// measure
		len = 0;
		for(i=0;i<255;i++)
			len += diffcolor(clist,i,i+1);

		// improve
		for(i=1;i<=Global.tryLength;i++)
			{
			p++;
			i0 = 1 + (int)(Math.random()*254);
			i1 = 1 + (int)(Math.random()*254);
			if((i0-i1)==1)
				{
				as_is = diffcolor(clist,i1-1,i1)+diffcolor(clist,i0,i0+1);
				swapd = diffcolor(clist,i1-1,i0)+diffcolor(clist,i1,i0+1);
				}
			else if((i1-i0)==1)
				{
				as_is = diffcolor(clist,i0-1,i0)+diffcolor(clist,i1,i1+1);
				swapd = diffcolor(clist,i0-1,i1)+diffcolor(clist,i0,i1+1);
				}
			else 
				{
				as_is = diffcolor(clist,i0,i0+1)+diffcolor(clist,i0,i0-1)
					+diffcolor(clist,i1,i1+1)+diffcolor(clist,i1,i1-1);
				swapd = diffcolor(clist,i1,i0+1)+diffcolor(clist,i1,i0-1)
					+diffcolor(clist,i0,i1+1)+diffcolor(clist,i0,i1-1);
				}
	
			if(swapd<as_is)
				{
				int z = clist[i0];
				clist[i0] = clist[i1];
				clist[i1] = z;
				len = Math.abs(len+swapd-as_is);
				}
			}
		if((tryit==1)||(len<len_best))
			{
			System.arraycopy(clist,0,cmap_best,0,clist.length);
			len_best = len;
			}
		}

	System.arraycopy(cmap_best,0,clist,0,cmap_best.length);
	
	// clean
	for(i=1;i<=1024;i++)
		{
		i0 = 1 + (int)(Math.random()*253);
		i1 = i0+1;
		as_is = diffcolor(clist,i0-1,i0) + diffcolor(clist,i1,i1+1);
		swapd = diffcolor(clist,i0-1,i1) + diffcolor(clist,i0,i1+1);
		if(swapd<as_is)
			{
			int z = clist[i0];
			clist[i0] = clist[i1];	
			clist[i1] = z;
			len_best = len_best + swapd - as_is;
			}
		}

	// create palette
	int pal[][] = new int[256][3];
	for(i=0;i<256;i++)
		{
		pal[i][0] = (clist[i]>>16)&0xFF;
		pal[i][1] = (clist[i]>>8)&0xFF;
		pal[i][2] = (clist[i])&0xFF;
		}

	CMap.copyPalette(pal,Global.mainCP.cmap);
	Global.mainCP.cmapindex = -1;
		
	Global.adjust.updateDisplay();
	if(Global.mutate.visible()) Global.mutate.updateDisplay();

	timer.enable();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	smoothPalette

/*****************************************************************************/

static final int diffcolor(int clist[], int i, int j)
{
int r1,g1,b1,r2,b2,g2;

r1 = (clist[j]>>16)&0xFF;
g1 = (clist[j]>>8)&0xFF;
b1 = (clist[j])&0xFF;

r2 = (clist[i]>>16)&0xFF;
g2 = (clist[i]>>8)&0xFF;
b2 = (clist[i])&0xFF;

return ((r1-r2)*(r1-r2))+((g1-g2)*(g1-g2))+((b1-b2)*(b1-b2));

}

/*****************************************************************************/

public void mnuOpenGradientClick()
{

Global.browser.show();

}	//	End of method	mnuOpenGradientClick


/*****************************************************************************/

public void mnuExportFlameClick()
{

if(Global.flam3Path.length()==0)
	{
	alert("The flam3-render renderer is not defined. Check your options");
	return;
	}

File file = new File(Global.flam3Path);
if(!file.exists())
	{
	alert("The flam3-render renderer could not be found. Check your options");
	return;
	}

String ext = "";
switch(Global.exportFileFormat)
	{
	case 1: ext = "jpg"; break;
	case 2: ext = "ppm"; break;
	case 3: ext = "png"; break;
	}

String filename = (Global.mainCP.name+"."+ext).replace(' ','_');
file = new File(Global.renderPath,filename);
Global.export.filename = file.getAbsolutePath();

Global.export.show();

}	//	End of method	mnuExportFlameClick

/*****************************************************************************/

public void mnuRenderClick()
{
String ext;
boolean newrender;

if(Global.render.renderthread!=null)
	confirm("Do you want to abort the current render ?", new RenderTask(false));
else
	renderToDisk(false);
	
}	//	End of method	mnuRenderClick

/*****************************************************************************/

void renderToDisk(boolean all)
{
if(Global.render.renderthread!=null)
	Global.render.renderthread.terminate();

Global.render.resetControls();
Global.render.setTab(0);

String ext = ".jpg";
switch(Global.renderFileFormat)
	{
	case 0 : ext = ".bmp"; break;
	case 1 : ext = ".png"; break;
	case 2 : ext = ".jpg"; break;
	}

System.out.println("mainCP = "+Global.mainCP);
System.out.println("name   = "+Global.mainCP.name);
String filename = Global.mainCP.name.replace(' ','_');

File file = new File(Global.renderPath,filename+ext);
Global.render.filename = file.getAbsolutePath();

Global.render.cp.copy(Global.mainCP);

CMap.copyPalette(Global.mainCP.cmap,Global.render.cp.cmap);
Global.render.zoom = Global.mainCP.zoom;
Global.render.center[0] = Global.mainCP.center[0];
Global.render.center[1] = Global.mainCP.center[1];

Global.render.renderall = all;

Global.render.show();

}

/*****************************************************************************/

public void mnuRenderAllClick()
{
String ext;
boolean newrender;

if(Global.render.renderthread!=null)
	confirm("Do you want to abort the current render ?", new RenderTask(true));
else
	renderToDisk(true);
	
}	//	End of method	mnuRenderClick

/*****************************************************************************/

public void mnuToolBarClick(Object button)
{
setBoolean(find("ToolBar"),"visible",getBoolean(button,"selected"));
}

/*****************************************************************************/

public void mnuStatusBarClick(Object button)
{
setBoolean(find("MainStatusBar"),"visible",getBoolean(button,"selected"));
}

/*****************************************************************************/

public void mnuFileContentsClick(Object button)
{
Object splitter = find("Splitter");

if(getBoolean(button,"selected"))
	{
	setInteger(splitter,"divider",splitpos);
	}
else
	{
	splitpos = getInteger(splitter,"divider");
	setInteger(splitter,"divider",0);
	}
}

/*****************************************************************************/

public void mnuRandomBatchClick()
{

randomBatch();

updateFlameList();

// select the first flame and force drawing

Object list = find("ListView");
setBoolean(getItem(list,0),"selected",true);
listViewChange(list);

}	//	End of method	mnuRandomBatchClick

/*****************************************************************************/

void randomBatch()
{
int b = Global.batchSize;
if(b==0) b = 1;

cps = new Vector();

for(int i=0;i<b;i++)
	{
	ControlPoint cp = ControlPoint.randomFlame(Global.mainCP);
	Global.randomIndex++;
	cp.name = Global.randomPrefix+Global.randomDate+"-"+Global.randomIndex;
	cps.addElement(cp);
	}

Global.openFile = "";
}

/*****************************************************************************/

void buildVariationMenu()
{
Object menu = find("mnuVar");


int nv = XForm.getNrVariations();

// create all the needed groups

Color color = new Color(0xD1CCC6);

int ig = 0;
while(true)
	{
	ig++;

	// count the number of variations in this group
	int n = 0;
	for(int i=0;i<nv;i++)
		if(XForm.getVariation(i).getGroup()==ig)
			n++;

	if(n==0) break;
	
	Object submenu = createImpl("menu");
	setString(submenu,"text","Group "+ig);
	setColor(submenu,"background",color);
	
	add(menu,submenu);

	for(int i=0;i<nv;i++)
		if(XForm.getVariation(i).getGroup()==ig)
			{
			Object item = createImpl("checkboxmenuitem");
			setString(item,"text",XForm.getVariation(i).getName());
			setMethod(item,"action","variantMenuClick(this,"+i+")",item,this);
			setString(item,"group","variations");
			setColor(item,"background",color);

			add(submenu,item);
			}

	}

}	//	End of method	fillVariantMenu

/*****************************************************************************/

public void variantMenuClick(Object item, int index)
{
unselectItems(find("mnuVar"));
setBoolean(item,"selected",true);

Global.variation = index;

updateUndo();
Global.mainCP.setVariation(index);
resetLocation();


timer.enable();

updateWindows();

}

/*****************************************************************************/

void unselectItems(Object menu)
{
Object items[]= getItems(menu);
for(int i=0;i<items.length;i++)
	if(getClass(items[i])=="menu")
		unselectItems(items[i]);
	else if(getClass(items[i])=="checkboxmenuitem")
		setBoolean(items[i],"selected",false);
}

/*****************************************************************************/

void resetLocation()
{
Global.mainCP.zoom = 0.0;
Global.mainCP.calcBoundBox();
center[0] = Global.mainCP.center[0];
center[1] = Global.mainCP.center[1];
}

/*****************************************************************************/

public void mnuFullScreenClick()
{
if(image==null) return;

Global.fullscreen.cp.copy(Global.mainCP);
Global.fullscreen.center[0] = Global.mainCP.center[0];
Global.fullscreen.center[1] = Global.mainCP.center[1];
CMap.copyPalette(Global.mainCP.cmap,Global.fullscreen.cp.cmap);

Global.fullscreen.image = image;

Global.fullscreen.show();

}	//	End of method	mnuFullScreenClick

/*****************************************************************************/

public void mnuRandomClick()
{
stopThread();
updateUndo();

Global.mainCP= ControlPoint.randomFlame(Global.mainCP);

Global.randomIndex++;
Global.mainCP.name = Global.randomPrefix+Global.randomDate+"-"+
	Global.randomIndex;

Global.transforms = Global.mainCP.trianglesFromCP(Global.mainTriangles);

if(Global.adjust.visible()) Global.adjust.updateDisplay();

resetLocation();

timer.enable();

updateWindows();

}	//	End of method	mnuRandomClick

/*****************************************************************************/

public void mnuRWeightsClick()
{
stopThread();
updateUndo();
Global.mainCP.randomizeWeights();

timer.enable();

updateWindows();

}	//	End of method	mnuRWeightsClick

/*****************************************************************************/

public void mnuEqualizeClick()
{
stopThread();
updateUndo();

Global.mainCP.equalizeWeights();

timer.enable();

updateWindows();
}

/*****************************************************************************/

public void mnuNormalWeightsClick()
{
stopThread();
updateUndo();


// todo ...

timer.enable();

updateWindows();

}

/*****************************************************************************/

public void mnuCalculateColorsClick()
{
stopThread();
updateUndo();

for(int i=0;i<Global.transforms;i++)
		Global.mainCP.xform[i].color = i/(Global.transforms-1);

timer.enable();

updateWindows();

}	//	End of method	mnuCalculateColorsClick

/*****************************************************************************/

public void mnuRandomizeColorsClick()
{
stopThread();
updateUndo();

for(int i=0;i<Global.transforms;i++)
	Global.mainCP.xform[i].color = Math.random();

timer.enable();

updateWindows();
}

/*****************************************************************************/

public void mnuRandomGradientClick()
{
int n = CMap.cmapnames.length;
int i = (int)(Math.random()*n);

stopThread();
updateUndo();

Global.mainCP.cmapindex = i;
CMap.getCMap(i,1,Global.mainCP.cmap);

timer.enable();

updateWindows();

}	//	End of method	mnuRandomGradientClick

/*****************************************************************************/

public void mnuRandomizeGradientClick()
{
stopThread();
updateUndo();

Global.mainCP.cmapindex = -1;
CMap.copyPalette(CMap.randomGradient(),Global.mainCP.cmap);

timer.enable();

updateWindows();

}	//	End of method	mnuRandomizeGradientClick

/*****************************************************************************/

public void mnuImageClick()
{
}	//	End of method	mnuImageClick

/*****************************************************************************/

public void mnuImageSizeClick()
{

Global.adjust.updateDisplay();
Global.adjust.setTab(3);
Global.adjust.show();

}	//	End of method	mnuImageSizeClick

/*****************************************************************************/

public void tbQualityBoxSet(Object combo)
{


try	{	
	int quality = Integer.parseInt(getString(combo,"text"));
	
	Global.defSampleDensity = quality;

	stopThread();
	timer.enable();
	updateWindows();
	}
catch(Exception ex)
	{
	}

}	//	End of method	tbQualityBoxSet

/*****************************************************************************/

public void mnuItemDeleteClick()
{
Object list = find("ListView");
int index = getSelectedIndex(list);
if(index<0) { beep(); return; }

boolean ok ;

if(Global.confirmDelete)
	{
	ControlPoint cp = (ControlPoint)cps.elementAt(index);
	Object item = getItem(list,index);
	confirm("Permanently delete flame '"+cp.name+"' ?",new DeleteTask(index));	
	}
else
	deleteFlame(index);

}	//	End of method	mnuItemDeleteClick
	
/*****************************************************************************/

void deleteFlame(int index)
{
cps.removeElementAt(index);

updateFlameList();

Object list = find("ListView");

if(cps.size()>index)
	{
	setBoolean(getItem(list,index),"selected",true);
	listViewChange(list);
	}
else if(cps.size()>0)
	{
	setBoolean(getItem(list,cps.size()-1),"selected",true);
	listViewChange(list);
	}

}	//	End of method	deleteFlame

/*****************************************************************************/

public void mnuItemRenameClick()
{
Object list = find("ListView");
int index = getSelectedIndex(list);
if(index<0) { beep(); return; }

ControlPoint cp = (ControlPoint)cps.elementAt(index);

ask("Name of the flame :",cp.name, new FlameRenameTask(index));

}	//	End of method	mnuItemRenameClick

/*****************************************************************************/

public void mnuResetLocationClick()
{
double scale;
double dx,dy,cdx,cdy;
double sina,cosa;

updateUndo();

double p2 = Math.pow(2,Global.mainCP.zoom);
scale = Global.mainCP.pixels_per_unit/Global.mainCP.width*p2;

cdx = Global.mainCP.center[0];
cdy = Global.mainCP.center[1];

resetLocation();

cdx = Global.mainCP.center[0] - cdx;
cdy = Global.mainCP.center[1] - cdy;

sina = Math.sin(Global.mainCP.fangle);
cosa = Math.cos(Global.mainCP.fangle);

if(sina==0)
	{
	dy = cdy*cosa;
	dx = cdx/cosa;
	}
else
	{
	dx = cdy*sina + cdx*cosa;
	dy = (dx*cosa - cdx)/sina;
	}

viewpos[0] = viewpos[0] - dx*scale*imagewidth;
viewpos[1] = viewpos[1] - dy*scale*imageheight;

p2 = Math.pow(2,Global.mainCP.zoom);
viewscale = viewscale*Global.mainCP.pixels_per_unit/Global.mainCP.width*p2/scale;

//drawImageView();

timer.enable();

updateWindows();

}	//	End of method	mnuResetLocationClick

/*****************************************************************************/

public void imageMouseDown(MouseEvent e, Rectangle bounds)
{
if(e.isPopupTrigger())
	{
	Object canvas = find("MainCanvas");
	Rectangle r = new Rectangle(bounds);
	setToAbsolutePosition(canvas,r);
	Object popup = find("MainPopup");
	popupPopup(popup,r.x+e.getX(),r.y+e.getY());
	return;	
	}

click.top = click.bottom = e.getY();
click.left = click.right = e.getX();

switch(fmousemovestate)
	{
	case msZoomWindow:
		select.top = select.bottom = e.getY();
		select.left = select.right = e.getX();
		drawZoomWindow();
		fmousemovestate = msZoomWindowMove;
		break;

	case msZoomOutWindow:
		select.top = select.bottom = e.getY();
		select.left = select.right = e.getX();
		drawZoomWindow();
		fmousemovestate = msZoomOutWindowMove;
		break;

	case msDrag:
		fmousemovestate = msDragMove;
		break;

	case msRotate:
		clickangle = Math.atan2(e.getY()-imageheight/2,imagewidth/2-e.getX());
		rotateangle = 0;
		drawRotateLines(rotateangle);
		fmousemovestate = msRotateMove;
		break;
	}

}	//	End of method	imageMouseDown

/*****************************************************************************/

int xdrag = 0;
int ydrag = 0;

public void imageMouseDrag(MouseEvent e, Rectangle bounds)
{
double dx,dy,cx,cy;
double scale;
int sgn;

if((e.getX()==xdrag)&&(e.getY()==ydrag)) return;

xdrag = e.getX();
ydrag = e.getY();


switch(fmousemovestate)
	{
	case msZoomWindowMove:
	case msZoomOutWindowMove:
		// erase previous selection
		if(drawselection)
			drawZoomWindow();
		click.bottom = ydrag;
		click.right = xdrag;
		dx = xdrag - click.left;
		dy = ydrag - click.top;
		
		sgn = (dy*dx>=0)?1:-1;
		if((dy==0)||(Math.abs(dx/dy)>=imagewidth*1.0/imageheight))
			{
			cy = (ydrag+click.top)/2;
			select.left = click.left;
			select.right = xdrag;
			select.top = (int)(cy-sgn*Math.round(dx/2/imagewidth*imageheight));
			select.bottom =(int)(cy+sgn*Math.round(dx/2/imagewidth*imageheight));
			}
		else
			{
			cx = (xdrag+click.left)/2;
			select.left=(int)(cx-sgn*Math.round(dy/2/imageheight*imagewidth));
			select.right=(int)(cx+sgn*Math.round(dy/2/imageheight*imagewidth));
			select.top = click.top;
			select.bottom = ydrag;
			}
		drawZoomWindow();
		drawselection = true;
		break;

	case msDragMove:
		xview = xdrag-click.left;
		yview = ydrag-click.top;
		repaint();
		break;

	case msRotateMove:
		if(drawselection)
			drawRotateLines(rotateangle);
		rotateangle = Math.atan2(ydrag-imageheight/2,imagewidth/2-xdrag)
			-clickangle;
		drawRotateLines(rotateangle);
		drawselection = true;
		break;
	}

}	//	End of method	imageMouseDrag

/*****************************************************************************/

public void imageMouseUp(MouseEvent e, Rectangle bounds)
{
double scale;


switch(fmousemovestate)
	{
	case msZoomWindowMove:
		drawZoomWindow();
		fmousemovestate = msZoomWindow;
		if(Math.abs(select.left-select.right)<10) return;
		if(Math.abs(select.top-select.bottom)<10) return;
		stopThread();
		updateUndo();
		scale = Global.mainCP.width*1.0/imagewidth;
		Global.mainCP.zoomToRect(scaleRect(select,scale));
		timer.enable();
		updateWindows();
		break;

	case msZoomOutWindowMove:
		drawZoomWindow();
		fmousemovestate = msZoomOutWindow;
		if(Math.abs(select.left-select.right)<10) return;
		if(Math.abs(select.top-select.bottom)<10) return;
		stopThread();
		updateUndo();
		scale = Global.mainCP.width*1.0/imagewidth;
		Global.mainCP.zoomOutToRect(scaleRect(select,scale));
		scale = imagewidth/Math.abs(select.right-select.left);
		timer.enable();	
		updateWindows();
		break;

	case msDragMove:
		fmousemovestate = msDrag;
		scale = Global.mainCP.width*1.0/imagewidth;
		double dx = (xdrag-click.left)*scale;
		double dy = (ydrag-click.top)*scale;
		stopThread();
		updateUndo();
		Global.mainCP.translate(dx,dy);
		timer.enable();
		updateWindows();
		break;

	case msRotateMove:
		drawRotateLines(rotateangle);
		fmousemovestate = msRotate;
		if(rotateangle==0) return;
		stopThread();
		updateUndo();
		if(Global.rotationMode==0)
			Global.mainCP.rotate(rotateangle);
		else
			Global.mainCP.rotate(-rotateangle);
		timer.enable();
		updateWindows();
		break;
	}

}	//	End of method	imageMouseUp

/*****************************************************************************/

void drawZoomWindow()
{
int dx,dy,cx,cy;
int l,r,t,b;

Point pos = getPositionInDesktop(canvas);

Graphics2D g = (Graphics2D)getGraphics();
g.translate(pos.x,pos.y);

g.setXORMode(Color.black);
g.setColor(Color.white);

g.setStroke(dots);
g.drawRect(click.left,click.top,click.right-click.left,
	click.bottom-click.top);


g.setStroke(basic);

dx = select.right - select.left;
if(dx>=0)
	{
	l = select.left-1;
	r = select.right;
	}
else
	{
	dx = -dx;
	l = select.right-1;
	r = select.left;
	}
dx = Math.min(dx/2-1,CORNER_SIZE);

dy = select.bottom - select.top;
if(dy>=0)
	{
	t = select.top-1;	
	b = select.bottom;
	}
else
	{
	dy = -dy;
	t = select.bottom-1;
	b = select.top;
	}
dy = Math.min(dy/2,CORNER_SIZE);

g.drawLine(l+dx,t,l,t);
g.drawLine(l,t,l,t+dy);
g.drawLine(r-dx,t,r,t);
g.drawLine(r,t,r,t+dy);
g.drawLine(r-dx,b,r,b);
g.drawLine(r,b,r,b-dy);
g.drawLine(l+dx,b,l,b);
g.drawLine(l,b,l,b-dy);

g.setPaintMode();
g.setStroke(basic);
g.translate(-pos.x,-pos.y);

}	//	End of method	drawZoomWindow

/*****************************************************************************/

void drawRotateLines(double angle)
{
int points[][] = new int[4][2];
int x,y;
int p0x,p0y,p1x,p1y,p2x,p2y,p3x,p3y;

points[0][0] = imagewidth/2-1;
points[0][1] = imageheight/2-1;
points[1][0] = imagewidth/2-1;
points[1][0] = -imageheight/2;
points[2][0] = -imagewidth/2;
points[2][1] = -imageheight/2;
points[3][0] = -imagewidth/2;
points[3][1] = imageheight/2-1;

x = imagewidth/2-1;
y = imageheight/2-1;
p0x = (int)Math.round(Math.cos(angle)*x+Math.sin(angle)*y) +imagewidth/2;
p0y = (int)Math.round(-Math.sin(angle)*x+Math.cos(angle)*y) +imageheight/2;

x = imagewidth/2-1;
y = -imageheight/2;
p1x = (int)Math.round(Math.cos(angle)*x+Math.sin(angle)*y) +imagewidth/2;
p1y = (int)Math.round(-Math.sin(angle)*x+Math.cos(angle)*y) +imageheight/2;

x = -imagewidth/2;
y = -imageheight/2;
p2x = (int)Math.round(Math.cos(angle)*x+Math.sin(angle)*y) +imagewidth/2;
p2y = (int)Math.round(-Math.sin(angle)*x+Math.cos(angle)*y) +imageheight/2;

x = -imagewidth/2;
y = imageheight/2-1;
p3x = (int)Math.round(Math.cos(angle)*x+Math.sin(angle)*y) +imagewidth/2;
p3y = (int)Math.round(-Math.sin(angle)*x+Math.cos(angle)*y) +imageheight/2;


Point pos = getPositionInDesktop(canvas);

Graphics2D g = (Graphics2D)getGraphics();
g.translate(pos.x,pos.y);

g.setXORMode(Color.black);
g.setColor(Color.white);

g.setStroke(dots);

g.drawLine(p0x,p0y,p1x,p1y);
g.drawLine(p1x,p1y,p2x,p2y);
g.drawLine(p2x,p2y,p3x,p3y);
g.drawLine(p3x,p3y,p0x,p0y);


g.setPaintMode();
g.setStroke(basic);
g.translate(-pos.x,-pos.y);

}	//	End of method	drawRotateLines

/*****************************************************************************/

SRect scaleRect(MouseRect r, double scale)
{
return new SRect(scale*r.left, scale*r.top, scale*r.right, scale*r.bottom);
}	//	End of method	scaleRect

/*****************************************************************************/

public void tbZoomWindowClick()
{
fmousemovestate = msZoomWindow;
}

public void tbZoomOutWindowClick()
{
fmousemovestate = msZoomOutWindow;
}

public void tbDragClick()
{
fmousemovestate = msDrag;
}

public void tbRotateClick()
{
fmousemovestate = msRotate;
}

/*****************************************************************************/
/*****************************************************************************/

public boolean destroy()
{
if(Global.confirmExit)
	{
	confirm("Do you really want to quit? All unsaved data will be lost!",
		new QuitTask());
	return false;
	}
else
	{
	quitApplication();
	return true;
	}

}

/*****************************************************************************/

void quitApplication()
{

Rectangle bounds  = launcher.getBounds();
Global.windowX = bounds.x;
Global.windowY = bounds.y;
Global.windowWidth = bounds.width;
Global.windowHeight = bounds.height;

bounds = getRectangle(find("BackPanel"),"bounds");
if(bounds!=null)
	{
	Global.panelWidth = bounds.width;
	Global.panelHeight = bounds.height;
	}

Global.writeSettings();
}

/*****************************************************************************/
//	ThreadTarget implementation

public void message(int index)
{
if(index==WM_THREAD_COMPLETE)
	handleThreadCompletion();	
}

/*****************************************************************************/

void handleThreadCompletion()
{

if(renderthread!=null)
	{
	xview = 0;
	yview = 0;
	image = renderthread.getImage();
	repaint();
	}

}

/*****************************************************************************/

void restoreWindowPosition()
{

if((Global.windowX>=0)&&(Global.windowY>=0))
	launcher.setLocation(Global.windowX,Global.windowY);
launcher.setSize(Global.windowWidth,Global.windowHeight);

}	//	End of method	restoreBackPanelSize

/*****************************************************************************/

protected void doLayout(Object component) {
super.doLayout(component);

if(component==find("BackPanel"))
	if(timer!=null)
		backPanelResize();
}


/*****************************************************************************/

void backPanelResize()
{

Rectangle bounds = getRectangle(find("BackPanel"),"bounds");
if((bounds.width==panelwidth)&&(bounds.height==panelheight)) return;

panelwidth = bounds.width;
panelheight = bounds.height;


stopThread();

if(Global.canDrawOnResize)
	{
	timer.enable();
	}

resizeImage();

drawImageView();

}	//	End of method	backPanelResize

/*****************************************************************************/

public void setWindowSize(int width, int height, boolean resize)
{

Global.mainCP.adjustScale(width,height);

if(resize)
	{
	Rectangle rwin = launcher.getBounds();
	Rectangle rpan = getRectangle(find("BackPanel"),"bounds");
	if(rpan!=null)
		{
		rwin.width +=  width-rpan.width;
		rwin.height += height-rpan.height;
		launcher.setBounds(rwin);
		launcher.doLayout();
		}
	}
else
	{
	resizeImage();
	timer.enable();
	updateWindows();
	}

/*
resizeImage();

timer.enable();
updateWindows();
*/

}	//	End of method	setWindowSize

/*****************************************************************************/

void stopThread()
{
timer.disable();

if(renderthread!=null)
	renderthread.terminate();

}	//	End of method	stopThread

/*****************************************************************************/

public void mnuOpenScriptClick()
{
Global.script.show();
Global.script.btnOpenClick();
}

/*****************************************************************************/

public void mnuEditScriptClick()
{
Global.script.show();
}	//	End of method	mnuEditScriptClick

/*****************************************************************************/

public void mnuRunScriptClick()
{
Global.script.btnRunClick();
}

/*****************************************************************************/

public void mnuStopScriptClick()
{
Global.script.btnStopClick();
}

/*****************************************************************************/

public void mnuHelpClick()
{
Global.helper.show();
Global.helper.setTopicByName("introduction");
}	//	End of method	mnuHelpClick

/*****************************************************************************/

public void mnuAboutClick()
{
try	{
	Object dialog = parse("about.xml");
	add(dialog);

	setString(find("aboutname"),"text",APPNAME);
	setString(find("aboutversion"),"text","Version "+VERSION);
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}
}

/*****************************************************************************/

public void closeAbout()
{
remove(find("aboutdialog"));
}

/*****************************************************************************/

public void mnuManageFavoritesClick()
{
Global.favorites.show();
}	//	End of method	mnuManageFavoritesClick

/*****************************************************************************/

public void mnuPostSheepClick()
{
Sheep.send(Global.mainCP);
}

/*****************************************************************************/

public void mnuCopyClick()
{

try	{
	StringWriter sw = new StringWriter();
	PrintWriter w = new PrintWriter(sw);
	Global.mainCP.save(w);
	w.flush();

	setClipboard(sw.toString());

	sw.close();
	}
catch(Exception ex)
	{
	}
 
}	//	End of method	mnuCopyClick

/*****************************************************************************/

public void mnuPasteClick()
{
Object list = find("ListView");
int index = getSelectedIndex(list);
if(index<0) return;

String s = getClipboard();
int i = s.indexOf("<flame ");
if(i<0) return;

s = s.substring(i);

try	{
	BufferedReader r = new BufferedReader(new StringReader(s));
	ControlPoint cp = readControlPoint(r);

	if(cp==null) return;

	updateUndo();
	stopThread();

	Global.mainCP.copy(cp);
	Global.transforms = Global.mainCP.trianglesFromCP(Global.mainTriangles);


	Object item = getItem(list,index);
	setString(item,"text",cp.name);
	cps.setElementAt(Global.mainCP,index);
	setStatus(cp.name);
	
	timer.enable();
	updateWindows();
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}	//	End of method	mnuPasteClick

/*****************************************************************************/
/*****************************************************************************/

void resizeImage()

{
int pw,ph;

Rectangle bounds = getRectangle(find("BackPanel"),"bounds");

if(bounds==null) return;

pw = bounds.width-2;
ph = bounds.height-2;
if((1.0*Global.mainCP.width/Global.mainCP.height)>(1.0*pw/ph))
	{
	imagewidth = pw;
	imageheight = (int)(Global.mainCP.height*pw/Global.mainCP.width+0.5);
	}
else
	{
	imageheight = ph;
	imagewidth = (int)(Global.mainCP.width*ph/Global.mainCP.height+0.5);
	}

setInteger(canvas,"width",imagewidth);
setInteger(canvas,"height",imageheight);
int x = bounds.width/2-imagewidth/2;
int y = bounds.height/2-imageheight/2;
setRectangle(canvas,"bounds",x,y,imagewidth,imageheight);

}

/*****************************************************************************/

void drawImageView()
{
}

/*****************************************************************************/
// ThreadTarget implementation

public void output(String msg)
{
}

public void progress(double value)
{

int ivalue = (int)(value*100);
if(ivalue<0) ivalue = 0;
if(ivalue>100) ivalue = 100;

setInteger(find("Status1"),"value",ivalue);

if(value>=1.0)
	{
	long elapsed = System.currentTimeMillis()-startTime;

	long mil = elapsed%1000;
	elapsed /=1000;

	long sec = elapsed%60;
	elapsed /=60;

	long min = elapsed%60;
	elapsed /=60;

	long hou = elapsed;

	String s = "Elapsed : "+
	((hou<10)?("0"+hou):(""+hou)) + ":" + 
	((min<10)?("0"+min):(""+min)) + ":" + 
	((sec<10)?("0"+sec):(""+sec)) + ":" +
	((mil<10)?("00"+min):(mil<100)?("0"+mil):(""+mil));

	setString(find("Status0"),"text",s);
	}

}	//	End of method	progress

/*****************************************************************************/
// Drag and drop implementation

public void dragEnter(DropTargetDragEvent e) {}
public void dragOver(DropTargetDragEvent e) {}
public void dragExit(DropTargetEvent e) {}
public void dropActionChanged(DropTargetDragEvent e) {}

public void drop(DropTargetDropEvent e)
{
Transferable t;

try	{
	e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	t = e.getTransferable();
	File files[] = getFiles(t);
	e.getDropTargetContext().dropComplete(true);
	openFiles(files);
	}
catch(Exception ex)
	{
	ex.printStackTrace();
	}

}   //  End of method   drop

/*****************************************************************************/

File[] getFiles(Transferable t) throws Exception
{
File files[] = null;

DataFlavor flavors[] = t.getTransferDataFlavors();
int nf = flavors.length;
for(int i=0;i<nf;i++)
	{
	Object o = t.getTransferData(flavors[i]);
	if(!(o instanceof java.util.List)) continue;
	java.util.List list = (java.util.List)o;	
	Object a[] = list.toArray();
	if(a[0] instanceof File)
		{
		files = (File[])a;
		break;
		}
	}

return files;

}	//	End of method	getURL

/*****************************************************************************/

public void 	openFiles(File files[])
{
boolean scriptloaded = false;
Vector newcps = new Vector();

for(int i=0;i<files.length;i++)
	{
	// should not occur
	if(!files[i].exists()) continue;

	
	String fname = files[i].getAbsolutePath();
	int k = fname.lastIndexOf('.');
	String ext = (k<0)?"":fname.toLowerCase().substring(k+1);

	if(ext.equals("flame"))
		{
		Vector v = openXMLFile(fname);
		if(newcps.size()==0) Global.openFile = fname;
		appendFlames(v,newcps);
		}
	else if(ext.equals("fla"))
		{
		Vector v = openFLAFile(fname);
		if(newcps.size()==0) Global.openFile = fname;
		appendFlames(v,newcps);
		}
	else if(ext.equals("upr"))
		openUPRFile(fname);
	else if(ext.equals("class"))
		XForm.installPlugin(files[i]);
	else if(ext.equals("jpg"))
		{
		Vector v = openJPGFile(fname);
		if(newcps.size()==0) Global.openFile = fname;
		appendFlames(v,newcps);
		}
	else if(ext.equals("png"))
		{
		Vector v = openPNGFile(fname);
		if(newcps.size()==0) Global.openFile = fname;
		appendFlames(v,newcps);
		}
	else if(ext.equals("ajs"))
		{
		Global.script.openFile(fname,false);
		scriptloaded = true;	
		}
	else if(ext.equals("asc"))
		{
		Global.script.openFile(fname,true);
		scriptloaded = true;
		}
	}

if(newcps.size()>0)
	{
	cps = newcps;
	updateFlameList();

	// select the first flame and force drawing
	Object list = find("ListView");
	setBoolean(getItem(list,0),"selected",true);
	listViewChange(list);

	checkUnknown();
	}

if(scriptloaded)	
	Global.script.btnRunClick();

}	//	End of method	openFile

/*****************************************************************************/

void appendFlames(Vector v, Vector newcps)
{
int n = v.size();
for(int i=0;i<n;i++)
	newcps.addElement(v.elementAt(i));
}

/*****************************************************************************/

void	checkUnknown()
{
// list of all unknown variations
Vector unknown = new Vector();

int n = cps.size();
for(int i=0;i<n;i++)
	{
	ControlPoint cp = (ControlPoint)cps.elementAt(i);
	if(cp.unknown!=null)
		{
		int nu = cp.unknown.size();
		for(int j=0;j<nu;j++)
			{
			String s = (String)cp.unknown.elementAt(j);
			if(!unknown.contains(s)) unknown.addElement(s);
			}
		}
	}

String msg = "";
String sep = "";

int nu = unknown.size();
int k = 0;
for(int j=0;j<nu;j++)
	{
	msg += sep+(String)unknown.elementAt(j);
	sep = ", ";
	k++;
	}

if(k==1)
	alert("Unknown variation : "+msg);
else if(k>1)
	alert("Unknown variations : "+msg);

}

/*****************************************************************************/

void initStrokes()
{
float thickness = 1f;
float miterLimit = 5f;
float[] dashPattern = {5f};
float dashPhase = 2.5f;

dots = new BasicStroke(thickness,
    BasicStroke.CAP_BUTT,
    BasicStroke.JOIN_MITER,
    miterLimit,
    dashPattern,
    dashPhase);

basic = new BasicStroke();

}	//	End of method	initStrokes

/*****************************************************************************/

public void updateFavorites()
{
Vector v  = Global.readFavorites();

Object menu = find("mnuScript");

Object items[] = getItems(menu);
for(int i=8;i<items.length;i++)
	remove(items[i]);

Color color = new Color(0xD1CCC6);

int n = v.size();
for(int i=0;i<n;i++)
	{
	File f = (File)v.elementAt(i);
	String title = f.getName();
	int k = title.lastIndexOf('.');
	if(k>=0) title = title.substring(0,k);
	
	Object menuitem = createImpl("menuitem");
	setString(menuitem,"text",title);
	setColor(menuitem,"background",color);
	putProperty(menuitem,"file",f);

	setMethod(menuitem,"action","favoriteClick(this)",
		getDesktop(),this);

	add(menu,menuitem);	
	}

}	//	End of method	updateFavorites

/*****************************************************************************/

public void favoriteClick(Object menuitem)
{
File f = (File)getProperty(menuitem,"file");
if(f==null) return;

String path = f.getAbsolutePath();
String ext = "ajs";
int i = path.lastIndexOf('.');
if(i>0) ext = path.substring(i+1);

boolean mustconvert = ext.equals("asc");
Global.script.openFile(path,mustconvert);
Global.script.btnRunClick();

}	//	End of method	favoriteClick

/*****************************************************************************/

public void mnuSortClick(int option)
{
Object list = find("ListView");
int index = getSelectedIndex(list);

Object o = (index>=0) ? cps.elementAt(index) : null;

int n = cps.size();
SortableControlPoint ss[] = new SortableControlPoint[n];
for(int i=0;i<n;i++)
	ss[i] = new SortableControlPoint((ControlPoint)cps.elementAt(i),option);

QuickSort.qsort(ss);

for(int i=0;i<n;i++)
	cps.setElementAt(ss[i].cp,(option%2)==0?i:n-1-i);

updateFlameList();

int k = -1;
for(int i=0;i<n;i++)
	if(cps.elementAt(i)==o)
		k = i;

if(k>=0)
	setBoolean(getItem(list,k),"selected",true);
else
	{
	setBoolean(getItem(list,0),"selected",true);
    listViewChange(list);
	}

}	//	End of method	mnuSortClick

/*****************************************************************************/

public void setStatus(String msg)
{
setString(find("Status2"),"text",msg);
}

/*****************************************************************************/

void showMemory(String msg)
{
Runtime r = Runtime.getRuntime();
long free = r.freeMemory();
long total = r.totalMemory();
long max = r.maxMemory();

String sfree = free+"";
while(sfree.length()<10) sfree = " "+sfree;

String stotal = total+"";
while(stotal.length()<10) stotal = " "+stotal;

String smax = max+"";
while(smax.length()<10) smax = " "+smax;

System.out.print("MEMORY ");
System.out.print(sfree);
System.out.print(stotal);
System.out.print(smax);
System.out.print("  ");
System.out.println(msg);

}

/*****************************************************************************/

String getClipboard()
{
String s = "";

Toolkit tk = Toolkit.getDefaultToolkit();
Transferable t = tk.getSystemClipboard().getContents(null);
if(t==null) return "";

try	{
	if(t.isDataFlavorSupported(DataFlavor.stringFlavor))
		s = (String)t.getTransferData(DataFlavor.stringFlavor);
	}
catch(Exception ex)
	{
	}

return s;

}	//	End of method	getClipboard

/*****************************************************************************/

void setClipboard(String s)
{

StringSelection ss = new StringSelection(s);
Toolkit tk = Toolkit.getDefaultToolkit();
tk.getSystemClipboard().setContents(ss,null);

}	//	End of method	setClipboard

/*****************************************************************************/

void setMenuShortcuts()
{

// look for the shortcut mask
Toolkit tk = Toolkit.getDefaultToolkit();
long mask = tk.getMenuShortcutKeyMask();

Object menubar = find("MainMenu");
Object menus[] = getItems(menubar);
for(int i=0;i<menus.length;i++)
	{
	int n = getCount(menus[i]);
	for(int j=0;j<n;j++)
		{
		Object item = getItem(menus[i],j);
		Long L = (Long)get(item,"accelerator");
		if(L==null) continue;
		long acc = L.longValue();
		long mod = L.longValue()>>32;
		if((mod&java.awt.Event.META_MASK)!=0)
			{
			mod = (mod&~java.awt.Event.META_MASK)|mask;
			acc = (acc&0xFFFFFFFFL) | (mod << 32);
			set(item,"accelerator",new Long(acc));
			}
		}
	}

}	//	End of method	setMenuShortcuts

/*****************************************************************************/

class OpenFileTask implements Task {

public void execute()
{
Global.browserPath = Global.opendialog.getBrowserPath();
File file = new File(Global.opendialog.filename);
File files[] = new File[]{file};
openFiles(files);
}

}	//	End of class	OpenFileTask

/*****************************************************************************/
/*****************************************************************************/

class SaveFileTask implements Task {

ControlPoint cp;

SaveFileTask(ControlPoint cp)
{
this.cp = cp;
}

public void execute()
{
Global.browserPath = Global.savedialog.getBrowserPath();
saveXMLFile(cp,Global.savedialog.filename);
}

}	//	End of class	SaveFlameTask
	
/*****************************************************************************/
/*****************************************************************************/

class SmoothPaletteTask implements Task {

public void execute()
{
Global.browserPath = Global.opendialog.getBrowserPath();
smoothPalette();
}

}	//	End of class	SmoothPaletteTask

/*****************************************************************************/

class FlameRenameTask implements Task {

int index;

FlameRenameTask(int index)
{
this.index = index;
}

public void execute()
{
renameFlame(index,_answer);
}

}

/*****************************************************************************/
/*****************************************************************************/

class DeleteTask implements Task {

int index;

DeleteTask(int index)
{
this.index = index;
}

public void execute()
{
deleteFlame(index);
}

}	//	End of class	DeleteTask

/*****************************************************************************/
/*****************************************************************************/

class RenderTask implements Task {

boolean all = false;

RenderTask(boolean all)
{
this.all = all;
}

public void execute()
{
renderToDisk(all);
}

}

/*****************************************************************************/
/*****************************************************************************/

class QuitTask implements Task {

public void execute()
{
quitApplication();
System.exit(0);
}

}

/*****************************************************************************/
/*****************************************************************************/

class Timer extends Thread {

int timerid = 0;

synchronized public void enable()
{
timerid++;
notify();
}

synchronized public void disable()
{
timerid = 0;
}

public void run()
{
int oldid = 0;

while(true)
	{
	synchronized(this) {
		try	{ wait(100); }
		catch(Exception ex) {} 

		// if still the same after 100 ms
		if((timerid>0)&&(timerid==oldid))
			redrawTimerTimer();
		else
			oldid = timerid;
		}
	}
}

}	//	End of class	Timer

/*****************************************************************************/
/*****************************************************************************/

class SortableControlPoint implements MySortable {

ControlPoint cp;
String name;

SortableControlPoint(ControlPoint cp, int option)
{
this.cp = cp;
switch(option)
	{
	case 0:
	case 1:
		name = cp.name;
		break;

	case 2:
	case 3:
		StringBuffer sb = new StringBuffer(cp.name);
		sb.reverse();
		name= sb.toString();
		break;	
	}

}

public long compare(MySortable s)
{
SortableControlPoint scp = (SortableControlPoint)s;
return name.compareTo(scp.name);
}

}

/*****************************************************************************/
/*****************************************************************************/

}	//	End of class	Main

