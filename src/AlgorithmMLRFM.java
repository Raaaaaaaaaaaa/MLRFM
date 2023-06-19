import com.mlhui.MLHUI;

import com.mlhui.others.UserSpecified;

public class AlgorithmMLRFM {
    private Statistics statistics;
    private RFM rfm;
    private MLHUI mlhui;

    public void initMLRFM() {
        statistics = new Statistics();
        rfm = new RFM();
        mlhui = new MLHUI();
    }

    public void run(String itemTablePath, String taxonomyFilePath, String transactionFilePath,
                    UserSpecified userSpecified) {

    }
}
