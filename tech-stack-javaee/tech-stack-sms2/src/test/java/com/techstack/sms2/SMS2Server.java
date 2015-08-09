package com.techstack.sms2;

import org.eclipse.jetty.server.Server;
import com.techstack.component.jetty.JettyFactory;

/**
 * Jetty启动运行web应用入口
 * 在Console输入回车快速重新加载应用.
 * 
 */
public class SMS2Server {

	public static final int PORT = 9556;
	public static final String CONTEXT = "/tech-stack-sms2";
	public static final String[] TLD_JAR_NAMES = new String[] { "sitemesh",
			"spring-webmvc", "shiro-web", "springside-core" };

	public static void main(String[] args) throws Exception{
		// 启动Jetty
		Server server = JettyFactory.createServerInSource(PORT, CONTEXT);
		JettyFactory.setTldJarNames(server, TLD_JAR_NAMES);

		try {
			server.start();

			System.out.println("[INFO] Server running at http://localhost:"
					+ PORT + CONTEXT);
			System.out
					.println("[HINT] Hit Enter to reload the application quickly");

			// 等待用户输入回车重载应用.
			while (true) {
				char c = (char) System.in.read();
				if (c == '\n') {
					JettyFactory.reloadContext(server);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}