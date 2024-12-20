package RiskBasedDefense.org;

import java.util.Arrays;

/**
 * This class is used to creat an object to calculate the risk of each vulnerability corresponding to the initial investments
 */
public class RiskBasedDefense {
    private final double[][] adjMatrix;

    /**
     * This constructor is used to initialize the required parameters for this class
     * @param adjMatrix The matrix which represents the attack graph
     */
    public RiskBasedDefense(double[][] adjMatrix) {
        if (adjMatrix == null) {
            throw new IllegalArgumentException("The matrix is null!");
        }
        this.adjMatrix = adjMatrix;
    }

    /**
     * This method is used to calculate the total risk of the given attack graph
     * @return The total risk
     */
    public double totalRisk() {
        double risk = 0;
        for (double[] matrix : adjMatrix) {
            risk += Arrays.stream(matrix, 0, adjMatrix.length)
                    .filter(connection -> connection > 0)
                    .map(connection -> Math.exp(-connection)).sum();
        }
        return risk;
    }
}
