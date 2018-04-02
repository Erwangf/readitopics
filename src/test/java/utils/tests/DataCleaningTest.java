package utils.tests;

import org.junit.Assert;
import org.junit.Test;

import utils.DataUtils;

public class DataCleaningTest {

	@Test
	public void cleanTextTest() {
		
		String s = "He dashed back across the road 42, hurried up to his office, snapped at his secretary not to disturb him, seized his telephone, and had almost finished dialing his home number when he changed his mind. He put the receiver back down and stroked his mustache, thinking...no, he was being stupid. Potter wasn’t such an unusual name. He was sure there were lots of people called Potter who had a son called Harry. Come to think of it, he wasn’t even sure his nephew was called Harry. He’d never even seen the boy. It might have been Harvey. Or Harold. There was no point in worrying Mrs. Dursley; she always got so upset at any mention of her sister. He didn’t blame her — if he’d had a sister like that...but all the same, those people in cloaks....";
		DataUtils converter = new DataUtils("tolower,punct,stopwords,min=2");
		String clean = DataUtils.tokenizeAndClean(s);
		String expected = "he dashed back across the road 42 hurried up to his office snapped at his secretary not to disturb him seized his telephone and had almost finished dialing his home number when he changed his mind he put the receiver back down and stroked his mustache thinking no he was being stupid potter wasn such an unusual name he was sure there were lots of people called potter who had son called harry come to think of it he wasn even sure his nephew was called harry he never even seen the boy it might have been harvey or harold there was no point in worrying mrs dursley she always got so upset at any mention of her sister he didn blame her if he had sister like that but all the same those people in cloaks";
		Assert.assertEquals(expected, clean);
		
		s = "\"Example of data with multiple \" quotation marks \"";
		System.out.println(s);
		clean = converter.protectQuotes(s);
		System.out.println(clean);
		expected = "\\\"Example of data with multiple \\\" quotation marks \\\"";
		System.out.println(expected);
		Assert.assertEquals(expected, clean);
		
	}

}
