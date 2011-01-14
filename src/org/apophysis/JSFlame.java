package org.apophysis;

public class JSFlame implements Constants {

	/*****************************************************************************/
	// FIELDS

	public double Gamma;
	public double Brightness;
	public double Vibrancy;
	public double Time;
	public double Zoom;
	public double X;
	public double Y;
	public double Width;
	public double Height;
	public double SampleDensity;
	public double Quality;
	public int Oversample;
	public double FilterRadius;
	public double Scale;

	public int[] Background = new int[3];
	public int[][] Gradient = new int[256][3];

	public String Name;
	public String Nick = "";
	public String URL = "";

	public double Hue;
	public int Batches;
	public boolean FinalXformEnabled;
	public double Angle;

	int nxforms = 0;

	private Script script = null;
	protected ControlPoint cp = null;

	/*****************************************************************************/

	JSFlame(Script script) {
		this.script = script;

		this.cp = new ControlPoint();

	}

	/*****************************************************************************/

	void java2js() {

		Name = cp.name;
		SampleDensity = cp.sample_density;
		Oversample = cp.spatial_oversample;
		FilterRadius = cp.spatial_filter_radius;
		Scale = cp.pixels_per_unit;

		Gamma = cp.gamma;
		Brightness = cp.brightness;
		Vibrancy = cp.vibrancy;
		Time = cp.time;
		Zoom = cp.zoom;
		X = cp.center[0];
		Y = cp.center[1];
		Width = cp.width;
		Height = cp.height;

		Quality = cp.actual_density;

		Background[0] = cp.background[0];
		Background[1] = cp.background[1];
		Background[2] = cp.background[2];

		for (int i = 0; i < 256; i++) {
			Gradient[i][0] = cp.cmap[i][0];
			Gradient[i][1] = cp.cmap[i][1];
			Gradient[i][2] = cp.cmap[i][2];
		}

		nxforms = cp.nxforms;

		FinalXformEnabled = cp.hasFinalXform;

	} // End of method java2js

	/*****************************************************************************/

	void js2java() {
		cp.name = Name;
		cp.sample_density = SampleDensity;
		cp.spatial_oversample = Oversample;
		cp.spatial_filter_radius = FilterRadius;
		cp.width = (int) Width;
		cp.height = (int) Height;
		cp.pixels_per_unit = Scale;
		cp.zoom = Zoom;

		cp.gamma = Gamma;
		cp.brightness = Brightness;
		cp.vibrancy = Vibrancy;
		cp.time = Time;
		cp.center[0] = X;
		cp.center[1] = Y;

		cp.actual_density = Quality;

		cp.background[0] = Background[0];
		cp.background[1] = Background[1];
		cp.background[2] = Background[2];

		for (int i = 0; i < 256; i++) {
			cp.cmap[i][0] = Gradient[i][0];
			cp.cmap[i][1] = Gradient[i][1];
			cp.cmap[i][2] = Gradient[i][2];
		}

		cp.nxforms = nxforms;

		cp.finalXformEnabled = FinalXformEnabled;
		cp.hasFinalXform = FinalXformEnabled;

	} // End of method js2java

	/*****************************************************************************/

	public JSFlame Clone() {
		JSFlame f = new JSFlame(script);

		f.cp.copy(cp);

		f.Gamma = Gamma;
		f.Brightness = Brightness;
		f.Vibrancy = Vibrancy;
		f.Time = Time;
		f.Zoom = Zoom;
		f.X = X;
		f.Y = Y;
		f.Width = Width;
		f.Height = Height;
		f.SampleDensity = SampleDensity;
		f.Quality = Quality;
		f.Oversample = Oversample;
		f.FilterRadius = FilterRadius;
		f.Scale = Scale;
		f.Hue = Hue;
		f.Batches = Batches;
		f.FinalXformEnabled = FinalXformEnabled;
		f.Angle = Angle;

		f.nxforms = nxforms;

		f.Name = Name;
		f.Nick = Nick;
		f.URL = URL;

		for (int i = 0; i < 3; i++) {
			f.Background[i] = Background[i];
		}

		CMap.copyPalette(Gradient, f.Gradient);

		return f;

	} // End of method Clone

	/*****************************************************************************/

} // End of class JSFlame
