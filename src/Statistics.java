/**
*
* @author ZengFanLong
* @date 19/6/2023 下午4:21
* @description used to store the statistics in the algorithm
 */
public class Statistics {
    private long startTimestamp;
    private long endTimestamp;
    private long maxMemory;
    //RFT-patterns counter
    private int pHUICount;
    //RFM-patterns counter
    private int HUICount;
    //transaction list size counter
    private int transactionCnt;

    public Statistics() {
    }

    public Statistics(long startTimestamp, long endTimestamp, long maxMemory, int pHUICount, int HUICount, int transactionCnt) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.maxMemory = maxMemory;
        this.pHUICount = pHUICount;
        this.HUICount = HUICount;
        this.transactionCnt = transactionCnt;
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

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getpHUICount() {
        return pHUICount;
    }

    public void setpHUICount(int pHUICount) {
        this.pHUICount = pHUICount;
    }

    public int getHUICount() {
        return HUICount;
    }

    public void setHUICount(int HUICount) {
        this.HUICount = HUICount;
    }

    public int getTransactionCnt() {
        return transactionCnt;
    }

    public void setTransactionCnt(int transactionCnt) {
        this.transactionCnt = transactionCnt;
    }
}
