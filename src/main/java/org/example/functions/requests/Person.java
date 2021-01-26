package org.example.functions.requests;

import com.microsoft.azure.storage.table.TableServiceEntity;

public class Person extends TableServiceEntity {
    private String PartitionKey;
    private String RowKey;
    private String name;
    private int age;

    public Person(String partitionKey, String rowKey, String name, int age) {
        PartitionKey = partitionKey;
        RowKey = rowKey;
        this.name = name;
        this.age = age;
    }

    public Person() {
    }

    @Override
    public String getPartitionKey() {return this.PartitionKey;}
    @Override
    public void setPartitionKey(String key) {this.PartitionKey = key; }
    @Override
    public String getRowKey() {return this.RowKey;}
    @Override
    public void setRowKey(String key) {this.RowKey = key; }
    public String getName() {return this.name;}
    public void setName(String name) {this.name = name; }
    public int getAge() {return age; }
    public void setAge(int age) {this.age = age; }
}