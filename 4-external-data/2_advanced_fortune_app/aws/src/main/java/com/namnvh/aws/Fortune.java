package com.namnvh.aws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

@Controller
public class Fortune {

    private static final String TABLE_NAME = "fortune";
    private static final String PRIMARY_KEY = "id";
    private static final String CONTENT_ATTR = "content";
    private List<String> initFortunes;
    private List<String> currentFortunes;
    private String newFortune;

    private DynamoDB dynamoDB;

    public Fortune() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.dynamoDB = new DynamoDB(client);

        // construct fortunes
        this.initFortunes = new ArrayList<String>();
        this.initFortunes.add("It may be difficult, but it will be worth it in the end");
        this.initFortunes.add("Learn from your mistakes. Try not to make them again.");
        this.initFortunes.add("Free your mind, and the rest will follow.");

        // init current fortunes
        this.currentFortunes = new ArrayList<String>();

        // init new fortune
        this.newFortune = "";
    }

    @GetMapping
    public String init(Model model) throws InterruptedException {
        deleteTableIfExist();
        createTable();
        insertInitData();

        // add list fortunes to display on html
        this.currentFortunes = this.initFortunes;
        model.addAttribute("currentFortunes", this.currentFortunes);
        return "index";
    }

    @PostMapping
    public String add(@ModelAttribute("newFortune") String newFortune, Model model) {

        // create list fortunes to display on html
        this.currentFortunes = getCurrentDataInDynamoDB();

        // insert new fortune to DB
        insertNewFortune(this.currentFortunes.size(), newFortune);
        this.currentFortunes.add(newFortune);
        model.addAttribute("currentFortunes", this.currentFortunes);
        return "index";
    }

    private void deleteTableIfExist() {
        Table table = this.dynamoDB.getTable(TABLE_NAME);
        try {
            table.delete();
            table.waitForDelete();
            System.out.print("Table successfully deleted.");
        } catch (Exception e) {
            System.err.println("Cannot perform table delete. Table already deleted.");
        }
    }

    private void createTable() throws InterruptedException {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(PRIMARY_KEY).withAttributeType("N"));

        List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
        keySchema.add(new KeySchemaElement().withAttributeName(PRIMARY_KEY).withKeyType(KeyType.HASH));

        CreateTableRequest request = new CreateTableRequest().withTableName(TABLE_NAME).withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                        new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        Table table = this.dynamoDB.createTable(request);

        table.waitForActive();
    }

    private void insertInitData() {
        Table table = this.dynamoDB.getTable(TABLE_NAME);

        for (int i = 0; i < this.initFortunes.size(); i++) {
            int currentId = i + 1;
            Item item = new Item().withPrimaryKey(PRIMARY_KEY, currentId).withString(CONTENT_ATTR,
                    this.initFortunes.get(i));
            // Write the item to the table
            table.putItem(item);
        }
    }

    private void insertNewFortune(int currentMaxId, String newFortuneContent) {
        Table table = this.dynamoDB.getTable(TABLE_NAME);
        // Write the item to the table
        Item newItem = new Item().withPrimaryKey(PRIMARY_KEY, currentMaxId + 1).withString(CONTENT_ATTR, newFortuneContent);

        table.putItem(newItem);
    }

    private List<String> getCurrentDataInDynamoDB() {
        this.currentFortunes = new ArrayList<String>();

        Table table = this.dynamoDB.getTable(TABLE_NAME);
        ItemCollection<ScanOutcome> items = table.scan();

        Iterator<Item> iter = items.iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            this.currentFortunes.add(item.getString(CONTENT_ATTR));
        }
        return this.currentFortunes;
    }

    public String getNewFortune() {
        return newFortune;
    }

    public void setNewFortune(String newFortune) {
        this.newFortune = newFortune;
    }

}
