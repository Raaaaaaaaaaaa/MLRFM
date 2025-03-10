import com.mlhui.MLHUI;

import com.mlhui.component.Taxonomy;
import com.mlhui.component.Transaction;
import com.mlhui.component.dataset.DataSet;
import com.mlhui.component.dataset.component.Element;
import com.mlhui.component.dataset.component.UtilityList;
import com.mlhui.others.ExternalPair;
import com.mlhui.others.UserSpecified;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AlgorithmMLRFM {

    private Statistics statistics;
    private RFM rfm;
    private MLHUI mlhui;

    public void initMLRFM() {
        statistics = new Statistics();
        rfm = new RFM();
        mlhui = new MLHUI();
    }

    public void run(String taxonomyFilePath, String transactionFilePath,
                    UserSpecified userSpecified, double rate, int size) {
        initMLRFM();

        statistics.setStartTimestamp(System.currentTimeMillis());

        DataSet dataSet = mlhui.getDataSet();
        dataSet.setTransactionList(loadTransaction(transactionFilePath, rate, size));// new
        mlhui.setTaxonomy(loadTaxonomy(taxonomyFilePath));
        constructTransactionItemListPerLevel();

        setMinThreshold(userSpecified, rfm, mlhui);

        calculateRecency(mlhui, rfm.getDelta());
        calculateFrequency(mlhui);
        calculateTWU(mlhui);

        revisedTransaction(getRFTPatterns(mlhui, rfm), mlhui);
        constructUtilityList(mlhui);

        startExploreSearchTree();

        statistics.setEndTimestamp(System.currentTimeMillis());
    }

    public void constructTransactionItemListPerLevel() {
        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();

        Taxonomy taxonomy = mlhui.getTaxonomy();
        Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();

        for (Transaction transaction : transactionList) {
            Map<Integer, List<Integer>> itemListPerLevel = transaction.getItemListPerLevel();

            //construct the high level item based on the last level itemList
            int maxLevel = taxonomy.getMaxLevel();
            for (int level = 1; level <= maxLevel; level++) {
                List<Integer> itemList = itemListPerLevel.get(level);
                List<Integer> itemListLastLevel = itemListPerLevel.get(level - 1);

                if(null == itemList) {
                    itemList = new ArrayList<>();
                }

                for (Integer itemInLastLevel : itemListLastLevel) {
                    List<Integer> ancestor = mapItemToAncestor.get(itemInLastLevel);
                    //the dataset in SPMF, sometimes item don't have ancestor
                    if(null == ancestor) {
//                        System.out.println("level :" + level);
//                        System.out.println("item " + itemInLastLevel);
                        continue;
                    }
                    //index 0 is the up one level ancestor of item, whole process is based on last level's item
                    //if you can understand, maybe you can see the constructMapItemToAncestor'example first.(Ctrl + f search "constructMapItemToAncestor")
                    Integer item = ancestor.get(0);

                    if(itemList.contains(item)) {
                        continue;
                    }

                    itemList.add(item);
                }
                itemListPerLevel.put(level, itemList);
            }
        }

    }

    public void startExploreSearchTree() {
        checkMemory();

        //exploring the RFM-patterns in different level.
        int maxLevel = mlhui.getTaxonomy().getMaxLevel();
        Map<Integer, List<UtilityList>> utilityListPerLevel = mlhui.getDataSet().getUtilityListPerLevel();
        Map<Integer, List<String>> rfmPatternsPerLevel = statistics.getRFMPatternsPerLevel();

        for (int level = 0; level <= maxLevel; level++) {
            List<String> rfmPatterns = rfmPatternsPerLevel.get(level);
            if(null == rfmPatterns) {
                rfmPatterns = new ArrayList<>();
            }

            statistics.getMinMonetaryPerLevel().put(level, rfm.getMinMonetary());
            //rfmPatterns is just for statistics
            exploreSearchTree(null, null, utilityListPerLevel.get(level), rfmPatterns);

            rfmPatternsPerLevel.put(level, rfmPatterns);

            //change the minUtility/minMonetary in different level
            rfm.setMinMonetary(rfm.getMinMonetary() * rfm.getTheta());
        }

        checkMemory();
    }

    public Taxonomy loadTaxonomy(String path){
        Taxonomy taxonomy = new Taxonomy();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while((line = br.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#'
                        || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }
                constructTaxonomyDetails(taxonomy, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        constructMapItemToAncestor(taxonomy);
        setMaxLevelInTaxonomy(taxonomy);
        setItemListInDifferentLevel(taxonomy);

        return taxonomy;
    }

    /*
    * example about constructMapItemToAncestor logic
    *       T
    *       |
    *       X
    *     /  \
    *    A    B
    *    |    |
    *    a    b
    * if construct from the lowest level(level 0, that is a, b in the fig above),
    * before the method, we have mapItemToAncestor that is:
    * a->(A)
    * b->(B)
    * A->(X)
    * B->(X)
    * X->(T)
    * every child's ancestorList only has 1 ancestor of up 1 level.
    * with the method run 1,we have :
     * a->(A, X, T)
     * b->(B)
     * A->(X)
     * B->(X)
     * X->(T)
     * and finally we have
     * a->(A, X, T)
     * b->(B, X, T)
     * A->(X, T)
     * B->(X, T)
     * X->(T)
     *
     * else if not construct the lowest level, suppose we have this run:
     * a->(A)
     * b->(B)
     * A->(X, T)
     * B->(X)
     * X->(T)
     * and then we construct a:
     * a->(A, X, T)
     * b->(B)
     * A->(X, T)
     * B->(X)
     * X->(T)
     * it's the same. because every item's ancestorList index 0 only has 1 ancestor of up 1 level,
     * actually it is LinkList that is a->(A->(X->T)), a->A->X->T.
     * so we can construct the mapItemToAncestor correctly not matter what the level order is.
     * */
    //construct the mapItemToAncestor from (child, ListOfUpOneLevelAncestor) to (child, ListOfAllAncestor),
    //and have example above.
    public void constructMapItemToAncestor(Taxonomy taxonomy) {
        Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();
        for (Integer key : mapItemToAncestor.keySet()) {
            Integer child = key;
            List<Integer> parentList = mapItemToAncestor.get(child);
            child = parentList.get(0);
            while(null != mapItemToAncestor.get(child)) {
                Integer newParent = mapItemToAncestor.get(child).get(0);
                if(null != newParent && !parentList.contains(newParent)) {
                    parentList.add(newParent);
                    child = newParent;
                }
            }
        }
    }

    public void constructTaxonomyDetails(Taxonomy taxonomy, String line) {
        String[] split = line.trim().split(",");
        Integer parent = Integer.parseInt(split[1]);
        Integer child = Integer.parseInt(split[0]);

        //construct the mapItemToChildren
        Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
        List<Integer> itemChildrenList = mapItemToChildren.get(parent);
        if(null == itemChildrenList) {
            itemChildrenList = new ArrayList<>();
        }
        itemChildrenList.add(child);
        mapItemToChildren.put(parent, itemChildrenList);

        //construct the mapItemToAncestor
        Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();
        List<Integer> itemParentList = mapItemToAncestor.get(child);
        if(null == itemParentList) {
            itemParentList = new ArrayList<>();
        }
        itemParentList.add(parent);
        mapItemToAncestor.put(child, itemParentList);
    }

    public void setMaxLevelInTaxonomy(Taxonomy taxonomy) {
        int maxLevel = 0;
        Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();
        for (Integer child : mapItemToAncestor.keySet()) {
            int currentLevel = mapItemToAncestor.get(child).size();
            maxLevel = Math.max(currentLevel, maxLevel);
        }

        taxonomy.setMaxLevel(maxLevel);
    }

    public void setItemListInDifferentLevel(Taxonomy taxonomy) {
        int maxLevel = taxonomy.getMaxLevel();
        Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();
        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);

            if(null == itemList) {
                itemList = new ArrayList<>();
            }

            if(0 == level) {
                List<Integer> itemTable = mlhui.getDataSet().getItemTable();
                itemList.addAll(itemTable);
            }else {
                //construct based on level 0 item
                List<Integer> childItemList = taxonomy.getItemListPerLevel().get(0);
                for (Integer child : childItemList) {
                    List<Integer> parentItemList = mapItemToAncestor.get(child);

                    //the taxonomy in SPMF are not a tree
                    if(parentItemList == null) {
//                        System.out.println("level: " + level);
//                        System.out.println("item: " + child);
                        continue;
                    }

                    //the dataset in SPMF maybe compose of many sub pic, so
                    //some items don't have level l's ancestor
                    if(level - 1 < parentItemList.size()) {
                        //because the item in this level l if and only if has one parent in up one level,
                        //so use ancestorList to construct the item in different level.
                        //`parentList.get(level-1)` means get the item in level l-1 's parent,
                        // and the parent is the level l item
                        Integer parentItem = parentItemList.get(level - 1);

                        if(null != parentItem && !itemList.contains(parentItem)) {
                            itemList.add(parentItem);
                        }
                    }
                }
            }

            taxonomy.getItemListPerLevel().put(level, itemList);
        }
    }

    @Deprecated
    public List<ExternalPair> loadItemProfitTable(String path) {
        List<ExternalPair> itemList =  new ArrayList<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while((line = br.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#'
                        || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }
                itemList =  constructItemPair(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return itemList;
    }

    public List<ExternalPair> constructItemPair(String line) {
        List<ExternalPair> itemList = new ArrayList<>();
        String[] split = line.trim().split(":");
        String[] items = split[0].split(",");
        String[] utility = split[1].split(",");

        int len = items.length;
        for (int i = 0; i < len; i++) {
            ExternalPair pair = new ExternalPair();
            pair.setItem(Integer.parseInt(items[i]));
            pair.setUtility(Integer.parseInt(utility[i]));

            itemList.add(pair);
        }

        return itemList;
    }

    public List<Transaction> loadTransaction(String path, double rate, int size) {
        List<Transaction> transactionList = new ArrayList<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            int tid = 1;
            while((line = br.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == '#'
                        || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }

                transactionList.add(constructTransactionForSPMF(tid, line, mlhui));

                if(tid >= (int) (rate * size)) {
                    break;
                }

                tid++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        statistics.setTransactionCnt(transactionList.size());

        return transactionList;
    }

    /*
    * only construct the level 0's transaction,
    * generalize item need to be further process
    * */
    private Transaction constructTransactionForSPMF(int tid,  String line, MLHUI mlhui) {
        List<Integer> itemTable = mlhui.getDataSet().getItemTable();

        Transaction transaction = new Transaction();
        transaction.setTid(tid);

        String[] split = line.trim().split(":");
        String[] items = split[0].trim().split(" ");
        String TU = split[1];
        String[] utilitys = split[2].trim().split(" ");

        transaction.setTU(Double.parseDouble(TU));
        List<Integer> itemList = new ArrayList<>();
        Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();

        int len = items.length;
        for (int i = 0; i < len; i++) {
            int item = Integer.parseInt(items[i]);
            itemList.add(item);
            mapItemToUtility.put(item, Double.valueOf(utilitys[i]));
            if(!itemTable.contains(item)) {
                itemTable.add(item);
            }
        }

        transaction.getItemListPerLevel().put(0, itemList);
        return transaction;
    }

    @Deprecated
    private Transaction constructTransaction(int tid,  String line, MLHUI mlhui) {
        Transaction transaction = new Transaction();
        transaction.setTid(tid);

        Taxonomy taxonomy = mlhui.getTaxonomy();

        String[] split = line.trim().split(":");
        String[] items = split[0].trim().split(",");
        String[] cnts = split[1].trim().split(",");

        int len = items.length, maxLevel = taxonomy.getMaxLevel();
        for (int level = 0; level <= maxLevel; level++) {
            Map<Integer, Integer> mapItemToCount = transaction.getMapItemToCount();
            List<Integer> itemList = transaction.getItemListPerLevel().get(level);
            if(null == itemList) {
                itemList = new ArrayList<>();
            }
            for (int j = 0; j < len; j++) {
                if(0 == level) {
                    int item = Integer.parseInt(items[j]);
                    int count = Integer.parseInt(cnts[j]);
                    mapItemToCount.put(item, count);

                    itemList.add(item);
                }else {
                    Map<Integer, List<Integer>> mapItemToAncestor = taxonomy.getMapItemToAncestor();
                    List<Integer> ancestor = mapItemToAncestor.get(Integer.parseInt(items[j]));

                    //get the level l's parent, because level 1's parent is store in index 0, so it is
                    //ancestor.get(level-1)
                    Integer parentItem = ancestor.get(level-1);

                    if(isItemListInLevelContains(itemList, parentItem)) {
                        continue;
                    }

                    itemList.add(parentItem);
                }
            }
            transaction.getItemListPerLevel().put(level, itemList);
        }

        return transaction;
    }

    public boolean isItemListInLevelContains(List<Integer> itemListInLevelI, Integer itemInLevelI) {
        for (Integer item : itemListInLevelI) {
            if(item.equals(itemInLevelI)) {
                return true;
            }
        }

        return false;
    }

    public void calculateRecency(MLHUI mlhui, double delta) {
        double constant = 1 - delta;
        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();
        int tLast =transactionList.size(), maxLevel = mlhui.getTaxonomy().getMaxLevel();
        Map<Integer, Double> mapItemToRecency = mlhui.getMapItemToRecency();

        for (Transaction transaction : transactionList) {
            transaction.setRecency(Math.pow(constant, tLast - transaction.getTid()));
            Map<Integer, List<Integer>> itemListPerLevel = transaction.getItemListPerLevel();
            for (int level = 0; level <= maxLevel; level++) {
                //calculate the item's recency in database, AKA R(x)
                List<Integer> itemList = itemListPerLevel.get(level);
                for (Integer item : itemList) {
                    Double itemRecency = mapItemToRecency.get(item);
                    if(null == itemRecency) {
                        itemRecency = 0.0;
                    }
                    itemRecency += transaction.getRecency();
                    mapItemToRecency.put(item, itemRecency);
                }
            }
        }

    }

    public void calculateFrequency(MLHUI mlhui) {
        int maxLevel = mlhui.getTaxonomy().getMaxLevel();
        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();
        Map<Integer, Integer> mapItemToFrequency = mlhui.getMapItemToFrequency();

        for (int level = 0; level <= maxLevel; level++) {
            for (Transaction transaction : transactionList) {
                List<Integer> ItemList = transaction.getItemListPerLevel().get(level);
                for (Integer item : ItemList) {

                    Integer itemFrequency = mapItemToFrequency.get(item);
                    if(null == itemFrequency) {
                        itemFrequency = 0;
                    }
                    ++itemFrequency;

                    mapItemToFrequency.put(item, itemFrequency);
                }
            }
        }

    }

    @Deprecated
    public void calculateTU(MLHUI mlhui) {
        DataSet dataSet = mlhui.getDataSet();
        List<ExternalPair> itemProfitTable = dataSet.getItemProfitTable();
        List<Transaction> transactionList = dataSet.getTransactionList();

        for (Transaction transaction : transactionList) {
            int tu = 0;
            Map<Integer, Integer> mapItemToCount = transaction.getMapItemToCount();
            Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();
            //use level l=0 's item to calculate TU
            List<Integer> itemList = transaction.getItemListPerLevel().get(0);
            for (int item : itemList) {
                int utility = getItemUtility(item, itemProfitTable);
                int cnt = mapItemToCount.get(item);
                //store the level 0 's utility
                mapItemToUtility.put(item, utility * cnt * 1.0);
                tu += utility * cnt;
            }

            transaction.setTU(tu);
        }

    }

    public void constructMapItemToUtilityInHighLevel(MLHUI mlhui, Map<Integer, List<Integer>> rftPatternsPerLevel){
        Taxonomy taxonomy = mlhui.getTaxonomy();
        int maxLevel = taxonomy.getMaxLevel();

        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();
        for (Transaction transaction : transactionList) {
            Map<Integer, List<Integer>> itemListPerLevel = transaction.getItemListPerLevel();
            Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
            Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();

            for (int level = 1; level <= maxLevel; level++) {
                List<Integer> itemList = itemListPerLevel.get(level);
                List<Integer> childItemList = itemListPerLevel.get(level - 1);

                List<Integer> RFTList = rftPatternsPerLevel.get(level);
                List<Integer> RFTChildList = rftPatternsPerLevel.get(level - 1);

                Iterator<Integer> iterator = itemList.iterator();
                while (iterator.hasNext()) {
                    Integer item = iterator.next();
                    if(RFTList.contains(item)) {
                        List<Integer> children = mapItemToChildren.get(item);
                        double itemUtility = 0;
                        for (Integer child : children) {
                            if(childItemList.contains(child) && RFTChildList.contains(child)) {
                                itemUtility += mapItemToUtility.get(child);
                            }
                        }

                        //if itemUtility = 0, that is its child has been prune.
                        if(0 != itemUtility) {
                            mapItemToUtility.put(item, itemUtility);
                        }else {
                            //prune high level item in the situation that the lower item that compose it all has been prune, then
                            //it has been prune in this transaction too.
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }




    @Deprecated
    public Integer getItemUtility(int item, List<ExternalPair> itemProfitTable) {
        for (ExternalPair externalPair : itemProfitTable) {
            if(item == externalPair.getItem()) {
                return externalPair.getUtility();
            }
        }

        return -1;
    }

    @Deprecated
    public void calculateMonetary(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();

        DataSet dataSet = mlhui.getDataSet();
        List<ExternalPair> itemProfitTable = dataSet.getItemProfitTable();
        List<Transaction> transactionList = dataSet.getTransactionList();

        Map<Integer, Double> mapItemToMonetary = mlhui.getMapItemToMonetary();

        int maxLevel = taxonomy.getMaxLevel();
        //calculate the Monetary where level = 0
        for (Transaction transaction : transactionList) {
            List<Integer> itemList = transaction.getItemListPerLevel().get(0);
            Map<Integer, Integer> mapItemToCount = transaction.getMapItemToCount();
            for (Integer item : itemList) {
                Double itemMonetary = mapItemToMonetary.get(item);
                if (null == itemMonetary) {
                    itemMonetary = 0.0;
                }

                int utility = getItemUtility(item, itemProfitTable);
                int cnt = mapItemToCount.get(item);
                itemMonetary += utility * cnt;

                mapItemToMonetary.put(item, itemMonetary);
            }
        }

        //calculate the Monetary where level > 0
        for (int level = 1; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
            Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
            for (Integer item : itemList) {
                List<Integer> childrenList = mapItemToChildren.get(item);
                Double itemMonetary = mapItemToMonetary.get(item);

                if(null == itemMonetary) {
                    itemMonetary = 0.0;
                }

                for (Integer child : childrenList) {
                    itemMonetary += mapItemToMonetary.get(child);
                }

                mapItemToMonetary.put(item, itemMonetary);
            }
        }
    }

    public void calculateTWU(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();
        int maxLevel = taxonomy.getMaxLevel();
        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();
        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();

        //calculate the Monetary where level = 0
        for (Transaction transactionSet : transactionList) {
            List<Integer> itemList = transactionSet.getItemListPerLevel().get(0);
            for (Integer item : itemList) {
                Double itemTWU = mapItemToTWU.get(item);
                if (null == itemTWU) {
                    itemTWU = 0.0;
                }
                itemTWU += transactionSet.getTU();

                mapItemToTWU.put(item, itemTWU);
            }
        }

        //calculate the Monetary where level > 0
        //TODO TWU of some high level item maybe too big
        calculateTWUInHighLevel(mlhui);
    }

    public void calculateTWUInHighLevel(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();

        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();
        int maxLevel = taxonomy.getMaxLevel();
        for (int level = 1; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
            Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
            for (Integer item : itemList) {
                List<Integer> childrenList = mapItemToChildren.get(item);
                Double itemTWU = mapItemToTWU.get(item);

                if (null == itemTWU) {
                    itemTWU = 0.0;
                }

                for (Integer child : childrenList) {
                    //TODO what?looks like some items don't appear in transaction
                    if(null == mapItemToTWU.get(child)) {
//                        System.out.println("level :" + level);
//                        System.out.println("item :" + child);
                        continue;
                    }

                    itemTWU += mapItemToTWU.get(child);
                }

                mapItemToTWU.put(item, itemTWU);
            }
        }
    }

    public void printStatus(double rate) {
        System.out.println("======================= ML-RFM Status =======================");
        System.out.println("*********************** User-specified Threshold ***********************");
        System.out.println("decayRate: " + rfm.getDelta() + " growth rate theta: " + rfm.getTheta());
        //TODO maybe output different level or only the initial
        System.out.println( "minRecency: " + rfm.getMinRecency()+ " minFrequency: " + rfm.getMinFrequency());
        printMinMonetaryPerLevel();
        System.out.println("************************************************************************");

        System.out.println("*********************** Basic Information ***********************");
        System.out.println("% Transaction: " + rate);
        System.out.println("Total time: " + (statistics.getEndTimestamp() - statistics.getStartTimestamp()) / 1000.0 + "s");
        System.out.println("Transaction counts: " + statistics.getTransactionCnt());
        System.out.println("Memory Consumption: " + statistics.getMaxMemory() + " MB");
        System.out.println("MaxLevel: " + mlhui.getTaxonomy().getMaxLevel());
        System.out.println("RFT-patterns count: " + statistics.getRFTPatternsCount());
        System.out.println("RFM-patterns count: " + statistics.getRFMPatternsCount());
        printRFMPatternsCountInDifferentLevel(true);
        System.out.println("************************************************************************");


        printItemInformation(false);
        printRFMPatterns(false);
        System.out.println("===============================================================");
        System.out.println();
        System.out.println();
    }

    public void printItemInformation(boolean isPrint) {
        if(!isPrint) {
            return;
        }

        Taxonomy taxonomy = mlhui.getTaxonomy();
        Map<Integer, Integer> mapItemToFrequency = mlhui.getMapItemToFrequency();
        Map<Integer, Double> mapItemToRecency = mlhui.getMapItemToRecency();
        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();
        int maxLevel = taxonomy .getMaxLevel();
        System.out.println("item   Recency   Frequency  TWU");
        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
            System.out.println("********************************* level"+level+" *********************************");
            for (Integer item : itemList) {
                System.out.print(item);
                System.out.print("   ");
                System.out.print(mapItemToRecency.get(item));
                System.out.print("     ");
                System.out.print(mapItemToFrequency.get(item));
                System.out.print("     ");
                System.out.print(mapItemToTWU.get(item));
                System.out.println();
            }
            System.out.println("***********************************************************************************");
        }
    }

    public void printRFMPatterns(boolean isPrint) {
        if(!isPrint) {
            return;
        }

        System.out.println("*********************** RFM patterns ***********************");
        Map<Integer, List<String>> rfmPatternsPerLevel = statistics.getRFMPatternsPerLevel();
        int maxLevel = mlhui.getTaxonomy().getMaxLevel();
        for (int level = 0; level <= maxLevel; level++) {
            System.out.println("+++++++++++++++++++++++level "+level+" +++++++++++++++++++++++++++++++++++");
            List<String> RFMPatterns = rfmPatternsPerLevel.get(level);
            int isNewLine = 0;
            for (String rfmPattern : RFMPatterns) {
                if(isNewLine == 5) {
                    isNewLine = 0;
                    System.out.println();
                }
                System.out.print(rfmPattern + " ");
                isNewLine++;
            }
            System.out.println();
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
        System.out.println("************************************************************************");
    }

    public void printRFMPatternsCountInDifferentLevel(boolean isPrint) {
        if(!isPrint) {
            return;
        }

        System.out.println("*********************** RFM patterns Count *****************************");

        Map<Integer, List<String>> rfmPatternsPerLevel = statistics.getRFMPatternsPerLevel();
        int maxLevel = mlhui.getTaxonomy().getMaxLevel();

        for (int level = 0; level <= maxLevel ; level++) {
            System.out.println("Level " + level + ": " + rfmPatternsPerLevel.get(level).size());
        }

        System.out.println("************************************************************************");
    }

    public void printMinMonetaryPerLevel() {
        System.out.println("minMonetary: ");


        Map<Integer, Double> minMonetaryPerLevel = statistics.getMinMonetaryPerLevel();
        int len = minMonetaryPerLevel.size();
        for (int i = 0; i < len; i++) {
            System.out.println("level " +i + ": " + minMonetaryPerLevel.get(i));
        }
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public RFM getRfm() {
        return rfm;
    }

    public void setRfm(RFM rfm) {
        this.rfm = rfm;
    }

    public MLHUI getMlhui() {
        return mlhui;
    }

    public void setMlhui(MLHUI mlhui) {
        this.mlhui = mlhui;
    }

    public void setMinThreshold(UserSpecified userSpecified, RFM rfm, MLHUI mlhui) {
        rfm.setMinRecency(userSpecified.getGamma());

        int transactionSize = mlhui.getDataSet().getTransactionList().size();
        rfm.setMinFrequency(transactionSize * userSpecified.getAlpha());

        rfm.setMinMonetary(userSpecified.getBeta());

        rfm.setDelta(userSpecified.getDelta());

        rfm.setTheta(userSpecified.getTheta());
    }

    public Map<Integer, List<Integer>> getRFTPatterns(MLHUI mlhui, RFM rfm) {
        Map<Integer, List<Integer>> RFTListPerLevel = new HashMap<>();

        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();
        Map<Integer, Double> mapItemToRecency = mlhui.getMapItemToRecency();
        Map<Integer, Integer> mapItemToFrequency = mlhui.getMapItemToFrequency();
        Map<Integer, List<Integer>> itemListPerLevel = mlhui.getTaxonomy().getItemListPerLevel();

        int maxLevel = mlhui.getTaxonomy().getMaxLevel();
        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> RFTList = RFTListPerLevel.get(level);

            if(null == RFTList) {
                RFTList = new ArrayList<>();
            }

//            int cnt = 0;
            for (Integer item : itemListPerLevel.get(level)) {
                if(mapItemToTWU.get(item) >= rfm.getMinMonetary() && mapItemToFrequency.get(item) >= rfm.getMinFrequency()
                        && mapItemToRecency.get(item) >= rfm.getMinRecency()) {
//                    if(cnt == 1353 || cnt == 1352) {
//                        System.out.println(mapItemToFrequency.get(cnt));
//                        System.out.println(mapItemToRecency.get(cnt));
//                        System.out.println(mapItemToTWU.get(cnt));
//                    }

                    RFTList.add(item);
//                    cnt++;
                }
            }

            statistics.setRFTPatternsCount(statistics.getRFTPatternsCount() + RFTList.size());

            RFTListPerLevel.put(level, RFTList);
        }

        return RFTListPerLevel;
    }

    public void revisedTransaction(Map<Integer, List<Integer>> RFTListPerLevel, MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();
        int maxLevel = taxonomy.getMaxLevel();

        DataSet dataSet = mlhui.getDataSet();
        List<Transaction> transactionList = dataSet.getTransactionList();

        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> RFTList = RFTListPerLevel.get(level);

            //remove the item not belong to RFT-patterns in taxonomy in different level
            //and sort to TWU ascending order in the itemListPerLevel in taxonomy
            List<Integer> itemListInTaxonomy = taxonomy.getItemListPerLevel().get(level);
            itemListInTaxonomy.removeIf(item -> !RFTList.contains(item));
            itemListInTaxonomy.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer item1, Integer item2) {
                    return compareItems(item1, item2);
                }
            });

            //remove the item not belong to RFT-patterns in transaction in different level
            //and sort to TWU ascending order in the itemListPerLevel in each transaction
            for (Transaction transaction : transactionList) {
                Map<Integer, Integer> mapItemToCount = transaction.getMapItemToCount();
                List<Integer> itemList = transaction.getItemListPerLevel().get(level);
                Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();

                Iterator<Integer> itemListIterator = itemList.iterator();
                while (itemListIterator.hasNext()) {
                    int item = itemListIterator.next();
                    if (!RFTList.contains(item)) {
                        if(0 == level) {
                            double utility = mapItemToUtility.get(item);
//
                            //update the TU
                            transaction.setTU(transaction.getTU() - utility);
                        }

                        itemListIterator.remove();
                    }
                }

                if(level > 0) {
                    revisedTWUInHighLevel(level, mlhui, RFTListPerLevel);
                }

                itemList.sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer item1, Integer item2) {
                        return compareItems(item1, item2);
                    }
                });

                transaction.getItemListPerLevel().put(level, itemList);
            }
        }

        constructMapItemToUtilityInHighLevel(mlhui, RFTListPerLevel);
    }

    public void revisedTWUInHighLevel(int level, MLHUI mlhui,Map<Integer, List<Integer>> RFTListPerLevel){
        Taxonomy taxonomy = mlhui.getTaxonomy();

        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();
        List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
        Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();

        List<Integer> RFTList = RFTListPerLevel.get(level-1);
        for (Integer item : itemList) {
            List<Integer> childrenList = mapItemToChildren.get(item);
            Double itemTWU = mapItemToTWU.get(item);
            itemTWU = 0.0;


            for (Integer child : childrenList) {
                if(RFTList.contains(child)) {
                    itemTWU += mapItemToTWU.get(child);
                }
            }

            mapItemToTWU.put(item, itemTWU);
        }
    }

    public void constructUtilityList(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();
        Map<Integer, List<Integer>> itemListPerLevel = taxonomy.getItemListPerLevel();

        DataSet dataSet = mlhui.getDataSet();
        List<Transaction> transactionList = dataSet.getTransactionList();

        int maxLevel = taxonomy.getMaxLevel();
        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> itemListInTaxonomy = itemListPerLevel.get(level);
            //UtilityList is construct based on TWU ascending order object itemListPerLevel in taxonomy,
            //so it is TWU ascending order and the items also RFT-patterns too.
            List<UtilityList> utilityLists = dataSet.getUtilityListPerLevel().get(level);

            if(null == utilityLists) {
                utilityLists = new ArrayList<>();
            }

            for (Integer item : itemListInTaxonomy) {
                UtilityList utilityList = new UtilityList();
                utilityList.setItem(item);
                utilityLists.add(utilityList);
            }

            for (Transaction transaction : transactionList) {
                List<Integer> itemList = transaction.getItemListPerLevel().get(level);
                Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();
                for (Integer item : itemList) {
                    Element element = new Element();
                    element.setTid(transaction.getTid());
                    element.setUtility(mapItemToUtility.get(item));
                    element.setRemainingUtility(getItemRUInTransaction(transaction, level, item));

                    UtilityList utilityList = getUtilityListOf(utilityLists, item);
                    if(null != utilityList) {
                        utilityList.addElement(element);
                    }
                }
            }

            dataSet.getUtilityListPerLevel().put(level, utilityLists);
        }
    }

    private int getItemRUInTransaction(Transaction transaction, int level,
                                       int item) {
        Map<Integer, List<Integer>> itemListPerLevel = transaction.getItemListPerLevel();
        List<Integer> itemList = itemListPerLevel.get(level);

        Map<Integer, Double> mapItemToUtility = transaction.getMapItemToUtility();

        int s = itemList.indexOf(item);
        int size = itemList.size();

        if(s >= size - 1) {
            return 0;
        }

        int ru = 0;
        for (int i = s + 1; i < size; i++) {
            Integer afterItem = itemList.get(i);

            ru += mapItemToUtility.get(afterItem);
        }

        return ru;
    }

    public UtilityList getUtilityListOf(List<UtilityList> utilityLists, int item) {
        for (UtilityList utilityList : utilityLists) {
            if(item == utilityList.getItem()) {
                return utilityList;
            }
        }

        return null;
    }

    public void exploreSearchTree(int[] prefix, UtilityList prefixUtilityLists,
                                  List<UtilityList> extendUtilityLists, List<String> rfmPatterns) {

        int len = extendUtilityLists.size();
        for (int i = 0; i < len; i++) {
            UtilityList PX_UL = extendUtilityLists.get(i);
            int itemX = PX_UL.getItem();
            if(PX_UL.getUtility() >= rfm.getMinMonetary() && PX_UL.getElements().size() >= rfm.getMinFrequency()
                    && getPXRecency(PX_UL) >= rfm.getMinRecency()) {
//                printRFMPatterns(itemX, prefix);
                int rfmPatternsCount = statistics.getRFMPatternsCount();
                statistics.setRFMPatternsCount(rfmPatternsCount + 1);

                String prefixToPrint = null==prefix?"":Arrays.toString(prefix);
                rfmPatterns.add("{" + prefixToPrint + itemX + "}");
            }

            //newExULs means that new Extend UtilityList, will use in next recursion.
            List<UtilityList> newExULs = new ArrayList<>();
            if(PX_UL.getUtility() + PX_UL.getRemainingUtility() >= rfm.getMinMonetary()) {//U-Prune
                for (int j = i + 1; j < len; j++) {
                    UtilityList PY_UL = extendUtilityLists.get(j);

                    UtilityList PXY_UL = construct(prefixUtilityLists, PX_UL, PY_UL);
                    if(null != PXY_UL && PXY_UL.getUtility() > 0) {
                        newExULs.add(PXY_UL);
                    }
                }

                //add x as a new prefix
                int[] newPrefix;
                if(null == prefix) {
                     newPrefix = new int[1];
                     newPrefix[0] = itemX;
                }else {
                    newPrefix = new int[prefix.length + 1];
                    System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                    newPrefix[prefix.length] = itemX;
                }

                //use new prefix and new extendUtilityList to exploring RFM-patterns recursively
                exploreSearchTree(newPrefix, PX_UL, newExULs, rfmPatterns);
            }
        }
    }

    public double getPXRecency(UtilityList PX_UL) {
        List<Element> elements = PX_UL.getElements();
        double delta = rfm.getDelta();
        double constant = 1 - delta;
        int tLast = mlhui.getDataSet().getTransactionList().size();

        double recency = 0.0;
        for (Element element : elements) {
            int tid = element.getTid();
            recency += Math.pow(constant, tLast - tid);
        }

        return recency;
    }


    //cite W.
    public UtilityList construct(UtilityList P_UL, UtilityList PX_UL, UtilityList PY_UL) {
        UtilityList PXY_UL = new UtilityList(PY_UL.getItem());
        double tempUtility = PX_UL.getUtility() + PX_UL.getRemainingUtility();

        List<Element> elements = PX_UL.getElements();

        for (Element eX : elements) {
            Element eY = findElementWithTID(eX.getTid(), PY_UL);
            if(null == eY) {
                tempUtility -= (eX.getUtility() + eX.getRemainingUtility());
                if(tempUtility < rfm.getMinMonetary()) {//LA-Prune
                    return null;
                }
//                 ey dosen't exist, then this transaction is not belong to itemset PXY.
                continue;
            }

            Element ePXY = null;
            if(null == P_UL) {
                ePXY = new Element(eX.getTid(), eX.getUtility() + eY.getUtility(), eY.getRemainingUtility());
            }else {
                Element eP = findElementWithTID(eX.getTid(), P_UL);
                if(null != eP) {
                    ePXY = new Element(eX.getTid(), eX.getUtility() + eY.getUtility() - eP.getUtility(), eY.getRemainingUtility());
                }
            }

            if (ePXY != null) {
                PXY_UL.addElement(ePXY);
            }
        }

        return PXY_UL;
    }

    //cite W.
    public Element findElementWithTID(int tid, UtilityList ul) {
        List<Element> list = ul.getElements();

        int first = 0;
        int last = list.size() - 1;

        // the binary search
        while (first <= last) {
            int middle = (first + last) >>> 1;

            if (list.get(middle).getTid() < tid) {
                first = middle + 1;
            } else if (list.get(middle).getTid() > tid) {
                last = middle - 1;
            } else {
                return list.get(middle);
            }
        }
        return null;
    }

    private int compareItems(int item1, int item2) {
        Map<Integer, Double> mapItemToTWU = mlhui.getMapItemToTWU();

        return Double.compare(mapItemToTWU.get(item1), mapItemToTWU.get(item2));
    }

    private void printRFMPatterns(int itemX, int[] prefix) {
        //handle the RFM-patterns
        System.out.print("{");
        if(null != prefix) {
            for (int p : prefix) {
                System.out.print(p+" ");
            }
        }
        System.out.print(itemX + "}");
        System.out.println();
    }

    /**
     * Method to check the memory usage and keep the maximum memory usage.
     */
    private void checkMemory() {
        // get the current memory usage
        double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
        // if higher than the maximum until now
        if (currentMemory > statistics.getMaxMemory()) {
            // replace the maximum with the current memory usage
            statistics.setMaxMemory(currentMemory);
        }
    }
}
