
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
	private static ArrayList<String> endCodes = new ArrayList<String>();
    
    public static void main(String [] args) {
	// responseCodes.add("250 ok");
	// responseCodes.add("221 bye");
	// responseCodes.add("551 invalid strategy");
	// responseCodes.add("552 no match");
	// responseCodes.add("550 invalid database, use SHOW DB for list");
	// responseCodes.add("554 no databases present");

	endCodes.add("250");
	endCodes.add("221");
	endCodes.add("551");
	endCodes.add("552");
	endCodes.add("550");
	endCodes.add("554");

	
	
	if (args.length == PERMITTED_ARGUMENT_COUNT) {
		debugOn = args[0].equals("-d");
		if (debugOn) {
			System.out.println("Debugging output enabled"); //TODO: Might need to remove this line, not in sample output?
		} else {
			System.out.println("997 Invalid command line option - Only -d is allowed");
			return;
		}
	} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
		System.out.println("996 Too many command line options - Only -d is allowed");
		return;
	}

	while (true) {
		byte cmdString[] = new byte[MAX_LEN];
		int len;
		// Verify command line arguments


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
	
				// System.out.println("The command is: " + command);
				// len = arguments.length;
				// System.out.println("The arguments are: ");
				// for (int i = 0; i < len; i++) {
				// System.out.println("    " + arguments[i]);
				// }
				// System.out.println("Done.");
	
	
				switch (command) {
					case "open": 
						if (!isValidNumberOfArgs(arguments, 2)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleOpenCommand(arguments);
						break;
					case "dict":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 0)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleDictCommand();
						break;
					case "set":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 1)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleSetCommand(arguments);
						break;
					case "define":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 1)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleDefineCommand(arguments);
						break;	
					case "match":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 1)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleMatchCommand(arguments, "match");
						break;	
					case "prefixmatch":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 1)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handlePrefixmatchCommand(arguments);
						break;	
					case "close":
						if (CSdict.socket == null) {
							System.out.println("903 Supplied command not expected at this time.");
							break;
						}
						if (!isValidNumberOfArgs(arguments, 0)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleCloseCommand();
						break;
					case "quit":
						if (!isValidNumberOfArgs(arguments, 0)) {
							System.out.println("901 Incorrect number of arguments.");
							break;
						}
						handleQuitCommand();
						break;
					default:
						System.err.println("900 Invalid command.");
						break;
				}
			} catch (IOException exception) {
			System.err.println("998 Input error while reading commands, terminating.");
			}
		}
    }

	public static boolean isValidNumberOfArgs(String[] args, int expected) {
		int given = args.length;
		return given == expected;
	}

	// https://stackoverflow.com/questions/60922164/reading-multiple-lines-from-server?fbclid=IwAR3tVir5sdSPmMoCNwFsAKjai_7S87mS4M19uOx818A5zAkb8udwPeUY-sM
	public static void readAllLines(String cmdString, String cmd) {
		try {
			String response = "";
			if (CSdict.debugOn) {
				System.out.println("> " + cmdString);
			}
			CSdict.out.println(cmdString);
			while ( (response = in.readLine()) != null ) {
				if (response.startsWith("110")) {
					if (debugOn) {
						System.out.println("<-- " + response);
					}
					continue;
				} else if (response.startsWith("150")) {
					if (debugOn) {
						System.out.println("<-- " + response);
					}
					continue;
				} else if (response.startsWith("151")) {
					if (debugOn) {
						System.out.println("<-- " + response);
					} 
					String[] tempString = response.split(" ");
					String[] dictName = Arrays.copyOfRange(tempString, 2, tempString.length);
					System.out.println("@ " + String.join(" ", dictName));
					continue; 
				} else if (response.startsWith("152")) {
						if (debugOn) {
							System.out.println("<-- " + response);
						}
						continue;
				} else if (response.startsWith("250")) {
					if (debugOn) {
						System.out.println("<-- " + response);
					}
					return;				
				}
				switch (cmd) {
					case "match":
						if (response.startsWith("552")) {
							if (debugOn) {
								System.out.println("<-- " + response);
							}
							System.out.println("****No matching word(s) found****");
							return;
						} 
						break;
					case "matchFromDefine":
						if (response.startsWith("552")) {
							if (debugOn) {
								System.out.println("<-- " + response);
							}
							System.out.println("****No matches found****");
							return;
						} 
					break;
	
					case "define":
						if (response.startsWith("552")) {
							if (debugOn) {
								System.out.println("<-- " + response);
							}
							System.out.println("****No definition found****");
							handleMatchCommand(new String[] {cmdString.split(" ")[2]}, "matchFromDefine");
							return;
						} 
						break;
	
					default:
						break;
				}
				for (String line : endCodes) { 
					if (response.startsWith(line)) {
						if (debugOn) {
							System.out.println("<-- " + response);
						}
						return;
					}
				}
				System.out.println(response);
			}
		} catch (IOException e) {
			// error
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
	public static void handleOpenCommand(String[] args) {
		if (!args[1].matches("[0-9]+")) {
			System.out.println("902 Invalid argument.");
			return;
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		try {
			CSdict.socket = new Socket(hostName, portNumber);
			CSdict.socket.setSoTimeout(30000);
			CSdict.out =
				new PrintWriter(socket.getOutputStream(), true);
			CSdict.in =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			String statusMsg = in.readLine();
			if (debugOn) {
				System.out.println("<-- " + statusMsg);
			}
		} catch (SocketTimeoutException e) {
			if (socket.isConnected()) {
				System.err.println("999 Processing error. Timed out while waiting for a response.");
			} else {
			 	System.err.println("920 Control connection to " + hostName +  " on port " + portNumber + " failed to open.");
			}
		} catch (UnknownHostException e) {
			System.err.println("999 Processing error. Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("999 Processing error. Couldn't get I/O for the connection to " +
				hostName);
			System.exit(1);
		} 

		// Every time a connection to a dictionary server is made the dictionary to use is reset to "*".
		CSdict.dictionary = "*";
	}

	public static void handleDictCommand() {
		String cmd = "show db";
		readAllLines(cmd, "dict");
	}

	public static void handleSetCommand(String[] arg) {
		CSdict.dictionary = arg[0];
	}

	public static void handleDefineCommand(String[] arg) {
		String cmd = "define" + " ";
		cmd += CSdict.dictionary == "*" ? "all" + " " + arg[0] : CSdict.dictionary + " " + arg[0];
		readAllLines(cmd, "define");
	}

	public static void handleMatchCommand(String[] arg, String matchOrigin) {
		String commandString = "MATCH " + CSdict.dictionary + " exact";
		String temp = " " + arg[0];
		commandString +=  temp;
		readAllLines(commandString, matchOrigin);
	}

	public static void handlePrefixmatchCommand(String[] arg) {
		String commandString = "MATCH " + CSdict.dictionary + " prefix";
		String temp = " " + arg[0];
		commandString +=  temp;
		readAllLines(commandString, "match");
	}

	public static void handleCloseCommand() {
		String cmd = "q";
		readAllLines(cmd, "close");
		if (CSdict.socket != null) {
			try {
				CSdict.socket.close();
				CSdict.socket = null; 
			} catch (IOException e) { 
				System.out.println("999 Processing error. Failed to close socket connection.");
			}
		}
	}

	public static void handleQuitCommand() {
		String cmd = "q";
		readAllLines(cmd, "quit");
		if (CSdict.socket != null) {
			try {
				CSdict.socket.close();
				CSdict.socket = null; 
			} catch (IOException e) {
				System.out.println("999 Processing error. Failed to close socket connection.");
			}
		}
		System.exit(-1);
	}
}
    
    
