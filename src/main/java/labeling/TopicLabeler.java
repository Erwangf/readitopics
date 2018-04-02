package labeling;

import java.io.IOException;

public interface TopicLabeler
{

	/* return all the k labels (possibly with the associated weights) */
	public String getLabels(boolean print_weight);
	
	/* return the label of topic i only (possibly with the associated weights) */
	public String getLabel(int i, boolean print_weight);

	/* export the labeling information to the file filename in the directory dir */
	public void export_labels();
	
	/* import the labeling information from the file filename in the directory dir */
	public void import_labels() throws IOException, ClassNotFoundException;
	
	/* get the name of the labeler */
	public String getName();

	/* get the short name of the labeler (2 characters exactly) */
	public String getShortName();

	// whatever methods we would need to be common to a TopicLabeller goes here
}