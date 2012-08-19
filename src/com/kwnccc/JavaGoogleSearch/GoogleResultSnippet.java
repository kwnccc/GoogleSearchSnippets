package com.kwnccc.JavaGoogleSearch;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * @author Constantine Leymonis
 *
 * GoogleResultSnippet is an object with the all the needed information for a Google snippet as search engine result.
 * The information that it contains consist of the title, body and url address of each snippet.
 * This object is Serializable.
 */

public class GoogleResultSnippet implements Serializable {

	private String _title, _body, _address;
	
	public GoogleResultSnippet(String title, String body, String address){
		_title = title;
		_body = body;
		_address = address;
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String title) {
		_title = title;
	}

	public String getBody() {
		return _body;
	}

	public void setBody(String body) {
		_body = body;
	}
	
	public String getAddress() {
		return _address;
	}

	public void setAddress(String address) {
		_address = address;
	}

	@Override
	public String toString() {
		return "<docID>\n\t<title>\n\t\t" + _title + "\n\t</title>\n\t<body>\n\t\t" 
   				+ _body + "\n\t</body>\n\t<address>" + _address + "</address>\n</docID>";
	}
	
	private void writeObject(ObjectOutputStream oos) {
		try {
			oos.writeObject(_title);
			oos.writeObject(_body);
			oos.writeObject(_address);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readObject(ObjectInputStream ois) {
		try {
			_title = (String)ois.readObject();
			_body = (String)ois.readObject();
			_address = (String)ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
