import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:21
* @description used to store the statistics in the algorithm
 */
public class Statistics {
    private long startTimestamp;
    private long endTimestamp;
    private double maxMemory;
    //RFT-patterns counter
    private int RFTPatternsCount = 0;
    //RFM-patterns counter
    private int RFMPatternsCount = 0;
    //transaction list size counter
    private int transactionCnt;

    private Map<Integer, List<String>> RFMPatternsPerLevel = new HashMap<>();

    public Statistics() {
    }

    public Statistics(long startTimestamp, long endTimestamp, double maxMemory, int RFTPatternsCount, int RFMPatternsCount, int transactionCnt, Map<Integer, List<String>> RFMPatternsPerLevel) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.maxMemory = maxMemory;
        this.RFTPatternsCount = RFTPatternsCount;
        this.RFMPatternsCount = RFMPatternsCount;
        this.transactionCnt = transactionCnt;
        this.RFMPatternsPerLevel = RFMPatternsPerLevel;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public double getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(double maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getRFTPatternsCount() {
        return RFTPatternsCount;
    }

    public void setRFTPatternsCount(int RFTPatternsCount) {
        this.RFTPatternsCount = RFTPatternsCount;
    }

    public int getRFMPatternsCount() {
        return RFMPatternsCount;
    }

    public void setRFMPatternsCount(int RFMPatternsCount) {
        this.RFMPatternsCount = RFMPatternsCount;
    }

    public int getTransactionCnt() {
        return transactionCnt;
    }

    public void setTransactionCnt(int transactionCnt) {
        this.transactionCnt = transactionCnt;
    }

    public Map<Integer, List<String>> getRFMPatternsPerLevel() {
        return RFMPatternsPerLevel;
    }

    public void setRFMPatternsPerLevel(Map<Integer, List<String>> RFMPatternsPerLevel) {
        this.RFMPatternsPerLevel = RFMPatternsPerLevel;
    }
}
