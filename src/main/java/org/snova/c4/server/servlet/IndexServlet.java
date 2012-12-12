/**
 * 
 */
package org.snova.c4.server.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.snova.c4.common.C4PluginVersion;

/**
 * @author wqy
 * 
 */
public class IndexServlet extends HttpServlet {
	private static String content = null;

	private static String getContent() {
		if (null == content) {
			InputStream is = IndexServlet.class.getResourceAsStream("/html/index.html");
			if (null == is) {
				return "#####No resource found.";
			}
			try {
				content = IOUtils.toString(is);
				content = content.replace("${version}", C4PluginVersion.value);
			} catch (IOException e) {
			}
		}
		return content;
	}

	@Override
	protected void doGet(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().print(getContent());
	}
}
