xq/*
	This class implements the interface Runnable so that we can 
	utilize the Thread class capabilites. 

	This class will handle receiving the audio from the communication line
	and playing it on the user's device.

	Authors: 	Alex Aguirre
				Brandon Wilson
	Created: 24 Feb 2016
	Updated: 26 Feb 2016
*/

import javax.sound.sampled.*;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.Hashtable;


public class AudioReceiverThread implements Runnable
{
	/* This is going to be the received voice data*/
	InputStream receivedVoiceData;

	/* See getAudioFormat() */
	AudioFormat audioFormat;

  	/* A line-connection to user's speakers or headphones that we'll write to*/
  	SourceDataLine sourceDataLine;

  	/* Audio stream*/
  	AudioInputStream audioInputStream;

  	/* Port to bind by the server*/
	int myPort;

	/* Datagram socket to be used*/
	protected DatagramSocket socket = null;

  	/* While-loop flag*/
  	boolean talking = true;

	/*Size of speaker's buffer size*/
	int sourceBufferSize;

	/* My personal router to keep track of packet sequence number and source
	 Let the source be the hashtable key, and the packet sequence number be the value
	*/
	static Hashtable packetTable;

  	/*
		@param portNumber: The port number given via command line.
	*/
	public AudioReceiverThread(String portNumber) throws IOException
	{
		//Set my port number
		myPort = Integer.parseInt(portNumber);
		socket = new DatagramSocket(myPort);
	}

	/*
		Defaults to the last port number of our given range by Dr. Lim.
	*/
	public AudioReceiverThread() throws IOException
	{
		this("10109");
	}

	public void run()
	{
		while(talking)
		{
			try
			{
				/* Setting up the audio data line */
				audioFormat = getAudioFormat();
				DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
				sourceDataLine.open(audioFormat);
      			sourceDataLine.start();

      			/*Ensuring our receiving buffer is the size of the data line's buffer*/
				sourceBufferSize = sourceDataLine.getBufferSize();

				//Create a buffer will capture the maximum amount of voice data
				byte[] voiceData = new byte[sourceBufferSize];

				/*Set up socket and listen (wait) for client(s)
				  socket.receive() saves received info into voicePackets*/
				DatagramPacket voicePackets = new DatagramPacket(voiceData, voiceData.length);
				socket.receive(voicePackets);
				
				/*Now that we have an array of bytes, set it up to play the 
				  voice data on the user's device.*/ 
				receivedVoiceData = new ByteArrayInputStream(voiceData);
				audioInputStream = new AudioInputStream(receivedVoiceData, audioFormat, voiceData.length);

      			byte[] buffer = new byte[sourceBufferSize];

  				/* while you have voice data in the stream play it on the speaker*/
  				int bites = 0;
  				while((bites = audioInputStream.read(buffer, 0, buffer.length)) != -1)
  				{
  					/*Write data to the internal buffer of the data line
          			  where it will be delivered to the speaker.*/
  					sourceDataLine.write(buffer, 0, bites);  					
  				}

  				sourceDataLine.drain();
  				sourceDataLine.close();

  		
			}
			catch(IOException e)
			{
				e.printStackTrace();
				talking = false;
			}
			catch(LineUnavailableException e)
			{
				e.printStackTrace();
				talking = false;
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


