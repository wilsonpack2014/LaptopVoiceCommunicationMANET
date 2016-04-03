/*
	This class implements the interface Runnable so that we can 
	utilize the Thread class capabilites. 

	This class will handle gathering the audio from the communication line
	and then sending them via UDP.

	Authors: 	Alex Aguirre
				Brandon Wilson
	Created: 24 Feb 2016
	Updated: 26 Feb 2016
*/

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

import java.util.Vector;

public class AudioTransmitterThread implements Runnable
{
	/* Destination port number*/
	int theirPort;

	/*Destination IP address*/
	InetAddress theirIP;

	/* Socket for this client*/
	protected DatagramSocket socket = null;

	/* While-loop flag*/
	boolean talking = true;

  	/*Voice data buffer
	  Note: buffer[0] = sequence number
	  		buffer[1] and buffer[2] = source address (upper, lower)
	  		buffer[3] and buffer[4] = destination address (upper, lower)
	  		buffer[5] and buffer[6] = Previous hop address (upper, lower)
  	*/
  	static byte[] buffer;

  	/* Audio format object*/
  	AudioFormat audioFormat;

  	/* Our audio data line*/
  	TargetDataLine targetDataLine;

  	/* Maximum packet size allowed as per requirements in Dr Lim's project description*/
  	int MAX_PACKET_SIZE = 128;

  	/* Packet sequence number*/
  	byte sequenceNumber = -1;

  	/* My 16-bit address*/
  	static short sourceAddress;

  	/* The person I want to send the packet to. This is their 16-bit address*/
  	short destinationAddress;

  	/* This will gather the proper information from the configuration file so we can update ourself*/
	private static ConfigFileReader configFileReader;

	/* The integer value of this user's ID in the configuration file. E.g. Node 55, the value will be 55*/
	int intID;

	/* Contains a list of all the nodes this user can communicate with*/
	//Vector<Integer>myNeighbors = new Vector<Integer>();
    int[] myNeighbors;


  	/* The address of the "previous hop" This is the address the tranmitter needs for 2 cases
  	   case 1: Initial source packet means the previous hop will by my source address
  	   case 2: Forwarding packet means it will need the packets*/

	/*
		@param address: IP address given via command line. It's the IP of the
						  person we wish to call.
		@param portNumber: The port number given via command line. 
							it's the port number to the person we wish to call.
	*/
	public AudioTransmitterThread(String address, String portNumber, String myNodeNumber, String destinationAddress_) throws IOException
	{
		theirIP = InetAddress.getByName(address);
		theirPort = Integer.parseInt(portNumber);

		/*Create a 16-bit address*/
		sourceAddress = (short) Integer.parseInt(myNodeNumber);
		destinationAddress = (short) Integer.parseInt(destinationAddress_);

		/*Identifier as an integer for use by the config file reader*/
		intID = Integer.parseInt(myNodeNumber);
	}

	/*
		Purpose is for Receiver thread to use AudioTransmitterThread.forwardPacket()
	*/
	public AudioTransmitterThread() throws IOException
	{
		//this("127.0.0.1", "10109", "99", "100");
	}

	public void run()
	{
		/* Get everything set up for capture
		   Get the microphone line from user's device*/
        try
        {
        	audioFormat = getAudioFormat();
	        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
	        targetDataLine =(TargetDataLine) AudioSystem.getLine(dataLineInfo);
	        targetDataLine.open(audioFormat);
	        targetDataLine.start();
    	}
    	catch(LineUnavailableException e)
    	{
    		e.printStackTrace();
			System.exit(0);
    	}	

		while(talking)
		{
			try
			{
				/*Get socket*/
				socket = new DatagramSocket();
				/*Ensuring our receiving buffer is the size that the requirements specified*/
				buffer = new byte[MAX_PACKET_SIZE];
					
  				int bitesRead;
				do /* Read the user's audio data line and then send it via UDP*/
				{
					/*Load buffer-packet with the required Flood Protocol packet header*/
					loadHeader();

					/*Load buffer-packet with the voice data*/
					bitesRead = targetDataLine.read(buffer, 6, buffer.length);

					/*Get all my neighbors at this time. How often should this be checked? What about when the config file changes?*/
					//getNeighborsAtThisTime();
					//int amtOfNeighbors = myNeighbors.size();
                    //int amtOfNeighbors = myNeighbors.length;

					/*Send to all neighbors this buffer-packet*/
					//for(int neighbor 0; neigbor < amtOfNeighbors; neigbor++)
					//{					
						DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, theirIP, theirPort);
						socket.send(sendPacket);
					//}
				}while(bitesRead > 0);


			}
			catch(IOException e)
			{
				e.printStackTrace();
				talking = false;
			}
			catch(IllegalArgumentException e)
  			{
  				e.printStackTrace();
  				talking = false;
  				System.exit(0);
  			}
		}

		if(socket != null)
		{
			socket.close();
		}


	}

	private AudioFormat getAudioFormat()
  	{
	    float sampleRate = 8000.0F;
	    //8000,11025,16000,22050,44100
	    int sampleSizeInBits = 16;
	    //8,16
	    int channels = 1;
	    //1,2
	    boolean signed = true;
	    //true,false
	    boolean bigEndian = false;
	    //true,false
	    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  	}

  	private void loadHeader()
  	{	
  		/*Add sequence number to packet*/
  		sequenceNumber++;
  		buffer[0] = sequenceNumber;

  		/*Add 16-bit source address to packet in two bytes*/
  		byte sourceUpperBits = (byte)(sourceAddress>>8);
  		byte sourceLowerBits = (byte)(sourceAddress);
  		buffer[1] = sourceUpperBits;
  		buffer[2] = sourceLowerBits;

  		/*Add 16-bit destination address to packet in two bytes*/
  		byte destinUpperBits = (byte)(destinationAddress>>8);
  		byte destinLowerBits = (byte)(destinationAddress);
  		buffer[3] = destinUpperBits;
  		buffer[4] = destinLowerBits;

  		/*Add 16-bit previous hop address to packet in two bytes*/
  		buffer[5] = sourceUpperBits;
  		buffer[6] = sourceLowerBits;

  	}

  	/*  This method will be used by the receiver thread so it can forward packets according to the requirements document
		@param myNeighbors: This will be numerical value to describe the other nodes this user can communicate with at the time of the function call.
		@param packet: 		The packet we will forward to this user's neighbors. Do not give the datagram packet for "packet" 
								you should give this method the byte array containg the header and voiceData 
	*/
  	public static void forwardPacket(Vector<Integer> myNeighbors_, byte[] packet) 
  	{
  		/*Set up "forwardersAddress" to be the "previous hop" of the packet information*/
  		byte hopUpperBits = (byte)(sourceAddress>>8);
  		byte hopLowerBits = (byte)(sourceAddress);
  		packet[5] = hopUpperBits;
  		packet[6] = hopLowerBits;

  		/*Socket to use for sending*/
  		DatagramSocket forwardSocket = null;

  		/*Get all the neighbor's IP address and UDP port number from config file*/
  		int amtOfNeighbors = myNeighbors_.size();
  		for(int neighbor = 0; neighbor < amtOfNeighbors; neighbor++)
  		{
  			/*Give me the neighbors IP address using a configfile reader*/
  			//InetAddress neighborsIP = configfile.getIPaddress(myNeighbors_[neighbor].intValue());

  			/*Give me the neighbors UDP port number using a configfile reader*/
  			//int neighborsPort = configfile.getPortNumber(myNeighbors_[neigbor].intValue());

  			try
  			{
	  			forwardSocket = new DatagramSocket();
	  			//DatagramPacket forwardPacket = new DatagramPacket(packet, packet.length, neighborsIP, neighborsPort);
	  			//forwardSocket.send(forwardPacket);

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

  		if(forwardSocket != null)
  		{
  			forwardSocket.close();
  		}
  	}

  	private void getNeighborsAtThisTime()
  	{
  		/*Configfile reader should return an Integer vector of all of this user's neighbors*/
  		//myNeighbors = configFileReader.getMyNeighbors(intID);
  	}






}
