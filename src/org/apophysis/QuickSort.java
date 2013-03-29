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

import java.util.List;

public class QuickSort {

	/*****************************************************************************/

	public static void qsort(MySortable a[]) {
		if (a.length >= 1) {
			qsort(a, 0, a.length - 1);
		}
	}

	/*****************************************************************************/

	private static void qsort(MySortable a[], int lowIndex, int highIndex) {
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		MySortable pivotObject;
		MySortable lowToHighObject;
		MySortable highToLowObject;
		MySortable parking;
		int newLowIndex;
		int newHighIndex;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotObject = a[pivotIndex];

		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		while ((newHighIndex + 1) < newLowIndex) {
			lowToHighObject = a[lowToHighIndex];
			while ((lowToHighIndex < newLowIndex)
					&& (lowToHighObject.compare(pivotObject) < 0)) {
				newHighIndex = lowToHighIndex;
				lowToHighIndex++;
				lowToHighObject = a[lowToHighIndex];
			}

			highToLowObject = a[highToLowIndex];
			while ((newHighIndex <= highToLowIndex)
					&& (highToLowObject.compare(pivotObject) > 0)) {
				newLowIndex = highToLowIndex;
				highToLowIndex--;
				highToLowObject = a[highToLowIndex];
			}

			if (lowToHighIndex == highToLowIndex) {
				newHighIndex = lowToHighIndex;
			} else if (lowToHighIndex < highToLowIndex) {
				if (lowToHighObject.compare(highToLowObject) >= 0) {
					parking = lowToHighObject;
					a[lowToHighIndex] = highToLowObject;
					a[highToLowIndex] = parking;

					newLowIndex = highToLowIndex;
					newHighIndex = lowToHighIndex;

					lowToHighIndex++;
					highToLowIndex--;
				}
			}
		}

		if (lowIndex < newHighIndex) {
			qsort(a, lowIndex, newHighIndex);
		}
		if (newLowIndex < highIndex) {
			qsort(a, newLowIndex, highIndex);
		}

	} // End of method qsort

	/*****************************************************************************/

	public static void qsort(List<MySortable> v) {
		if (v.size() >= 1) {
			qsort(v, 0, v.size() - 1);
		}
	}

	/*****************************************************************************/

	private static void qsort(List<MySortable> v, int lowIndex, int highIndex) {
		int lowToHighIndex;
		int highToLowIndex;
		int pivotIndex;
		MySortable pivotObject;
		MySortable lowToHighObject;
		MySortable highToLowObject;
		MySortable parking;
		int newLowIndex;
		int newHighIndex;

		lowToHighIndex = lowIndex;
		highToLowIndex = highIndex;
		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
		pivotObject = get(v, pivotIndex);

		newLowIndex = highIndex + 1;
		newHighIndex = lowIndex - 1;
		while ((newHighIndex + 1) < newLowIndex) {
			lowToHighObject = get(v, lowToHighIndex);
			while ((lowToHighIndex < newLowIndex)
					&& (lowToHighObject.compare(pivotObject) < 0)) {
				newHighIndex = lowToHighIndex;
				lowToHighIndex++;
				lowToHighObject = get(v, lowToHighIndex);
			}

			highToLowObject = get(v, highToLowIndex);
			while ((newHighIndex <= highToLowIndex)
					&& (highToLowObject.compare(pivotObject) > 0)) {
				newLowIndex = highToLowIndex;
				highToLowIndex--;
				highToLowObject = get(v, highToLowIndex);
			}

			if (lowToHighIndex == highToLowIndex) {
				newHighIndex = lowToHighIndex;
			} else if (lowToHighIndex < highToLowIndex) {
				if (lowToHighObject.compare(highToLowObject) >= 0) {
					parking = lowToHighObject;
					set(v, highToLowObject, lowToHighIndex);
					set(v, parking, highToLowIndex);

					newLowIndex = highToLowIndex;
					newHighIndex = lowToHighIndex;

					lowToHighIndex++;
					highToLowIndex--;
				}
			}
		}

		if (lowIndex < newHighIndex) {
			qsort(v, lowIndex, newHighIndex);
		}
		if (newLowIndex < highIndex) {
			qsort(v, newLowIndex, highIndex);
		}

	} // End of method qsort

	/*****************************************************************************/

	private static void set(List<MySortable> v, MySortable e, int index) {
		v.set(index, e);
	}

	/*****************************************************************************/

	private static MySortable get(List<MySortable> v, int index) {
		return v.get(index);
	}

	/*****************************************************************************/

	public static void invert(List<MySortable> v) {
		int n = v.size();
		for (int i = 0; i < n / 2; i++) {
			MySortable o = v.get(i);
			v.set(i, v.get(n - 1 - i));
			v.set(n - 1 - i, o);
		}
	}

	/*****************************************************************************/

} // End of class QuickSort

