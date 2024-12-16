package com.mind.ocr.splitOCR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;




@Service
public class DecryptionUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DecryptionUtil.class);
	/** The iv. */
	private static byte[] iv= {1, 12, 17, 44, 15, 96, 87, 88};
	private static AlgorithmParameterSpec paramSpec=null;
	static{
		paramSpec = new IvParameterSpec(iv);
	}

	/**
	 * Encrypt file and write to destination location.
	 * 
	 * @param file the file
	 */
	public void decryptFile(File sourceFile, File destinationFile, String key) {

		InputStream inputStreamSourceFile = null;
		OutputStream outputStreamDestinationFile = null;
		// create CipherOutputStream to encrypt the data using encryptCipher
		try {
			inputStreamSourceFile = new FileInputStream(sourceFile);
			outputStreamDestinationFile = new FileOutputStream(destinationFile);
			// decode the key fetched from the database which is further stored
			// in encrypted form
			byte[] encodedKey = Base64.decodeBase64(key);

			// get the key from the encoded string
			SecretKey secretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES");

			Cipher decryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			// Initialize the DES encryption
			decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			inputStreamSourceFile = new CipherInputStream(inputStreamSourceFile, decryptCipher);

			writeFileToDisk(inputStreamSourceFile, outputStreamDestinationFile);

		} catch (Exception exception) {
			exception.printStackTrace();
			LOGGER.error("Error occured while decryption.", exception);			
		}
		finally {
			try {
				if (inputStreamSourceFile != null) {
					inputStreamSourceFile.close();
				}
				if (outputStreamDestinationFile != null) {
					outputStreamDestinationFile.close();
				}
			}catch (Exception exception) {
				exception.printStackTrace();
				LOGGER.error("Error occured while release resources at decryption time.", exception);		
			}
		}
	}
	public void encryptFile(File sourceFile, File destinationFile, String encryptionKey) {
		
		InputStream inputStreamSourceFile = null;
		OutputStream outputStreamDestinationFile = null;
		// create CipherOutputStream to encrypt the data using encryptCipher
		try {
			inputStreamSourceFile = new FileInputStream(sourceFile);
			outputStreamDestinationFile = new FileOutputStream(destinationFile);
			byte[] encodedKey = Base64.decodeBase64(encryptionKey);
			// get the key from the encoded string
			SecretKey secretKey = new SecretKeySpec(encodedKey, 0,encodedKey.length, "DES");
			Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			// Initialize the DES encryption
			encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			outputStreamDestinationFile = new CipherOutputStream(outputStreamDestinationFile, encryptCipher);
			// write content to file
			writeFileToDisk(inputStreamSourceFile, outputStreamDestinationFile);
			
		} catch (Exception exception) {
			LOGGER.error("Error occured while encryption.", exception);
		}finally {
			try {
				if (inputStreamSourceFile != null) {
					inputStreamSourceFile.close();
				}
				if (outputStreamDestinationFile != null) {
					outputStreamDestinationFile.close();
				}
			}catch (Exception exception) {
				LOGGER.error("Error occured while release resources at encryption time.", exception);		
			}
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
	}}
