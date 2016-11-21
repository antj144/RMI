/*

* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmsa3leader;

/**
 *
 * @author Anthony
 */
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class Node extends UnicastRemoteObject implements ElectionNode {

	
	private static final int min = 1;
	private static final int max = 5;

	private static final int delay = 5000;
	private final int silencePeriod = 
			(min + (int)(Math.random() * ((max - min) + 1))) * 1000;
	private int messagePeriod = 0;

	private String host;

	private String name;
	private String leaderName = "";

	private int ignoreElection = 0;
	private boolean heardFromLeader = false;
	private boolean noLeaderFound = true;

	@SuppressWarnings("unused")
	private Node() throws RemoteException {super();}

	public Node(String nameIn, String hostIn) throws RemoteException {
		super();
		
		this.name = nameIn;
		this.host = hostIn;
		while (messagePeriod < silencePeriod) {
			messagePeriod = (min + (int) (Math.random() * ((max - min) + 1))) * 1000;
		}
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!name.equals(leaderName) && 
						!heardFromLeader) {
					try {
						System.out.println("Calling election...");
						Registry reg = LocateRegistry.getRegistry(host);
						for (String nodeName : reg.list()) {
							try {
								if (!nodeName.equals(name) && nodeName.compareTo(name) > 0) {
									ElectionNode otherNode = (ElectionNode) reg.lookup(nodeName);
									String response = otherNode.startElection(name);
									
									if (response.length() > 0) {
										noLeaderFound = false;
										break;
									}
								}
							} catch (DeadNodeException e) {
								System.out.println(e.toString());
							}
						}
						
						if (noLeaderFound) {
							try {
								System.out.println("No leader found, electing myself.");
								startElection(name);
								noLeaderFound = false;
							} catch (DeadNodeException e) {
								System.out.println("Node Error: " + e.toString());
							}
						}
					} catch (RemoteException e) {
						System.out.println("Node Error: " + e.toString());
						e.printStackTrace();
					}
				} else if (heardFromLeader)
					heardFromLeader = false;
			}
		}, delay, silencePeriod);

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (leaderName != null && !name.equals(leaderName)) {
					sendLeaderMsg("Message from " + name);
				}
			}
		}, delay, messagePeriod);
		
		System.out.println(name + " ready.");
	}
	private void sendLeaderMsg(String msg) {
		try {
			Registry reg = LocateRegistry.getRegistry(host);
			try {
				ElectionNode leaderNode = (ElectionNode) reg.lookup(leaderName);

				String response = leaderNode.recvMsg(name, msg);
				System.out.println(leaderName + ": " + response);

				if (!heardFromLeader)
					heardFromLeader = true;
			} catch (NotBoundException e) {
				try {
					System.out.println("Node Error: " + leaderName + " unbound.");
					reg.unbind(leaderName);
				} catch (NotBoundException er) {
					// Shouldn't happen
				}
			} catch (ConnectException e) {
				try {
					System.out.println("Node Error: " + leaderName + " unbound.");
					reg.unbind(leaderName);
				} catch (NotBoundException er) {
					// Shouldn't happen
				}
			}
		} catch (RemoteException e) {
			System.out.println("Node Error: " + e.toString());
			e.printStackTrace();
		}
	}
	@Override
	public String startElection(String senderName) throws DeadNodeException {
		String ret = "";
		
		if (ignoreElection > 0) {
			ignoreElection--;
			System.out.println(ignoreElection + " more elections being ignored.");
			throw new DeadNodeException(name + " is dead.");
		} else {
			System.out.println("Election started.");

			try {
				Registry reg = LocateRegistry.getRegistry(host);
				
				ret = "Leader accepted.";
				System.out.println(ret);
				leaderName = name;
				for (String nodeName : reg.list()) {
					if (!nodeName.equals(name)) {
						try {
							ElectionNode node = (ElectionNode) reg.lookup(nodeName);
							node.newLeader(name);
						} catch (NotBoundException e) {
							try {
								System.out.println("Node Error: " + nodeName + " unbound.");
								reg.unbind(nodeName);
							} catch (NotBoundException er) {
								// Shouldn't happen
							}
						} catch (ConnectException e) {
							try {
								System.out.println("Node Error: " + nodeName + " unbound.");
								reg.unbind(nodeName);
							} catch (NotBoundException er) {
								// Shouldn't happen
							}
						}
					}
				}
			} catch (RemoteException e) {
				System.out.println("Node Error: " + e.toString());
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	@Override
	public void newLeader(String newLeaderName) {
		leaderName = newLeaderName;
		System.out.println(newLeaderName + " is the new leader.");
	}
	@Override
	public String recvMsg(String senderName, String msg) {
		String ret = "Not the leader.";

		if (leaderName.equals(name)) {
			System.out.println(senderName + ": " + msg);
			ret = "Message received.";
		}

		return ret;
	}
}

