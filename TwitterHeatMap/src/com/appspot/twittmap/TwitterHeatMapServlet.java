package com.appspot.twittmap;

import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class TwitterHeatMapServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String op=req.getParameter("op");
		resp.getWriter().println(op);
		TwitterStreamListener listener=new TwitterStreamListener();
		resp.setContentType("text/plain");
		if (op.equals("gettweet")){
			listener.getTweets(resp);
			resp.getWriter().println("Hello, world");
		}
		else if (op.equals("update")){
			listener.update_wordcount();
		}
		
	}
}
