package org.example.functions;

import java.io.*;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.*;
import java.util.logging.Level;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableOperation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.MultipartStream;
import com.microsoft.azure.functions.*;
import org.apache.commons.lang3.StringUtils;
import org.example.functions.requests.Person;
import org.example.functions.resources.HMSConstants;

/**
 * Azure Functions with HTTP Trigger.
 */
public class MultiPartCVSController {

    private static final String storageConnectionString = System.getenv("AzureWebJobsStorage");
    protected static CloudTableClient tableClient;
    protected static CloudTable persontable;

    static {
        try {
            System.out.println("ok1");
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            System.out.println("ok2");
            tableClient = account.createCloudTableClient();
            System.out.println("ok3");
            persontable = tableClient.getTableReference("Person");
            System.out.println("ok4");
        } catch (URISyntaxException | StorageException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @FunctionName("helloFunction")
    public HttpResponseMessage greet(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        //context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("name");
        String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }


    @FunctionName("personsSave")
    public HttpResponseMessage personsSave(
            @HttpTrigger(name = "personsSave",
                    methods = { HttpMethod.POST },
                    authLevel = AuthorizationLevel.FUNCTION, route="hms/persons",
                    dataType = "binary")
                    HttpRequestMessage<Optional<byte[]>> request,
            @TableOutput(name = "person", tableName = "Person", connection="AzureWebJobsStorage")
                    OutputBinding<Person> person,
            final ExecutionContext context)
            throws URISyntaxException, StorageException, IOException {

        context.getLogger().info("* Enter personsSave *");
        // start file upload

        // parse headers
        String contentType = request.getHeaders().get("content-type"); // Get content-type header
        context.getLogger().info("content-type: " + contentType);
        // here the "content-type" must be lower-case
        //String boundary = contentType.split(";")[1].split("=")[1]; // Get boundary from content-type header
        String boundary = StringUtils.substringAfter(contentType,"boundary=");
        if (boundary.length() >= 2 && boundary.charAt(0) == '"' && boundary.charAt(boundary.length() - 1) == '"')
        {
            boundary = boundary.substring(1, boundary.length() - 1);
        }
        context.getLogger().info("boundary: " + boundary);
        int bufSize = 1024;
        byte[] bs = request.getBody().get();
        InputStream in = new ByteArrayInputStream(bs); // Convert body to an input stream
        MultipartStream multipartStream = new MultipartStream(in, boundary.getBytes(), bufSize, null); // Using
        // MultipartStream to parse body input stream
        boolean nextPart = multipartStream.skipPreamble();

        while (nextPart) {
            String header = multipartStream.readHeaders();
            context.getLogger().info("header: " + header);

            if (header.contains("filename")) {
                String fileName = extractFileName(header);
                context.getLogger().info("fileName: " + fileName);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                multipartStream.readBodyData(baos);
                context.getLogger().info("start csv parser block ");
                try (
                        Reader reader = new StringReader(baos.toString());
                        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim());

                ) {
                    int rowKeyIndex = 0;
                    TableOperation createPersion;
                    Person newPerson ;
                    for (CSVRecord csvRecord : csvParser) {
                        newPerson = new Person();
                        setPersonAttributes(csvRecord, rowKeyIndex, newPerson);
                        createPersion = TableOperation.insert(newPerson);
                        persontable.execute(createPersion);
                        rowKeyIndex++;
                    }
                }
                catch (Exception exp) {
                    context.getLogger().log(Level.SEVERE, "Error occurred in parsing persons: ", exp);
                }
            }
            nextPart = multipartStream.readBoundary();
        }

        // return response
        context.getLogger().log(Level.INFO, "Java HTTP file upload ended. Length: " + bs.length);
        return request.createResponseBuilder(HttpStatus.OK).body("\nHello\n request body length->, " + bs.length
                +" \ncontentType> " + contentType
                + " \nboundary> " + boundary
                +" \nRequest> " +request.getBody()).build();
    }

    private Person setPersonAttributes(CSVRecord csvRecord, int rowKeyIndex, Person person) {

        person.setAge(Integer.parseInt(csvRecord.get(HMSConstants.AGE)));
        person.setName(csvRecord.get(HMSConstants.NAME));
        person.setPartitionKey(csvRecord.get(HMSConstants.PERSONTYPE));
        person.setRowKey(csvRecord.get(HMSConstants.PERSONTYPE) + rowKeyIndex);
        return person;
    }

    // extracts file name from a multipart boundary
    public static String extractFileName(String header) {
        final String FILENAME_PARAMETER = "filename=";
        final int FILENAME_INDEX = header.indexOf(FILENAME_PARAMETER);
        String name = header.substring(FILENAME_INDEX + FILENAME_PARAMETER.length(), header.lastIndexOf("\""));
        String fileName = name.replaceAll("\"" , "").replaceAll(" ", "");

        return fileName;
    }

}
