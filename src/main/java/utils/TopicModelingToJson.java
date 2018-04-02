package utils;

import cc.mallet.topics.ParallelTopicModel;

public class TopicModelingToJson {

	/**
	 * simple and dirty json serializer for the web app. Output expected is as
	 * follows on a toy 2-topics example :
	 * 
	 * <pre>
	 *{
	 *"numtopics": "10",
	 *"topics": [
	 *		[
	 *			{"mot1": "0.23"},
	 *			{"mot2": "0.12"}
	 *		],
	 *		[
	 *			{"mot12": "0.43"}, 
	 *			{"mot2": "0.12"}
	 *		]
	 *	]
	 *}
	 * </pre>
	 * @param model a ParallelTopicModel
	 * @param nbWords number of words for each topic
	 */
	public static String toJson(ParallelTopicModel model, int nbWords) {

		Object[][] topicsords = model.getTopWords(nbWords);

		StringBuffer sb = new StringBuffer();
		sb.append("jsonstr = {\n\t");
		sb.append("\"numtopics\" : \"" + model.numTopics + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < model.numTopics; i++) {
			addTopic(sb, topicsords[i], model, (i < model.numTopics - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}

	private static void addTopic(StringBuffer sb, Object[] objects, ParallelTopicModel model, boolean sep, int k) {

		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + k + ",\n");
		sb.append("\t\t\"tokens\" : [\n");
		for (int i = 0; i < objects.length; i++) {
			sb.append("\t\t\t{\"mot\" : \"" + objects[i] + "\", \"proba\" : \"1\"}");
			if (i < objects.length - 1) {
				sb.append(",");
			}
			sb.append("\n");

		}
		sb.append("\t\t\t]\n\t\t}");
		if (sep) {
			sb.append(",");
		}
		sb.append("\n");
	}
}
