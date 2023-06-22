import com.mlhui.others.UserSpecified;

public class main {
    public static void main(String[] args) {
        String itemTableFilePath = "./data/itemTable.txt",
                taxonomyFilePath = "./data/taxonomy.txt",
                transactionFilePath = "./data/transaction.txt";

        /*
        * example 1. UserSpecified userSpecified = new UserSpecified(0.43, 500, 3, 0.01)
        * example 2. UserSpecified userSpecified = new UserSpecified(0.43, 130, 3, 0.01);
        * example 3. UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01);/ UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);
         * */
        UserSpecified userSpecified = new UserSpecified(0.43, 170, 3, 0.01, 1.6);

        AlgorithmMLRFM algorithmMLRFM = new AlgorithmMLRFM();
        algorithmMLRFM.run(itemTableFilePath, taxonomyFilePath, transactionFilePath, userSpecified);
    }
}
