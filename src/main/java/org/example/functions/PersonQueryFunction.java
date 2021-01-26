package org.example.functions;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableQuery;
import org.apache.commons.lang3.StringUtils;
import org.example.functions.requests.Person;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Azure Functions with HTTP Trigger.
 */
public class PersonQueryFunction {

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

    /**
     * This function listens at endpoint "/api/PersionRetrieveFunction". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/PersionRetrieveFunction
     * 2. curl {your host}/api/PersionRetrieveFunction?name=HTTP%20Query
     */
    @FunctionName("PersonQueryFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "persons") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        Map<String, String> queryParams = request.getQueryParameters();
        String name = "";
        if (queryParams.get("name") != null) {
            name = queryParams.get("name");
        }
        String age = "";
        if (queryParams.get("age") != null) {
            age = queryParams.get("age");
        }
        //System.out.println("name: " + name);
        //System.out.println("age: " + age);
        final String personFilter = createFilters(name, (age.equals("")?null:Integer.parseInt(age)));
        System.out.println("personFilter: " + personFilter);
        TableQuery<Person> personTableQuery = TableQuery.from(Person.class).where(personFilter);
        Set<Person> persons = new HashSet<Person>();
        Person person;
        for (Person entity : personTable.execute(personTableQuery)) {
            person = new Person();
            person.setRowKey(entity.getRowKey());
            person.setPartitionKey(entity.getPartitionKey());
            person.setName(entity.getName());
            person.setAge(entity.getAge());
            persons.add(person);
            System.out.println("rowKey:" + entity.getRowKey());
            System.out.println("name:" + entity.getName());
            System.out.println("age:" + entity.getAge());

        }

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(persons)
                .build();
    }

    public String createFilters(String name, Integer age) {
        List<String> filters = new ArrayList<>();
        if (name != null && !name.equals("")) {
            filters.add(TableQuery.generateFilterCondition("Name", TableQuery.QueryComparisons.EQUAL, name));
        }
        if (age != null) {
            filters
                    .add(TableQuery.generateFilterCondition("Age", TableQuery.QueryComparisons.EQUAL, age));
        }
        return andFilters(filters);
    }

    private static String andFilters(List<String> filters) {
        if (filters.isEmpty() || filters.stream().filter(StringUtils::isNotBlank).findAny().isPresent() == false) {
            return null;
        } else {
            // More than 1 filter, join them together
            return filters.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(" and "));
        }
    }

    public static void main(String[] args) {
        try
        {
            // Define constants for filters.
            final String PARTITION_KEY = "PartitionKey";
            final String ROW_KEY = "RowKey";
            final String TIMESTAMP = "Timestamp";

            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse("UseDevelopmentStorage=true");

            // Create the table client.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create a cloud table object for the table.
            CloudTable cloudTable = tableClient.getTableReference("person");

            // Create a filter condition where the partition key is "Smith".
            String partitionFilter = TableQuery.generateFilterCondition(
                    PARTITION_KEY,
                    TableQuery.QueryComparisons.EQUAL,
                    "person");

            // Specify a partition query, using "Smith" as the partition key filter.
            TableQuery<Person> partitionQuery =
                    TableQuery.from(Person.class)
                            .where(partitionFilter);

            // Loop through the results, displaying information about the entity.
            for (Person entity : cloudTable.execute(partitionQuery)) {
                System.out.println(entity.getPartitionKey() +
                        " " + entity.getRowKey() +
                        "\t" + entity.getName() +
                        "\t" + entity.getAge());
            }
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }
    }

}