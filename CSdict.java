
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.lang.System;
import java.io.IOException;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output. 
//


public class CSdict {
    static final int MAX_LEN = 255;
    static Boolean debugOn = false;
    
    private static final int PERMITTED_ARGUMENT_COUNT = 1;
	// Richard - do we need these as properties? Moved them to local vars in main
    // private static String command;
    // private static String[] arguments;
	private static Socket socket; 
	private static PrintWriter out;
	private static BufferedReader in;
	private static String dictionary = "*";
	// private static ArrayList<String> responseCodes = new ArrayList<String>();
    
    public static void main(String [] args) {
	// responseCodes.add("250 ok");
	// responseCodes.add("221 bye");
	// responseCodes.add("551 invalid strategy");
	// responseCodes.add("552 no match");
	// responseCodes.add("550 invalid database, use SHOW DB for list");
	// responseCodes.add("554 no databases present");
	


	while (true) {
		byte cmdString[] = new byte[MAX_LEN];
		int len;
		// Verify command line arguments

			if (args.length == PERMITTED_ARGUMENT_COUNT) {
				debugOn = args[0].equals("-d");
				if (debugOn) {
					System.out.println("Debugging output enabled");
				} else {
					System.out.println("997 Invalid command line option - Only -d is allowed");
					return;
				}
			} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
				System.out.println("996 Too many command line options - Only -d is allowed");
				return;
			}

		// Example code to read command line input and extract arguments.
			try {
				System.out.print("csdict> ");
				System.in.read(cmdString);

				// Richard - Added these vars locally instead of as properties since we process each command one at a time?
				String command = null; 
    			String[] arguments = null;
	
				// Convert the command string to ASII
				String inputString = new String(cmdString, "ASCII");
				
				// Split the string into words
				String[] inputs = inputString.trim().split("( |\t)+");
				// Set the command
				command = inputs[0].toLowerCase().trim();
				// Remainder of the inputs is the arguments. 
				arguments = Arrays.copyOfRange(inputs, 1, inputs.length);
	
				System.out.println("The command is: " + command);
				len = arguments.length;
				System.out.println("The arguments are: ");
				for (int i = 0; i < len; i++) {
				System.out.println("    " + arguments[i]);
				}
				System.out.println("Done.");
	
	
				switch (command) {
					case "open": 
						handleOpenCommand(arguments);
						break;
					case "dict":
						handleDictCommand();
						break;
					case "set":
						handleSetCommand(arguments);
						break;
					case "define":
						handleDefineCommand(arguments);
						break;	
					case "match":
						handleMatchCommand(arguments);
						break;	
					case "prefixmatch":
						handlePrefixmatchCommand(arguments);
						break;	
					case "close":
						handleCloseCommand();
						break;
					case "quit":
						handleQuitCommand();
						break;
					default:
						System.err.println("900 Invalid command");
						break;
				}
			} catch (IOException exception) {
			System.err.println("998 Input error while reading commands, terminating.");
			}
		}
    }

	// public static boolean checkResponseCode(String line) throws Exception {

	// 	for (String i : responseCodes) {
	// 		if (line.contains(i)) {
	// 			System.out.println(i);
	// 			return false;
	// 		}
	// 	}
    // 	return true;
	// }

	// https://stackoverflow.com/questions/60922164/reading-multiple-lines-from-server?fbclid=IwAR3tVir5sdSPmMoCNwFsAKjai_7S87mS4M19uOx818A5zAkb8udwPeUY-sM
	public static void readAllLines(String cmd) {
		try {
			String response = "";
			CSdict.out.println(cmd);
			while ( !((response = in.readLine()).trim().equals(".")) ){
					System.out.println(response);
			}
			// System.out.println("."); // TODO: fix if have time
			String responseCode = CSdict.in.readLine();
			// System.out.println(responseCode.split(" [")[0]);
			// if (responseCode.split(" [")[0].equals("552 no match")) {
			// 	System.out.println("****No definition found****");
			// }
			System.out.println(responseCode); // print the status code
		} catch (IOException e) {
			// error
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
	public static void handleOpenCommand(String[] args) {
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		try {
			CSdict.socket = new Socket(hostName, portNumber);
			CSdict.out =
				new PrintWriter(socket.getOutputStream(), true);
			CSdict.in =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			// BufferedReader stdIn =
			// 	new BufferedReader(
			// 		new InputStreamReader(System.in));
			System.out.println("<-- " + in.readLine());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
				hostName);
			System.exit(1);
		} 

		// Every time a connection to a dictionary server is made the dictionary to use is reset to "*".
		CSdict.dictionary = "*";
	}

	public static void handleDictCommand() {
		String cmd = "show db";
		readAllLines(cmd);
	}

	public static void handleSetCommand(String[] arg) {
		// code
		CSdict.dictionary = arg[0];
	}

	public static void handleDefineCommand(String[] arg) {
		String cmd = "define" + " ";
		cmd += CSdict.dictionary == "*" ? "all" + " " + arg[0] : CSdict.dictionary + " " + arg[0];
		readAllLines(cmd);
	}

	public static void handleMatchCommand(String[] arg) {
		String commandString = "MATCH " + CSdict.dictionary + " exact";
		for (int i = 0; i < arg.length; i++) {
			String temp = " " + arg[i];
			commandString +=  temp;
		}
		readAllLines(commandString);
	}

	public static void handlePrefixmatchCommand(String[] arg) {
		String commandString = "MATCH " + CSdict.dictionary + " prefix";
		for (int i = 0; i < arg.length; i++) {
			String temp = " " + arg[i];
			commandString +=  temp;
		}
		readAllLines(commandString);
	}

	public static void handleCloseCommand() {
		String cmd = "q";
		readAllLines(cmd);
	}

	public static void handleQuitCommand() {
		String cmd = "q";
		readAllLines(cmd);
		System.exit(-1);
	}
}
    
    
