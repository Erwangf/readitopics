package exe;

import static spark.Spark.get;
import static spark.Spark.staticFiles;
import static spark.Spark.port;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Spark;
import utils.DataUtils;
import webserver.pojo.DatasetResources;
import webserver.pojo.DatasetResourcesUtils;
import webserver.pojo.JsonUtil;
import webserver.responses.DatasetCount;
import webserver.responses.DatasetList;
import webserver.responses.DocumentList;
import webserver.responses.DocumentListAll;
import webserver.responses.DocumentListAllpzd;
import webserver.responses.DocumentListTopicPair;
import webserver.responses.LabelList;
import webserver.responses.LabelList4eval;
import webserver.responses.TopNearestNeigh;
import webserver.responses.TopTopics;
import webserver.responses.TopicCoherenceList;
import webserver.responses.TopicCount;
import webserver.responses.WordList;
import webserver.responses.TopicSize;

/**
 * 
 * @author cgravier
 *
 */
public class WebServerMain {

	// private String topicSerialized;
	// private String embeddingSerialized;
	private Map<String, DatasetResources> webResources = new HashMap<>();
	private String parent_file;

	private TopTopics toptopics;
	
	// private static final Logger log =
	// Logger.getLogger(WebServerMain.class.getName());
	private final static Logger logger = LoggerFactory.getLogger(WebServerMain.class);

	public WebServerMain(String datasetsDirectory) {
		port(1234);
		// 1 - for each subfolder
		String[] datasetsFolders = DatasetResourcesUtils.getSubFolders(datasetsDirectory);
		if (datasetsFolders == null) {
			logger.error("Cannot find any datasets root folder at " + datasetsDirectory);
		}
		parent_file = DatasetResourcesUtils.getParentFolders(datasetsDirectory);
		// 2 - build 4 string for each file as expected. If any doesn't exist,
		// rollback launch
		String sep = "/"; // reminder : java NIO will cope with any sep w.r.t.
		// hosting machine runnning the server.
		for (String folder : datasetsFolders) {
			String datasetName = new File(folder).getName();
			String labelFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "labels.json";
			String topdocFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "topdocs.json";
			String topdocTopicPairFile = datasetsDirectory + sep + folder + sep + "models" + sep + "topdocs_pairs.json";
			String topicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "model.json";
			String toptopicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "theta_toptopics.json";
			String descFile = datasetsDirectory + sep + folder + sep + "description" + sep + "des.txt";
			String corFile = datasetsDirectory + sep + folder + sep + "analytics" + sep + "cor_doc_based.json";
			String cohFile = datasetsDirectory + sep + folder + sep + "analytics" + sep + "coherence.json";
			validateResourceFile(folder, labelFile);
			validateResourceFile(folder, topdocFile);
			validateResourceFile(folder, topicsFile);
			validateResourceFile(folder, toptopicsFile);
			validateResourceFile(folder, descFile);
			// 3 - build a DatasetConfiguration and add it to the Map.
			DatasetResources dr = new DatasetResources(datasetName, labelFile, topdocFile, topicsFile, toptopicsFile,
					descFile, corFile, topdocTopicPairFile,cohFile);
			logger.info("Adding dataset " + datasetName + " to the pull of available datasets...");
			webResources.put(datasetName, dr);
		}
		init_lookup_toptopics();
	}

	public String getParent_file() {
		return parent_file;
	}

	public void setParent_file(String parent_file) {
		parent_file = parent_file;
	}

	private void validateResourceFile(String folder, String embFile) {
		if (!DatasetResourcesUtils.isValidFile(embFile)) {
			logger.error("The dataset " + folder + " has an invlid resource! (resource file \""
					+ embFile + "\" does not exist");
		}
	}

	public static void main(String[] args) throws IOException {
		
		if (args.length == 0)
			throw new IOException("Require at least 1 argument: configuration file");

		String datasetsLocation = args[0];
		// System.out.println(datasetsLocation);
		File f = new File(datasetsLocation);
		if (!f.exists()) {
			logger.error("Datasets folder " + datasetsLocation + " does not exists !");
			System.exit(-9);
		}

		WebServerMain server = new WebServerMain(datasetsLocation);
		server.initRoutes();		
	}

	private void initRoutes() throws IOException {
		// staticFiles.externalLocation("/Users/cgravier/Coding/topiclabeling/src/main/resources/webapp2");
		
		staticFiles.location("/webapp2/");
		// staticFiles.location("/webapp2/js/");
		// staticFiles.location("/webapp2/css/");
		// staticFiles.location("/webapp2/fonts/");

		get("/datasets/count", (req, res) -> {
			res.type("application/json; charset=utf-8");
			res.status(200);
			return new DatasetCount(webResources.keySet().size());
		}, JsonUtil.json());

		get("/datasets/list", (req, res) -> {
			res.type("application/json; charset=utf-8");
			res.status(200);
			return new DatasetList(webResources.keySet());
		}, JsonUtil.json());
		
		get("/users/list", (req, res) -> {
			try {
				res.status(200);
				byte[] encoded = Files.readAllBytes(Paths.get(parent_file+"/Grades/users.txt"));
				String content = new String(encoded, Charset.defaultCharset());
				return content;
			} catch (IOException e) {
				res.status(500);
				return "oups;";
			}
		});
		get("/datasets/:datasetid/hiddentopics", (req, res) -> {
			try {
				String datasetid = req.params(":datasetid");		
				byte[] encoded = Files.readAllBytes(Paths.get(parent_file+"/datasets/" + datasetid + "/graphviz/hidden_topics"));
				String content = new String(encoded, Charset.defaultCharset());
				res.status(200);
				return content;
			} catch (IOException e) {
				res.status(500);
				return "erreur;";
			}
		});
		
		get("/datasets/:datasetid/hiddentopics/save/:set", (req, res) -> {
			String datasetid = req.params(":datasetid");			
			String hide_set = req.params(":set");			
			try {
				DatasetResourcesUtils.writeHiddenTopics(parent_file + "/datasets/" + datasetid + "/graphviz/hidden_topics", hide_set); 
				return true;
			} catch (IOException e) {
				return false;
			}
		});
		
		get("/users/:user/story", (req, res) -> {;
			String user = req.params(":user");
			try {
				res.status(200);
				return DatasetResourcesUtils.getdataset_topic_foruser(user,parent_file);
			} catch (IOException e) {
				res.status(200);
				return "done";
			}
		});
		
		get("/users/:user/maxEval", (req, res) -> {;
			String user = req.params(":user");
			try {
				res.status(200);
				return DatasetResourcesUtils.getMaxEval(user,parent_file);
			} catch (IOException e) {
				res.status(200);
				return "done";
			}
		});
		
		get("/user/:user/:datasetid/:tid/:tab/:type", (req, res) -> {
			String user = req.params(":user");
			String datasetid = req.params(":datasetid");
			String type = req.params(":type");
			int topicid = Integer.parseInt(req.params(":tid"));
			String tab = req.params(":tab");
			try {
				DatasetResourcesUtils.WriteLabelRanking(parent_file + "/Grades/"+user+"-evaluation.txt",parent_file + "/Grades/"+user+"-evaluation_"+type+".txt",  datasetid,topicid, tab,type);
				return true;
			} catch (IOException e) {
				return false;
			}
		});
		
		get("/datasets/:datasetid/topics/count", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			//webResources.get(datasetid).getTopicsFile();
			res.status(200);
			return new TopicCount(webResources.get(datasetid).getTopicCount());
		}, JsonUtil.json());

		get("/datasets/:datasetid/topics/:tid/words/list/:count", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			int wordcount = Integer.parseInt(req.params(":count"));
			WordList result = null;
			try {
				res.status(200);
				result = new WordList(webResources.get(datasetid), topicid, wordcount);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch word list server-side");
			}
			return result;
		}, JsonUtil.json());

		get("/datasets/:datasetid/topics/:tid/size", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			double size = -1;
			int topicid = Integer.parseInt(req.params(":tid"));						
			try {
				res.status(200);
				TopicSize ts = new TopicSize(webResources.get(datasetid), topicid);
				size = ts.getsize();
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch topic list server-side");
			}
			return size;
		}, JsonUtil.json());
		
		get("/datasets/:datasetid/topics/:tid/labels/list/:count", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			int labelcount = Integer.parseInt(req.params(":count"));

			try {
				res.status(200);
				return new LabelList(webResources.get(datasetid), topicid, labelcount);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
		get("/datasets/:datasetid/topics/:tid/coherence", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			try {
				res.status(200);
				return new TopicCoherenceList(webResources.get(datasetid),datasetid, topicid);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
	
		get("/datasets/:datasetid/topics/:tid/labels/listforeval/:count", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			int labelcount = Integer.parseInt(req.params(":count"));

			try {
				res.status(200);
				LabelList4eval lb = new LabelList4eval(webResources.get(datasetid), topicid, labelcount);
				return lb;			
			} catch (Exception e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
		
		get("/datasets/:datasetid/topics/:tid/topdocs/:nb", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			int nbtd = Integer.parseInt(req.params(":nb"));
			try {
				res.status(200);
				return new DocumentListAll(webResources.get(datasetid), topicid, nbtd);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
		get("/datasets/:datasetid/topics/:tid/topdocspzd/:nb", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			int nbtd = Integer.parseInt(req.params(":nb"));
			try {
				res.status(200);
				return new DocumentListAllpzd(webResources.get(datasetid), topicid, nbtd);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
		get("/datasets/:datasetid/topics/:tid/topics/:t2id/topdocs/:nb", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid_i = Integer.parseInt(req.params(":tid"));
			int topicid_j = Integer.parseInt(req.params(":t2id"));
			int nbtd = Integer.parseInt(req.params(":nb"));
			try {
				res.status(200);
				return new DocumentListTopicPair(webResources.get(datasetid), topicid_i, topicid_j, nbtd);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());		
		
		get("/datasets/:datasetid/topics/:tid/words/:wid/topdocs/list", (req, res) -> {
			res.type("application/json; charset=utf-8");
			String datasetid = req.params(":datasetid");
			int topicid = Integer.parseInt(req.params(":tid"));
			String wid = req.params(":wid");
			try {
				res.status(200);
				return new DocumentList(parent_file,webResources.get(datasetid), topicid, wid);
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		}, JsonUtil.json());
		
		get("/Labeler/:labler", (req, res) -> {
			String lbr = req.params(":labler");
			try {
				res.status(200);
				return DatasetResourcesUtils.getLabelDescription(parent_file, lbr);
			} catch (IOException e) {
				res.status(500);
				return "label description not found";
			}

		});
		
		get("/datasets/:datasetid/des", (req, res) -> {
			String dats = req.params(":datasetid");
			try {
				String descinfos = webResources.get(dats).getDescFile();
				byte[] encoded = Files.readAllBytes(Paths.get(descinfos));
				String content = new String(encoded, Charset.defaultCharset());
				res.status(200);
				return content;
			} catch (IOException e) {
				res.status(500);
				return "";
			}
		});
		
		/*get("/datasets/:datasetid/tsne", (req, res) -> {
			res.type("application/json");
			String datasetid = req.params(":datasetid");

			try {
				res.status(200);
				String tsneinfos = webResources.get(datasetid).getTsneFile();
				logger.info(tsneinfos);
				byte[] encoded = Files.readAllBytes(Paths.get(tsneinfos));
				//System.out.println(datasetid + " -> " + encoded[0]);
				String content = new String(encoded, Charset.defaultCharset());
				// logger.info(content);
				return content;
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch label list server-side");
			}
			return null;
		});		*/
		
		get("/datasets/:datasetid/init_toptopics", (req, res) -> {
			String datasetid = req.params(":datasetid");
			try {
				res.status(200);
				if (toptopics == null)
					toptopics = new TopTopics();
				toptopics.add_lookup(datasetid, webResources.get(datasetid));
				return "ok";
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch top topics list server-side");
			}
			return "";
			//return null;
		});		
		
		get("/datasets/:datasetid/toptopics/:iddoc", (req, res) -> {
			res.type("application/json");
			String datasetid = req.params(":datasetid");
			String iddoc = req.params(":iddoc");
			try {
				res.status(200);
				return new TopTopics(webResources.get(datasetid), iddoc).get_toptopics();
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch top topics list server-side");
			}
			//return null;
			return "";
		}, JsonUtil.json());
		
		get("/datasets/:datasetid/fasttoptopics/:iddoc", (req, res) -> {
			res.type("application/json");
			String datasetid = req.params(":datasetid");
			String iddoc = req.params(":iddoc");			
			if (toptopics != null)
			{
				res.status(200);
				return toptopics.get_toptopics(datasetid, iddoc);
			}
			else
			{
				res.status(500);
				res.body("Cannot fetch top topics list server-side");
			}		
			return null;
		}, JsonUtil.json());

		get("/datasets/:datasetid/cor/:idtop", (req, res) -> {
			res.type("application/json");
			String datasetid = req.params(":datasetid");
			String idtop = req.params(":idtop");
			try {
				res.status(200);
				return new TopNearestNeigh(webResources.get(datasetid), Integer.parseInt(idtop));
			} catch (IOException e) {
				res.status(500);
				res.body("Cannot fetch cor list server-side");
			}
			return null;
		}, JsonUtil.json());
		// TODO coherence and embeddings

		//
		// get("/datasets/:datasetid/topics/:tid/coherence/:measure", (req, res)
		// -> {
		//
		// }, JsonUtil.json());

		// get("/words/:tid/:count", (request, response) -> {
		//
		// // Reader reader = new InputStreamReader(new
		// // FileInputStream(topicSerialized));
		// Reader reader = new InputStreamReader(new
		// FileInputStream(topicSerialized));
		// int topicid = Integer.parseInt(request.params(":tid"));
		// int count = Integer.parseInt(request.params(":count"));
		//
		// JsonArray subWords = new JsonArray();
		// try {
		// JsonObject jsonObject = Json.parse(reader).asObject();
		// JsonArray topics = jsonObject.get("topics").asArray();
		//
		// JsonObject topic = topics.get(topicid).asObject();
		// JsonArray words = topic.get("tokens").asArray();
		//
		// int nb = Math.min(words.size(), count);
		// for (int i = 0; i < nb; i++) {
		// subWords.add(words.get(i));
		// }
		//
		// System.out.println(subWords);
		// } catch (Exception e) {
		// System.out.println(e);
		// logger.info(e.getMessage());
		// e.printStackTrace();
		// }
		//
		// // set response header to json
		//
		// reader.close();
		//
		// return "{ \"words\" : " + subWords + "}";
		//
		// });

		// // get embeddings for a word
		// get("/embeddings/:word", (request, response) -> {
		//
		// // Reader reader = new InputStreamReader(new
		// // FileInputStream(embeddingSerialized));
		// String word = request.params(":word");
		// ArrayList<Double> emb = embeddings.get(word);
		//
		// if (emb == null) {
		// System.out.println("Cannot find embedding for " + word);
		// return "null";
		// } else {
		// return embeddings.get(word);
		// }
		//
		// });

		// // get graphic for a topic
		// get("/graphics/:type/:topicid/:word/:period", (request, response) ->
		// {
		//
		// String type = request.params(":type");
		// int topicid = Integer.parseInt(request.params(":topicid"));
		// int word = Integer.parseInt(request.params(":word"));
		// int period = Integer.parseInt(request.params(":period"));
		//
		// System.out.println(type);
		// System.out.println(topicid);
		// System.out.println(word);
		// System.out.println(period);
		//
		// if (type.equals("topiccoherence")) {
		// return "topic coherence";
		// } else if (type.equals("embedding")) {
		// return "embedding";
		// } else if (type.equals("coherenceevolution")) {
		// return "coherence evolution";
		// } else if (type.equals("pulse")) {
		// return "pulse";
		// } else if (type.equals("trajectory")) {
		// return "trajectory";
		// } else {
		// System.out.println("Cannot find " + type + " as graphic.");
		// return "null";
		// }
		//
		// });
		// get("/topic/:id", (request, response) -> {
		// int topicid =Integer.parseInt(request.params(":id"));
		// return ""; });
		//
		// response.header(
		// get("/topics/count/", (req, res) -> {
		// // return m.getNumTopics();
		// return "hey";
		// });
		//
		// get("/topics/:ntop", (request, response) -> {
		// int numtopic = Integer.parseInt(request.params(":ntop"));
		//
		// return "Hello: " + numtopic;
		// });
	}

	private void init_lookup_toptopics() {		
		List<String> datasets = new DatasetList(webResources.keySet()).getDatasetsKeys();
		for (String dataset : datasets)
		{
			try {
				if (toptopics == null)
					toptopics = new TopTopics();
				toptopics.add_lookup(dataset, webResources.get(dataset));
			} catch (IOException e) {
				System.out.println("Cannot fetch top topics list server-side");
			}
		}
	}
	
}
