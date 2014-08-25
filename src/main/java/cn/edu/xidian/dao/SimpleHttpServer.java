package cn.edu.xidian.dao ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
<<<<<<< HEAD
/**
 * 用Java语言实现HTTP服务器,首先启动一个java.net.ServerSocket在提供服务的端口上监听连接.向客户返回文本时,
 * 可以用PrintWriter,但是如果返回二进制数据,则必须使用OutputStream.write(byte[])方法,返回的应答消息字符串可以使
 * 用String.getBytes()方法转换为字节数组返回,或者使用PrintStream的print()方法写入文本,用write(byte[])方法写入二进制数据. 
 * @author WangPeng 
 * @version 1.0   
 * @since JDK 1.7
=======

/**
 * 
 * 类名称：SimpleHttpServer   
 * 类描述：用Java语言实现HTTP服务器,首先启动一个java.net.ServerSocket在提供服务的端口上监听连接.向客户返回文本时,
 * 可以用PrintWriter,但是如果返回二进制数据,则必须使用OutputStream.write(byte[])方法,返回的应答消息字符串可以使
 * 用String.getBytes()方法转换为字节数组返回,或者使用PrintStream的print()方法写入文本,用write(byte[])方法写入二进制数据.  
 * 创建人：WangPeng  
 * 创建时间：2014-5-12 下午2:41:07   
 * 修改人：WangPeng   
 * 修改时间：2014-5-12 下午2:41:07   
 * 修改备注：   
 * @version 1.0   
 *
>>>>>>> 53089e316f00bf934408d86a9098454d9c24e223
 */
public class SimpleHttpServer {
	
	/**
	 * 服务器SocketServer
	 */
	private static ServerSocket serverSocket;
	/**
	 * 线程池，用来每当有客户端连接服务器，就将客户端请求交个一个线程来处理
	 */
	private static ExecutorService pool = Executors.newFixedThreadPool(10);

	/**
	* 服务器监听端口, 默认为 8080.
	*/
	public static int PORT = 8080;// 标准HTTP端口

	/**
	* 启动 HTTP 服务器
	* @param args
	*/
	public static void main(String[] args) {
		/**
		* 初始化，开始服务器 Socket 线程.
		*/
		try {
			if (args.length < 1) {
				System.out.println("这是一个简单的web服务器 ，端口是： 80.");
			} else if (args.length == 1) {
				PORT = Integer.parseInt(args[0]);
			}
			serverSocket = new ServerSocket(PORT);
			if (serverSocket == null){
				System.exit(1);
			}
			System.out.println("HTTP服务器正在运行,端口:" + PORT);
			
		} catch (Exception e) {
			System.out.println("无法启动HTTP服务器:" + e.getMessage());
		}
		
		while (true)
		{
			try
			{
				Socket client = serverSocket.accept() ;
				pool.execute(new Process(client)) ;
			}
			catch (Exception e)
			{
				e.printStackTrace() ;
			}
		}
	}
}

/**
 * 
 * 类名称：Process   
 * 类描述：处理客户端请求的线程类  
 * 创建人：WangPeng  
 * 创建时间：2014-5-12 下午2:54:17   
 * @version 1.0   
 *
 */
class Process extends Thread {
	
	private Socket client;
	
	Process(Socket client) {
		this.client = client;
	}

	public void run() {
		
		if (client != null) {
			System.out.println("连接到服务器的用户:" + client);
			try {
				/**
				 *  第一阶段: 打开输入流
				 */
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				System.out.println("客户端发送的请求信息: ***************");
				/**
				 *  读取第一行, [请求方法  请求资源URL HTTP协议]
				 */
				System.out.println("http协议头部信息：");
				String line = in.readLine();
				System.out.println(line);
				/**
				 * 请求资源的URL
				 */
				String resource = line.substring(line.indexOf('/'),
				line.lastIndexOf('/') - 5);
				/**
				 *  获得请求的资源的地址
				 */
				resource = URLDecoder.decode(resource, "gbk");// 反编码
	
				String method = new StringTokenizer(line).nextElement().toString();// 获取请求方法, GET 或者 POST
	
				/**
				 * 读取浏览器发送过来的请求参数头部信息
				 */
				while ((line = in.readLine()) != null) {
					System.out.println(line);
					if (line.equals(""))
					break;
				}
	
				System.out.println("http协议头部结束 ***************");
				System.out.println("用户请求的资源是:" + resource);
				System.out.println("请求的类型是: " + method);
				
				/**
				 * 查找字符串
				 */
				String params = null;
	
				if (resource.indexOf("?") > -1) {
					params = resource.substring(resource.indexOf("?") + 1);
					resource = resource.substring(0, resource.indexOf("?"));
				}
	
				/**
				 * 显示 POST 表单提交的内容, 这个内容位于请求的主体部分
				 */
				if ("POST".equalsIgnoreCase(method)) {
					if (params != null) {
						params += "&" + in.readLine();
					} else {
						params = in.readLine();
					}
				}
	
				System.out.println("打印提交的数据：");
				printParams(params);
				
				/**
				 *  读取资源并返回给客户端
				 */
				fileReaderAndReturn(resource, client);
				/**
				 *  关闭客户端链接
				 */
				client.close();
				System.out.println("客户端返回完成！");
			} catch (Exception e) {
				System.out.println("HTTP服务器错误:" + e.getMessage());
			}
		}
	}

	/**
	 * 读取一个文件的内容并返回给浏览器端.
	 * @param fileName 文件名
	 * @param socket 客户端 socket.
	 * @return void
	 * @throws IOException
	 */
	void fileReaderAndReturn(String fileName, Socket socket) throws IOException {
		/**
		 * 设置欢迎页面！
		 */
		if ("/".equals(fileName)) {
			fileName = "/index.html";
		}
		fileName = fileName.substring(1);
		
		PrintStream out = new PrintStream(socket.getOutputStream(), true);
		File fileToSend = new File(fileName);
		/**
		 * 请求资源的扩展名
		 */
		String fileEx = fileName.substring(fileName.indexOf(".") + 1);
		String contentType = null;
		/**
		 * 设置返回的内容类型
		 * 此处的类型与tomcat/conf/web.xml中配置的mime-mapping类型是一致的。测试之用，就写这么几个。
		 */
		if ("htmlhtmxml".indexOf(fileEx) > -1) {
			contentType = "text/html;charset=GBK";
		} else if ("jpegjpggifbpmpng".indexOf(fileEx) > -1) {
			contentType = "application/binary";
		}
		
		if (fileToSend.exists() && !fileToSend.isDirectory()) {
			/**
			 * HTTP响应信息
			 */
			out.println("HTTP/1.0 200 OK");// 响应状态，并结束应答
			out.println("Content-Type:" + contentType); //响应内容类型
			out.println("Content-Length:" + fileToSend.length());// 返回内容字节数
			out.println();// 根据 HTTP 协议, 空行将结束头信息
			
			/**
			 * 将请求资源输出到客户端的浏览器
			 */
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(fileToSend);
			} catch (FileNotFoundException e) {
				out.println("<h1>404错误！</h1>" + e.getMessage());
			}
			byte data[];
			try {
				data = new byte[fis.available()];
				fis.read(data);
				out.write(data);
			} catch (IOException e) {
				out.println("<h1>500错误!</h1>" + e.getMessage());
				e.printStackTrace();
			} finally {
				out.close();
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			out.println("<h1>404错误！</h1>" + "文件没有找到");
			out.close();
		}
	}
		
	/**
	 * 解析查找字符串	
	 * @param params
	 * @return void
	 * @throws IOException
	 */
	void printParams(String params) throws IOException {
		if (params == null) {
			return;
		}
		String[] maps = params.split("&");
		for (String temp : maps) {
			String[] map = temp.split("=");
			System.out.println(map[0] + "==" + map[1]);
		}
	}
} 