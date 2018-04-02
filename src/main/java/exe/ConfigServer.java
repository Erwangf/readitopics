package exe;

import com.google.gson.Gson;
import core.Constantes;
import io.config.ConfigManager;
import io.config.DatasetManager;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;
import spark.Request;
import spark.ResponseTransformer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static io.UnzipFile.unzip;
import static spark.Spark.*;

public class ConfigServer {
    public static void main(String[] args) {
        port(8000);


        staticFiles.location("/webapp_config_manager");

        /* ======= Config ======= */
        get("/configNames", (request, response) -> ConfigManager.getAllConfigNames(), new JsonTransformer());

        get("/configs", ((request, response) -> ConfigManager.getAllConfigs()), new JsonTransformer());

        get("/config/:name", ((request, response) -> ConfigManager.getConfigByName(request.params("name"))), new JsonTransformer());

        get("/config/delete/:name", ((request, response) -> {
            ConfigManager.deleteConfig(request.params("name"));
            return "OK";
        }), new JsonTransformer());

        post("/createConfig", (request, response) -> {

            System.out.println(request.body());
            List<NameValuePair> a = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            JSONObject json = new JSONObject(a.get(0).getValue());
            HashMap<String, String> map = new LinkedHashMap<>();
            for (Object o : json.keySet()) {
                map.put(o.toString(), json.get(o.toString()).toString());
            }
            ConfigManager.createConfig(map);
            return "OK";
        });

        post("/editConfig", (request, response) -> {


            List<NameValuePair> a = URLEncodedUtils.parse(request.body(), Charset.defaultCharset());
            JSONObject json = new JSONObject(a.get(0).getValue());
            HashMap<String, String> map = new LinkedHashMap<>();
            for (Object o : json.keySet()) {
                map.put(o.toString(), json.get(o.toString()).toString());
            }
            ConfigManager.editConfig(map);
            return "OK";
        });

        File uploadDir = new File("tmp");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist



        /* ======= Dataset ======= */

        get("/datasets", ((request, response) -> DatasetManager.getAllDatasets()), new JsonTransformer());

        post("/createDataset",((request, response) -> {
            System.out.println(request);
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            String filename = getFileName(request.raw().getPart("uploaded_file"));
            Path tempFile = Files.createTempFile(uploadDir.toPath(), filename, ".zip");

            try (InputStream input = request.raw().getPart("uploaded_file").getInputStream()) { // getPart needs to use same "name" as input field in form

                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);

                unzip(tempFile.toFile().getAbsolutePath(),Constantes.DATA_DIR+Constantes.separateur);

                logInfo(request,tempFile);
            } catch (Exception e){
                e.printStackTrace();
            }


            response.redirect("/datasetList.html");
            return "OK";
        }),new JsonTransformer());

        get("/dataset/delete/:name",((request, response) -> {
            String name = request.params("name");
            DatasetManager.deleteDataset(name);
            return "OK";
        }),new JsonTransformer());



    }


    private static class JsonTransformer implements ResponseTransformer {

        private Gson gson = new Gson();

        @Override
        public String render(Object model) {
            return gson.toJson(model);
        }

    }

    // methods used for logging
    private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
        System.out.println("Uploaded file '" + getFileName(req.raw().getPart("uploaded_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'");
    }

    private static String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }


}


