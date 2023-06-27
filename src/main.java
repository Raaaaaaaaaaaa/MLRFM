import com.mlhui.others.UserSpecified;

public class main {
    public static void main(String[] args) {
        String itemTableFilePath = "./data/itemTable.txt",
                taxonomyFilePath = "./data/taxonomy.txt",
                transactionFilePath = "./data/transaction.txt";

        //set the user-specified variable
        UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);

        AlgorithmMLRFM algorithmMLRFM = new AlgorithmMLRFM();
        algorithmMLRFM.run(taxonomyFilePath, transactionFilePath, userSpecified);
        algorithmMLRFM.printStatus();
    }
}
