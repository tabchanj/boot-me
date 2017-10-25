package hello.web.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tab chan 10/25/2017
 */
public class SolrResutlsToInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrResutlsToInstance.class);
    private Map<String, Object> keyPair = new HashMap<>();

    private SolrResutlsToInstance() {
    }

    private static SolrResutlsToInstance this_me = null;

    public static Object instanceFromDocument(SolrDocument solrDocument, Class<T> targetCls) {
        this_me = new SolrResutlsToInstance();
        Object target = BeanUtils.instantiate(targetCls);
        this_me.fuelKeyPair(solrDocument);
        return this_me.returnInstance(this_me.keyPair, targetCls);
    }

    public Object returnInstance(Map<String, Object> keyPair, Class<T> targetCls) {
        Object target = BeanUtils.instantiate(targetCls);
        Field[] fields = target.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = keyPair.get(field.getName());
            if (value != null) {
                field.setAccessible(true);
                try {
                    LOGGER.info("字段值:"+value+",类型："+value.getClass()+","+(value instanceof Long));
                    if (value instanceof Long) {
                        LOGGER.info("---");
                        field.set(target, Integer.parseInt(value.toString()));
                    }else if (field.getName().contains("time")) {
                        LOGGER.info("---");
                        field.set(target, formatDateWithTimeZone((getTzone(null)),value.toString()));
                    }else if("keylist".equals(field.getName())){
                        LOGGER.info("+++");
                        field.set(target,value.toString());
                    }else {
                        LOGGER.info("===");
                        field.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("为字段设置值发生错误：", e);
                }

            }

        }
        return target;
    }

    private void fuelKeyPair(SolrDocument solrDocument) {
        Collection<String> fieldNames = solrDocument.getFieldNames();
        keyPair.clear();
        for (String fieldName : fieldNames) {
            Object fieldValue = solrDocument.getFieldValue(fieldName);
            keyPair.put(fieldName, fieldValue);
        }
    }

    public static List<T> instanceFromSolrDocuments(List<SolrDocument> solrDocuments, Class<T> targetCls) {
        List results = null;
        if (CollectionUtils.isNotEmpty(solrDocuments)) {
            results = new ArrayList<>();
            for (SolrDocument solrDocument : solrDocuments) {
                Object target = instanceFromDocument(solrDocument, targetCls);
                results.add( target);
            }
        }
        return results;
    }

    public TimeZone getTzone(String tid){
        String tzid = TimeZone.getDefault().getID();
        if(!StringUtils.isEmpty(tid)){
            tzid = tid;
        }
        TimeZone tz = TimeZone.getTimeZone(tzid);
        return tz;
    }

    public Date formatDateWithTimeZone(TimeZone tz,String dateStrWithTimezone){
        Date parsed = null;
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(tz);
//        Date date = new Date(dateStrWithTimezone);
        try {
            parsed  = sdf.parse(dateStrWithTimezone);
        } catch (ParseException e) {
            LOGGER.error("解析时间错误，带时区的",e);
        }
        return parsed;
    }
}