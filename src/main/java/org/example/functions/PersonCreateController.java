package org.example.functions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import org.example.functions.requests.Person;
import org.json.JSONException;
import org.json.JSONObject;


public class PersonCreateController {
    // Headers
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";

    private static final String storageConnectionString = System.getenv("AzureWebJobsStorage");
    //private static final String storageConnectionString = "UseDevelopmentStorage=true";

    protected static CloudTableClient tableClient;
    protected static CloudTable personTable;

    static {
        try {
            System.out.println("storageConnectionString: " + storageConnectionString);
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            tableClient = account.createCloudTableClient();
            personTable = tableClient.getTableReference("Person");
        } catch (URISyntaxException | StorageException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @FunctionName("createPersonFunction")
    public HttpResponseMessage add(
            @HttpTrigger(name = "postPerson", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route="persons/{partitionKey}/{rowKey}") HttpRequestMessage<Optional<Person>> request,
            @BindingName("partitionKey") String partitionKey,
            @BindingName("rowKey") String rowKey,
            @TableOutput(name="person", partitionKey="{partitionKey}", rowKey = "{rowKey}", tableName="Person", connection="AzureWebJobsStorage") OutputBinding<Person> person,
            final ExecutionContext context) {

        Person outPerson = request.getBody().get();
        outPerson.setPartitionKey(partitionKey);
        outPerson.setRowKey(rowKey);
        //outPerson.setName(request.getBody().get().getName());
        System.out.println("name---------------" + request.getBody().get().getName());

        person.setValue(outPerson);

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(outPerson)
                .build();
    }

    @FunctionName("addPersonFunction")
    public HttpResponseMessage post(
            @HttpTrigger(name = "postPerson", methods = {
                    HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route = "persons")
                    HttpRequestMessage<Optional<Person>> request,
            @TableOutput(name = "person", tableName = "Person", connection = "AzureWebJobsStorage")
                    OutputBinding<Person> person,
            final ExecutionContext context)
            throws JSONException, ParseException, URISyntaxException, StorageException, InvalidKeyException, IOException {

        Person personEntitiy = new Person();
        // Parse query parameter
        if (request.getBody().isPresent()) {
            JSONObject jsonObject = new JSONObject(request.getBody().get());
            personEntitiy = setPersonEntityAttributes(jsonObject, personEntitiy);
        }
        // create rowKey
        String partitionFilter = TableQuery.generateFilterCondition(
                "PartitionKey",
                TableQuery.QueryComparisons.EQUAL,
                "person");
        TableQuery<Person> partitionQuery =
                TableQuery.from(Person.class)
                        .where(partitionFilter);

        List<Person> personList = new ArrayList<>();
        // Loop through the results, displaying information about the entity.
        for (Person entity : personTable.execute(partitionQuery)) {
            personList.add(entity);
        }
        String rowKey = personEntitiy.getRowKey();
        if (rowKey == null || rowKey.isEmpty() || rowKey.equals("null")) {
            rowKey = new Integer(personList.size() + 1).toString();
            personEntitiy.setRowKey(rowKey);
        }
        TableOperation createPerson = TableOperation.insert(personEntitiy);
        personTable.execute(createPerson);

        return request.createResponseBuilder(HttpStatus.OK)
                .header(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .body(personEntitiy)
                .build();
    }

    private Person setPersonEntityAttributes(JSONObject jsonObject, Person person)
            throws JSONException, ParseException, IOException {
        person.setName(jsonObject.getString("name"));
        person.setAge(jsonObject.getInt("age"));
        person.setPartitionKey("person");

/*        try {
            String rowKey = jsonObject.getString("rowKey");
            if (!jsonObject.isNull(rowKey) || !rowKey.isEmpty()) {
                personEntitiy.setRowKey(rowKey);
            }
        } catch (JSONException e) {
            System.out.println("********************");
            e.printStackTrace();
        }*/

        return person;
    }
}