package org.apophysis;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class JSTransform extends ScriptableObject {

	/*****************************************************************************/

	public double A;
	public double B;
	public double C;
	public double D;
	public double E;
	public double F;
	public double Color;
	public double Symmetry;
	public double Weight;

	public double Variation[];
	public double Variable[];

	boolean initialized = false;

	private Script script = null;

	/*****************************************************************************/

	JSTransform(Script script) {
		this.script = script;

		int nv = XForm.getNrVariations();
		Variation = new double[nv];

		int np = XForm.getNrParameters();
		Variable = new double[np];

	}

	/*****************************************************************************/

	@Override
	public String getClassName() {
		return "Transform";
	}

	/*****************************************************************************/

	void init() {

		ScriptableObject.putProperty(this, "Variation",
				Context.javaToJS(Variation, this));

		ScriptableObject.putProperty(this, "Variable",
				Context.javaToJS(Variable, this));

		ScriptableObject.putProperty(this, "Clear",
				Context.javaToJS(new JSTransformClear(), this));

		ScriptableObject.putProperty(this, "Rotate",
				Context.javaToJS(new JSTransformRotate(), this));

		ScriptableObject.putProperty(this, "Scale",
				Context.javaToJS(new JSTransformScale(), this));

		ScriptableObject.putProperty(this, "RotateOrigin",
				Context.javaToJS(new JSTransformRotateOrigin(), this));

		initialized = true;
	}

	/*****************************************************************************/

	void java2js(ControlPoint cp, int index) {
		if (!initialized)
			init();

		A = cp.xform[index].c00;
		B = -cp.xform[index].c01;
		C = -cp.xform[index].c10;
		D = cp.xform[index].c11;
		E = cp.xform[index].c20;
		F = cp.xform[index].c21;

		Color = cp.xform[index].color;
		Symmetry = cp.xform[index].symmetry;
		Weight = cp.xform[index].density;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++)
			Variation[i] = cp.xform[index].vars[i];

		int kp = 0;
		for (int i = 0; i < nv; i++) {
			int np = XForm.getVariation(i).getNrParameters();
			for (int j = 0; j < np; j++) {
				Variable[kp] = cp.xform[index].pvalues[kp];
				kp++;
			}
		}

		updateProperties();

	} // End of method java2js

	/*****************************************************************************/

	void updateProperties() {
		ScriptableObject.putProperty(this, "A",
				Context.javaToJS(new Double(A), this));

		ScriptableObject.putProperty(this, "B",
				Context.javaToJS(new Double(B), this));

		ScriptableObject.putProperty(this, "C",
				Context.javaToJS(new Double(C), this));

		ScriptableObject.putProperty(this, "D",
				Context.javaToJS(new Double(D), this));

		ScriptableObject.putProperty(this, "E",
				Context.javaToJS(new Double(E), this));

		ScriptableObject.putProperty(this, "F",
				Context.javaToJS(new Double(F), this));

		ScriptableObject.putProperty(this, "Color",
				Context.javaToJS(new Double(Color), this));

		ScriptableObject.putProperty(this, "Symmetry",
				Context.javaToJS(new Double(Symmetry), this));

		ScriptableObject.putProperty(this, "Weight",
				Context.javaToJS(new Double(Weight), this));

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			ScriptableObject.putProperty(this, XForm.getVariation(i).getName(),
					Context.javaToJS(new Double(Variation[i]), this));
		}

		int kp = 0;
		for (int i = 0; i < nv; i++) {
			int np = XForm.getVariation(i).getNrParameters();
			for (int j = 0; j < np; j++) {
				ScriptableObject.putProperty(this, XForm.getVariation(i)
						.getParameterName(j), Context.javaToJS(new Double(
						Variable[kp]), this));
				kp++;
			}
		}
	} // End of method updateProperties

	/*****************************************************************************/

	void js2java(ControlPoint cp, int index) {
		if (!initialized)
			init();

		Object o;

		o = ScriptableObject.getProperty(this, "A");
		A = getValue(o);
		cp.xform[index].c00 = A;

		o = ScriptableObject.getProperty(this, "B");
		B = getValue(o);
		cp.xform[index].c01 = -B;

		o = ScriptableObject.getProperty(this, "C");
		C = getValue(o);
		cp.xform[index].c10 = -C;

		o = ScriptableObject.getProperty(this, "D");
		D = getValue(o);
		cp.xform[index].c11 = D;

		o = ScriptableObject.getProperty(this, "E");
		E = getValue(o);
		cp.xform[index].c20 = E;

		o = ScriptableObject.getProperty(this, "F");
		F = getValue(o);
		cp.xform[index].c21 = F;

		o = ScriptableObject.getProperty(this, "Color");
		Color = getValue(o);
		cp.xform[index].color = Color;

		o = ScriptableObject.getProperty(this, "Symmetry");
		Symmetry = getValue(o);
		cp.xform[index].symmetry = Symmetry;

		o = ScriptableObject.getProperty(this, "Weight");
		Weight = getValue(o);
		cp.xform[index].density = Weight;

		int nv = XForm.getNrVariations();
		for (int i = 0; i < nv; i++) {
			o = ScriptableObject.getProperty(this, XForm.getVariation(i)
					.getName());
			double value1 = getValue(o);
			double value2 = Variation[i];
			if (value1 != cp.xform[index].vars[i])
				cp.xform[index].vars[i] = value1;
			else if (value2 != cp.xform[index].vars[i])
				cp.xform[index].vars[i] = value2;
		}

		int kp = 0;
		for (int i = 0; i < nv; i++) {
			int np = XForm.getVariation(i).getNrParameters();
			for (int j = 0; j < np; j++) {
				o = ScriptableObject.getProperty(this, XForm.getVariation(i)
						.getParameterName(j));
				double value1 = getValue(o);
				double value2 = Variable[kp];
				if (value1 != cp.xform[index].pvalues[kp])
					cp.xform[index].pvalues[kp] = value1;
				else if (value2 != cp.xform[index].pvalues[kp])
					cp.xform[index].pvalues[kp] = value2;
				kp++;
			}
		}

	} // End of method js2java

	/*****************************************************************************/

	double getValue(Object o) {
		if (o instanceof Double)
			return ((Double) o).doubleValue();
		else if (o instanceof Float)
			return ((Float) o).floatValue();
		else if (o instanceof Integer)
			return ((Integer) o).intValue();
		else if (o instanceof Long)
			return ((Long) o).longValue();
		else
			return 0;
	}

	/*****************************************************************************/

	public void Clear() {
		script.clearTransform();
	}

	/*****************************************************************************/

	public void Rotate(double degrees) {
		script.rotateTransform(degrees);
	}

	/*****************************************************************************/

	public void Scale(double s) {
		script.scaleTransform(s);
	}

	/*****************************************************************************/

	public void RotateOrigin(double degrees) {
		script.rotateOriginTransform(degrees);
	}

	/*****************************************************************************/

	JSTransform Clone() {
		JSTransform t = new JSTransform(script);

		t.init();

		t.A = A;
		t.B = B;
		t.C = C;
		t.D = D;
		t.E = E;
		t.F = F;
		t.Color = Color;
		t.Symmetry = Symmetry;
		t.Weight = Weight;

		System.arraycopy(Variation, 0, t.Variation, 0, Variation.length);
		System.arraycopy(Variable, 0, t.Variable, 0, Variable.length);

		t.updateProperties();

		return t;

	} // End of method Clone

	/*****************************************************************************/

} // End of class JSTransform

