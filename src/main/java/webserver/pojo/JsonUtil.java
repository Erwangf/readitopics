package webserver.pojo;

import com.google.gson.Gson;

import spark.ResponseTransformer;

/**
 * From the excellent http://www.mscharhag.com/java/building-rest-api-with-spark
 * 
 * @author cgravier
 *
 */
public class JsonUtil {

	public static String toJson(Object object) {
		return new Gson().toJson(object);
	}

	public static ResponseTransformer json() {
		return JsonUtil::toJson;
	}
}
