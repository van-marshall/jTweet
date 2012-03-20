import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;


public class TweetLinks {

	public Map<String, Integer> get_urls(String hashtag) {
	    Twitter twitter = new TwitterFactory().getInstance();
	    Query query = new Query("#"+hashtag);
	    query.setRpp(100);
	    query.setResultType(Query.RECENT);
	    QueryResult result;
	    Map<String, String> cache = new HashMap<String, String>();
	    Map<String, Integer> urls = new HashMap<String, Integer>();
		try {
			result = twitter.search(query);	
		    for (Tweet tweet : result.getTweets()) {
		        if (tweet.getMediaEntities() != null)
			    	for (MediaEntity entity : tweet.getMediaEntities()) {
			    		add(entity.getExpandedURL().toString(), cache, urls);
			        }
		        if (tweet.getURLEntities() != null)
			        for (URLEntity entity : tweet.getURLEntities()) {
			    		add(entity.getExpandedURL().toString(), cache, urls);
			        }
		    }		
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return urls;
	}
	
	private void add(String url, Map<String, String> cache, Map<String, Integer> urls) {
		String resolved = null;
		if (cache.containsKey(url)) {
			resolved = cache.get(url);
		}
		else {
			resolved = resolve(url);
			cache.put(url, resolved);
		}
		if (urls.containsKey(resolved)) {
			urls.put(resolved, urls.get(resolved).intValue() + 1);
		}
		else {
			urls.put(resolved, 1);
		}
	}
	
	private String resolve(String location) {
     	System.out.println("Resolving "+location);
	    try {
	    	while (true) {
	    		URI uri = new URI(location);
	    		HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
	    		con.setInstanceFollowRedirects(false);
	    		con.setRequestMethod("HEAD");
	    		if (location.equals(con.getHeaderField("Location")) || 
	    				con.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM)
	    			break;
	    		location = uri.resolve(con.getHeaderField("Location")).toURL().toString();
	    	}
	      }
	      catch (Exception e) {
	    	 System.out.println("Warning: Failed to resolve url");
	         e.printStackTrace();
	      }
	      return location;
	}
	
	static enum Order {ASC, DESC}
	static <K, V extends Comparable<V>> List<Map.Entry<K, V>> valueSort(Map<K, V> map, final Order order) {
	    List<Map.Entry<K, V>> list = new ArrayList<Entry<K, V>>(map.entrySet());
	    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	    	public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	    		return ((order==Order.ASC)? 1 : -1 ) * o1.getValue().compareTo(o2.getValue());
	    	}
	    });		
		return list;
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Retrieves all the unique http links in the last 100 most recent tweets");
			System.out.println("usage: java -jar jtweet.jar <hashcode>");
			System.out.println("NOTE(S): automatically expands tiny-urls");
			System.exit(-1);
		}
		Map<String, Integer> urls = new TweetLinks().get_urls(args[0]);
		int i = 1;
		for (Map.Entry<String, Integer>  e : valueSort(urls, Order.DESC)) {
			System.out.printf("%d. %s\n", i++, e.getKey());
		}
	}
}
