package com.salquestfl.ibnlivereader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.widget.Toast;
import android.util.Log;


public class RssReader extends DefaultHandler {
	
    private static final String TAG = "IBNLiveReader";

    private ArrayList<HashMap<String, String>> rssItems = new ArrayList<HashMap<String, String>>();
    private HashMap<String, String> rssItem = new HashMap<String, String>();
    private StringBuilder chars;

    public static ArrayList<HashMap<String, String>> read(Reader ir) throws SAXException, IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            RssReader handler = new RssReader();
            InputSource input = new InputSource(ir);
            reader.setContentHandler(handler);
            reader.parse(input);
            
            return handler.rssItems;
        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        // reset the string buffer
        chars = new StringBuilder();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        chars.append(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName != null && qName.length() > 0) {
            String field = qName.toLowerCase();
            // Check if looking for article, and if article is complete
            if (field.equals("entry") || field.equals("item")) {
                rssItems.add(rssItem);
                Log.i(TAG, "adding " + rssItem.toString());
                rssItem = new HashMap<String, String>();
            }
            else {
                if (field.equals("description")) {
                  // Hack to get the image span embedded in the description as a thumbnail
                  Spanned desc = Html.fromHtml(chars.toString());
                  ImageSpan[] imageSpans = desc.getSpans(0, desc.length(), ImageSpan.class);
                  if (imageSpans.length == 1) {
                    rssItem.put("thumbnail", imageSpans[0].getSource());
                  }
                  rssItem.put(field, desc.toString());
                } else {
                  rssItem.put(field, chars.toString());
                }
            }
        }
                    
    }

}
