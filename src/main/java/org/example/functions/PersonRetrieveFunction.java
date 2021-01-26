package org.example.functions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.example.functions.requests.Person;

/**
 * Azure Functions with HTTP Trigger.
 */
public class PersonRetrieveFunction {
    /**
     * This function listens at endpoint "/api/PersionRetrieveFunction". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/PersionRetrieveFunction
     * 2. curl {your host}/api/PersionRetrieveFunction?name=HTTP%20Query
     */
    @FunctionName("PersonRetrieveFunction")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS, route = "persons/{rowKey}") HttpRequestMessage<Optional<String>> request,
            @BindingName("rowKey") String rowKey,
            @TableInput(name = "person", tableName = "Person", partitionKey = "person", rowKey = "{rowKey}", connection = "AzureWebJobsStorage") Person inputData,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(inputData)
                .build();
    }
}