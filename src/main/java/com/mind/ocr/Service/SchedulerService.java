package com.mind.ocr.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.mind.ocr.configs.OcrConfig;
import com.mind.ocr.splitOCR.DecryptionUtil;
import com.mind.ocr.splitOCR.Ocrque;
import com.mind.ocr.splitOCR.Query;
import com.mind.ocr.splitOCR.SplitLog;
import com.profesorfalken.jpowershell.PowerShellResponse;

import net.sourceforge.tess4j.Tesseract1;


@Service
public class SchedulerService {
	
	  /** The temp folder path. */
    @Value("${application.server.download.folder}")
    private String tempFdrPath;

    /** The decryption key. */
    @Value("${sys.encryption.key}")
    private String decryptionKey;

    /** The OCR code. */
    @Value("${ocr.code}")
    private String ocrCode;

    /** The Tika location. */
    @Value("${dms.solr.tika.fileLocation}")
    private String tikaLocation;
	
	
	
	private final OcrConfig ocrConfig;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);
	 @Autowired
	    public SchedulerService(OcrConfig ocrConfig) {
	        this.ocrConfig = ocrConfig;
	    }
	 @Resource
	 Query query;
	 @Resource
	 DecryptionUtil decryptionUtil;

	// Supplier to load the list when needed
	  private  final Supplier<List<String>> businessUnitSupplier = () -> loadBusinessUnitList();
	  private  List<String> businessUnitList = null;
	  
	 
	 Tesseract1 instance=null;
	 public static Properties properties = null;
	 File errorLogFile = null;
    
    
 // Lazy loading with Lambda expression (Supplier)
    public  List<String> getBusinessUnitList() {
        if (businessUnitList == null) {
            synchronized (SchedulerService.class) {
                if (businessUnitList == null) {
                    businessUnitList = businessUnitSupplier.get(); // Get the list from the Supplier
                }
            }
        }
        return businessUnitList;
    }
    
    // Method to load the list from the database or configuration
    private  List<String> loadBusinessUnitList() {
        
        return query.getEncryptionEnabledBuList();
    }
    
    public static String ocrPDF(String searchableFile) {
		String fileText = "";
		try {
			// Create PdfReader instance.
			PdfReader pdfReader = new PdfReader(searchableFile);

			int pages = pdfReader.getNumberOfPages();

			// Iterate the pdf through pages.
			for (int i = 1; i <= pages; i++) {
				// Extract the page content using PdfTextExtractor.
				fileText = fileText + PdfTextExtractor.getTextFromPage(pdfReader, i) + " ";
				fileText = StringEscapeUtils.escapeXml10(fileText);

			}

			// Close the PdfReader.
			pdfReader.close();
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
		}
		return fileText;
	}
    
    public boolean doOCR() {
		LOGGER.info("Start SplitOCR -> method: doOCR()");
		List<Ocrque> ocrQList = query.getList();
		Map<String, String> map = null;
		for (Ocrque ocrque : ocrQList) {
			try {
				// Get Server Path
				map = query.getFileServerPath(ocrque.getBuId());
			} catch (Exception e) {
				LOGGER.error("Error in doOCR:" + e);
			}
			String serverPath = map.get("path");
			// Initial Insert In OCR_Q_LOG
			LOGGER.info("Initial Insert In OCRQLog for document:" + ocrque.getDocId());
			doIstInOCRQLog(ocrque.getId(), ocrque.getDocId(), ocrque.getDocRevId(), ocrque.getOcrTyp(),
					ocrque.getPath(), 0);

			if (567 == ocrque.getOcrTyp()) {// If page Split
				LOGGER.info("Page Split Start for document:" + ocrque.getDocId());
				doOCRForPageSplit(ocrque.getDocId(), ocrque.getDocRevId(), serverPath + "//" + ocrque.getPath(),
						instance, ocrque);
			} else if (565 == ocrque.getOcrTyp()) {// If page Split child
				LOGGER.info("Page Split child Start for document:" + ocrque.getDocId());
				doOCRForChildPageSplit(ocrque.getDocId(), ocrque.getDocRevId(), serverPath, instance, ocrque);
			} else if (566 == ocrque.getOcrTyp()) {// If page Split parent
				LOGGER.info("Page Split Parent Start for document:" + ocrque.getDocId());
				doOCRForChildPageSplit(ocrque.getDocId(), ocrque.getDocRevId(), serverPath, instance, ocrque);
			} else if (568 == ocrque.getOcrTyp()) {// If page Split restore
				LOGGER.info("Page Split Restore Start for document:" + ocrque.getDocId());
				doOCRForChildPageSplit(ocrque.getDocId(), ocrque.getDocRevId(), serverPath, instance, ocrque);
			}
			// Delete row from OCR_Q table
			LOGGER.info("Update OCRQLog fordocument:" + ocrque.getDocId());
			doUpadteOCRQLog(ocrque.getId(), ocrque.getDocId(), 1);
			// delete row from OCR_Q
			LOGGER.info("Delete  OCRQ for document:" + ocrque.getDocId());
			doDeleteOCRQ(ocrque.getId());

		}
		LOGGER.info("End SplitOCR -> method: doOCR()");
		return true;
	}
    void doIstInOCRQLog(long id, long docId, long docRevId, long ocr_q_typ, String page_path, int ocr_status) {
		query.insertInToOCRQLog(id, docId, docRevId, ocr_q_typ, page_path, ocr_status);
	}

    void doOCRForPageSplit(long docId, long docRevId, String path, net.sourceforge.tess4j.Tesseract1 instance,
			Ocrque ocrque) {
		PowerShellResponse response = null;
		Process p = null;
		PrintWriter stdin = null;

		LOGGER.info("path::" + path);
		System.getProperty("file.separator");
		// Create folder for ocr in file server
		path = path.replace("\\", System.getProperty("file.separator"));
		path = path.replace("//", System.getProperty("file.separator"));
		String ocrFldrInFileServer = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")))
				+ System.getProperty("file.separator") + docId + ocrCode;

		File ocr_folder = new File(ocrFldrInFileServer);
		// if not created ,create it
		if (!ocr_folder.exists()) {
			ocr_folder.mkdir();
		}
		// Create temp folder
		String tempFlder = createTempLoc(tempFdrPath);
		// create ocr folder in side temp folder to keep ocr files
		String strOCRFolder = tempFlder + System.getProperty("file.separator") + "ocr";

		File createFolderOcr = new File(strOCRFolder);
		if (!createFolderOcr.exists()) {
			// create folder
			createFolderOcr.mkdir();
		}
		// get file type
		String fileTyp = query.getFileType(docId);
		// Get all split log detials
		List<SplitLog> splitLogList = query.getSplitLogList(docRevId);
		// Create Tikka location
		File tikkaLocation = new File(tikaLocation);
		if (!tikkaLocation.exists()) {
			// Create Folder
			tikkaLocation.mkdirs();
		}
		// main file for solr tikka
		File org_file = new File(tempFlder + System.getProperty("file.separator") + docId + ".txt");
		OutputStream outputMain = null;
		try {
			outputMain = new FileOutputStream(org_file, false);
		} catch (FileNotFoundException e1) {
			LOGGER.error("Error SplitOCR -> method:doOCRForPageSplit() ", e1);
		}
		for (SplitLog splitLog : splitLogList) {
			PrintWriter printWriter = null;
			File srcFile = new File(path + System.getProperty("file.separator") + splitLog.getPageId());
			File desFile = new File(
					tempFlder + System.getProperty("file.separator") + splitLog.getPageId() + "." + fileTyp);

			if (IsEnctypedEnaUnit(splitLog.getPath())) {
				decryptionUtil.decryptFile(srcFile, desFile, decryptionKey);
			} else {
				copyFilesToTempFolder(srcFile, desFile);
			}
			String fileName = desFile.getAbsolutePath();

			String outFldr = fileName.substring(0, fileName.lastIndexOf(System.getProperty("file.separator")));
			String ocrData = null;

			LOGGER.info("Split File -->outFldr:" + outFldr);

			//String[] command = { "cmd " };
			String searchableFile = fileName.substring(0, fileName.lastIndexOf(".")) + "_ocr.pdf";
			ocrData = ocrPDF(fileName);

			LOGGER.info("Split File -->ocrData.length():" + ocrData.length());
			try {
				try {
					if (ocrData.length() < 4) {
						try {

							String mainCommand = "ocrmypdf --tesseract-pagesegmode 1  --optimize 0 --output-type pdf -r --rotate-pages-threshold 50 --fast-web-view 0 --force-ocr -l eng "
									+ fileName + " " + searchableFile;
							LOGGER.info(mainCommand);
							Process process = Runtime.getRuntime().exec(mainCommand);
							LOGGER.info("after process:" + process);
							process.waitFor();
							int exitValue = process.exitValue();
							 if (exitValue == 0) {
								 LOGGER.info("Command executed successfully.");
					            } else {
					            	LOGGER.info("Error executing command. Exit code: " + exitValue);
					            }
							 File file =new File(searchableFile);
							 if (file.exists()) {
									// Create Folder
								 LOGGER.info("File Present in the given folder---"+file);
							}

						} catch (Exception e) {
							e.printStackTrace();
							LOGGER.error("Split File -->IOException:" + e.getMessage());
						}
						// stdin = new PrintWriter(p.getOutputStream());

						ocrData = ocrPDF(searchableFile);
						FileUtils.forceDelete(new File(searchableFile));
					}

				} catch (Exception e1) {

					e1.printStackTrace();
				}

				desFile.delete();
				//
				File ocrTxtFile = new File(
						strOCRFolder + System.getProperty("file.separator") + splitLog.getPageId() + ".txt");

				LOGGER.info("Split File -->ocrTxtFile:" + ocrTxtFile);

				printWriter = new PrintWriter(new FileOutputStream(ocrTxtFile, false));

				printWriter.write( StringEscapeUtils.escapeXml10(ocrData));

				printWriter.close();

				// put encryptFile text file in server
				if (IsEnctypedEnaUnit(splitLog.getPath())) {
					decryptionUtil.encryptFile(ocrTxtFile,
							new File(ocrFldrInFileServer + System.getProperty("file.separator") + splitLog.getPageId()),
							decryptionKey);
				} else {
					copyFilesToTempFolder(ocrTxtFile, new File(
							ocrFldrInFileServer + System.getProperty("file.separator") + splitLog.getPageId()));
				}

				BufferedInputStream input = new BufferedInputStream(new FileInputStream(ocrTxtFile));
				try {

					LOGGER.info("Split File -->input:" + input);

					LOGGER.info("Split File -->outputMain:" + outputMain);

					IOUtils.copy(input, outputMain);
				} catch (IOException e) {
					LOGGER.error("Error SplitOCR -> method:doOCRForPageSplit() " + e);
				}
				IOUtils.closeQuietly(input);
				ocrTxtFile.delete();

			} catch (FileNotFoundException e) {
				LOGGER.error("Error SplitOCR -> method:doOCRForPageSplit() ", e);
				e.printStackTrace();
			}

		}
		IOUtils.closeQuietly(outputMain);
		// move file to tikka location

		LOGGER.info("OCR File Path::" + tikkaLocation + System.getProperty("file.separator") + docId + ".txt");
		org_file.renameTo(new File(tikkaLocation + System.getProperty("file.separator") + docId + ".txt"));
		// delete temp folder
		try {
			FileUtils.deleteDirectory(new File(tempFlder));
		} catch (IOException e) {
			LOGGER.error("Error  SplitOCR -> method:doOCRForPageSplit() " + e);
		} // End for look

	}
	
    String createTempLoc(String TEMP_LOCATION) {
		String filePath = null;
		long currentTime = System.currentTimeMillis();
		// File createTempLocation = new File(TEMP_LOCATION + "\\" + currentTime);
		File createTempLocation = new File(TEMP_LOCATION + System.getProperty("file.separator") + currentTime);
		boolean isDirectoryCreated = createTempLocation.mkdir();
		if (isDirectoryCreated) {
			filePath = createTempLocation.getAbsolutePath();
		}
		return filePath;
	}

    private void copyFilesToTempFolder(File srcFile, File desFile) {
		try {
			InputStream inputStreamSourceFile = null;
			OutputStream outputStreamDestinationFile = null;
			inputStreamSourceFile = new FileInputStream(srcFile);
			outputStreamDestinationFile = new FileOutputStream(desFile);
			writeFileToDisk(inputStreamSourceFile, outputStreamDestinationFile);
		} catch (FileNotFoundException e) {
			LOGGER.error("Error SplitOCR -> method:copyFilesToTempFolder() " + e);
		} catch (IOException e) {
			LOGGER.error("Error SplitOCR -> method:copyFilesToTempFolder() " + e);
		}
	}

    private static void writeFileToDisk(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int numRead = 0;
		// read and write operation
		while ((numRead = is.read(buf)) >= 0) {
			os.write(buf, 0, numRead);
		}
		os.close();
		is.close();
	}

    protected Boolean IsEnctypedEnaUnit(String subPath) {
    	if(businessUnitList==null) {
    		getBusinessUnitList();
    	}
    	Boolean encyptionAlw = businessUnitList.contains(Integer.valueOf(subPath.substring(4, 7)).toString());
		return encyptionAlw;
	}

	void doOCRForChildPageSplit(long docId, long docRevId, String path, net.sourceforge.tess4j.Tesseract1 instance,
			Ocrque ocrque) {
		// Create folder for ocr in file server
		path = path.replace('\\', '/');
		// Create temp folder
		String tempFlder = createTempLoc(tempFdrPath);
		// create ocr folder in side temp folder to keep ocr files
		String strOCRFolder = tempFlder + System.getProperty("file.separator") + "ocr";

		File createFolderOcr = new File(strOCRFolder);
		if (!createFolderOcr.exists()) {
			// create folder
			createFolderOcr.mkdir();
		}
		// Get all split log detials
		List<SplitLog> splitLogList = query.getSplitLogList(docRevId);
		// Create Tikka location
		File tikkaLocation = new File(tikaLocation);
		if (!tikkaLocation.exists()) {
			// Create Folder
			tikkaLocation.mkdirs();
		}
		// main file for solr tikka
		File org_file = new File(tempFlder + System.getProperty("file.separator") + docId + ".txt");
		LOGGER.info("main file for solr tikka ->" + tempFlder + System.getProperty("file.separator") + docId + ".txt");

		OutputStream outputMain = null;
		try {
			outputMain = new FileOutputStream(org_file, false);
		} catch (FileNotFoundException e1) {
			LOGGER.error("Error in doOCR:" + e1);
		}
		for (SplitLog splitLog : splitLogList) {
			String subPath = splitLog.getPath().replace('\\', '/');
			subPath = subPath.substring(0, subPath.lastIndexOf("_")) + ocrCode;
			File srcFile = new File(path + System.getProperty("file.separator") + subPath
					+ System.getProperty("file.separator") + splitLog.getPageId());
			File desFile = new File(tempFlder + System.getProperty("file.separator") + splitLog.getPageId() + ".txt");

			if (IsEnctypedEnaUnit(subPath)) {
				decryptionUtil.decryptFile(srcFile, desFile, decryptionKey);
			} else {
				copyFilesToTempFolder(srcFile, desFile);
			}
			try {
				BufferedInputStream input = new BufferedInputStream(new FileInputStream(desFile));
				try {
					IOUtils.copy(input, outputMain);
				} catch (IOException e) {
					LOGGER.error("Error SplitOCR -> method:doOCR() ", e);
					e.printStackTrace();
				}
				IOUtils.closeQuietly(input);
				desFile.delete();

			} catch (FileNotFoundException e) {
				LOGGER.error("Error in doOCR:" + e);
			}

		}
		IOUtils.closeQuietly(outputMain);
		// move file to tikka location
		org_file.renameTo(new File(tikkaLocation + System.getProperty("file.separator") + docId + ".txt"));
		// delete temp folder
		try {
			FileUtils.deleteDirectory(new File(tempFlder));
		} catch (IOException e) {
			LOGGER.error("Error in doOCR:" + e);
		} // End for look

	}
	
	void doUpadteOCRQLog(long id, long docId, int ocr_status) {
		query.updInToOCRQLog(id, docId, ocr_status);
	}

	
	void doDeleteOCRQ(long id) {
		query.deleteOCRQ(id);
	}

}
