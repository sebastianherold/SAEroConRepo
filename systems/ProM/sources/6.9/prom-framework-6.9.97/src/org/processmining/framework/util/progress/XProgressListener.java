/*
 * OpenXES
 * 
 * The reference implementation of the XES meta-model for event 
 * log data management.
 * 
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
 * 
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * The use of this software can also be conditionally licensed for
 * other programs, which do not satisfy the specified conditions. This
 * requires an exemption from the general license, which may be
 * granted on a per-case basis.
 * 
 * If you want to license the use of this software with a program
 * incompatible with the LGPL, please contact the author for an
 * exemption at the following email address: 
 * christian@deckfour.org
 * 
 */
package org.processmining.framework.util.progress;

/**
 * Interface for listening for progress on
 * an unspecified process.
 * 
 * A progress listener can abort the monitored
 * process. Abortion is specified by using the
 * <code>isAborted()</code> method, which should
 * be queried periodically by the using party.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public interface XProgressListener {

	/**
	 * Notifies the listener about progress being made.
	 * 
	 * @param progress Current progress step achieved.
	 * @param maxProgress Maximal progress steps available.
	 */
	public void updateProgress(int progress, int maxProgress);
	
	/**
	 * Checks whether the monitored process has been canceled.
	 */
	public boolean isAborted();
	
}
