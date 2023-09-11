package scouter;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import java.util.ArrayList;

public class MatchMatrix {
    //Each row is one alliance of a match
    //Each col is a team, represented by teamNums --> blue is even, red is odd
    private final int [] teamNums;
    private double [][] presenceMtx;
    private ArrayList<MtxCoordinates> coords;
    private double [][] totalScoresMtx;
    private double [][] teleGPCountMtx;
    private double [][] teleGPPointsMtx;

    public MatchMatrix(ArrayList<Team> teams) {
        teamNums = new int[teams.size()];
        for (int i = 0; i < teamNums.length; i++) {
            teamNums[i] = teams.get(i).getTeam_number();
        }

        presenceMtx = new double[10][teams.size()];
        coords = new ArrayList<>();
        totalScoresMtx = new double[10][1];
        teleGPCountMtx = new double[10][1];
        teleGPPointsMtx = new double[10][1];
    }

    public int [] getTeamNums() {
        return teamNums;
    }

    public double [][] getPresenceMtx() {
        return presenceMtx;
    }

    public double [][] getTotalScoresMtx() {
        return totalScoresMtx;
    }

    public double [][] getTeleGPCountMtx() {
        return teleGPCountMtx;
    }

    public double [][] getTeleGPPointsMtx() {
        return teleGPPointsMtx;
    }

    public void addTeamToMatch(int teamNum, int matchNum, boolean inBlueAlliance) {
        int row = inBlueAlliance ? 2 * (matchNum - 1) : 2 * (matchNum - 1) + 1;
        int col = binarySearchTeamNums(teamNum);

        if (row == presenceMtx.length - 1) resizeMatrices();

        presenceMtx[row][col] = 1;
        coords.add(new MtxCoordinates(row, col));
    }

    public void addTotalScoreToMatch(int score, int matchNum, boolean inBlueAlliance) {
        int row = inBlueAlliance ? 2 * (matchNum - 1) : 2 * (matchNum - 1) + 1;

        if (row == totalScoresMtx.length - 1) resizeMatrices();

        totalScoresMtx[row][0] = score;
    }

    public void addTeleGamePieceNumToMatch(int count, int matchNum, boolean inBlueAlliance) {
        int row = inBlueAlliance ? 2 * (matchNum - 1) : 2 * (matchNum - 1) + 1;

        if (row == teleGPCountMtx.length - 1) resizeMatrices();

        teleGPCountMtx[row][0] = count;
    }

    public void addTeleGamePieceScoreToMatch(int score, int matchNum, boolean inBlueAlliance) {
        int row = inBlueAlliance ? 2 * (matchNum - 1) : 2 * (matchNum - 1) + 1;

        if (row == teleGPPointsMtx.length - 1) resizeMatrices();

        teleGPPointsMtx[row][0] = score;
    }

    private void resizeMatrices() {
        double [][] presence = new double[presenceMtx.length + 25][presenceMtx[0].length];
        double [][] scores = new double[totalScoresMtx.length + 25][totalScoresMtx[0].length];
        double [][] teleGPCount = new double[teleGPCountMtx.length + 25][teleGPCountMtx[0].length];
        double [][] teleGPScores = new double[teleGPPointsMtx.length + 25][teleGPPointsMtx[0].length];

        for (MtxCoordinates coordinates : coords) {
            presence[coordinates.getRow()][coordinates.getCol()] = 1;
        }

        for (int i = 0; i < totalScoresMtx.length; i++) {
            scores[i][0] = totalScoresMtx[i][0];
            teleGPCount[i][0] = teleGPCountMtx[i][0];
            teleGPScores[i][0] = teleGPPointsMtx[i][0];
        }

        presenceMtx = presence;
        totalScoresMtx = scores;
        teleGPCountMtx = teleGPCount;
        teleGPPointsMtx = teleGPScores;
    }

    public int binarySearchTeamNums(int teamNum) {
        int lower = 0;
        int upper = teamNums.length - 1;

        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;

            if (teamNums[middle] == teamNum) return middle;
            if (teamNums[middle] < teamNum) lower = middle + 1;
            else upper = middle - 1;
        }

        return -1;
    }

    public void printByteMtx(byte [][] mtx) {
        for (byte [] matrix : mtx) {
            for (byte i : matrix) System.out.print(i + " ");
            System.out.println();
        }
    }

    public void printIntMtx(int [][] mtx) {
        for (int[] matrix : mtx) {
            for (int i : matrix) System.out.print(i + " ");
            System.out.println();
        }
    }

    public void printDoubleMtx(double [][] mtx) {
        for (double[] matrix : mtx) {
            for (double i : matrix) System.out.print(i + " ");
            System.out.println();
        }
    }

    public DoubleMatrix solveLeastSquaresDoubleMtx(double [][] scoresMtx) {
        DoubleMatrix mtxA = new DoubleMatrix(presenceMtx);
        DoubleMatrix mtxB = new DoubleMatrix(scoresMtx);

        return Solve.solveLeastSquares(mtxA, mtxB);
    }
    
    public double [][] solveLeastSquares(double [][] scoresMtx) {
    	double [][] transpose = transpose(presenceMtx);
    	
        double [][] mtxA = multiply(transpose, presenceMtx);
        double [][] mtxB = multiply(transpose, scoresMtx);

        return conjugateDescentAlg(mtxA, mtxB);
    }
    
    //Matrix operations
	protected double [][] transpose(double [][] mtx) {
		double [][] newMtx = new double[mtx[0].length][mtx.length];

		for (int i = 0; i < mtx.length; i++) {
			for (int j = 0; j < mtx[i].length; j++) {
				newMtx[j][i] = mtx[i][j];
			}
		}

		return newMtx;
	}
	
	//TODO see if Strassen is stable for doubles, and if so, use that
	public double[][] multiply(double[][] matrixA, double[][] matrixB) {
		double[][] multipliedMatrix = new double[matrixA.length][matrixB[0].length];

		for (int i = 0; i < matrixA.length; i++) {
			for (int j = 0; j < matrixB[0].length; j++) {
				for (int k = 0; k < matrixA[0].length; k++)
					multipliedMatrix[i][j] += matrixA[i][k] * matrixB[k][j];
			}
		}

		return multipliedMatrix;
	}

	private static double [][] vectorMult(double [][] mtx, double [][] vector) {
		double [][] newMtx = new double[mtx.length][1];

		for (int i = 0; i < mtx.length; i++) {
			for (int j = 0; j < mtx[i].length; j++) {
				newMtx[i][0] += mtx[i][j] * vector[j][0];
			}
		}

		return newMtx;
	}

	protected double [][] conjugateDescentAlg(double [][] A, double [][] b) {
		double [][] xMat = new double[A[0].length][1]; //0 vector originally

		double [][] r = deepCopy(b);
		double [][] d = deepCopy(r);

		double rOld = vectorATransposeA(r);

		for (int i = 0; i < b.length; i++) {
			double [][] aTimesD = vectorMult(A, d);

			double alpha = rOld / (vectorATransposeB(d, aTimesD));

			addInPlace(xMat, scale(d, alpha));
			subtract(r, scale(aTimesD, alpha));

			double rNew = vectorATransposeA(r);

			if (rNew <= (Math.pow(10, -5))) break;

			d = add(r, scale(d, (rNew / rOld)));
			rOld = rNew;
		}

		return xMat;
	}

	private double [][] deepCopy(double [][] mtx) {
		double [][] newMtx = new double[mtx.length][mtx[0].length];

		for (int i = 0; i < mtx.length; i++) {
            System.arraycopy(mtx[i], 0, newMtx[i], 0, mtx[i].length);
		}

		return newMtx;
	}

	private double vectorATransposeA(double [][] vector) {
		double sum = 0;

        for (double [] doubles : vector) {
            sum += Math.pow(doubles[0], 2);
        }

		return sum;
	}

	private double vectorATransposeB(double [][] vectorA, double [][] vectorB) {
		double sum = 0;

		for (int i = 0; i < vectorA.length; i++) {
			sum += vectorA[i][0] * vectorB[i][0];
		}

		return sum;
	}

	private double [][] scale(double [][] mtx, double scalar) {
		double [][] newMtx = new double[mtx.length][mtx[0].length];

		for (int i = 0; i < mtx.length; i++) {
			for (int j = 0; j < mtx[i].length; j++) {
				newMtx[i][j] = mtx[i][j] * scalar;
			}
		}

		return newMtx;
	}

	private void addInPlace(double [][] a, double [][] b) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j] += b[i][j];
			}
		}
	}

	private double [][] add(double [][] a, double [][] b) {
		double [][] newMtx = new double[a.length][a[0].length];

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				newMtx[i][j] = a[i][j] + b[i][j];
			}
		}

		return newMtx;
	}

	private void subtract(double [][] a, double [][] b) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				a[i][j] -= b[i][j];
			}
		}
	}
}
