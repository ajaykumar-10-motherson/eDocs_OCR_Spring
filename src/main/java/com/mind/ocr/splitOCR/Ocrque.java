package com.mind.ocr.splitOCR;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ocrque {
	/** The id. */
	long id;
	/** The doc id. */
	private long docId;
	
	/** The doc rev id. */
	private long docRevId;
	
	/** The ocr typ. */
	private long ocrTyp;
	
	/** The path. */
	private String path;

	/** The bu id. */
	private long buId;
}
