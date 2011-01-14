package org.apophysis;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

public class JSTransformScale extends BaseFunction {

	/****************************************************************************/

	@Override
	public String getFunctionName() {
		return "Scale";
	}

	/****************************************************************************/

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable obj,
			Object[] args) {
		JSTransform transform = (JSTransform) obj;

		if (args.length != 1)
			throw new EvaluatorException("Wrong number of arguments");

		transform.Scale(transform.getValue(args[0]));

		return null;
	}

	/****************************************************************************/

} // End of class JSTransformScale
