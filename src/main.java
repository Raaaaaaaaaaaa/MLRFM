import com.mlhui.others.UserSpecified;

public class main {
    public static void main(String[] args) {
//        String taxonomyFilePath = "./data/test_taxonomy.txt",
//                transactionFilePath = "./data/test.txt";

        String taxonomyFilePath = "./data/liquor_taxonomy.txt",
                transactionFilePath = "./data/liquor_11.txt";

        //set the user-specified variable
//        UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);

        UserSpecified userSpecified = new UserSpecified(0.005, 15000, 0.005, 0.01, 2);
//        UserSpecified userSpecified = new UserSpecified(0.01, 1000, 10, 0.01, 1);

        double[] rateList = new double[]{0.25, 0.5, 0.75, 1.0};
        int size = 52131;
        AlgorithmMLRFM algorithmMLRFM = new AlgorithmMLRFM();
        for (double rate : rateList) {
            algorithmMLRFM.run(taxonomyFilePath, transactionFilePath, userSpecified, rate, size);
            algorithmMLRFM.printStatus(rate);
        }
    }
}
