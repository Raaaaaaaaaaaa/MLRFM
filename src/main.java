import com.mlhui.others.UserSpecified;

public class main {
    public static void main(String[] args) {
//        String taxonomyFilePath = "./data/test_taxonomy.txt",
//                transactionFilePath = "./data/test.txt";

        String taxonomyFilePath = "./data/test_taxonomy.txt",
                transactionFilePath = "./data/test.txt";

        //set the user-specified variable
//        UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);

        UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);
//        UserSpecified userSpecified = new UserSpecified(0.01, 1000, 10, 0.01, 1);

        AlgorithmMLRFM algorithmMLRFM = new AlgorithmMLRFM();
        algorithmMLRFM.run(taxonomyFilePath, transactionFilePath, userSpecified);
        algorithmMLRFM.printStatus();
    }
}
