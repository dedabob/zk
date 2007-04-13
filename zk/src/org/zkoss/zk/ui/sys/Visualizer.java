/* Visualizer.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Fri Jan 20 23:06:03     2006, Created by tomyeh
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.ui.sys;

import java.util.List;
import org.zkoss.zk.ui.Execution;

/**
 * A part of {@link Execution} for visualizing the components whose
 * visual parts are modified. There is one-to-one relationship between
 * the visualizer and the execution.
 *
 * <p>Its implementation highly depends on {@link UiEngine}.
 *
 * @author tomyeh
 */
public interface Visualizer {
	/** Returns the execution that this visualizer is associated with.
	 */
	public Execution getExecution();
	/** Whether the execution associated with this visualizer
	 * is caued by an async-update.
	 *
	 * <p>Note: unlike {@link Execution#isAsyncUpdate}, which it
	 * tells whether a page is in async-update, this method tells whether
	 * this first execution is caused by async-update (no matter this
	 * execution is async-update or creating a new page).
	 */
	public boolean isEverAsyncUpdate();
	/** Adds the responses to the first {@link Visualizer} if it is
	 * caused by async-update.
	 * @return true if respones are added; false if the first {@link Visualizer}
	 * is NOT caused by assync responses.
	 */
	public boolean addToFirstAsyncUpdate(List responses);
}
