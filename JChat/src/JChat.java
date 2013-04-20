import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Simple chat server and client, implemented in Java. Uses raw socket
 * communication over TCP ports. Intended for compatibility with Python
 * implementation.
 * 
 * @author Zekoff
 * 
 */
public class JChat {
	/**
	 * Simple chat server. Spawns one thread upon startup that blocks for
	 * command-line input, which is sent to all connected clients. Each new
	 * client connection spawns another listener thread which receives input
	 * from the client and prints it to standard output.
	 * <p>
	 * Uses a ServerSocket to listen for requests, since Java Sockets have no
	 * listen() method. Accepts an arbitrary number of client connections.
	 * 
	 * @author Zekoff
	 * 
	 */
	class Server {
		String HOST = "";
		int PORT = 50007;
		// Vector is thread-safe
		Vector<Socket> connections = new Vector<Socket>();

		Server() {
			// use default values for HOST and PORT
		}

		Server(String host) {
			if (host != null)
				this.HOST = host;
			else
				this.HOST = "127.0.0.1";
		}

		Server(String host, String port) {
			if (host != null)
				this.HOST = host;
			else
				this.HOST = "127.0.0.1";
			if (port != null)
				this.PORT = Integer.parseInt(port);
			else
				this.PORT = 50007;
		}

		public void start() throws Exception {
			Thread speakerThread = new Thread(new ServerSpeakerThread(
					connections));
			speakerThread.start();
			ServerSocket s = new ServerSocket(PORT);
			System.out.println("Server online at:");
			System.out.println("(HOST) " + HOST);
			System.out.println("(PORT) " + PORT);
			while (true) {
				Socket newSocket = s.accept();
				System.out.println("New client connected.");
				Thread listenerThread = new Thread(new ServerListenerThread(
						newSocket));
				listenerThread.start();
				// add this socket to list of connections so that the speaker
				// thread can send its messages to this client
				connections.add(newSocket);
			}
		}
	}

	/**
	 * Thread to listen for messages sent from a single client and print them to
	 * standard output.
	 * 
	 * @author Zekoff
	 * 
	 */
	private class ServerListenerThread implements Runnable {
		BufferedReader reader;

		/**
		 * Receives a reference to the client socket, but discards it after
		 * instantiating a new BufferedReader, since it never has the use the
		 * socket directly after that.
		 * 
		 * @param socket
		 *            The connection to the client, from which a BufferedReader
		 *            is created.
		 */
		public ServerListenerThread(Socket socket) {
			try {
				// The socket's input stream (i.e. the information being sent
				// FROM the socket) is wrapped in an InputStreamReader, which is
				// itself wrapped in a BufferedReader for the sake of
				// efficiency. This is basically just a Java idiom for reading
				// input.
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					// Thread blocks on readLine() until it receives input ended
					// by a newline character.
					String line = reader.readLine();
					System.out.println("CLIENT: " + line);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * The single input thread that the server uses. All connected clients
	 * receive the same messages sent from this thread.
	 * 
	 * @author Zekoff
	 * 
	 */
	private class ServerSpeakerThread implements Runnable {
		Vector<Socket> connections = new Vector<Socket>();
		BufferedReader reader;

		/**
		 * Creates a new speaker thread and stores a reference to all connected
		 * clients.
		 * 
		 * @param connections
		 *            A Vector containing Sockets for all currently connected
		 *            clients. This Vector is then iterated over each time input
		 *            is received so that the input can be sent as a message to
		 *            all clients.
		 */
		public ServerSpeakerThread(Vector<Socket> connections) {
			this.connections = connections;
			reader = new BufferedReader(new InputStreamReader(System.in));
		}

		@Override
		public void run() {
			while (true) {
				try {
					String line = reader.readLine();
					for (Socket s : connections) {
						PrintWriter pw = new PrintWriter(s.getOutputStream());
						pw.println(line);
						pw.flush(); // Must flush to actually send anything
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Client makes a single persistent connection to a server that is already
	 * running. Dies if server shuts down or is not present at creation.
	 * 
	 * @author Zekoff
	 * 
	 */
	class Client {
		Socket socket;

		Client() throws Exception {
			socket = new Socket("127.0.0.1", 50007);
		}

		Client(String host) throws Exception {
			socket = new Socket(host, 50007);
		}

		Client(String host, String sPort) throws Exception {
			Integer port = Integer.parseInt(sPort);
			socket = new Socket(host, port);
		}

		public void start() throws Exception {
			System.out.println("Client connected to ("
					+ socket.getRemoteSocketAddress().toString() + ")");
			Thread listenerThread = new Thread(new ClientListenerThread(socket));
			listenerThread.start();
			Thread speakerThread = new Thread(new ClientSpeakerThread(socket));
			speakerThread.start();
		}
	}

	private class ClientListenerThread implements Runnable {
		BufferedReader reader;

		ClientListenerThread(Socket socket) throws IOException {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		}

		@Override
		public void run() {
			while (true) {
				try {
					String line = "";
					line = reader.readLine();
					System.out.println("SERVER: " + line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class ClientSpeakerThread implements Runnable {
		BufferedReader reader;
		PrintWriter writer;

		public ClientSpeakerThread(Socket socket) throws IOException {
			reader = new BufferedReader(new InputStreamReader(System.in));
			writer = new PrintWriter(socket.getOutputStream());
		}

		@Override
		public void run() {
			while (true) {
				try {
					String line = reader.readLine();
					writer.println(line);
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Presents choice of Client or Server at runtime, then options for desired
	 * host and port to connect to. Host defaults to 127.0.0.1 (localhost) and
	 * port defaults to 50007 (arbitrary high-numbered port).
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		JChat instance = new JChat();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("Enter 1 for Client, 2 for Server:");
		Integer choice = Integer.parseInt(reader.readLine());
		String host = "";
		if (choice == 1) {
			System.out.println("Enter HOST (leave blank for 127.0.0.1):");
			host = reader.readLine();
			if (host.compareTo("") == 0)
				host = "127.0.0.1";
		}
		System.out.println("Enter PORT (leave blank for 50007):");
		String port = reader.readLine();
		if (port.compareTo("") == 0)
			port = "50007";
		System.out.println("-----");
		switch (choice) {
		case 1:
			JChat.Client client = instance.new Client(host, port);
			client.start();
			break;
		case 2:
			JChat.Server server = instance.new Server(host, port);
			server.start();
			break;
		}
	}
}
