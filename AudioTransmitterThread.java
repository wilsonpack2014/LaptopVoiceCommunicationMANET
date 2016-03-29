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

public class AudioTransmitterThread implements Runnable
{
	/* Destination port*/
	int theirPort;

	/*Destination IP*/
	InetAddress theirIP;

	/* Socket for this client*/
	protected DatagramSocket socket = null;

	/* While-loop flag*/
	boolean talking = true;

  	/*Voice data buffer*/
  	byte[] buffer;

  	/* Audio format object*/
  	AudioFormat audioFormat;

  	/* Our audio data line*/
  	TargetDataLine targetDataLine;

	/*
		@param address: IP address given via command line. It's the IP of the
						  person we wish to call.
		@param portNumber: The port number given via command line. 
							it's the port number to the person we wish to call.
	*/
	public AudioTransmitterThread(String address, String portNumber) throws IOException
	{
		theirIP = InetAddress.getByName(address);
		theirPort = Integer.parseInt(portNumber);
	}

	/*
		Defaults to the last port number of our given range by Dr. Lim.
	*/
	public AudioTransmitterThread() throws IOException
	{
		this("127.0.0.1", "10109");
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
				/*Ensuring our receiving buffer is the size of the data line's buffer*/
				buffer = new byte[targetDataLine.getBufferSize()];
					
  				int bitesRead;
				do /* Read the user's audio data line and then send it via UDP*/
				{
					bitesRead = targetDataLine.read(buffer, 0, buffer.length);					
					DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, theirIP, theirPort);
					socket.send(sendPacket);
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








}
