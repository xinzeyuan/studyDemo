package com.study.demo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:
 * @Date: 2019/7/29
 */
//@Component
public class MongoDBClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoClient.class);

    @Value("${mongodb.server}")
    private String server;

    @Value("${mongodb.user}")
    private String user;

    @Value("${mongodb.password}")
    private String password;

    @Value("${mongodb.dbName}")
    private String dbName;

    @Value("${mongodb.connectionsPerHost}")
    private String connectionsPerHost;

    @Value("${mongodb.maxConnectionIdleTime}")
    private String maxConnectionIdleTime;

    @Value("${mongodb.connectTimeout}")
    private String connectTimeout;

    @Value("${mongodb.socketTimeout}")
    private String socketTimeout;

    @Value("${mongodb.minConnectionsPerHost}")
    private String minConnectionsPerHost;

    @Value("${mongodb.maxWaitTime}")
    private String maxWaitTime;

    private MongoClient mongoClient;

    @PostConstruct
    public void init() {
        if (StringUtils.isEmpty(server)) {
            LOGGER.error("=========mongoDB init error,please check server address============");
            //throw new MongoDBInitException("empty address");
        }

        List<ServerAddress> serverAddressList = parseServerAddress();
        MongoCredential credential = MongoCredential.createScramSha1Credential(user, dbName, password.toCharArray());

        MongoClientOptions options = MongoClientOptions.builder()
                .connectionsPerHost(Integer.parseInt(connectionsPerHost))
                .maxConnectionIdleTime(Integer.parseInt(maxConnectionIdleTime))
                .connectTimeout(Integer.parseInt(connectTimeout))
                .socketTimeout(Integer.parseInt(socketTimeout))
                .minConnectionsPerHost(Integer.parseInt(minConnectionsPerHost))
                .maxWaitTime(Integer.parseInt(maxWaitTime))
                .build();

        mongoClient = new MongoClient(serverAddressList,credential,options);

        LOGGER.info("==============mongoDB connect success,server:{}================", server);
    }

    /**
     * 解析服务器配置
     *
     * @return
     */
    private List<ServerAddress> parseServerAddress() {
        List<ServerAddress> serverAddressList = new ArrayList<ServerAddress>();
        String[] servers = server.split(",");
        for (String address : servers) {
            String[] hostPort = address.split(":");
            ServerAddress serverAddress = new ServerAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            serverAddressList.add(serverAddress);
        }

        return serverAddressList;
    }

    public MongoDatabase getDataBase() {
        return mongoClient.getDatabase(dbName);
    }

    @PreDestroy
    public void shutdown(){
        mongoClient.close();
    }
}
