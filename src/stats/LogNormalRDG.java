/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package stats;



/**
 * Random deviate generator that produces log-normally distributed deviates with
 * the specified mean and squared coefficient of variation.
 * <p>
 * the values for &mu; and &sigma;<sup>2</sup> are computed using the formulae
 * described at <a
 * href="http://en.wikipedia.org/wiki/Log_normal#Mean_and_standard_deviation"
 * >Mean and standard deviation</a>
 * 
 * @author <a href="mailto:michele@cs.ucy.ac.cy">Michele Mazzucco</a>
 * 
 */
public class LogNormalRDG extends ExponentialRDG {

    private static final double TWO_PI = 2 * Math.PI;

    /**
     * The squared coefficient of variation.
     */
    private final double scv;

    /** Normal mean */
    private final double mu;

    /** Normal standard deviation. */
    private final double sigma;

    private double u;
    private double v;
    private boolean createNormalVariates;


    /**
     * Creates a new PRNG that produces random deviates distributed according to
     * a log-normal distribution with the specified mean and squared coefficient
     * of variation.
     * 
     * @param mean The mean value.
     * @param scv The squared coefficient of variation.
     */
    public LogNormalRDG(double mean, double scv) {
        super(mean);
        this.scv = scv;

        double sigma2 = Math.log(scv + 1d);
        sigma = Math.sqrt(sigma2);
        mu = Math.log(mean) - (sigma2 / 2d);

        u = -1d;
        v = -1d;
        createNormalVariates = true;
    }


    /**
     * Generates a random deviate using the Box-Muller transform for generating
     * a normally distributed variate <i>d</i>.
     * <p>
     * The returned value is d = e<sup>&mu;+&sigma;X</sup>
     * <p>
     * This algorithms is an optimized version, as it uses the same <i>u</i> and
     * <i>v</i> couple for generating <i>X</i> and <i>Y</i>.
     * <p>
     * The Java implementation described <a
     * href="http://web173.cletus.kundenserver42.de/update/Ziggurat.java"
     * >here</a> gets slow sometime.
     * 
     * @return A log-normal random deviate.
     * @see <a
     *      href="http://en.wikipedia.org/wiki/Normal_distribution#Generating_values_from_normal_distribution">Generating
     *      log-normally-distributed random variates</a>
     * @see <a
     *      href="http://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform">Box–Muller
     *      transform</a>
     */
    public double generateDeviate() {
        double d = 0d;
        if (this.createNormalVariates) {
            this.u = nextRandom();
            this.v = nextRandom();
            double x = Math.sqrt(-2d * Math.log(u)) * Math.cos(TWO_PI * v);
            d = Math.exp(this.mu + (this.sigma * x));
        } else {
            double y = Math.sqrt(-2d * Math.log(u)) * Math.sin(TWO_PI * v);
            d = Math.exp(this.mu + (this.sigma * y));
            this.createNormalVariates = true;
        }

        return d;

    }


    public double getScv() {
        return this.scv;
    }


    /**
     * Example showing how to use this class.
     * <p>
     * The Log-Normal distribution converges rather slowly, so the obtained
     * mean and squared coefficient of variation do not match exactly when
     * running the for loop for one million times.
     * 
     * @param args
     */
    public static void main(String[] args) {
    	LogNormalRDG p = new LogNormalRDG(50d, 5d);
        
    	double mu = p.mu;
        double sigma = p.sigma;

        System.out.printf("mu %.5f, sigma %.5f, mean %.3f, cov2 %.5f\n", mu,
        sigma, p.getMean(), p.getScv());

        Cov scv = new Cov();
        for (int i = 0; i < 1000000; i++) {
        	scv.add(p.generateDeviate());
        }
        System.out.printf("mean %10.10f\n", scv.mean());
        System.out.printf("scv %10.10f\n", scv.getCov2());
      }

}
