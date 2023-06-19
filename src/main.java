import com.mlhui.others.UserSpecified;

public class main {
    public static void main(String[] args) {
        String itemTableFilePath = "./data/itemTable.txt",
                taxonomyFilePath = "./data/taxonomy.txt",
                transactionFilePath = "./data/transaction.txt";

        UserSpecified userSpecified = new UserSpecified(0, 0, 0, 0.01);

        AlgorithmMLRFM algorithmMLRFM = new AlgorithmMLRFM();
        algorithmMLRFM.run(itemTableFilePath, taxonomyFilePath, transactionFilePath, userSpecified);
    }
}
