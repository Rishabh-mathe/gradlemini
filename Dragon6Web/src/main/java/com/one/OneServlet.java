package com.one;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OneServlet extends HttpServlet{

	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		MiddleWare middleWare = new MiddleWare();
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.print("<h1 color='red'>"+middleWare.middle()+" Buddy from one servlet<h1>");
		writer.close();
	}
}
