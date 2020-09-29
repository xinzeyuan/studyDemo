package com.study.demo.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class TestElasticsearch {

    private RestHighLevelClient client;

    @Before
    public void init(){
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }

    @Test
    public void insert(){
        IndexRequest request = new IndexRequest("posts", "doc", "1");
        String jsonString = "{\"user\":\"kimchy\",\"postDate\":\"2013-01-30\",\"message\":\"trying out Elasticsearch\"}";
        request.source(jsonString, XContentType.JSON);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void get(){
        GetRequest getRequest = new GetRequest("posts", "doc", "1");
        try {
            GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            if(getResponse.isExists()){
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                System.out.println(sourceAsMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
