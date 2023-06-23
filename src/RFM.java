/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:22
* @description used to store the min thresholds in the algorithm
 */
public class RFM {
    //threshold of F-pattern
    private double minFrequency;
    //threshold of R-pattern
    private double minRecency;
    //threshold of M-pattern
    private double minMonetary;
    //used to calculate the recency
    private double delta;
    //used to calculate the minMonetary in different level
    private double theta;

    public RFM() {
    }

    public RFM(double minFrequency, double minRecency, double minMonetary, double delta, double theta) {
        this.minFrequency = minFrequency;
        this.minRecency = minRecency;
        this.minMonetary = minMonetary;
        this.delta = delta;
        this.theta = theta;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public RFM(double minFrequency, double minRecency, double minMonetary) {
        this.minFrequency = minFrequency;
        this.minRecency = minRecency;
        this.minMonetary = minMonetary;
    }

    public double getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(double minFrequency) {
        this.minFrequency = minFrequency;
    }

    public double getMinRecency() {
        return minRecency;
    }

    public void setMinRecency(double minRecency) {
        this.minRecency = minRecency;
    }

    public double getMinMonetary() {
        return minMonetary;
    }

    public void setMinMonetary(double minMonetary) {
        this.minMonetary = minMonetary;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }
}
