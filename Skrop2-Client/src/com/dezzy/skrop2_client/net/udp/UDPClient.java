package com.dezzy.skrop2_client.net.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.dezzy.skrop2_client.game.ClientController;

public class UDPClient implements Runnable {
	/**
	 * UDP packet size, in bytes
	 */
	public static final int UDP_PACKET_MAX_BYTE_LENGTH = 700;
	
	private final ClientController clientController;
	
	private final DatagramSocket socket;
	private final InetAddress serverAddress;
	private volatile boolean isRunning = true;
	
	public UDPClient(final ClientController _clientController, final String ip, int port) throws IOException {
		clientController = _clientController;
		serverAddress = InetAddress.getByName(ip);
		socket = new DatagramSocket();
		
		socket.send(new DatagramPacket("Hello UDP".getBytes(), "Hello UDP".getBytes().length, serverAddress, port));
	}
	
	@Override
	public void run() {
		try {
			byte[] buffer = new byte[UDP_PACKET_MAX_BYTE_LENGTH];
			
			while (isRunning) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				socket.receive(packet);
				
				String message = new String(buffer);
				clientController.relayUDPMessage(message.substring(0, message.indexOf((char)0)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
