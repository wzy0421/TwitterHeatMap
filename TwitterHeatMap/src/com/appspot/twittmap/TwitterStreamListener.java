package com.appspot.twittmap;


import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;





//import com.google.appengine.api.datastore.Key;
//import com.google.appengine.api.datastore.KeyFactory;
import twitter4j.*;
public class TwitterStreamListener{
	TwitterStreamListener(/*int maxCount*/){
		datastore = DatastoreServiceFactory.getDatastoreService();
		syncCache = MemcacheServiceFactory.getMemcacheService();
		try{
			word_count_dict=(Hashtable<String, Integer>) syncCache.get("worddict");
			if (word_count_dict==null){
				update_wordcount();
			}
		}
		catch(Exception e){

		}
		count=0;
	}
	int maxCount;
	private TwitterStream twitterStream;
	private Hashtable<String,Integer> word_count_dict;
	MemcacheService syncCache;
	private int count;
	private DatastoreService datastore;
	//TwitterStream parent;

    String[] getKeywords(String tweet){
		String[] tweet_keywords=tweet.split("\\s");//words are separated by space characters
		return tweet_keywords;
	}
	public void update_wordcount(){
		Query q=new Query("Keyword");//.addSort("count",SortDirection.DESCENDING);
		PreparedQuery pq = datastore.prepare(q);

		for (Entity result : pq.asIterable()) {
			datastore.delete(result.getKey());
		}
		word_count_dict=new Hashtable<String,Integer>();
		Query ptweets=new Query("Tweets");
		PreparedQuery pqtweets = datastore.prepare(ptweets);
		for (Entity result : pqtweets.asIterable()) {
			String[] keywords=getKeywords((String)result.getProperty("Text"));
			for (String keyword:keywords){
				if (word_count_dict.containsKey(keyword)){
					word_count_dict.put(keyword, 1+word_count_dict.get(keyword));
				}
				else{
					word_count_dict.put(keyword, 1);
				}
			}
		}
		int keyword_count=0;
		for (Iterator<String> itkeyword = word_count_dict.keySet().iterator();itkeyword.hasNext();/*&&keyword_count<20;*/){
			String keyword = itkeyword.next();
			if (word_count_dict.get(keyword)!=null && word_count_dict.get(keyword)>10){
				Entity keywordentity = new Entity("Keyword");
				keywordentity.setProperty("Count", word_count_dict.get(keyword));
				keywordentity.setProperty("Word", keyword);
				datastore.put(keywordentity);
				keyword_count+=1;
			}
		}
		syncCache.put("worddict",word_count_dict);
		
	}
//	public List<GeoPosition> KeywordQuery(String keyword){
//		List<GeoPosition> result;
//		try{
//			result= (List<GeoPosition>) syncCache.get(keyword);
//		}
//		catch(Exception e){
//			result=new LinkedList<GeoPosition>();
//			Query query = new Query("Tweets");
//			PreparedQuery pq = datastore.prepare(query);
//			for (Entity result1 : pq.asIterable()) {
//				String[] keywords=getKeywords((String)result1.getProperty("Text"));
//				for (String keyword1:keywords){
//					if (keyword1.equals(keyword)){
//						result.add(new GeoPosition((double)result1.getProperty("Latitude"),(double)result1.getProperty("Longitude"),(String)result1.getProperty("CreateAt")));
//						break;
//					}
//				}
//			}
//			syncCache.put(keyword,result);
//		}
	public String KeywordQuery(String keyword){
		List<GeoPosition> result;
		try{
			result= (List<GeoPosition>) syncCache.get(keyword);
		}
		catch(Exception e){
			result=new LinkedList<GeoPosition>();
			Query query = new Query("Tweets");
			PreparedQuery pq = datastore.prepare(query);
			for (Entity result1 : pq.asIterable()) {
				String[] keywords=getKeywords((String)result1.getProperty("Text"));
				for (String keyword1:keywords){
					if (keyword1.equals(keyword)){
						result.add(new GeoPosition((double)result1.getProperty("Latitude"),(double)result1.getProperty("Longitude"),(String)result1.getProperty("CreateAt")));
						break;
					}
				}
			}
			syncCache.put(keyword,result);
		}
		
		return result.toString();
		
	}
	public void getTweets(HttpServletResponse resp) throws IOException{
		Twitter twitter = new TwitterFactory().getInstance();

        try {
        	
            twitter4j.Query query = new twitter4j.Query("Cocaine");
            query.geoCode(new GeoLocation(0,0), 20000, "mi");
            QueryResult result;
            Random random = new Random();
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                
                for (Status tweet : tweets) {
                	try {
//                    	try{
//	                		tweet.getGeoLocation().getLatitude();
//	                	}
//	                	catch (Exception e){
//	                		System.out.println("NULL");
//	                		continue;
//	                	}
                		count+=1;
                		Entity status = new Entity("Tweets");
//                    	status.setProperty("Latitude", tweet.getGeoLocation().getLatitude());
//                    	status.setProperty("Longitude", tweet.getGeoLocation().getLongitude());
                		status.setProperty("Latitude", random.nextInt(180)-90.);
                    	status.setProperty("Longitude", random.nextInt(360)-180.);
                		resp.getWriter().println(tweet.getUser().getLocation());
                    	status.setProperty("Language", tweet.getLang());
                    	status.setProperty("CreateAt", tweet.getCreatedAt());
                    	status.setProperty("RetweetCount", tweet.getRetweetCount());
                    	status.setProperty("Text", tweet.getText());
                    	status.setProperty("FavouriteCount", tweet.getFavoriteCount());
                    	status.setProperty("Country", "");
                    	status.setProperty("FullName", "");
                    	status.setProperty("CountryCode", "");
//                    	status.setProperty("Country", tweet.getPlace().getCountry());
//                    	status.setProperty("FullName", tweet.getPlace().getFullName());
//                    	status.setProperty("CountryCode", tweet.getPlace().getCountryCode());
                    	datastore.put(status);
                    	String [] keywords=getKeywords(tweet.getText());
                    	for (String keyword:keywords){
            				if (word_count_dict.containsKey(keyword)){
            					word_count_dict.put(keyword, 1+word_count_dict.get(keyword));
            				}
            				else{
            					word_count_dict.put(keyword, 1);
            				}
            			}
					} catch (Exception e) {
						resp.getWriter().println(e.getMessage());
					}
                	resp.getWriter().println(count);
                	resp.getWriter().println(word_count_dict.get("RT"));
                }
            } while (count<50);//(query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            te.printStackTrace();
            try {
				resp.getWriter().println("Failed to search tweets: " + te.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        
	}    
}

