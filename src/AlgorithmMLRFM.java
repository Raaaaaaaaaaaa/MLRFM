import com.mlhui.MLHUI;

import com.mlhui.component.Taxonomy;
import com.mlhui.component.Transaction;
import com.mlhui.component.dataset.DataSet;
import com.mlhui.others.ExternalPair;
import com.mlhui.others.UserSpecified;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        initMLRFM();

        statistics.setStartTimestamp(System.currentTimeMillis());

        mlhui.setTaxonomy(loadTaxonomy(taxonomyFilePath));

        DataSet dataSet = mlhui.getDataSet();
        dataSet.setItemProfitTable(loadItemProfitTable(itemTablePath));
        dataSet.setTransactionList(loadTransaction(transactionFilePath));

        calculateTU(mlhui);
        calculateRecency(mlhui, userSpecified.getDelta());
        calculateFrequency(mlhui);
        calculateTWU(mlhui);


        statistics.setEndTimestamp(System.currentTimeMillis());

        printStatus(mlhui);
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

        setMaxLevelInTaxonomy(taxonomy);
        setItemListInDifferentLevel(taxonomy);

        return taxonomy;
    }

    public void constructTaxonomyDetails(Taxonomy taxonomy, String line) {
        String[] split = line.trim().split(",");
        Integer parent = Integer.parseInt(split[0]);
        Integer child = Integer.parseInt(split[1]);

        Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
        List<Integer> itemChildrenList = mapItemToChildren.get(parent);
        if(null == itemChildrenList) {
            itemChildrenList = new ArrayList<>();
        }
        itemChildrenList.add(child);
        mapItemToChildren.put(parent, itemChildrenList);

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
                itemList.addAll(mapItemToAncestor.keySet());
            }else {
                for (Integer child : mapItemToAncestor.keySet()) {
                    List<Integer> parentList = mapItemToAncestor.get(child);
                    //the parent in parentList is begin from 0. so the item in level 1 is `parentList.get(level-1)`
                    Integer parentItem = parentList.get(level - 1);
                    if(!itemList.contains(parentItem)) {
                        itemList.add(parentList.get(level-1));
                    }
                }
            }

            taxonomy.getItemListPerLevel().put(level, itemList);
        }
    }

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

    public List<Transaction> loadTransaction(String path) {
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

                transactionList.add(constructTransaction(tid, line, mlhui));

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

        return transactionList;
    }

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

                    //get the level's parent
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
            for (int level = 0; level <= maxLevel; level++) {
                //calculate the item's recency in database, AKA R(x)
                for (Integer item : transaction.getItemListPerLevel().get(level)) {
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
            for (Transaction transactionSet : transactionList) {
                List<Integer> ItemList = transactionSet.getItemListPerLevel().get(level);
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

    public void calculateTU(MLHUI mlhui) {
        DataSet dataSet = mlhui.getDataSet();
        List<ExternalPair> itemProfitTable = dataSet.getItemProfitTable();
        List<Transaction> transactionList = dataSet.getTransactionList();

        for (Transaction transaction : transactionList) {
            int tu = 0;
            Map<Integer, Integer> mapItemToCount = transaction.getMapItemToCount();
            List<Integer> itemList = transaction.getItemListPerLevel().get(0);
            for (int item : itemList) {
                int utility = getItemUtility(item, itemProfitTable);
                int cnt = mapItemToCount.get(item);
                tu += utility * cnt;
            }
            transaction.setTU(tu);
        }
    }

    public Integer getItemUtility(int item, List<ExternalPair> itemProfitTable) {
        for (ExternalPair externalPair : itemProfitTable) {
            if(item == externalPair.getItem()) {
                return externalPair.getUtility();
            }
        }

        return -1;
    }
    public void calculateTWU(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();
        int maxLevel = taxonomy.getMaxLevel();
        List<Transaction> transactionList = mlhui.getDataSet().getTransactionList();
        Map<Integer, Integer> mapItemToTWU = mlhui.getMapItemToTWU();

        //calculate the Monetary where level = 0
        for (Transaction transactionSet : transactionList) {
            List<Integer> itemList = transactionSet.getItemListPerLevel().get(0);
            for (Integer item : itemList) {
                Integer itemTWU = mapItemToTWU.get(item);
                if (null == itemTWU) {
                    itemTWU = 0;
                }
                itemTWU += transactionSet.getTU();

                mapItemToTWU.put(item, itemTWU);
            }
        }

        //calculate the Monetary where level > 0
        for (int level = 1; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
            Map<Integer, List<Integer>> mapItemToChildren = taxonomy.getMapItemToChildren();
            for (Integer item : itemList) {
                List<Integer> childrenList = mapItemToChildren.get(item);
                Integer itemTWU = mapItemToTWU.get(item);

                if (null == itemTWU) {
                    itemTWU = 0;
                }

                for (Integer child : childrenList) {
                    itemTWU += mapItemToTWU.get(child);
                }

                mapItemToTWU.put(item, itemTWU);
            }
        }
    }

    public void printStatus(MLHUI mlhui) {
        Taxonomy taxonomy = mlhui.getTaxonomy();
        Map<Integer, Double> mapItemToRecency = mlhui.getMapItemToRecency();
        Map<Integer, Integer> mapItemToFrequency = mlhui.getMapItemToFrequency();
        Map<Integer, Integer> mapItemToTWU = mlhui.getMapItemToTWU();

        int maxLevel = taxonomy .getMaxLevel();
        System.out.println("item   Recency   Frequency  TWU");
        for (int level = 0; level <= maxLevel; level++) {
            List<Integer> itemList = taxonomy.getItemListPerLevel().get(level);
            System.out.println("=========================level"+level+"===========================");
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
            System.out.println("=========================level"+level+"===========================");
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
}
