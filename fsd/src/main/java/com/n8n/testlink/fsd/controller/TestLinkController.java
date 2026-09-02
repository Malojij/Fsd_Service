package com.n8n.testlink.fsd.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n8n.testlink.fsd.service.TestLinkService;


@RestController
@CrossOrigin
@RequestMapping("/testlink")

public class TestLinkController {
	
	private static final Logger log = LoggerFactory.getLogger(TestLinkController.class);
	
	@Autowired
	private TestLinkService testLinkService;
	
	@Value("${testlink.username}")
	private String userName;
	
	@Value("${testlink.devkey}")
	private String devKey;

	@PostMapping(value = "/upload", consumes = "application/json")
	
	public ResponseEntity<Map<String, Object>> uploadTestCases(@RequestBody Map<String, String> request) {
		
		String projectName = request.get("projectName");
		String testSuiteName = request.get("testSuiteName");
		String xmlContent = request.get("xmlContent");
		
		System.out.println("\n\n\n");
		log.info("============================📥 Request received============================");
		
		LocalDateTime timeNow = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String timestamp = timeNow.format(formatter);
		log.info("Request received at:"  + timestamp );
		
		log.info("PROJECT: {}",  projectName);
		log.info("SUITE: {}",  testSuiteName);

						
		if (projectName == null || projectName.isBlank()) 
		{
			return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "ProjectName is required"));
		}
		
		if (xmlContent == null || xmlContent.isBlank()) 
		{
			return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "XMLContent is required"));
		}
		
		long start = System.currentTimeMillis();
		
		Map<String, Object> result = testLinkService.uploadTestCasesFromXMLString(userName, devKey, projectName, "MJTEST", testSuiteName, xmlContent);
		
		long duration = System.currentTimeMillis() - start;
	
	
		log.info("⏱ Time Taken: {} seconds",  duration/1000.0);
		log.info("============================📥 Upload Completed ============================");		
		
		//log.info("\n\n\n");
		System.out.println("\n\n\n");
		
		return ResponseEntity.ok(Map.of("status", "success", "message", result));
	}
}





