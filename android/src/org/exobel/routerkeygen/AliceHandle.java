package org.exobel.routerkeygen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


class AliceHandle extends DefaultHandler implements Serializable{
	private static final long serialVersionUID = -1867841551140131246L;
	String alice;
	List <AliceMagicInfo> supportedAlice;

	public AliceHandle(String alice){
		super();
		this.alice = alice;
		supportedAlice = new ArrayList<AliceMagicInfo>();
	} 
	public void startElement(String uri, String localName,
	        String qName, Attributes attributes){
		boolean supported;
		int [] magic = new int[2];
		String serial;
		String mac;
		if ( alice.equalsIgnoreCase(localName) )
		{
			serial = attributes.getValue("sn");
			mac = attributes.getValue("mac");
			magic[0] = Integer.parseInt(attributes.getValue("q"));
			magic[1] = Integer.parseInt(attributes.getValue("k"));
			supported = true;
			supportedAlice.add(new AliceMagicInfo(alice, supported, magic, serial, mac));
		}
	}
	
	public void endElement( String namespaceURI,
	              String localName,
	              String qName ) throws SAXException {}
	
	public void characters( char[] ch, int start, int length )
	              throws SAXException {}
}

