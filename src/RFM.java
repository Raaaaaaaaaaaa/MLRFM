/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:22
* @description used to store the min thresholds in the algorithm
 */
public class RFM {
    private double minFrequency;
    private double minRecency;
    private double minMonetary;

    public RFM() {
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
}
