package com.mind.ocr.splitOCR;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class Query {
	private static final Logger LOGGER = LoggerFactory.getLogger(Query.class);
	
	/** The Constant GET_LIST_FOR_OCR. */
	final static String GET_LIST_FOR_OCR="SELECT ID,DOC_ID,DOC_REV_ID,OCR_REQ_TYPE,PAGE_PATH,BU_ID from dbo.PAGEOCRQ";
	
	
	/** The Constant GET_LIST_FOR_SPLIT_LOG. */
	final static String GET_LIST_FOR_SPLIT_LOG="SELECT PAGE_ID,PAGE_PATH FROM dbo.SPLTLOG WHERE DOC_REV_ID=:docRevId AND ACTIVE=1";
	
	
	/** The Constant INSERT_OCR_Q_LOG. */
	final static String INSERT_OCR_Q_LOG="INSERT INTO dbo.PAGEOCRQ_LOG(ID,DOC_ID,DOC_REV_ID,OCR_REQ_TYPE,PAGE_PATH,OCR_STATUS,INSD) values (:id, :docId, :docRevId, :ocr_q_typ, :page_path, :ocr_status, now()::timestamp)";
	
	
	/** The Constant UPDATE_OCR_Q_LOG. */
	final static String UPDATE_OCR_Q_LOG="UPDATE dbo.PAGEOCRQ_LOG SET OCR_STATUS=1, UPD_AT=now()::timestamp,UPDD=now()::timestamp WHERE ID=:id AND DOC_ID=:docId ";
	
	
	/** The Constant DELETE_FROM_OCR_Q. */
	final static String DELETE_FROM_OCR_Q="DELETE FROM dbo.PAGEOCRQ WHERE ID=:id  ";
	
	/** The Constant GET_FILE_TYPE. */
	final static String GET_FILE_TYPE="SELECT FILE_TYPE FROM dbo.DMS_DOCS_REPOSITORY WHERE DOCUMENT_ID=:docId ";
	
	
	/** The Constant GET_BASE_PATH. */
	final static String GET_BASE_PATH = "select Config_Value from dbo.ECRM_CA_SYS_CONFIG  where Config_Key = ?";
	
	/** The Constant GET_FILE_SERVER_PATH. */
	public static final String GET_FILE_SERVER_PATH = "SELECT SOURCE_IMAGES_PATH FROM dbo.ECRM_CA_FILES_SOURCE WHERE BUSINESS_UNIT_ID=:buId";
	
	public static final String	GET_ENC_ENABLED_BU_LIST = "SELECT BUSINESS_UNIT_ID FROM dbo.ECRM_CA_FILES_SOURCE WHERE ENABLE_ENCRYPTION = 1";
	
	  // Default paths
    private static final String DEFAULT_ERROR_PATH = "1174";
    private static final String DEFAULT_EMPTY_PATH = "1804";
	
	 
	 
	 @Autowired
	 private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	/**
	 * Gets the file server path.
	 *
	 * @param buId the bu id
	 * @return the file server path
	 * @throws Exception the exception
	 */
	 public Map<String, String> getFileServerPath(long buId) {
	        LOGGER.info("Start Query -> method:getFileServerPath(), buId: {}", buId);
	        Map<String, String> methodResponse = new HashMap<>();
	        methodResponse.put("isError", "1"); // Default to error

	        Map<String, Object> params = new HashMap<>();
		    params.put("buId", buId);
	        try {
	            // Query for a single result
	            String path = namedParameterJdbcTemplate.queryForObject(
	                GET_FILE_SERVER_PATH, 
	                params, 
	                String.class
	            );

	            if (path != null && !path.isBlank()) {
	                methodResponse.put("isError", "0");
	                methodResponse.put("path", path);
	            } else {
	                methodResponse.put("path", DEFAULT_EMPTY_PATH);
	            }

	        } catch (Exception e) {
	            LOGGER.error("Error while fetching file server path for buId: {}", buId, e);
	            methodResponse.put("path", DEFAULT_ERROR_PATH);
	        }

	        LOGGER.info("End Query -> method:getFileServerPath()");
	        return methodResponse;
	    }
	
	
	
	
	
	/**
	 * Gets the file type.
	 *
	 * @param docId the doc id
	 * @return the file type
	 */
	public String getFileType(long docId) {  
		LOGGER.info("Start Query -> method:getFileType(), docId: {}", docId);

	    Map<String, Object> params = new HashMap<>();
	    params.put("docId", docId);

	    try {
	        return namedParameterJdbcTemplate.queryForObject(
	            GET_FILE_TYPE,
	            params,
	            String.class
	        );
	    } catch (EmptyResultDataAccessException ex) {
	        LOGGER.warn("No result found for docId: {}", docId, ex);
	        return "";
	    } catch (DataAccessException ex) {
	        LOGGER.error("Database error in method:getFileType()", ex);
	        throw ex;
	    } finally {
	        LOGGER.info("End Query -> method:getFileType()");
	    }
    }
	
	/**
	 * Delete ocrq.
	 *
	 * @param id the id
	 */
	public  void deleteOCRQ(long id) {
		LOGGER.info("Start Query -> method:deleteOCRQ(), id: {}", id);
    Map<String, Object> params = new HashMap<>();
    params.put("id", id);

    try {
        int rowsAffected = namedParameterJdbcTemplate.update(DELETE_FROM_OCR_Q, params);
        LOGGER.info("Query executed successfully -> method:deleteOCRQ(), rows affected: {}", rowsAffected);
    } catch (DataAccessException e) {
        LOGGER.error("Database error in method:deleteOCRQ()", e);
        throw e;
    } finally {
        LOGGER.info("End Query -> method:deleteOCRQ()");
    }
    }
	
	
	
	/**
	 * Upd in to ocrq log.
	 *
	 * @param id the id
	 * @param docId the doc id
	 * @param ocr_status the ocr_status
	 */
	public  void updInToOCRQLog(long id,long docId,int ocr_status) {
	    LOGGER.info("Start Query -> method:updInToOCRQLog(), id: {}, docId: {}", id, docId);

	    Map<String, Object> params = new HashMap<>();
	    params.put("id", id);
	    params.put("docId", docId);

	    try {
	        int rowsAffected = namedParameterJdbcTemplate.update(UPDATE_OCR_Q_LOG, params);
	        LOGGER.info("Query executed successfully -> method:updInToOCRQLog(), rows affected: {}", rowsAffected);
	    } catch (DataAccessException e) {
	        LOGGER.error("Database error in method:updInToOCRQLog()", e);
	        throw e;
	    } finally {
	        LOGGER.info("End Query -> method:updInToOCRQLog()");
	    }
	    }
	
	
	
	
	
	/**
	 * Insert in to ocrq log.
	 *
	 * @param id the id
	 * @param docId the doc id
	 * @param docRevId the doc rev id
	 * @param ocr_q_typ the ocr_q_typ
	 * @param page_path the page_path
	 * @param ocr_status the ocr_status
	 */
	public  void insertInToOCRQLog(long id,long docId,long docRevId,long ocr_q_typ,String page_path,int ocr_status) {
		LOGGER.info("Start Query -> method:insertInToOCRQLog()");
    
    try {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", id);
        parameters.addValue("docId", docId);
        parameters.addValue("docRevId", docRevId);
        parameters.addValue("ocr_q_typ", ocr_q_typ);
        parameters.addValue("page_path", page_path);
        parameters.addValue("ocr_status", ocr_status);

        LOGGER.info("Query -> method:insertInToOCRQLog() -> " + INSERT_OCR_Q_LOG + " , id: " + id + " , docId: " + docId
                + " , docRevId: " + docRevId + " , ocr_q_typ: " + ocr_q_typ + " , page_path: " + page_path + " , ocr_status: "
                + ocr_status);

        namedParameterJdbcTemplate.update(INSERT_OCR_Q_LOG, parameters);

    } catch (Exception e) {
        e.printStackTrace();
    }
    
    LOGGER.info("End Query -> method:insertInToOCRQLog()");}
	
	
	/**
	 * Gets the list.
	 *
	 * @return the list
	 */
	public List<Ocrque> getList() { 
		LOGGER.info("Start Query -> method:getList()");
    
    List<Ocrque> ocrQueList = namedParameterJdbcTemplate.query(
        GET_LIST_FOR_OCR,
        new RowMapperResultSetExtractor<Ocrque>(new RowMapper<Ocrque>() { 
        	// Explicitly specify <Ocrque>
            @Override
            public Ocrque mapRow(ResultSet rs, int rowNum) throws SQLException {
                Ocrque infoRec = new Ocrque();
                infoRec.setId(rs.getLong("ID"));
                infoRec.setDocId(rs.getLong("DOC_ID"));
                infoRec.setDocRevId(rs.getLong("DOC_REV_ID"));
                infoRec.setOcrTyp(rs.getLong("OCR_REQ_TYPE"));
                infoRec.setPath(rs.getString("PAGE_PATH"));
                infoRec.setBuId(rs.getLong("BU_ID"));
                return infoRec;
            }
        })
    );

    LOGGER.info("End Query -> method:getList()");
    return ocrQueList;
    }
	
	/**
	 * Gets the split log list.
	 *
	 * @param docRevId the doc rev id
	 * @return the split log list
	 */
	public  List<SplitLog> getSplitLogList(long docRevId) {
		LOGGER.info("Start Query -> method:getSplitLogList()");

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("docRevId", docRevId);


    LOGGER.info("Query -> method:getSplitLogList() ->" + GET_LIST_FOR_SPLIT_LOG + " , docRevId:" + docRevId);

    List<SplitLog> splitLogList = namedParameterJdbcTemplate.query(
        GET_LIST_FOR_SPLIT_LOG,
        params,
        new RowMapper<SplitLog>() {
            @Override
            public SplitLog mapRow(ResultSet rs, int rowNum) throws SQLException {
                SplitLog splitRec = new SplitLog();
                splitRec.setPageId(rs.getString("PAGE_ID"));
                splitRec.setPath(rs.getString("PAGE_PATH"));
                return splitRec;
            }
        }
    );

    LOGGER.info("End Query -> method:getSplitLogList()");
    return splitLogList;
    }
	
	public List<String> getEncryptionEnabledBuList() {
		LOGGER.info("Start Query -> method:getEncryptionEnabledBuList()");


    LOGGER.info("Query -> method:getEncryptionEnabledBuList() -> " + GET_ENC_ENABLED_BU_LIST);

    // Query and map result directly to a list of strings
    List<String> encEnabledBuList = namedParameterJdbcTemplate.query(
        GET_ENC_ENABLED_BU_LIST,
        (rs, rowNum) -> String.valueOf(rs.getLong("BUSINESS_UNIT_ID"))
    );

    LOGGER.info("End Query -> method:getEncryptionEnabledBuList()");
    return encEnabledBuList;
    }



	
}
