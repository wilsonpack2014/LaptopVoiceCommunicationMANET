/*
	This object will be used by the AudioReceiverThread object
	in order to forward a packet when AudioReceiverThread object
	recognized that the packet received is not for the person
*/

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ForwardThread implements Runnable
{
	int myNodeNumber;
	short myNodeAddress;
	ConfigFileReader configFileReader;
	byte[] forwardPacket;

	/* The constructor to use by the AudioReceiverThread
	   @param myNodeNumber_in: This will be the node number of the mobile user
	   							as per the configuration file.
	   @param myNodeAddress_in: This is the 16-bit address for the MANET. 
	   @param configFileName: The string containing the file name. This will
	   							be used by the configFileReader object
	   @param packet: The packet to be forwarded. This, in design, is coming
	   					from the AudioReceiverThread object. 
	*/
	public ForwardThread(int myNodeNumber_in, short myNodeAddress_in, String configFileName, byte[] packet)
	{
		myNodeNumber = myNodeNumber_in;
		myNodeAddress = myNodeAddress_in;
		configFileReader = new ConfigFileReader(configFileName);
		forwardPacket = packet;
	}

	public void run()
	{
		/*Get my neighbors*/
		int[] myNeighbors = configFileReader.getNeighbors(myNodeNumber);

		/*Set up "forwardersAddress" to be the "previous hop" of the packet information*/
  		byte hopUpperBits = (byte)(myNodeAddress>>8);
  		byte hopLowerBits = (byte)(myNodeAddress);
  		forwardPacket[5] = hopUpperBits;
  		forwardPacket[6] = hopLowerBits;

  		/*Socket to use for sending*/
  		DatagramSocket forwardSocket = null;

  		/*Forward the packet to each of my neighbors*/
  		int amtOfNeighbors = myNeighbors.length;
  		for(int neighbor = 0; neighbor < amtOfNeighbors; neighbor++)
  		{
  			/*Give me the neighbor's IP address using a configfile reader*/
  			InetAddress neighborsIP = configfile.getIPaddress(myNeighbors[neighbor];

  			/*Give me the neighbor's UDP port number using a configfile reader*/
  			int neighborsPort = configfile.getPortNumber(myNeighbors[neigbor];

  			/*Forward the packet to my neighbor*/
  			try
  			{
	  			forwardSocket = new DatagramSocket();
	  			DatagramPacket forwardPacket = new DatagramPacket(forwardPacket, forwardPacket.length, neighborsIP, neighborsPort);
	  			forwardSocket.send(forwardPacket);
	  		}
	  		catch(IOException e)
			{
				e.printStackTrace();
			}
			catch(IllegalArgumentException e)
  			{
  				e.printStackTrace();
  			}
  		}
  		/*Close socket*/
  		if(forwardSocket != null)
  		{
  			forwardSocket.close();
  		}

	}




}