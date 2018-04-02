/*  ClusterEvaluator.java

    Copyright (c) 2009 Andrew Rosenberg


    ClusterEvaluator.java is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ClusterEvaluator.java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ClusterEvaluator.java.  If not, see <http://www.gnu.org/licenses/>.
 */

package evaluation;

/**
 * A class for evaluating custering solutions. The solution is represented by a ContingencyTable object.  A variety of metrics
 * can be used to perform the evaluation.
 *
 * @see ContingencyTable
 */

public class ClusterEvaluator {
  private ContingencyTable data;
  private static final boolean DEBUG = false;

  /**
   * displays debugging info if debug set to true
   *
   * @param s debugging string
   */
  private void debug(String s) {
    if (DEBUG) {
      System.err.println(s);
    }
  }

  /**
   * Sets up a toy clustering solution following Dom2001's procedure Clusters are considered as either "Useful" or "Noise" Useful
   * clusters have a fixed probability mass evenly distributed across a single corresponding class, and some error mass
   * distributed across all other classes Noise clusters have probability mass evenly distributed across all classes. Noise
   * Classes have probability mass evenly distributed across all useful clusters. The total error mass (e1+e2+e3) should be less
   * than or equal to 1. On completion, the ContingencyTable is ready for analysis.
   *
   * @param numClassesUseful  The number of classes used in the experiment
   * @param numClassesNoise   The number of 'Noise' classes, as defined above.
   * @param numClustersUseful The number of 'Useful' clusters, as defined above.
   * @param numClustersNoise  The number of 'Noise' clusters, as defined above.
   * @param e1                The "Useful" error mass.  This error is evenly distributed across all "incorrect" cells within the
   *                          useful clusters
   * @param e2                The "Cluster Noise" error mass. This error is evenly distributed across every cell within the noise
   *                          clusters
   * @param e3                The "Class Noise" error mass. This error is evenly distributed across every cluster within the
   *                          useful clusters
   * @throws ClusterEvaluatorException if invalid error terms are provided
   * @see ContingencyTable
   */
  public void configureExperiment(int numClassesUseful, int numClustersUseful, int numClassesNoise, int numClustersNoise,
                                  double e1, double e2, double e3) throws ClusterEvaluatorException {
    if (numClustersUseful == 1) numClassesNoise = 0;

    if (e1 + e2 + e3 > 1) {
      throw new ClusterEvaluatorException("Error mass greater than 1");
    }

    data = new ContingencyTable(numClustersUseful + numClustersNoise, numClassesUseful + numClassesNoise);

    int numUseCells = Math.max(numClassesUseful, numClustersUseful);
    int numE1Cells = (numClassesUseful * numClustersUseful) - numUseCells;
    if (numE1Cells == 0) e1 = 0.0;
    int numE2Cells = numClustersNoise * numClassesUseful;
    if (numE2Cells == 0) e2 = 0.0;
    int numE3Cells = numClassesNoise * (numClustersUseful);
    if (numE3Cells == 0) e3 = 0.0;

    for (int i = 0; i < numClustersUseful + numClustersNoise; ++i) {
      for (int j = 0; j < numClassesUseful + numClassesNoise; ++j) {

        double v = 0.0;

        if (j >= numClassesUseful) {// noise class
          if (i < numClustersUseful)// useful cluster
            v = e3 / numE3Cells;
          //v = Math.min(e3/numE3Cells,(1.0-e1-e2-e3)/numUseCells);
        } else if (i < numClustersUseful) {// useful cluster

          if (classClusterMatch(i, j, numClustersUseful, numClassesUseful)) {
            // divide the remaining probability mass 1-e1-e2 over the 'correct' assignments i.e. data[i][j] where i==j
            v = (1.0 - e1 - e2 - e3) / numUseCells;
          } else {
            // divide e1 evenly over the 'incorrect' assignments to the useful clusters
            v = e1 / numE1Cells;
          }
        } else {// noise cluster
          // divide e2 evenly over the noise clusers
          v = e2 / numE2Cells;
          //v = Math.min(e2/numE2Cells,(1.0-e1-e2-e3)/numUseCells);
        }

        data.set(i, j, Math.ceil(v * 10000));
      }
    }
  }

  /**
   * This is a complicated way of determining which "useful clusters" correspond to which classes.
   * <p/>
   * Defined in Dom2001 for testing evaluation metrics
   *
   * @param i           The class index
   * @param j           The cluster index
   * @param numClasses  The number of Classes
   * @param numClusters The number of Clusters
   * @return Whether or not the class is paired to the cluster.
   */
  private boolean classClusterMatch(int i, int j, int numClasses, int numClusters) {
    // square matrix.
    if (numClasses == numClusters) {
      if (i == j) {
        return true;
      }
    }

    if (numClasses < numClusters) {

      if (i == 0) {
        if (j < Math.ceil((1.0 * numClusters) / numClasses)) {
          return true;
        } else {
          return false;
        }
      } else {
        if (j < Math.ceil((1.0 * numClusters) / numClasses)) {
          return false;
        } else {
          return classClusterMatch(i - 1, j - (int) Math.ceil((1.0 * numClusters) / numClasses), numClasses - 1,
              numClusters - (int) Math.ceil((1.0 * numClusters) / numClasses));
        }
      }
    }

    // symmetric to above.

    if (numClasses > numClusters) {

      if (j == 0) {
        if (i < Math.ceil((1.0 * numClasses) / numClusters)) {
          return true;
        } else {
          return false;
        }
      } else {
        if (i < Math.ceil((1.0 * numClasses) / numClusters)) {
          return false;
        } else {
          return classClusterMatch(i - (int) Math.ceil((1.0 * numClasses) / numClusters), j - 1,
              numClasses - (int) Math.ceil((1.0 * numClasses) / numClusters), numClusters - 1);
        }
      }
    }

    return false;//To change body of created methods use File | Settings | File Templates.
  }

  /**
   * Calculate the FMeasure of a given clustering solution
   *
   * @return F-Measure
   */
  public double getFMeasure() {
    double f = 0.0;

    Double[] class_n = data.getColSum();
    Double n = vectorSum(class_n);

    for (int j = 0; j < data.getNumCols(); j++) {

      double max_f = -1;

      for (int i = 0; i < data.getNumRows(); i++) {
        double f_ij = getF(i, j);

        if (Double.isNaN(f_ij)) {
          System.err.println("f_ij is NaN");
        }
        max_f = Math.max(max_f, f_ij);
      }

      if (n != 0) f += max_f * (class_n[j] / n);
    }

    return f;
  }

  /**
   * get the "F-Measure" for a given class-cluster cell
   * <p/>
   * where F_ij = (2pr)/(p+r)
   * <p/>
   * p = a_ij/k_i r = a_ij/c_j
   *
   * @param i the cluster of interest
   * @param j the class of interest
   * @return F_ij as calculated above
   */
  private double getF(int i, int j) {
    Double[] class_N = data.getColSum();
    Double[] clust_N = data.getRowSum();

    if ((clust_N[i] == 0) || (class_N[j] == 0)) return 0;

    double p = data.get(i, j) / clust_N[i];
    double r = data.get(i, j) / class_N[j];

    if (p + r == 0) return 0;

    return (2 * p * r) / (p + r);
  }

  /**
   * Calculates V-Measure (Rosenberg2007) as calculated by v = (1+beta) h*c / beta*h+c c = 1-H(K|C)/H(K) h = 1-H(C|K)/H(C)
   *
   * @param beta The weighted harmonic mean parameter.
   * @return v-measure as calculated above
   */
  public double getVMeasure(double beta) {

    double homogeneity = getHomogeneity();
    double completeness = getCompleteness();

    if (homogeneity + completeness == 0.0) return 0.0;
    return (1 + beta) * homogeneity * completeness / ((beta * homogeneity) + completeness);
  }

  /**
   * completeness = 1-H(K|C)/H(K)
   *
   * @return completeness
   */
  public double getCompleteness() {
    double h_k_c = 0.0;
    double h_k;

    Double[] class_v = data.getColSum();
    Double[] clust_v = data.getRowSum();

    double n = vectorSum(class_v);

    h_k = calcEntropy(clust_v);

    double joint = calcJointEntropy();

    for (int i = 0; i < data.getNumRows(); ++i)
      for (int j = 0; j < data.getNumCols(); ++j) {
        if (data.get(i, j) != 0) {
          h_k_c -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / class_v[j]) / Math.log(2));
        }
      }

    debug("h(k):" + h_k);
    debug("h(k|c):" + h_k_c);
    debug("joint:" + joint);
    if (h_k < 0) h_k = 0.0;
    if (h_k_c < 0) h_k_c = 0.0;
    if ((h_k_c / h_k > 1.0) || (1 - (h_k_c / h_k) < 0.000000001)) return 0.0;
    return (h_k == 0) ? 1 : 1 - (h_k_c / h_k);
  }

  private double calcJointEntropy() {
    double joint = 0.0;

    double n = data.getN();

    for (int i = 0; i < data.getNumRows(); ++i)
      for (int j = 0; j < data.getNumCols(); ++j) {
        if (data.get(i, j) != 0) {
          joint -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / n) / Math.log(2));
        }
      }

    return joint;
  }

  /**
   * homogeneity = 1 - H(C|K)/H(C)
   *
   * @return homogeneity
   */
  public double getHomogeneity() {
    double h_c_k = 0.0;
    double h_c;

    Double[] class_v = data.getColSum();
    Double[] clust_v = data.getRowSum();

    double n = vectorSum(class_v);

    h_c = calcEntropy(class_v);

    double joint = calcJointEntropy();

    for (int i = 0; i < data.getNumRows(); ++i)
      for (int j = 0; j < data.getNumCols(); ++j) {
        if (data.get(i, j) != 0) {
          h_c_k -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / clust_v[i]) / Math.log(2));
        }
      }

    debug("h(c):" + h_c);
    debug("h(c|k):" + h_c_k);
    debug("joint:" + joint);
    if (h_c < 0) h_c = 0.0;
    if (h_c_k < 0) h_c_k = 0.0;

    // handle numerical error that can occur when h_c_k and h_c are nearly identical.
    if ((h_c_k / h_c > 1.0) || (1 - (h_c_k / h_c) < 0.000000001)) return 0.0;
    return (h_c == 0) ? 1 : 1 - (h_c_k / h_c);
  }

  private double calcEntropy(Double[] vector) {

    double h = 0.0;

    Double[] ar = normalize(vector);

    for (int i = 0; i < ar.length; ++i)
      if (ar[i] != 0) h -= ar[i] * (Math.log(ar[i]) / Math.log(2));

    return h;
  }

  private Double[] normalize(Double[] vector) {

    Double[] ret = new Double[vector.length];

    double total = vectorSum(vector);
    for (int i = 0; i < vector.length; ++i)
      ret[i] = (total == 0) ? 0 : vector[i] / total;

    return ret;
  }

  /**
   * Calculate the purity of the cluster solution as defined in Zhao
   * <p/>
   * Purity = sum_{r=1}^k n_r/n  1/n_r max_i (n_r^i)
   *
   * @return purity as calculated above
   */
  public double getPurity() {
    double purity = 0.0;
    double n = vectorSum(data.getRowSum());

    for (int i = 0; i < data.getNumRows(); ++i) {
      purity += 1 / n * data.maxColValue(i);
    }

    return purity;
  }

  /**
   * Calculate the Entropy of the cluster solution as defined by Zhao
   * <p/>
   * Entropy = sum over clusters of (size of cluster/N) -1/log numClasses entropy of the Cluster
   *
   * @return enropy as calculated above
   */
  public double getEntropy() {
    double h = 0.0;
    double n = vectorSum(data.getRowSum());

    for (int i = 0; i < data.getNumRows(); ++i) {
      h += (vectorSum(data.getRow(i)) / n) * (1 / (Math.log(data.getNumCols() / Math.log(2))) * calcEntropy(data.getRow(i)));
    }

    return h;
  }

  /**
   * Calculate Dom2001's Q_0 Measure
   *
   * @return Q_0
   */
  public double getQ0() {
    Double[] clust_v = data.getRowSum();

    double n = vectorSum(clust_v);
    double h_c_k = 0.0;

    for (int i = 0; i < data.getNumRows(); ++i)
      for (int j = 0; j < data.getNumCols(); ++j) {
        if (data.get(i, j) != 0) {
          h_c_k -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / clust_v[i]) / Math.log(2));
        }
      }

    double codeLength = 0;
    for (int i = 0; i < clust_v.length; ++i) {
      codeLength += logComb((int) Math.ceil(clust_v[i]) + data.getNumCols() - 1, data.getNumCols() - 1);
    }
    codeLength /= n;

    return h_c_k + codeLength;
  }

  /**
   * Calculates log (n \choose m)
   *
   * @param n the n term
   * @param m the m term
   * @return log (n \choose m)
   */
  private double logComb(int n, int m) {
    double v = 0.0;

    for (int i = n; i > (n - m); --i)
      v += Math.log(i) / Math.log(2);

    for (int i = m; i > 0; --i)
      v -= Math.log(i) / Math.log(2);

    return v;
  }

  /**
   * Calculates the sum of the values stored in an array
   *
   * @param vector the array
   * @return the sum
   */
  private double vectorSum(Double[] vector) {
    double d = 0.0;

    for (int i = 0; i < vector.length; ++i)
      d += vector[i];
    return d;
  }

  /**
   * Calculates Dom 2001's normalized Q_0 metric, Q_2
   *
   * @return Q_2
   */
  public double getQ2() {
    Double[] class_v = data.getColSum();
    double n = vectorSum(class_v);

    double codeLength = 0;

    for (int i = 0; i < class_v.length; ++i) {
      codeLength += logComb((int) Math.ceil(class_v[i]) + data.getNumCols() - 1, data.getNumCols() - 1);
    }
    codeLength /= Math.log(2);
    codeLength /= n;

    return codeLength / this.getQ0();
  }

  /**
   * Calculates the Rand Index clustering metric as defined in W.M. Rand. Objective criterion for evaluation of clustering
   * methods. Journal of American Statistical Associateion 1971
   *
   * @return rand index
   */
  public double getRandIndex() {
    ContingencyTable a = data.buildPairwiseContingencyTable();

    return (a.get(0, 0) + a.get(1, 1)) / a.getN();
  }

  /**
   * Calculates the Jaccard clustering metric defined in Milligan, Sokol and Soon. The effect of cluster size, dimensionality and
   * the number of clusters on recovery of true cluster structure. IEEE Trans PAMI 1983
   *
   * @return jaccard metric
   */
  public double getJaccard() {
    ContingencyTable a = data.buildPairwiseContingencyTable();

    return a.get(0, 0) / (a.get(0, 0) + a.get(1, 0) + a.get(0, 1));
  }

  /**
   * Calculates the Fowlkes and Mallows metric defined in E. Fowlkes and C. Mallows. A method for comparing two hierarchical
   * clusterings. Journal of American Statistical Association 1983.
   *
   * @return Fowlkes and Mallows metric.
   */
  public double getFowlkes() {
    ContingencyTable a = data.buildPairwiseContingencyTable();

    return a.get(0, 0) / (Math.sqrt((a.get(0, 1) + a.get(0, 0)) * (a.get(1, 0) + a.get(0, 0))));
  }

  /**
   * Calculates the Gamma statistic defined in  L.J. Hubert and J. Schultz. Quadratic assignment as a general data analysis
   * strategy. British Journal of Mathematical and Statistical Psychology 1976
   *
   * @return gamma statistic
   */
  public double getGamma() {

    ContingencyTable a = data.buildPairwiseContingencyTable();

    double a_tilde = (a.get(0, 1) + a.get(0, 0)) * (a.get(1, 0) + a.get(0, 0));
    double M = a.getN();

    double num = (M * a.get(0, 0)) - a_tilde;
    double denom = Math.sqrt(a_tilde * (M - a.get(0, 1) - a.get(0, 0)) * (M - a.get(1, 0) - a.get(0, 0)));

    return (num / denom);
  }

  /**
   * Calculates the Mirkin measure as cited in Meila 2006
   *
   * @return merkin measure
   */
  public double getMirkin() {
    ContingencyTable a = data.buildPairwiseContingencyTable();

    double n = a.getN();

    return n * (n - 1) * (1 - getRandIndex());
  }

  /**
   * Calculates the N-invaraint merkin measure.
   *
   * @return Mirkin/n^2
   */
  public double getInvariantMirkin() {
    return getMirkin() / (data.getN() * data.getN());
  }

  /**
   * Calculates variation of information (VI) (Meila 2006)
   * <p/>
   * vi = H(C|K) + H(K|C)
   *
   * @return vi
   */
  public double getVI() {
    double h_c_k = 0.0;
    double h_k_c = 0.0;

    Double[] class_v = data.getColSum();
    Double[] clust_v = data.getRowSum();

    double n = vectorSum(class_v);

    for (int i = 0; i < data.getNumRows(); ++i)
      for (int j = 0; j < data.getNumCols(); ++j) {
        if (data.get(i, j) != 0) {
          h_c_k -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / clust_v[i]) / Math.log(2));
          h_k_c -= (data.get(i, j) / n) * (Math.log(data.get(i, j) / class_v[j]) / Math.log(2));
        }
      }
    return h_c_k + h_k_c;
  }

  /**
   * Calculate the misclassification index (MI). This is a fancy name for error.  The metric assumes a bijection from classes to
   * clusters. The MI metric is the count of the data points in a cluster that are not members of the class to which the cluster
   * has been matched to This bijection can be obtained using a variety of technique. We use the assignment that is used in the
   * Dom2001 experiments for pairing useful clusters with classes.
   *
   * @return misclassification index
   */
  public double getMI() {
    double error = 0.0;

    for (int i = 0; i < data.getNumRows(); ++i) {
      for (int j = 0; j < data.getNumCols(); ++j) {
        // The class cluster match is arbitrarily defined for this metric, so we use the one used for matching useful clusters to classes

        if (!classClusterMatch(j, i, data.getNumCols(), data.getNumRows())) {
          error += data.get(i, j);
        }
      }
    }

    return error / data.getN();
  }

  /**
   * Calculates the Adjusted Rand Index.  Defined in L. Hubert and P. Arabie. Comparing partitions. Journal of Classification
   * 1985
   *
   * @return adjusted rand index
   */
  public double getAdjustedRandIndex() {

    ContingencyTable a = data.buildPairwiseContingencyTable();

    double num = a.get(0, 0) - ((a.get(0, 1) + a.get(0, 0)) * (a.get(1, 0) + a.get(0, 0))) / a.getN();
    double denom = ((2 * a.get(0, 0)) + a.get(0, 1) + a.get(1, 0)) / 2.0 -
        ((a.get(0, 1) + a.get(0, 0)) * (a.get(1, 0) + a.get(0, 0))) / a.getN();

    return num / denom;
  }

  /**
   * Retrieve the attached ContingencyTable object
   *
   * @return the contingency table
   */
  public ContingencyTable getData() {
    return data;
  }

  /**
   * Affix a ContingencyTable to the Cluster Evaluator
   *
   * @param ct the ContingencyTable to attach
   */
  public void setData(ContingencyTable ct) {
    data = ct;
  }

  /**
   * An example evaluation to demonstrate the ``Problem of Matching'' that
   */
  public static void problemOfMatching() {

    ClusterEvaluator eval = new ClusterEvaluator();

    Double[][] data = new Double[3][3];

    data[0][0] = 3.0;
    data[0][1] = 1.0;
    data[0][2] = 1.0;
    data[1][0] = 1.0;
    data[1][1] = 3.0;
    data[1][2] = 1.0;
    data[2][0] = 1.0;
    data[2][1] = 1.0;
    data[2][2] = 3.0;

    eval.setData(new ContingencyTable(data));

    System.out.println("Solution A");
    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;

    eval.setData(new ContingencyTable(data));

    System.out.println("Solution B");
    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data = new Double[6][3];
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;
    data[3][0] = 1.0;
    data[3][1] = 1.0;
    data[3][2] = 0.0;
    data[4][0] = 0.0;
    data[4][1] = 1.0;
    data[4][2] = 1.0;
    data[5][0] = 1.0;
    data[5][1] = 0.0;
    data[5][2] = 1.0;

    eval.setData(new ContingencyTable(data));

    System.out.println("Solution C");
    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data = new Double[9][3];
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;
    data[3][0] = 1.0;
    data[3][1] = 0.0;
    data[3][2] = 0.0;
    data[4][0] = 0.0;
    data[4][1] = 1.0;
    data[4][2] = 0.0;
    data[5][0] = 0.0;
    data[5][1] = 0.0;
    data[5][2] = 1.0;
    data[6][0] = 1.0;
    data[6][1] = 0.0;
    data[6][2] = 0.0;
    data[7][0] = 0.0;
    data[7][1] = 1.0;
    data[7][2] = 0.0;
    data[8][0] = 0.0;
    data[8][1] = 0.0;
    data[8][2] = 1.0;

    eval.setData(new ContingencyTable(data));

    System.out.println("Solution D");
    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data = new Double[3][3];
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;

    eval.setData(new ContingencyTable(data));

    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data = new Double[6][6];
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;
    data[0][3] = 0.0;
    data[0][4] = 0.0;
    data[0][5] = 0.0;
    data[1][3] = 0.0;
    data[1][4] = 0.0;
    data[1][5] = 0.0;
    data[2][3] = 0.0;
    data[2][4] = 0.0;
    data[2][5] = 0.0;
    data[3][0] = 0.0;
    data[3][1] = 0.0;
    data[3][2] = 0.0;
    data[4][0] = 0.0;
    data[4][1] = 0.0;
    data[4][2] = 0.0;
    data[5][0] = 0.0;
    data[5][1] = 0.0;
    data[5][2] = 0.0;
    data[3][3] = 3.0;
    data[3][4] = 2.0;
    data[3][5] = 0.0;
    data[4][3] = 0.0;
    data[4][4] = 3.0;
    data[4][5] = 2.0;
    data[5][3] = 2.0;
    data[5][4] = 0.0;
    data[5][5] = 3.0;
    eval.setData(new ContingencyTable(data));

    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));

    System.out.println("-----");
    data = new Double[6][3];
    data[0][0] = 3.0;
    data[0][1] = 2.0;
    data[0][2] = 0.0;
    data[1][0] = 0.0;
    data[1][1] = 3.0;
    data[1][2] = 2.0;
    data[2][0] = 2.0;
    data[2][1] = 0.0;
    data[2][2] = 3.0;
    data[3][0] = 3.0;
    data[3][1] = 2.0;
    data[3][2] = 0.0;
    data[4][0] = 0.0;
    data[4][1] = 3.0;
    data[4][2] = 2.0;
    data[5][0] = 2.0;
    data[5][1] = 0.0;
    data[5][2] = 3.0;
    eval.setData(new ContingencyTable(data));

    System.out.println(eval.getData());
    System.out.println("F:" + eval.getFMeasure());
    System.out.println("VI:" + eval.getVI());
    System.out.println("V:" + eval.getVMeasure(1));
  }

  public static void main(String[] args) {

    if (args[1].equals("matching")) problemOfMatching();
    else

      // Evaluates a cluster evaluation measure against the criteria specified in Dom 2001.

      try {
        ClusterEvaluator eval = new ClusterEvaluator();

        int verified = 0;
        int total = 0;

        for (int noiseClusters = 0; noiseClusters <= 6; ++noiseClusters)
          for (int noiseClasses = 0; noiseClasses <= 6; ++noiseClasses)
            for (double e1 = 0.0; e1 <= 0.1; e1 += 0.1 / 3)
              for (double e2 = 0.0; e2 <= 0.2; e2 += 0.2 / 3)
                for (double e3 = 0.0; e3 <= 0.2; e3 += 0.2 / 3) {

                  /**
                   * if it is desired for the metric to increase with the variable in question
                   * direction = 1.0
                   * if is is desired for the metric to decrease with the variable
                   * direction = -1.0
                   */
                  double direction = 1.0;
                  double prev_metric = 0;
                  ContingencyTable prev_data = null;
                  boolean good = true;
                  boolean first = true;
                  for (int usefulClusters = 6; usefulClusters <= 11; ++usefulClusters) {
                    eval.configureExperiment(5, usefulClusters, noiseClasses, noiseClusters, e1, e2, e3);
                    double metric = eval.getQ0();
                    if (first) {
                      first = false;
                    } else {
                      if ((prev_metric * direction) > (metric * direction)) {
                        good = false;
                        System.out.println(prev_data);
                        System.out.println(eval.getData());
                        System.out.println("oops.");
                      }
                    }

                    System.out.println(
                        "5," + usefulClusters + "," + noiseClasses + "," + noiseClusters + "," + e1 + "," + e2 + "," + e3 + "," +
                            metric + ",h:" + eval.getHomogeneity() + ",c:" + eval.getCompleteness());

                    prev_metric = metric;
                    prev_data = eval.getData();
                  }

                  if (good) verified++;
                  else {
                    System.out.println("--Failed--");
                  }

                  total++;
                }

        System.out.println(verified + "/" + total + " (" + (verified * 1.0 / total) + ") passed");
      } catch (ClusterEvaluatorException e) {
        e.printStackTrace();
      }
  }
}