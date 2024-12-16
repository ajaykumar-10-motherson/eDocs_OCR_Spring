package com.mind.ocr.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.mind.ocr.Service.SchedulerService;



@RestController
public class SchedulerController {

    @Resource
    SchedulerService schedulerService;    

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerController.class);
    
	/** The enable. */
	private static String enable = "1";
	
	/** The running status. */
	private static int runningStatus = 0;
	

    @Scheduled(cron = "${scheduler.cron.every20sec1}")
    public void printCurrentTime() {
    	LOGGER.info("Start OCRSchuler");
    	if (enable.equalsIgnoreCase("1") && runningStatus == 0) {
				 
			runningStatus = 1;
			boolean isSucces=schedulerService.doOCR();
			if (isSucces) {
				runningStatus = 0;
			}
			
		}
    	
    	LOGGER.info("End OCRSchuler");
    }
}
