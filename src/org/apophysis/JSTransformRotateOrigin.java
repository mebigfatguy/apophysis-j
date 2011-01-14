package org.apophysis;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

public class JSTransformRotateOrigin extends BaseFunction {

	/****************************************************************************/

	@Override
	public String getFunctionName() {
		return "RotateOrigin";
	}

	/****************************************************************************/

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable obj,
			Object[] args) {
		JSTransform transform = (JSTransform) obj;

		if (args.length != 1)
			throw new EvaluatorException("Wrong number of arguments");

		transform.RotateOrigin(transform.getValue(args[0]));

		return null;
	}

	/****************************************************************************/

} // End of class JSTransformRotateOrigin
