package scouter;

public class StrassenMult {
	public static int[][] strassensMtxMultWrapper(int[][] a, int[][] b, int nZero) {
		if (a.length == 1) {
			return new int[][] { { a[0][0] * b[0][0] } };
		}

		int[][] mtxMult = strassensMtxMult(a, b, nZero);

		if (checkIfPadded(mtxMult) && a.length % 2 != 0) {
			mtxMult = removePadding(mtxMult);
		}

		if (mtxMult.length < a.length) {
			mtxMult = padMtx(mtxMult);
		}

		return mtxMult;
	}

	private static int[][] strassensMtxMult(int[][] a, int[][] b, int nZero) {
		if (a.length <= nZero) {
			return conventionalMtxMult(a, b, a.length, a[0].length, b.length, b[0].length);
		}

		int[][] newA = a, newB = b;

		if (a.length % 2 != 0) {
			newA = padMtx(a);
		}

		if (b.length % 2 != 0) {
			newB = padMtx(b);
		}

		int[][] mtxMult = new int[newA.length][newA[0].length];

		int[][] A = getMtxSubregion(newA, 'A');
		int[][] B = getMtxSubregion(newA, 'B');
		int[][] C = getMtxSubregion(newA, 'C');
		int[][] D = getMtxSubregion(newA, 'D');
		int[][] E = getMtxSubregion(newB, 'E');
		int[][] F = getMtxSubregion(newB, 'F');
		int[][] G = getMtxSubregion(newB, 'G');
		int[][] H = getMtxSubregion(newB, 'H');

		int[][] recursiveX = A;
		int[][] recursiveY = subtractMtx(F, H); // F - H

		int[][] recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P1

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, 0, mtxMult[0].length / 2);
		populateMtxAdd(mtxMult, recursiveMult, mtxMult.length / 2, mtxMult[0].length / 2);

		recursiveX = addMtx(A, B);
		recursiveY = H;

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P2

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxSubtract(mtxMult, recursiveMult, 0, 0);
		populateMtxAdd(mtxMult, recursiveMult, 0, mtxMult[0].length / 2);

		recursiveX = addMtx(C, D);
		recursiveY = E;

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P3

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, mtxMult.length / 2, 0);
		populateMtxSubtract(mtxMult, recursiveMult, mtxMult.length / 2, mtxMult[0].length / 2);

		recursiveX = D;
		recursiveY = subtractMtx(G, E);

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P4

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, 0, 0);
		populateMtxAdd(mtxMult, recursiveMult, mtxMult[0].length / 2, 0);

		recursiveX = addMtx(A, D);
		recursiveY = addMtx(E, H);

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P5

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, 0, 0);
		populateMtxAdd(mtxMult, recursiveMult, mtxMult.length / 2, mtxMult[0].length / 2);

		recursiveX = subtractMtx(B, D);
		recursiveY = addMtx(G, H);

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P6

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, 0, 0);

		recursiveX = subtractMtx(C, A);
		recursiveY = addMtx(E, F);

		recursiveMult = strassensMtxMult(recursiveX, recursiveY, nZero); // P7

		if (checkIfPadded(recursiveMult)) {
			recursiveMult = removePadding(recursiveMult);
		}

		populateMtxAdd(mtxMult, recursiveMult, mtxMult.length / 2, mtxMult[0].length / 2);

		if (checkIfPadded(mtxMult)) {
			mtxMult = removePadding(mtxMult);
		}

		return mtxMult;
	}

	private static int[][] removePadding(int[][] mtx) {
		int[][] newMtx = new int[mtx.length - 1][mtx[0].length - 1];

		for (int i = 0; i < newMtx.length; i++) {
			for (int j = 0; j < newMtx[0].length; j++) {
				newMtx[i][j] = mtx[i][j];
			}
		}

		return newMtx;
	}

	private static boolean checkIfPadded(int[][] mtx) {
		boolean isPadded = true;

		for (int i = 0; i < mtx.length; i++) {
			if (mtx[i][mtx[i].length - 1] != 0) {
				isPadded = false;
				break;
			}
		}

		for (int j = 0; j < mtx[0].length; j++) {
			if (mtx[mtx.length - 1][j] != 0) {
				isPadded = false;
				break;
			}
		}

		return isPadded;
	}

	private static int[][] getMtxSubregion(int[][] a, char region) {
		int[][] newMtx = new int[a.length / 2][a[0].length / 2];
		int startRow = 0, endRow = 0, startCol = 0, endCol = 0;

		if (region == 'A' || region == 'E') {
			startRow = 0;
			endRow = a.length / 2;
			startCol = 0;
			endCol = a[0].length / 2;
		} else if (region == 'B' || region == 'F') {
			startRow = 0;
			endRow = a.length / 2;
			startCol = a[0].length / 2;
			endCol = a[0].length;
		} else if (region == 'C' || region == 'G') {
			startRow = a.length / 2;
			endRow = a.length;
			startCol = 0;
			endCol = a[0].length / 2;
		} else { // D or H
			startRow = a.length / 2;
			endRow = a.length;
			startCol = a[0].length / 2;
			endCol = a[0].length;
		}

		for (int i = startRow, u = 0; i < endRow; i++, u++) {
			for (int j = startCol, v = 0; j < endCol; j++, v++) {
				newMtx[u][v] = a[i][j];
			}
		}

		return newMtx;
	}

	private static int[][] addMtx(int[][] a, int[][] b) {
		int maxLength;
		boolean maxLenIsA = false;

		if (a.length > b.length) {
			maxLength = a.length;
			maxLenIsA = true;
		} else {
			maxLength = b.length;
		}

		int[][] newMtx = new int[maxLength][maxLength];

		if (maxLenIsA) {
			for (int i = 0; i < a.length; i++) {
				for (int j = 0; j < a[i].length; j++) {
					if (i >= b.length || j >= b[0].length) {
						newMtx[i][j] = a[i][j];
					} else {
						newMtx[i][j] = a[i][j] + b[i][j];
					}
				}
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				for (int j = 0; j < b[i].length; j++) {
					if (i >= a.length || j >= a[0].length) {
						newMtx[i][j] = b[i][j];
					} else {
						newMtx[i][j] = a[i][j] + b[i][j];
					}
				}
			}
		}

		return newMtx;
	}

	private static int[][] subtractMtx(int[][] a, int[][] b) {
		int maxLength;
		boolean maxLenIsA = false;

		if (a.length > b.length) {
			maxLength = a.length;
			maxLenIsA = true;
		} else {
			maxLength = b.length;
		}

		int[][] newMtx = new int[maxLength][maxLength];

		if (maxLenIsA) {
			for (int i = 0; i < a.length; i++) {
				for (int j = 0; j < a[i].length; j++) {
					if (i >= b.length || j >= b[0].length) {
						newMtx[i][j] = a[i][j];
					} else {
						newMtx[i][j] = a[i][j] - b[i][j];
					}
				}
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				for (int j = 0; j < b[i].length; j++) {
					if (i >= a.length || j >= a[0].length) {
						newMtx[i][j] = -b[i][j];
					} else {
						newMtx[i][j] = a[i][j] - b[i][j];
					}
				}
			}
		}

		return newMtx;
	}

	public static int[][] conventionalMtxMult(int[][] a, int[][] b) {
		int[][] newMtx = new int[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int k = 0; k < a.length; k++) {
				for (int j = 0; j < a[i].length; j++) {
					newMtx[i][j] += a[i][k] * b[k][j];
				}
			}
		}

		return newMtx;
	}

	private static int[][] conventionalMtxMult(int[][] a, int[][] b, int endRowA, int endColA, int endRowB,
			int endColB) {
		int maxLength = Math.max(endRowA, Math.max(endColA, Math.max(endRowB, endColB)));

		int[][] paddedA = fillPaddedMtx(a, endRowA, endColA, maxLength);
		int[][] paddedB = fillPaddedMtx(b, endRowB, endColB, maxLength);

		return conventionalMtxMult(paddedA, paddedB);
	}

	private static int[][] fillPaddedMtx(int[][] mtx, int endRow, int endCol, int maxLength) {
		int[][] padded = new int[maxLength][maxLength];

		for (int i = 0, u = 0; i < maxLength; i++, u++) {
			for (int j = 0, v = 0; j < maxLength; j++, v++) {
				if (i >= endRow || j >= endCol) {
					padded[u][v] = 0;
				} else {
					padded[u][v] = mtx[i][j];
				}
			}
		}

		return padded;
	}

	private static int[][] padMtx(int[][] mtx) {
		int[][] padded = new int[mtx.length + 1][mtx[0].length + 1];

		for (int i = 0; i < mtx.length; i++) {
			for (int j = 0; j < mtx[i].length; j++) {
				padded[i][j] = mtx[i][j];
			}
		}

		return padded;
	}

	private static void populateMtxAdd(int[][] fullMtx, int[][] subMtx, int startRow, int startCol) {
		for (int i = startRow; i < startRow + subMtx.length; i++) {
			for (int j = startCol; j < startCol + subMtx[i - startRow].length; j++) {
				fullMtx[i][j] += subMtx[i - startRow][j - startCol];
			}
		}
	}

	private static void populateMtxSubtract(int[][] fullMtx, int[][] subMtx, int startRow, int startCol) {
		for (int i = startRow; i < startRow + subMtx.length; i++) {
			for (int j = startCol; j < startCol + subMtx[i - startRow].length; j++) {
				fullMtx[i][j] -= subMtx[i - startRow][j - startCol];
			}
		}
	}
}