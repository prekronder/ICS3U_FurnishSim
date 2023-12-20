package furnishsim;

import java.util.Random;

/**
 * AKA GAME STATE
 */
public class Business
{

    // RANDOM CHANCE MAKER ===
    private Random aiBehaviours = new Random();
    // =======================

    private String companyName;
    private boolean storeBought;
    private double customerAttraction;
    private double money;
    private int profit; // TODO: -40 AT THE START.
    private int[] resources;
    final private double[] resourcePrice = {8.0, 0.15, 0.05, 32};
    final public int WOOD = 0;
    final public int NAILS = 1;
    final public int SCREWS = 2;
    final public int HARDBOARD = 3;
    // TODO: ADD BALANCING CHANGES
    private Furniture[] furnitures = { // TODO ADD THE REST OF THE ICONS
            /* new Furniture("Stool", 0, 3, 10, true),*/
            new Furniture("Stool", 0.0, new int[] {2, 4, 0, 0}, 17.6, false, "furnishResources/STOOL.png", "furnishResources/STOOL_HOVER.png", "furnishResources/STOOL_PRESSED.png"),
            new Furniture("Chair", 200.0, new int[] {3, 6, 4, 0}, 25.1, false, "furnishResources/CHAIR.png", "furnishResources/CHAIR_HOVER.png", "furnishResources/CHAIR_PRESSED.png"),
            new Furniture("Table", 400, new int[] {8, 4, 8, 1}, 96.8, false, "furnishResources/TABLE.png", "furnishResources/TABLE_HOVER.png", "furnishResources/TABLE_PRESSED.png"),
            new Furniture("Shelf", 650, new int[] {2, 4, 0, 1}, 48.6, false, "furnishResources/SHELF.png", "furnishResources/SHELF_HOVER.png", "furnishResources/SHELF_PRESSED.png"),
            new Furniture("Mailbox", 1500, new int[] {3, 40, 12, 0}, 30.6, false, "furnishResources/MAILBOX.png", "furnishResources/MAILBOX_HOVER.png", "furnishResources/MAILBOX_PRESSED.png") };
    private int ticksActive;
    private int customersInStore;
    private int enterPercentage; // <-- will change based off stage
    private int numberOfPosters;
    final private int POSTER_PRICE = 150; // TODO: price changes based off of numberOfPosters.

    public Business()
    {
        companyName = "Industrial Furniture Inc.";
        money = 150;
        storeBought = false;
        ticksActive = 0;
        profit = 0;
        resources = new int[] {0, 0, 0, 0};
        enterPercentage = 45; // STAGE 1 STAT.
        customerAttraction = 1.0;
        numberOfPosters = 0;
    }

    public String tick()
    {
        String tickUpdates = "\n";

        // Run customer ai; entering store, leaving, buying;
        tickUpdates += runCustomerAI();

        // Run event probability.
        tickUpdates += runEvent();

        return tickUpdates;

        /*
        this will be called from gui; kind of like an "update" method
        updates values per tick like
        - customers entering
        - events (robberies, discounts, etc) <-- random events
        - sales
        - updates time spent?
         */
    }

    public String runCustomerAI()
    {
        String customerUpdates = "";

        // CUSTOMER PURCHASING
        if (customersInStore > 0) {
            for (int customer = 1; customer <= customersInStore; customer++) {
                // checks stock
                if (findTotalStock() == 0) { //  no stock TODO say how many customers in store
                    customerUpdates += "[CUSTOMER] NO ITEMS IN STOCK: CUSTOMER LEFT\n";
                    customersInStore -= 1;
                    customerAttraction -= 0.025;
                    break;
                }
                // there are people in the store
                else
                {
                    // TODO: make random selection; not in line. (tip, change order of if and for)
                    if (aiBehaviours.nextInt(1, 100) <= 30) {
                        // checks availibility in order; might randomize for realism later through random indexing [5-15min]
                        for (Furniture furniture : furnitures) {
                            if (furniture.getNumInStock() > 0) {
                                money += furniture.getSellingPrice();
                                furniture.setNumInStock(furniture.getNumInStock() - 1);
                                customerUpdates += "[TRANSACTION] " + furniture.getFurnitureName().toUpperCase() + " WAS BOUGHT | - 1 UNIT | + " + furniture.getSellingPrice() + "$\n";
                                customersInStore--;
                                break;
                            }
                        }
                    }
                    else
                    {
                        customerUpdates += "[CUSTOMERS] CURRENTLY IN STORE: " + customersInStore + "\n";
                    }
                }
            }
        }
        else
        {
            customerUpdates += "[CUSTOMERS] NO CUSTOMERS IN STORE\n";
        }

        double customerEnterPercentage = customerAttraction * enterPercentage; // [stage.getEnterPercentage] store popularity  ; TODO rename variable
        if (aiBehaviours.nextDouble(0.0, 100.0) <= customerEnterPercentage)
        {
            customersInStore++;
            customerUpdates += "[CUSTOMER] ENTERED STORE\n";
            // TODO multiple people
        }

        return customerUpdates;
    }

    public String runEvent()
    {
        String eventUpdates = "";

        // probability of ANY event to happen: 10%
        if (aiBehaviours.nextInt(1, 100) <= 5)
        {
            // another roll for which event happens (sections of 20+ intervals)
            int eventToOccur = aiBehaviours.nextInt(1, 100);

            if (eventToOccur <= 20)
            {
                // EVENT 1 | ROBBERY
                // - money
                // - customerAttraction
                int moneyLost = aiBehaviours.nextInt(1, 75);
                eventUpdates += "[EVENT] YOU WERE ROBBED | - " + moneyLost + "$ |";
                if ((money - moneyLost) <= 0)
                {
                    money = 0;
                }
                else
                {
                    money -= moneyLost;
                }

                // how could you get robbed so easily??
                customerAttraction -= 0.1;

            }
            else if (eventToOccur <= 40)
            {
                // EVENT 2 | DISTANT RELATIVE [MILLIONARE] DIED
                // + money
                int moneyInherited;

                // 2%
                if (aiBehaviours.nextInt(1, 100) <= 2)
                {
                    eventUpdates += "[EVENT] A VERY RICH RELATIVE PASSED AWAY |";
                    moneyInherited = aiBehaviours.nextInt(500, 2000);
                }
                else
                {
                    eventUpdates += "[EVENT] A DISTANT RELATIVE PASSED AWAY |";
                    moneyInherited = aiBehaviours.nextInt(50, 250);
                }
                eventUpdates += " + " + moneyInherited + "$ |";
                money += moneyInherited;

            }
            else if (eventToOccur <= 60)
            {
                // EVENT 3 | FURNITURE BREAKS
                // random --> -= stock
                eventUpdates = "[EVENT] you woulda lost some furniture if it wasnt for this tupoy programmer!";
                // TODO
            }
            else if (eventToOccur <= 80)
            {
                // EVENT 4 | WARPED WOOD OR RATS GNAWED ON WOOD STOCK
                int woodLost = aiBehaviours.nextInt(1, 75);
                eventUpdates += "[EVENT] RATS GNAWED ON WOOD | - " + woodLost + " |";
                if ((resources[WOOD] - woodLost) <= 0)
                {
                    resources[WOOD] = 0;
                }
                else
                {
                    resources[WOOD] -= woodLost;
                }
            }
            else if (eventToOccur <=100)
            {
                // EVENT 5 | SOCIAL MEDIA INFLUENCER CRITISIZES OR PRAISES BRAND
                // randomBoolean --> true: + customerAttraction --> false: -customers
                if (aiBehaviours.nextBoolean())
                {
                    // GOT PRAISED!
                    eventUpdates += "[EVENT] \"w rizz chat\" | YOU WENT VIRAL |";
                    customerAttraction += 0.5;
                }
                else
                {
                    // BOO! BAD INDUSTRIAL COMPANY!
                    eventUpdates += "[EVENT] \"industrial revolution tiktok slideshow\" | YOUR REPUTATION WENT DOWN |";
                    customerAttraction -= 0.2;
                }

            }
            else
            {
                eventUpdates += "[EVENT] uhh there was supposed to be an event..";
            }
        }

        return eventUpdates;
    }

    // ========================
    public void buyResource(int quantity, int resourceIndex)
    {
            if (money >= (resourcePrice[resourceIndex] * quantity))
            {
                money -= (resourcePrice[resourceIndex] * quantity);
                resources[resourceIndex] += quantity;
            }
    }

//        switch(type) {
//            case 0:
//                if (money >= (WOOD_PRICE * quantity)) {
//                money -= (WOOD_PRICE * quantity);
//                resources[WOOD] += quantity;
//                }
//            case 1:
//                if (money >= (NAIL_PRICE * quantity)) {
//                    money -= (NAIL_PRICE * quantity);
//                    resources[NAILS] += quantity;
//                }
//            case 2:
//                if (money >= (SCREW_PRICE * quantity)) {
//                    money -= (SCREW_PRICE * quantity);
//                    resources[SCREWS] += quantity;
//                }
//            case 3:
//                if (money >= (HARDBOARD_PRICE * quantity)) {
//                    money -= (HARDBOARD_PRICE * quantity);
//                    resources[HARDBOARD] += quantity;
//                }
//        }

    public void buyStore() {
        if (money >= 8500) {
            storeBought = true;
        }
    }

    public void makeFurniture(Furniture furniture)
    {
        if ((furniture.hasBlueprint()) && (listIsGreater(resources, furniture.getResourceCost())))
        {
            subtractList(resources, furniture.getResourceCost());
            furniture.setNumInStock(furniture.getNumInStock() + 1);
        }
    }

    public void buyBlueprint(Furniture furniture)
    {
        if (money >= furniture.getBlueprintCost())
        {
            money -= furniture.getBlueprintCost();
            furniture.setHasBlueprint(true);
        }
    }

    public void putUpPoster()
    {
        if (money >= POSTER_PRICE)
        {
            money -= POSTER_PRICE;
            customerAttraction += Math.round(1000.0 * (5.0 / ((Math.pow(numberOfPosters, 2.0)) + 25.0))) / 1000.0;
            numberOfPosters++;
        }
    }


    public Furniture[] getFurnitures() {
        return furnitures;
    }

    public int findTotalStock()
    {
        int totalStock = 0;

        for (Furniture furniture : furnitures)
        {
            totalStock += furniture.getNumInStock();
        }

        return totalStock;
    }



    public int getNumPosters()
    {
        return numberOfPosters;
    }

    public double getCustomerAttraction()
    {
        return customerAttraction;
    }

    public String toString()
    {
        // all basic info
        String businessData = companyName.toUpperCase() + " | " + "MONEY: " + money + "$ | WOOD: " + resources[WOOD] + " | NAILS: " + resources[NAILS] + " | SCREWS: " + resources[SCREWS] + " | HARDWOOD: " + resources[HARDBOARD] +
                " | TICKS SINCE START: " + ticksActive + " | TOTAL STOCK: " + findTotalStock() + " | CUSTOMERS IN STORE: " + customersInStore + " | TOTAL PROFITS: " + profit + " | CUSTOMER ATTRACTION: " + customerAttraction + " | ";

        // stock
        for (Furniture furniture : furnitures)
        {
            businessData += (furniture.getFurnitureName().toUpperCase() + ": " + furniture.getNumInStock() + " | ");
        }

        return businessData;
    }

    public int getTicksActive() {
        return ticksActive;
    }

    public void setTicksActive(int ticksActive) {
        this.ticksActive = ticksActive;
    }

    public int getBlueprintsUnlocked()
    {
        int blueprintsUnlocked = 0;

        for (Furniture furniture : furnitures)
        {
            if (furniture.hasBlueprint())
            {
                blueprintsUnlocked++;
            }
        }

        return blueprintsUnlocked;
    }

    public double getMoney()
    {
        return money;
    }

    public int[] getResources() {
        return resources;
    }

    public String getCompanyName() {
        return companyName;
    }

    public boolean isBoughtStore() {
        return storeBought;
    }



    // utility
    public boolean listIsGreater(int[] list1, int[] list2) { // returns true if list1's elements are all greater than or equal to list2's
        for (int i = 0; i < list1.length; i++) {
            if (list1[i] < list2[i]) {
                return false;
            }
        }
        return true;
    }
    public void subtractList(int[] list1, int[] list2) { // subtracts list2 from list1
        for (int i = 0; i < list1.length; i++) {
            list1[i] -= list2[i];
        }
    }
}

