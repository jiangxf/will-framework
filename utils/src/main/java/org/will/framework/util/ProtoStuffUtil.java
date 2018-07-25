package org.will.framework.util;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by qmx on 2017/8/19.
 */
public class ProtoStuffUtil {

    @SuppressWarnings("unchecked")
    public static <T> byte[] serializer(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserializer(byte[] data, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    private ProtoStuffUtil() {
    }

    private static Logger logger = LoggerFactory.getLogger(ProtoStuffUtil.class);

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    public static void main(String[] args) {

//        AQRequest aqRequest = new AQRequest("111", "{\n" +
//                "    \"uid\": \"aaa\",\n" +
//                "    \"lat\": 100,\n" +
//                "    \"lng\": 100,\n" +
//                "    \"mos\": \"WI\",\n" +
//                "    \"ip\": \"192.168.17.210\",\n" +
//                "    \"mv\": \"7.0\",\n" +
//                "    \"v\": \"smy/sxy\",\n" +
//                "    \"vtp\": \"5.9.0\",\n" +
//                "    \"area\": \"beijing\",\n" +
//                "    \"mid\": \"a1231241312432141\",\n" +
//                "    \"m\": \"huawei-EVA100\",\n" +
//                "    \"data\": [\n" +
//                "        {\n" +
//                "            \"t\": 123123123,\n" +
//                "            \"e\": \"10010111\",\n" +
//                "            \"d\":\"aaa\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"t\": 123123123,\n" +
//                "            \"e\": \"10010111\",\n" +
//                "            \"d\":\"aaa\"\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}", 3);
//
//        int i = 0;
//        long beginTime = 0;

//        logger.info("begin test json");
//        beginTime = System.currentTimeMillis();
//        while (i++ < 100000) {
//
////            logger.info("begin json");
//            String json = JsonHelper.toJson(aqRequest);
////            logger.info("end json");
//
////            logger.info("begin rejson");
//            try {
//                JsonHelper.toObject(json, AQRequest.class);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
////            logger.info("end rejson");
//        }
//        logger.info("end test json: " + (System.currentTimeMillis() - beginTime));


//        i = 0;
//        logger.info("begin test protostuff");
//        beginTime = System.currentTimeMillis();
//        while (i++ < 100000) {
//
////            logger.info("begin serializer");
//            byte[] bytes = serializer(aqRequest);
////            logger.info("end serializer");
//
//            String temp = new String(bytes);
////
////            logger.info("begin deserializer");
//            AQRequest newAqRequest = deserializer(temp.getBytes(Charset.defaultCharset()), AQRequest.class);
////            logger.info("end deserializer");
//        }
//        logger.info("end test protostuff: " + (System.currentTimeMillis() - beginTime));
    }
}
