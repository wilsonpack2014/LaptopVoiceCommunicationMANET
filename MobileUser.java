/*
	This class will contain everything needed for a mobile node in the mesh network. 
*/

public class MobileUser
{
	/* My 16-bit address that's not my IP address*/
	private static short myAddress;
	/* The other mobile users that I can communicate to. They are "in-range"*/					
	private static Vector myNeighbors;
	/* My personal router to keep track of packet sequence number and source
	   Let the source be the hashtable key, and the packet sequence number be the value*/
	private static Hashtable packetTable;
	/* Gathers the voice and transmits it to the destination*/
	private static AudioTransmitter transmitter;
	/* Recieves voice packets and plays them on device*/
	private static AudioReceiver receiver;
	/* This will gather the proper information from the configuration file so we can update ourself*/
	private static ConfigFileReader configFileReader;

	/* Contructor*/

	/*Getters and setter*/


}