package com.n8n.testlink.fsd.config;

import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;

@Configuration
public class TestLinkConfig {

    @Bean
    public TestLinkAPI testLinkAPI(
            @Value("${testlink.URL}") String testLinkUrl,
            @Value("${TESTLINK_DEVKEY}") String devKey) throws Exception {

        return new TestLinkAPI(new URL(testLinkUrl),devKey);
    }
}