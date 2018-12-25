package com.jhh.dc.loan.common.util.http;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Date;


/**
 * <b>JSON资料格式处理类</b>
 *
 * @author xiongchun
 * @date 2009-10-06
 * @since 1.0
 */
public class AOSJson {

    private static Logger log = LoggerFactory.getLogger(AOSJson.class);

    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        //注册日期时间类型反序列化时的适配器(针对反序列化到JavaBean的情况，Map类型不需要处理)
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return AOSUtils.stringToDate(json.getAsString());
            }
        });
        gson = builder.create();
    }

    /**
     * 将Java对象进行JSON序列化
     * <p>
     * 支持自定义日期时间类型格式
     * <p>
     *
     * @param pObject
     * @param pDateFormat 日期时间类型格式字符串
     * @return
     */
    public static final String toJson(Object pObject, String pDateFormat) {
        if (pObject == null) {
            log.debug("跟踪到JSON序列化对象为null。");
            return null;
        }
        String jsonString = "";
        if (AOSUtils.isEmpty(pDateFormat)) {
            pDateFormat = AOSCons.DATATIME;
        }
        GsonBuilder builder = new GsonBuilder();
        //可将null字段输出
        //GsonBuilder builder = new GsonBuilder().serializeNulls();
        //builder.setPrettyPrinting(); 是否格式化输出JSON
        builder.setDateFormat(pDateFormat);
        Gson gson = builder.create();
        jsonString = gson.toJson(pObject);
        return jsonString;
    }

    /**
     * 将Java对象进行JSON序列化
     * <p>
     * 缺省的日期时间类型为：yyyy-MM-dd HH:mm:ss
     * <p>
     *
     * @param pObject
     * @return
     */
    public static final String toJson(Object pObject) {
        String jsonString = toJson(pObject, AOSCons.DATATIME);
        return jsonString;
    }

    /**
     * 将Json字符串转换为Java对象
     *
     * @param json
     * @param type 如果Java对象是一个普通类则直接JsonUtils.fromJson(json,
     *             HashDto.class);即可。如果是一个泛型类(如一个dto集合类)则需要
     *             使用如下方式传参：JsonUtils.fromJson(json, new
     *             TypeToken<List<HashDto>>() {}.getType());
     * @return
     */
    public static final <T> T fromJson(String json, Type type) {
        T list = gson.fromJson(json, type);
        return (T) list;
    }

}