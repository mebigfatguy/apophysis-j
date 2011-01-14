package org.apophysis;

public class JSOptions implements Constants {

	/*****************************************************************************/
	// FIELDS

	public String ParameterFile;
	public String SmoothPaletteFile;
	public int JPEGQuality;
	public int NumTries;
	public int TryLength;
	public int BatchSize;
	public boolean ConfirmDelete;
	public boolean FixedReference;
	public double SampleDensity;
	public double Gamma;
	public double Brightness;
	public double Vibrancy;
	public int Oversample;
	public double FilterRadius;
	public double PreviewLowQuality;
	public double PreviewMediumQuality;
	public double PreviewHighQuality;
	public boolean UltraFractalCompatible;
	public int MinTransforms;
	public int MaxTransforms;
	public int MutateMinTransforms;
	public int MutateMaxTransforms;
	public String RandomPrefix;
	public boolean KeepBackground;
	public int SymmetryType;
	public int SymmetryOrder;
	public boolean Variations[];
	public int GradientOnRandom;
	public int MinNodes;
	public int MaxNodes;
	public int MinHue;
	public int MaxHue;
	public int MinSaturation;
	public int MaxSaturation;
	public int MinLuminance;
	public int MaxLuminance;
	public double UPRSampleDensity;
	public double UPRFilterRadius;
	public int UPROversample;
	public int UPRWidth;
	public int UPRHeight;
	public String UPRColoringIdent;
	public String UPRColoringFile;
	public String UPRFormulaIdent;
	public String UPRFormulaFile;
	public boolean UPRAjustDensity;

	public boolean FlameInComment;

	/*****************************************************************************/

	JSOptions() {
		int nv = XForm.getNrVariations();
		Variations = new boolean[nv];
	}

	/*****************************************************************************/

	void java2js() {
		ParameterFile = "";
		SmoothPaletteFile = Global.smoothPaletteFile;
		JPEGQuality = Global.jpegQuality;
		NumTries = Global.numTries;
		TryLength = Global.tryLength;
		BatchSize = Global.batchSize;
		ConfirmDelete = Global.confirmDelete;
		FixedReference = false;
		SampleDensity = Global.defSampleDensity;
		Gamma = Global.defGamma;
		Brightness = Global.defBrightness;
		Vibrancy = Global.defVibrancy;
		Oversample = Global.defOversample;
		FilterRadius = Global.defFilterRadius;
		PreviewLowQuality = Global.prevLowQuality;
		PreviewMediumQuality = Global.prevMediumQuality;
		PreviewHighQuality = Global.prevHighQuality;
		UltraFractalCompatible = false;
		MinTransforms = Global.randMinTransforms;
		MaxTransforms = Global.randMaxTransforms;
		MutateMinTransforms = Global.mutantMinTransforms;
		MutateMaxTransforms = Global.mutantMaxTransforms;
		RandomPrefix = Global.randomPrefix;
		KeepBackground = Global.keepBackground;
		SymmetryType = Global.symmetryType;
		SymmetryOrder = Global.symmetryOrder;

		int nv = Variations.length;
		for (int i = 0; i < nv; i++)
			Variations[i] = XForm.isAuthorized(i);

		GradientOnRandom = Global.randGradient;
		MinNodes = Global.minNodes;
		MaxNodes = Global.maxNodes;
		MinHue = Global.minHue;
		MaxHue = Global.maxHue;
		MinSaturation = Global.minSat;
		MaxSaturation = Global.maxSat;
		MinLuminance = Global.minLum;
		MaxLuminance = Global.maxLum;

		/*
		 * UPRSampleDensity; UPRFilterRadius; UPROversample; UPRWidth;
		 * UPRHeight; UPRColoringIdent; UPRColoringFile; UPRFormulaIdent;
		 * UPRFormulaFile; UPRAjustDensity;
		 */

		FlameInComment = Global.jpegComment == 1;

	} // End of method java2js

	/*****************************************************************************/

	void js2java() {
		Global.smoothPaletteFile = SmoothPaletteFile;
		Global.jpegQuality = JPEGQuality;
		Global.numTries = NumTries;
		Global.tryLength = TryLength;
		Global.batchSize = BatchSize;
		Global.confirmDelete = ConfirmDelete;
		Global.defSampleDensity = SampleDensity;
		Global.defGamma = Gamma;
		Global.defBrightness = Brightness;
		Global.defVibrancy = Vibrancy;
		Global.defOversample = Oversample;
		Global.defFilterRadius = FilterRadius;
		Global.prevLowQuality = PreviewLowQuality;
		Global.prevMediumQuality = PreviewMediumQuality;
		Global.prevHighQuality = PreviewHighQuality;
		Global.randMinTransforms = MinTransforms;
		Global.randMaxTransforms = MaxTransforms;
		Global.mutantMinTransforms = MutateMinTransforms;
		Global.mutantMaxTransforms = MutateMaxTransforms;
		Global.randomPrefix = RandomPrefix;
		Global.keepBackground = KeepBackground;
		Global.symmetryType = SymmetryType;
		Global.symmetryOrder = SymmetryOrder;

		int nv = Variations.length;
		for (int i = 0; i < nv; i++)
			XForm.authorizeVariation(i, Variations[i]);

		Global.randGradient = GradientOnRandom;
		Global.minNodes = MinNodes;
		Global.maxNodes = MaxNodes;
		Global.minHue = MinHue;
		Global.maxHue = MaxHue;
		Global.minSat = MinSaturation;
		Global.maxSat = MaxSaturation;
		Global.minLum = MinLuminance;
		Global.maxLum = MaxLuminance;

		/*
		 * UPRSampleDensity; UPRFilterRadius; UPROversample; UPRWidth;
		 * UPRHeight; UPRColoringIdent; UPRColoringFile; UPRFormulaIdent;
		 * UPRFormulaFile; UPRAjustDensity;
		 */

		Global.jpegComment = FlameInComment ? 1 : 0;

	} // End of method put

	/*****************************************************************************/

} // End of class JSOptions
