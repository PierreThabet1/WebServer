package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * This class simulates a webServer for sending and receiving files.
 * 
 ***********************************************************************
 *    Title: Lab 11: A Web Server
 *    Author: University of The People
 *    Date: 13 May 2020
 *    Availability: https://my.uopeople.edu/mod/page/view.php?id=268287&inpopup=1
 ***********************************************************************
 *
 */
public class WebServer { // Begin of WebServer class.

	private static final int LISTENING_PORT = 50505;
	private static Socket client;
	private static DataInputStream in;
	private static PrintStream out;
	private static String requestedFile;

	public static void main(String[] args) { // Begin of main.

		ServerSocket serverSocket;

		try {

			serverSocket = new ServerSocket(LISTENING_PORT);

		}
		catch (Exception e) {

			System.out.println("Failed to create listening socket.");
			return;

		}

		System.out.println("Listening on port " + LISTENING_PORT);

		try {

			while (true) {

				Socket connection = serverSocket.accept();

				System.out.println("\nConnection from "
						+ connection.getRemoteSocketAddress());

				/**
				 * Start a new thread with the new connection.
				 */
				ConnectionThread thread = new ConnectionThread(connection);
				thread.start();


			}

		}

		catch (Exception e) {

			System.out.println("Server socket shut down unexpectedly!");
			System.out.println("Error: " + e);
			System.out.println("Exiting.");

		}


		try {

			serverSocket.close();

		} 

		catch (IOException e) {

			e.printStackTrace();

		}

	} // End of main.

	/**
	 * This method handles the connection, establishes connection, receive
	 * files and read it. 
	 * This method calls the sendRH method, sendFile method,
	 * sendError method, and getMimeType method.
	 * 
	 * @param connection of type Socket.
	 */
	private static void handleConnection(Socket connection) {

		String rootDir = "F:\\RootDirectory";

		client = connection;

		try {

			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());

			String line = null;
			String request = null;

			request = in.readLine();

			line = request;

			while (line.length() > 0) {

				line = in.readLine();

			}

			StringTokenizer strTk = new StringTokenizer(request);

			if (! strTk.nextToken().equals("GET")) {

				sendError(501);
				return;

			}

			requestedFile = strTk.nextToken();

			File file = new File(rootDir + requestedFile);

			if (! file.canRead()) {

				sendError(404);
				return;

			}

			sendRH(getMimeType(requestedFile), (int) file.length());

			sendFile(file, client.getOutputStream());

		} 

		catch (IOException e) {

			e.printStackTrace();
		}

		finally {

			try {

				connection.close();

			} 

			catch (IOException e) {

				e.printStackTrace();

			}

		}

	}

	/**
	 * This method is responsible for sending files.
	 * 
	 * @param file type File.
	 * @param socket type OutputStream.
	 * @throws IOException.
	 */
	private static void sendFile(File file, OutputStream socket) throws IOException {

		InputStream in = new BufferedInputStream(new FileInputStream(file));
		OutputStream out = new BufferedOutputStream(socket);

		while (true) {

			int b = in.read();

			if (b < 0) {

				break;

			}

			out.write(b);

		}

		in.close();
		out.flush();
		out.close();

	}

	/**
	 * This method sends information to the client through the connection.
	 * 
	 * 
	 * @param type type Object.
	 * @param length type int.
	 */
	private static void sendRH(Object type, int length) {

		out.println("HTTP/1.1 200 OK");
		out.println("Connection: close " + "\r\n"); 
		out.println("Content-type: " +type);
		out.println("Content-Length: " +length);                

	}

	/**
	 * 
	 * @param fileName type String.
	 * @return Object.
	 */
	private static Object getMimeType(String fileName) {

		int pos = fileName.lastIndexOf('.');

		if (pos < 0)  // no file extension in name

			return "x-application/x-unknown";

		String ext = fileName.substring(pos+1).toLowerCase();

		if (ext.equals("txt")) return "text/plain";

		else if (ext.equals("html")) return "text/html";

		else if (ext.equals("htm")) return "text/html";

		else if (ext.equals("css")) return "text/css";

		else if (ext.equals("js")) return "text/javascript";

		else if (ext.equals("java")) return "text/x-java";

		else if (ext.equals("jpeg")) return "image/jpeg";

		else if (ext.equals("jpg")) return "image/jpeg";

		else if (ext.equals("png")) return "image/png";

		else if (ext.equals("gif")) return "image/gif";

		else if (ext.equals("ico")) return "image/x-icon";

		else if (ext.equals("class")) return "application/java-vm";

		else if (ext.equals("jar")) return "application/java-archive";

		else if (ext.equals("zip")) return "application/zip";

		else if (ext.equals("xml")) return "application/xml";

		else if (ext.equals("xhtml")) return"application/xhtml+xml";

		else return "x-application/x-unknown";

		// Note:  x-application/x-unknown  is something made up;
		// it will probably make the browser offer to save the file.

	}

	/**
	 * This method sends Error depending on the type of the error code.
	 * 
	 * 
	 * @param ecode type int.
	 */
	private static void sendError(int ecode) {

		switch (ecode) {

		case 404 -> {

			out.print("HTTP/1.1 404 Not Found");
			out.println("Connection: close " );
			out.println("Content-type: text/plain" +"\r\n");
			out.println("<html><head><title>Error</title></head><body> <h2>Error: 404 Not Found</h2> <p>The resource that you requested does not exist on this server.</p> </body></html>");
			break;

		}

		case 501 -> {

			out.print("The method is invalid");
			out.println("Connection: close " );
			out.println("Content-type: text/plain" +"\r\n");
			break;

		}

		case 400 -> {

			out.print("The syntax of the request is bad");
			out.println("Connection: close " );
			out.println("Content-type: text/plain" +"\r\n");
			break;

		}

		default -> {

			out.print("Internal Server Error");
			out.println("Connection: close " );
			out.println("Content-type: text/plain" +"\r\n");
			break;

		}

		}

	}

	/**
	 * 
	 * This class is responsible for creating threads with new connections.
	 *
	 */
	private static class ConnectionThread extends Thread {

		Socket connection;

		ConnectionThread(Socket connection) {

			this.connection = connection;

		}

		/**
		 * This method runs the connection in a new thread.
		 */
		public void run() {

			handleConnection(connection);

		}

	}

} // End of WebServer class.
