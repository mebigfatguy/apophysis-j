package org.apophysis;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class JSTransformClear extends BaseFunction {

	/****************************************************************************/

	@Override
	public String getFunctionName() {
		return "Clear";
	}

	/****************************************************************************/

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable obj,
			Object[] args) {
		JSTransform transform = (JSTransform) obj;
		transform.Clear();
		return null;
	}

	/****************************************************************************/

} // End of class JSTransformClear
