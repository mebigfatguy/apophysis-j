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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Global implements Constants {

	/*****************************************************************************/
	// FIELDS

	public static String apopath = null;

	public static Main main = null;
	public static Editor editor = null;
	public static Adjust adjust = null;
	public static Browser browser = null;
	public static Mutate mutate = null;
	public static Options options = null;
	public static Export export = null;
	public static Script script = null;
	public static Helper helper = null;
	public static Preview preview = null;
	public static Favorites favorites = null;
	public static Fullscreen fullscreen = null;
	public static Render render = null;

	public static OpenDialog opendialog = null;
	public static SaveDialog savedialog = null;
	public static ColorDialog colordialog = null;
	public static EntryDialog entrydialog = null;

	public static Random randomGenerator = null;

	public static Crypto crypto = null;

	/*****************************************************************************/

	public static int mainSeed;
	public static int transforms = 0; // nb of xforms (not counting final xform
										// )
	public static boolean enableFinalXform = false;
	public static String appPath;
	public static String openFile;
	public static boolean canDrawOnResize = true;
	public static boolean preserveWeights;

	public static boolean resizeMain;
	public static boolean maintainRatio;

	// mainTriangle[M1] is mainTriangle[-1] in delphi
	public static Triangle[] mainTriangles = new Triangle[NXFORMS + 2];

	// UPR options

	public static int uprSampleDensite;
	public static double uprFilterRadius;
	public static int uprOversample;
	public static boolean uprAdjustDensity;
	public static String uprColoringIdent;
	public static String uprColoringFile;
	public static String uprFormulaIdent;
	public static String uprFormulaFile;
	public static int uprWidth;
	public static int uprHeight;
	public static String imageFolder;
	public static String uprPath;
	public static int cmap_index; // index to current gradient

	public static int variation = -1; // current variation

	public static int numTries;
	public static int tryLength; // settings for smooth palette
	public static String smoothPaletteFile;

	// Editor

	public static boolean useFlameBackground;
	public static boolean useTransformColors;
	public static boolean helpersEnabled;
	public static boolean resetLocation;
	public static int editorBkgColor;
	public static int referenceTriangleColor;
	public static int gridColor1;
	public static int gridColor2;
	public static int helpersColor;
	public static boolean extEditEnabled;
	public static boolean transformAxisLock;
	public static boolean doubleClickSetVars;
	public static boolean showAllXforms;

	// Display

	public static double defSampleDensity, defPreviewDensity;
	public static double defGamma, defBrightness, defVibrancy, defFilterRadius;
	public static int defOversample;

	// Render

	public static double renderDensity, renderFilterRadius;
	public static int renderOversample, renderWidth, renderHeight;
	public static int renderBitsPerSample;
	public static String renderPath;
	public static int jpegQuality;
	public static int renderFileFormat;
	public static int internalBitsPerSample;

	public static int nrThreads;
	public static int useNrThreads;

	public static int pngTransparency;
	public static boolean showTransparency;

	public static double mainPreviewScale;
	public static boolean extendMainPreview;

	// Defaults

	public static boolean oldPaletteFormat;
	public static boolean confirmExit;
	public static boolean confirmStopRender;
	public static boolean confirmDelete;

	public static String savePath, smoothPalettePath;
	public static String randomPrefix, randomDate;
	public static int randomIndex;

	public static String flameFile, gradientFile, gradientEntry, flameEntry;
	public static String paramFolder;

	public static double prevLowQuality, prevMediumQuality, prevHighQuality;

	public static String defSmoothPaletteFile;

	public static String browserPath = System.getProperty("user.dir");

	public static int editPrevQual, mutatePrevQual, adjustPrevQual;
	public static int randMinTransforms, randMaxTransforms;
	public static int mutantMinTransforms, mutantMaxTransforms;

	public static boolean keepBackground;
	public static int randGradient;
	public static String randGradientFile;
	public static String defFlameFile;

	public static boolean playSoundOnRenderComplete;
	public static String renderCompleteSoundFile;

	public static boolean saveIncompleteRenders;
	public static boolean showRenderStats;

	public static int symmetryType;
	public static int symmetryOrder;
	public static int symmetryNVars;

	public static boolean variations[];

	public static int rotationMode;
	public static boolean preserveQuality;

	// For random gradients

	public static int minNodes, maxNodes, minHue, maxHue, minSat, maxSat,
			minLum, maxLum;
	public static int referenceMode;
	public static int batchSize;
	public static int compatibility; // 0 = original, 1 = draves

	// Favorites: TStringList;

	public static String scriptPath;

	public static String sheepServer, sheepNick, sheepURL, sheepPW, flam3Path;
	public static int exportBatches, exportOversample, exportWidth;
	public static int exportHeight, exportFileFormat;

	public static double exportFilter, exportDensity;
	public static double exportEstimator, exportEstimatorMin,
			exportEstimatorCurve;
	public static int exportJitters;
	public static double exportGammaThreshold;

	public static int openFileType;

	public static boolean showProgress;
	public static String defLibrary;
	public static boolean limitVibrancy = true;

	// DefaultPalette: TColorMap;

	public static boolean debug = false;

	// Image Size Presets
	// 0 = left, 1=top, 2=width, 3=height
	public static int[][] sizepresets = new int[3][4];

	public static ControlPoint mainCP = null;

	// screen
	public static int windowX;
	public static int windowY;
	public static int windowWidth;
	public static int windowHeight;

	public static int panelWidth;
	public static int panelHeight;

	// flame parameter encryption parameter
	public static int jpegComment;
	public static int encryptedComment;
	public static String passwordText;

	public static int watermark;
	public static int watermarkPosition = 8;
	public static String watermarkFile;

	/*****************************************************************************/

	double det(double a, double b, double c, double d) {
		return (a * d - b * c);
	}

	/*****************************************************************************/

	double round6(double x) {
		long l = (long) (x * 1000000);
		return l / 1000000.0;
	}

	/*****************************************************************************/

	double solve3(double x1, double x2, double x1h, double y1, double y2,
			double y1h, double z1, double z2, double z1h, double abe[]) {
		double det1;

		det1 = x1 * det(y2, 1.0, z2, 1.0) - x2 * det(y1, 1.0, z1, 1.0) + 1
				* det(y1, y2, z1, z2);

		if (det1 != 0.0) {
			abe[0] = (x1h * det(y2, 1.0, z2, 1.0) - x2
					* det(y1h, 1.0, z1h, 1.0) + 1 * det(y1h, y2, z1h, z2))
					/ det1;
			abe[1] = (x1 * det(y1h, 1.0, z1h, 1.0) - x1h
					* det(y1, 1.0, z1, 1.0) + 1 * det(y1, y1h, z1, z1h))
					/ det1;
			abe[2] = (x1 * det(y2, y1h, z2, z1h) - x2 * det(y1, y1h, z1, z1h) + x1h
					* det(y1, y2, z1, z2))
					/ det1;
			abe[0] = round6(abe[0]);
			abe[1] = round6(abe[1]);
			abe[2] = round6(abe[2]);
		}

		return det1;

	} // End of method solve3

	/*****************************************************************************/

	static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	/*****************************************************************************/

	static double line_dist(double x, double y, double x1, double y1,
			double x2, double y2) {
		double a, b, e, c;

		a = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
		b = Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
		e = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		if ((a * a + e * e) < b * b) {
			return a;
		} else if ((b * b + e * e) < a * a) {
			return b;
		} else if (e != 0) {
			c = (b * b - a * a - e * e) / (-2 * e);
			if ((a * a - c * c) < 0.0) {
				return 0.0;
			} else {
				return Math.sqrt(a * a - c * c);
			}
		} else {
			return a;
		}
	}

	/*****************************************************************************/
	/*****************************************************************************/
	// PREFERENCES

	static Map<String, String> values = new ConcurrentHashMap<String, String>();

	/*****************************************************************************/

	static void readSettings() {

		SimpleDateFormat fmt = new SimpleDateFormat("yyMMdd");
		String today = fmt.format(new Date());

		String defaultPath = System.getProperty("user.dir");

        File f = new File(Global.apopath, CONFNAME);
		try (BufferedReader r = new BufferedReader(new FileReader(f))) {
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}

				int i = line.indexOf('=');
				if (i < 0) {
					continue;
				}

				String key = line.substring(0, i).trim();
				String val = line.substring(i + 1).trim();
				values.put(key, val);
			}
		} catch (FileNotFoundException fnfex) {
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		defFlameFile = getString("DefaultFlameFile", "");
		gradientFile = getString("GradientFile", "");
		savePath = getString("SavePath", "MyFlames.flame");
		defSmoothPaletteFile = getString("SmoothPaletteFile", "smooth.ugr");
		playSoundOnRenderComplete = getBoolean("PlaySoundOnRenderComplete",
				false);
		renderCompleteSoundFile = getString("RenderCompleteSoundFile", "");

		confirmDelete = getBoolean("ConfirmDelete", true);
		oldPaletteFormat = getBoolean("OldPaletteFormat", false);
		confirmExit = getBoolean("ConfirmExit", true);
		confirmStopRender = getBoolean("ConfirmStopRender", true);
		preserveQuality = getBoolean("PreserveQuality", true);
		keepBackground = getBoolean("KeepBackground", false);

		resizeMain = getBoolean("Adjust.ResizeMain", false);
		maintainRatio = getBoolean("Adjust.MaintainRatio", false);

		numTries = getInt("NumTries", 10);
		tryLength = getInt("TryLength", 100000);
		randMinTransforms = getInt("MinTransforms", 2);
		randMaxTransforms = getInt("MaxTransforms", 3);
		mutantMinTransforms = getInt("MutationMinTransforms", 2);
		mutantMaxTransforms = getInt("MutationMaxTransforms", 6);
		randGradient = getInt("RandomGradient", 0);
		randGradientFile = getString("RandomGradientFile", "");

		paramFolder = getString("ParameterFolder", "Parameters");
		uprPath = getString("UPRPath", defaultPath);
		imageFolder = getString("ImageFolder", defaultPath);

		uprWidth = getInt("UPRWidth", 640);
		uprHeight = getInt("UPRheight", 480);

		browserPath = getString("BrowserPath", defaultPath);

		editPrevQual = getInt("EditPreviewQuality", 1);
		mutatePrevQual = getInt("MutatePreviewQuality", 1);
		adjustPrevQual = getInt("AdjustPreviewQuality", 1);

		randomPrefix = getString("RandomPrefix", "Apophysis-");
		randomDate = getString("RandomDate", "");
		randomIndex = getInt("RandomIndex", 0);
		if (!randomDate.equals(today)) {
			randomDate = today;
			randomIndex = 0;
		}

		symmetryType = getInt("SymmetryType", 0);
		symmetryOrder = getInt("SymmetryOrder", 4);
		symmetryNVars = getInt("SymmetryNVars", 12);

		minNodes = getInt("MinNodes", 2);
		minHue = getInt("MinHue", 0);
		minSat = getInt("MinSta", 0);
		minLum = getInt("MinLum", 0);
		maxNodes = getInt("MaxNodes", 10);
		maxHue = getInt("MaxHue", 600);
		maxSat = getInt("MaxSat", 100);
		maxLum = getInt("MaxLum", 100);

		// randomGradientFile = getString("RandomGradientFile","");

		referenceMode = getInt("ReferenceMode", 0);
		rotationMode = getInt("RotationMode", 0);

		batchSize = getInt("BatchSize", 1);

		scriptPath = getString("ScriptPath", "Scripts");
		defLibrary = getString("FunctionLibrary", "");

		exportFileFormat = getInt("ExportFileFormat", 1);
		exportWidth = getInt("ExportWidth", 640);
		exportHeight = getInt("ExportHeight", 480);
		exportDensity = getDouble("ExportDensity", 100);
		exportOversample = getInt("ExportOversample", 2);
		exportFilter = getDouble("ExportFilter", 0.6);
		exportBatches = getInt("ExportBatches", 3);

		sheepNick = getString("nick", "");
		sheepURL = getString("URL", "");
		sheepPW = getString("PASS", "");
		sheepServer = getString("SheepServer", "sheepserver.net");

		flam3Path = getString("Renderer", "");

		showProgress = getBoolean("ShowProgress", true);
		saveIncompleteRenders = getBoolean("SaveIncompleteRenders", false);
		showRenderStats = getBoolean("ShowRenderStats", false);

		pngTransparency = getInt("PNGTransparency", 1);
		showTransparency = getBoolean("ShowTransparency", false);
		extendMainPreview = getBoolean("ExtendMainPreview", true);

		mainPreviewScale = getDouble("MainPreviewScale", 1);
		nrThreads = getInt("NrThreads", 1);
		internalBitsPerSample = getInt("InternalBitsPerSample", 0);

		jpegComment = getInt("Render.JPEGComment", 1);
		encryptedComment = getInt("Render.EncryptedComment", 0);
		passwordText = getString("Render.Password", "");

		watermark = getInt("Render.Watermark", 0);
		watermarkPosition = getInt("Render.WatermarkPosition", 8);
		watermarkFile = getString("Render.WatermarkFile", "");

		// EDITOR

		useTransformColors = getBoolean("Editor.UseTransformColors", false);
		helpersEnabled = getBoolean("Editor.HelpersEnabled", true);
		showAllXforms = getBoolean("Editor.ShowAllXforms", true);
		resetLocation = getBoolean("Editor.ResetLocation", false);
		editorBkgColor = getInt("Editor.BackgroundColor", 0x000000);
		gridColor1 = getInt("Editor.GridColor1", 0x444444);
		gridColor2 = getInt("Editor.GridColor2", 0x333333);
		helpersColor = getInt("Editor.HelpersColor", 0x808080);
		referenceTriangleColor = getInt("Editor.ReferenceTriangleColor",
				0x7F7F7F);
		extEditEnabled = getBoolean("Editor.ExtendedEdit", true);
		transformAxisLock = getBoolean("Editor.LockTransformAxis", true);
		doubleClickSetVars = getBoolean("Editor.DoubleClickSetVars", true);

		// RENDER

		renderPath = getString("Render.Path", defaultPath);
		renderDensity = getDouble("Render.SampleDensity", 200);
		renderFilterRadius = getDouble("Render.FilterRadius", 0.4);
		renderOversample = getInt("Render.Oversample", 2);
		renderWidth = getInt("Render.Width", 1024);
		renderHeight = getInt("Render.Height", 768);

		jpegQuality = getInt("Render.JPEGQuality", 100);

		renderFileFormat = getInt("Render.FileFormat", 3);
		renderBitsPerSample = getInt("Render.BitsPerSample", 0);

		defOversample = getInt("Display.Oversample", 1);
		defPreviewDensity = getDouble("Display.PreviewDensity", 0.5);
		defFilterRadius = getDouble("Display.FilterRadius", 0.2);
		defVibrancy = getDouble("Display.Vibrancy", 1.0);
		defBrightness = getDouble("Display.Brightness", 4.0);
		defGamma = getDouble("Display.Gamma", 4.0);
		defSampleDensity = getDouble("Display.SampleDensity", 5.0);

		prevLowQuality = getDouble("Display.PreviewLowQuality", 0.1);
		prevMediumQuality = getDouble("Display.PreviewMediumQuality", 1.0);
		prevHighQuality = getDouble("Display.PreviewHighQuality", 5.0);

		for (int i = 0; i < 3; i++) {
			sizepresets[i][0] = getInt("Preset" + i + ".Left", 40);
			sizepresets[i][1] = getInt("Preset" + i + ".Top", 60);
			sizepresets[i][2] = getInt("Preset" + i + ".Width", 512);
			sizepresets[i][3] = getInt("Preset" + i + ".Height", 384);
		}

		// screen size
		windowX = getInt("Window.X", -1);
		windowY = getInt("Window.Y", -1);
		windowWidth = getInt("Window.Width", 660);
		windowHeight = getInt("Window.Height", 552);

		if (windowWidth < 400) {
			windowWidth = 400;
		}
		if (windowHeight < 300) {
			windowHeight = 300;
		}

		panelWidth = getInt("Panel.Width", 100);
		panelHeight = getInt("Panel.Height", 100);

		// get old format
		int nv = XForm.getNrVariations();
		variations = new boolean[nv];

		// variationOptions = getLong("VariationOptions",262143)
		long variationOptions = getLong("VariationOptions", -1);
		if (variationOptions < 0) {
			// get new format
			int ns = 0;
			for (int i = 0; i < nv; i++) {
				String vname = XForm.getVariation(i).getName();
				variations[i] = getBoolean("Variation." + vname, false);
				if (variations[i]) {
					ns++;
				}
			}
			if (ns == 0) {
				variationOptions = 0;
			}
		}

		if (variationOptions >= 0) {
			// translate old format
			if (variationOptions == 0) {
				variationOptions = 262143;
			}

			for (int i = 0; i < nv; i++) {
				variations[i] = false;
			}
			for (int i = 0; i < 64; i++) {
				if (i < nv) {
					variations[i] = ((variationOptions >> i) & 1) != 0;
				}
			}
		}

	} // End of method readSettings

	/*****************************************************************************/

	static void writeSettings() {

        File f = new File(Global.apopath, CONFNAME);
		try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(f)))) {

			writeString(w, "DefaultFlameFile", defFlameFile);
			writeString(w, "GradientFile", gradientFile);
			writeString(w, "SavePath", savePath);
			writeString(w, "SmoothPaletteFile", defSmoothPaletteFile);

			writeBoolean(w, "PlaySoundOnRenderComplete",
					playSoundOnRenderComplete);
			writeString(w, "RenderCompleteSoundFile", renderCompleteSoundFile);

			writeBoolean(w, "ConfirmDelete", confirmDelete);
			writeBoolean(w, "OldPaletteFormat", oldPaletteFormat);
			writeBoolean(w, "ConfirmExit", confirmExit);
			writeBoolean(w, "ConfirmStopRender", confirmStopRender);
			writeBoolean(w, "PreserveQuality", preserveQuality);
			writeBoolean(w, "KeepBackground", keepBackground);

			writeBoolean(w, "Adjust.ResizeMain", resizeMain);
			writeBoolean(w, "Adjust.MaintainRatio", maintainRatio);

			writeInt(w, "NumTries", numTries);
			writeInt(w, "TryLength", tryLength);
			writeInt(w, "MinTransforms", randMinTransforms);
			writeInt(w, "MaxTransforms", randMaxTransforms);
			writeInt(w, "MutationMinTransforms", mutantMinTransforms);
			writeInt(w, "MutationMaxTransforms", mutantMaxTransforms);
			writeInt(w, "RandomGradient", randGradient);
			writeString(w, "RandomGradientFile", randGradientFile);

			writeString(w, "ParameterFolder", paramFolder);
			writeString(w, "UPRPath", uprPath);
			writeString(w, "ImageFolder", imageFolder);

			writeInt(w, "UPRWidth", uprWidth);
			writeInt(w, "UPRHeight", uprHeight);

			writeString(w, "BrowserPath", browserPath);

			writeInt(w, "EditPreviewQuality", editPrevQual);
			writeInt(w, "MutatePreviewQuality", mutatePrevQual);
			writeInt(w, "AdjustPreviewQuality", adjustPrevQual);

			writeString(w, "RandomPrefix", randomPrefix);
			writeString(w, "RandomDate", randomDate);
			writeInt(w, "RandomIndex", randomIndex);

			writeInt(w, "SymmetryType", symmetryType);
			writeInt(w, "SymmetryOrder", symmetryOrder);
			writeInt(w, "SymmetryNVars", symmetryNVars);

			writeInt(w, "MinNodes", minNodes);
			writeInt(w, "MinHue", minHue);
			writeInt(w, "MinSat", minSat);
			writeInt(w, "MinLum", minLum);

			writeInt(w, "MaxNodes", maxNodes);
			writeInt(w, "MaxHue", maxHue);
			writeInt(w, "MaxSat", maxSat);
			writeInt(w, "MaxLum", maxLum);

			writeInt(w, "ReferenceMode", referenceMode);
			writeInt(w, "RotationMode", rotationMode);

			writeInt(w, "BatchSize", batchSize);

			writeString(w, "ScriptPath", scriptPath);
			writeString(w, "FunctionLibrary", defLibrary);

			writeInt(w, "ExportFileFormat", exportFileFormat);
			writeInt(w, "ExportWidth", exportWidth);
			writeInt(w, "ExportHeight", exportHeight);
			writeDouble(w, "ExportDensity", exportDensity);
			writeInt(w, "ExportOversample", exportOversample);
			writeDouble(w, "ExportFilter", exportFilter);

			writeInt(w, "ExportBatches", exportBatches);

			writeString(w, "nick", sheepNick);
			writeString(w, "URL", sheepURL);
			writeString(w, "PASS", sheepPW);
			writeString(w, "SheepServer", sheepServer);

			writeString(w, "Renderer", flam3Path);

			writeBoolean(w, "ShowProgress", showProgress);
			writeBoolean(w, "SaveIncompleteRenders", saveIncompleteRenders);
			writeBoolean(w, "ShowrenderStats", showRenderStats);

			writeInt(w, "PNGTransparency", pngTransparency);
			writeBoolean(w, "ShowTransparency", showTransparency);
			writeBoolean(w, "ExtendMainPreview", extendMainPreview);

			writeDouble(w, "MainPreviewScale", mainPreviewScale);
			writeInt(w, "NrThreads", nrThreads);
			writeInt(w, "InternalBitsPerSample", internalBitsPerSample);

			// EDITOR

			writeBoolean(w, "Editor.UseTransformColors", useTransformColors);
			writeBoolean(w, "Editor.HelpersEnabled", helpersEnabled);
			writeBoolean(w, "Editor.ResetLocation", resetLocation);
			writeBoolean(w, "Editor.ShowAllXforms", showAllXforms);
			writeInt(w, "Editor.BackgroundColor", editorBkgColor);
			writeInt(w, "Editor.GridColor1", gridColor1);
			writeInt(w, "Editor.GridColor2", gridColor2);
			writeInt(w, "Editor.HelpersColor", helpersColor);
			writeInt(w, "Editor.ReferenceTriangleColor", referenceTriangleColor);

			writeBoolean(w, "Editor.ExtendedEdit", extEditEnabled);
			writeBoolean(w, "Editor.LockTransformAxis", transformAxisLock);
			writeBoolean(w, "Editor.DoubleClickSetVars", doubleClickSetVars);

			writeInt(w, "Display.Oversample", defOversample);
			writeDouble(w, "Display.PreviewDensity", defPreviewDensity);
			writeDouble(w, "Display.FilterRadius", defFilterRadius);
			writeDouble(w, "Display.Vibrancy", defVibrancy);
			writeDouble(w, "Display.Brightness", defBrightness);
			writeDouble(w, "Display.Gamma", defGamma);
			writeDouble(w, "Display.SampleDensity", defSampleDensity);

			writeDouble(w, "Display.PreviewLowQuality", prevLowQuality);
			writeDouble(w, "Display.PreviewMediumQuality", prevMediumQuality);
			writeDouble(w, "Display.PreviewHighQuality", prevHighQuality);

			writeString(w, "Render.Path", renderPath);
			writeDouble(w, "Render.SampleDensity", renderDensity);
			writeDouble(w, "Render.FilterRadius", renderFilterRadius);
			writeInt(w, "Render.Oversample", renderOversample);
			writeInt(w, "Render.Width", renderWidth);
			writeInt(w, "Render.Height", renderHeight);

			writeInt(w, "Render.JPEGQuality", jpegQuality);

			writeInt(w, "Render.FileFormat", renderFileFormat);
			writeInt(w, "Render.BitsPerSample", renderBitsPerSample);

			writeInt(w, "Render.JPEGComment", jpegComment);
			writeInt(w, "Render.EncryptedComment", encryptedComment);
			writeString(w, "Render.Password", passwordText);

			writeInt(w, "Render.Watermark", watermark);
			writeInt(w, "Render.WatermarkPosition", watermarkPosition);
			writeString(w, "Render.WatermarkFile", watermarkFile);

			for (int i = 0; i < 3; i++) {
				writeInt(w, "Preset" + i + ".Left", sizepresets[i][0]);
				writeInt(w, "Preset" + i + ".Top", sizepresets[i][1]);
				writeInt(w, "Preset" + i + ".Width", sizepresets[i][2]);
				writeInt(w, "Preset" + i + ".Height", sizepresets[i][3]);
			}

			writeInt(w, "Window.X", windowX);
			writeInt(w, "Window.Y", windowY);
			writeInt(w, "Window.Width", windowWidth);
			writeInt(w, "Window.Height", windowHeight);

			writeInt(w, "Panel.Width", panelWidth);
			writeInt(w, "Panel.Height", panelHeight);

			// VARIATIONS

			int nv = XForm.getNrVariations();
			for (int i = 0; i < nv; i++) {
				String vname = XForm.getVariation(i).getName();
				if (variations[i]) {
					writeBoolean(w, "Variation." + vname, true);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method saveConfig

	/*****************************************************************************/

	static boolean getBoolean(String key, boolean value) {
		String s = values.get(key);
		if (s == null) {
			return value;
		} else {
			return Integer.parseInt(s) != 0;
		}
	}

	/*****************************************************************************/

	static double getDouble(String key, double value) {
		String s = values.get(key);
		if (s == null) {
			return value;
		} else {
			return Double.parseDouble(s);
		}
	}

	/*****************************************************************************/

	static int getInt(String key, int value) {
		String s = values.get(key);
		if (s == null) {
			return value;
		} else {
			return Integer.parseInt(s);
		}
	}

	/*****************************************************************************/

	static long getLong(String key, long value) {
		String s = values.get(key);
		if (s == null) {
			return value;
		} else {
			return Long.parseLong(s);
		}
	}

	/*****************************************************************************/

	static String getString(String key, String value) {
		String s = values.get(key);
		if (s == null) {
			return value;
		} else {
			return s;
		}
	}

	/*****************************************************************************/

	static void writeBoolean(PrintWriter w, String key, boolean value) {
		w.print(key);
		w.print("=");
		w.print(value ? "1" : "0");
		w.println("");
	}

	/*****************************************************************************/

	static void writeInt(PrintWriter w, String key, int value) {
		w.print(key);
		w.print("=");
		w.print(value);
		w.println("");
	}

	/*****************************************************************************/

	static void writeBoolean(PrintWriter w, String key, String value) {
		w.print(key);
		w.print("=");
		w.print(value);
		w.println("");
	}

	/*****************************************************************************/

	static void writeLong(PrintWriter w, String key, long value) {
		w.print(key);
		w.print("=");
		w.print(value);
		w.println("");
	}

	/*****************************************************************************/

	static void writeString(PrintWriter w, String key, String value) {
		w.print(key);
		w.print("=");
		w.print(value);
		w.println("");
	}

	/*****************************************************************************/

	static void writeDouble(PrintWriter w, String key, double value) {
		w.print(key);
		w.print("=");
		w.print(value);
		w.println("");
	}

	/*****************************************************************************/

	static List<File> readFavorites() {
		List<File> v = new ArrayList<File>();

        File f = new File(apopath, FAVNAME);
		try (BufferedReader r = new BufferedReader(new FileReader(f))){
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}

				File fs = new File(line.trim());
				if (fs.exists()) {
					v.add(fs);
				}
			}
		} catch (FileNotFoundException fnfex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return v;

	} // End of method readFavorites

	/*****************************************************************************/

	static void writeFavorites(List<File> v) {

        File f = new File(apopath, FAVNAME);
		try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(f)))) {
			int n = v.size();
			for (int i = 0; i < n; i++) {
				File fs = v.get(i);
				w.println(fs.getAbsolutePath());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	} // End of method writeFavorites

	/*****************************************************************************/

	public static List<Preset> readPresets() {
		List<Preset> v = new ArrayList<Preset>();

        File file = new File(apopath, PRSTNAME);
		try (BufferedReader r = new BufferedReader(new FileReader(file))) {
			
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}

				line = line.trim();
				if (line.endsWith("{")) {
					line = line.substring(0, line.length() - 1).trim();
					List<String> w = new ArrayList<String>();
					w.add(line);
					while (true) {
						line = r.readLine();
						if (line == null) {
							break;
						}
						if (line.startsWith("}")) {
							break;
						}
						w.add(line.trim());
					}
					Preset preset = new Preset(w);
					v.add(preset);
				}
			}
		} catch (FileNotFoundException fnfex) {
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return v;

	} // End of method readPresets

	/*****************************************************************************/

	public static void writePresets(List<Preset> v) {
        File f = new File(apopath, PRSTNAME);
		try (PrintWriter w = new PrintWriter(new FileWriter(f))) {

			int n = v.size();
			for (int i = 0; i < n; i++) {
				Preset preset = v.get(i);
				preset.write(w);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method writePresets

	/*****************************************************************************/

	public static void copyFile(File filesrc, File filedst) throws IOException {
		if (!filesrc.exists()) {
			throw new IOException("Cannot copy " + filesrc.getName());
		}

		InputStream is = null;
		OutputStream os = null;

		try {
			byte[] buffer = new byte[512];
			is = new BufferedInputStream(new FileInputStream(filesrc));
			os = new BufferedOutputStream(new FileOutputStream(filedst));
			while (true) {
				int n = is.read(buffer);
				if (n <= 0) {
					break;
				}
				os.write(buffer, 0, n);
			}
		} finally {
			IOUtils.close(is);
			IOUtils.close(os);
		}

	} // End of method copyFile

	/*****************************************************************************/

	public static double random() {
		return randomGenerator.nextDouble();
	}

	/*****************************************************************************/

} // End of class Global

